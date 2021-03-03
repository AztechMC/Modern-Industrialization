/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machinesv2.components;

import static aztech.modern_industrialization.util.Simulation.ACT;
import static aztech.modern_industrialization.util.Simulation.SIMULATE;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CrafterComponent implements IComponent {
    public CrafterComponent(Inventory inventory, Behavior behavior) {
        this.inventory = inventory;
        this.behavior = behavior;
    }

    public interface Inventory {
        List<ConfigurableItemStack> getItemInputs();

        List<ConfigurableItemStack> getItemOutputs();

        List<ConfigurableFluidStack> getFluidInputs();

        List<ConfigurableFluidStack> getFluidOutputs();
    }

    public interface Behavior {
        long consumeEu(long max, Simulation simulation);

        default boolean banRecipe(MachineRecipe recipe) {
            return recipe.eu > getMaxRecipeEu();
        }

        MachineRecipeType recipeType();

        long getBaseRecipeEu();

        long getMaxRecipeEu();

        World getWorld();
    }

    private final Inventory inventory;
    private final Behavior behavior;

    private MachineRecipe activeRecipe = null;
    private Identifier delayedActiveRecipe;

    private long usedEnergy;
    private long recipeEnergy;
    private long recipeMaxEu;

    private int efficiencyTicks;
    private int maxEfficiencyTicks;

    public float getProgress() {
        return (float) usedEnergy / recipeEnergy;
    }

    public int getEfficiencyTicks() {
        return efficiencyTicks;
    }

    public int getMaxEfficiencyTicks() {
        return maxEfficiencyTicks;
    }

    public boolean hasActiveRecipe() {
        return activeRecipe != null;
    }

    public long getCurrentRecipeEu() {
        Preconditions.checkArgument(hasActiveRecipe());
        return recipeMaxEu;
    }

    public long getBaseRecipeEu() {
        Preconditions.checkArgument(hasActiveRecipe());
        return activeRecipe.eu;
    }

    /**
     * Perform a crafter tick, and return whether the crafter is active after the
     * tick.
     */
    public boolean tickRecipe() {
        if (behavior.getWorld().isClient()) {
            throw new IllegalStateException("May not call client side.");
        }
        boolean isActive;

        loadDelayedActiveRecipe();

        // START RECIPE IF NECESSARY
        // usedEnergy == 0 means that no recipe is currently started
        boolean recipeStarted = false;
        if (usedEnergy == 0) {
            if (behavior.consumeEu(1, SIMULATE) == 1) {
                recipeStarted = updateActiveRecipe();
            }
        }

        // PROCESS RECIPE TICK
        long eu = 0;
        boolean finishedRecipe = false; // whether the recipe finished this tick
        if (activeRecipe != null && (usedEnergy > 0 || recipeStarted)) {
            recipeMaxEu = getRecipeMaxEu(activeRecipe.eu, recipeEnergy, efficiencyTicks);
            eu = behavior.consumeEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), ACT);
            isActive = eu > 0;
            usedEnergy += eu;

            if (usedEnergy == recipeEnergy) {
                putItemOutputs(activeRecipe, false, false);
                putFluidOutputs(activeRecipe, false, false);
                clearLocks();
                usedEnergy = 0;
                finishedRecipe = true;
            }
        } else {
            isActive = false;
        }

        // ADD OR REMOVE EFFICIENCY TICKS
        // If we finished a recipe, we can add an efficiency tick
        if (finishedRecipe) {
            if (efficiencyTicks < maxEfficiencyTicks)
                ++efficiencyTicks;
        } else if (eu < recipeMaxEu) { // If we didn't use the max energy this tick and the recipe is still ongoing,
            // remove one efficiency tick
            if (efficiencyTicks > 0) {
                efficiencyTicks--;
            }
        }

        // If the recipe is done, allow starting another one when the efficiency reaches
        // zero
        if (efficiencyTicks == 0 && usedEnergy == 0) {
            activeRecipe = null;
        }

        return isActive;
    }

    private void loadDelayedActiveRecipe() {
        if (delayedActiveRecipe != null) {
            activeRecipe = behavior.recipeType().getRecipe((ServerWorld) behavior.getWorld(), delayedActiveRecipe);
            delayedActiveRecipe = null;
            if (activeRecipe == null) { // If a recipe got removed, we need to reset the efficiency and the used energy
                // to allow the machine to resume processing.
                efficiencyTicks = 0;
                usedEnergy = 0;
            }
        }
    }

    private boolean updateActiveRecipe() {
        // Only then can we run the iteration over the recipes
        for (MachineRecipe recipe : getRecipes()) {
            if (behavior.banRecipe(recipe))
                continue;
            if (tryStartRecipe(recipe)) {
                // Make sure we recalculate the max efficiency ticks if the recipe changes or if
                // the efficiency has reached 0 (the latter is to recalculate the efficiency for
                // 0.3.6 worlds without having to break and replace the machines)
                if (activeRecipe != recipe || efficiencyTicks == 0) {
                    maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(recipe.eu, (long) recipe.eu * recipe.duration);
                }
                activeRecipe = recipe;
                usedEnergy = 0;
                recipeEnergy = (long) recipe.eu * recipe.duration;
                recipeMaxEu = getRecipeMaxEu(recipe.eu, recipeEnergy, efficiencyTicks);
                return true;
            }
        }
        return false;
    }

    private Iterable<MachineRecipe> getRecipes() {
        if (efficiencyTicks > 0) {
            return Collections.singletonList(activeRecipe);
        } else {
            ServerWorld serverWorld = (ServerWorld) behavior.getWorld();
            MachineRecipeType recipeType = behavior.recipeType();
            List<MachineRecipe> recipes = new ArrayList<>(recipeType.getFluidOnlyRecipes(serverWorld));
            for (ConfigurableItemStack stack : inventory.getItemInputs()) {
                if (!stack.getItemKey().isEmpty()) {
                    recipes.addAll(recipeType.getMatchingRecipes(serverWorld, stack.getItemKey().getItem()));
                }
            }
            return recipes;
        }
    }

    /**
     * Try to start a recipe. Return true if success, false otherwise. If false,
     * nothing was changed.
     */
    private boolean tryStartRecipe(MachineRecipe recipe) {
        if (takeItemInputs(recipe, true) && takeFluidInputs(recipe, true) && putItemOutputs(recipe, true, false)
                && putFluidOutputs(recipe, true, false)) {
            takeItemInputs(recipe, false);
            takeFluidInputs(recipe, false);
            putItemOutputs(recipe, true, true);
            putFluidOutputs(recipe, true, true);
            return true;
        } else {
            return false;
        }
    }

    private static double getEfficiencyOverclock(int efficiencyTicks) {
        return Math.pow(2.0, efficiencyTicks / 32.0);
    }

    private long getRecipeMaxEu(long recipeEu, long totalEu, int efficiencyTicks) {
        long baseEu = Math.max(behavior.getBaseRecipeEu(), recipeEu);
        return Math.min(totalEu, Math.min((int) Math.floor(baseEu * getEfficiencyOverclock(efficiencyTicks)), behavior.getMaxRecipeEu()));
    }

    private int getRecipeMaxEfficiencyTicks(long eu, long totalEu) {
        if (efficiencyTicks != 0)
            throw new RuntimeException("Illegal state");
        for (int ticks = 0; true; ++ticks) {
            if (getRecipeMaxEu(eu, totalEu, ticks) == Math.min(behavior.getMaxRecipeEu(), totalEu))
                return ticks;
        }
    }

    public void writeNbt(CompoundTag tag) {
        tag.putLong("usedEnergy", this.usedEnergy);
        tag.putLong("recipeEnergy", this.recipeEnergy);
        tag.putLong("recipeMaxEu", this.recipeMaxEu);
        if (activeRecipe != null) {
            tag.putString("activeRecipe", this.activeRecipe.getId().toString());
        } else if (delayedActiveRecipe != null) {
            tag.putString("activeRecipe", this.delayedActiveRecipe.toString());
        }
        tag.putInt("efficiencyTicks", this.efficiencyTicks);
        tag.putInt("maxEfficiencyTicks", this.maxEfficiencyTicks);
    }

    public void readNbt(CompoundTag tag) {
        this.usedEnergy = tag.getInt("usedEnergy");
        this.recipeEnergy = tag.getInt("recipeEnergy");
        this.recipeMaxEu = tag.getInt("recipeMaxEu");
        this.delayedActiveRecipe = tag.contains("activeRecipe") ? new Identifier(tag.getString("activeRecipe")) : null;
        if (delayedActiveRecipe == null && usedEnergy > 0) {
            usedEnergy = 0;
            ModernIndustrialization.LOGGER.error("Had to set the usedEnergy of CrafterComponent to 0, but that should never happen!");
        }
        this.efficiencyTicks = tag.getInt("efficiencyTicks");
        this.maxEfficiencyTicks = tag.getInt("maxEfficiencyTicks");
    }

    @Override
    public boolean isClientSynced() {
        return false;
    }

    /**
     * cachedItemCounts must be correct when this function is called, and are
     * guaranteed to be correct after this call
     */
    private boolean takeItemInputs(MachineRecipe recipe, boolean simulate) {
        List<ConfigurableItemStack> baseList = inventory.getItemInputs();
        List<ConfigurableItemStack> stacks = simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

        boolean ok = true;
        for (MachineRecipe.ItemInput input : recipe.itemInputs) {
            if (!simulate && input.probability < 1) { // if we are not simulating, there is a chance we don't need to take this output
                if (ThreadLocalRandom.current().nextFloat() >= input.probability) {
                    continue;
                }
            }
            int remainingAmount = input.amount;
            for (int i = 0; i < stacks.size(); i++) {
                ConfigurableItemStack stack = stacks.get(i);
                if (stack.getCount() > 0 && input.matches(stack.getItemKey().toStack())) { // TODO: ItemStack creation slow?
                    int taken = Math.min(stack.getCount(), remainingAmount);
                    stack.decrement(taken);
                    remainingAmount -= taken;
                    if (remainingAmount == 0)
                        break;
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }

        return ok;
    }

    protected boolean takeFluidInputs(MachineRecipe recipe, boolean simulate) {
        List<ConfigurableFluidStack> baseList = inventory.getFluidInputs();
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        boolean ok = true;
        for (MachineRecipe.FluidInput input : recipe.fluidInputs) {
            if (!simulate && input.probability < 1) { // if we are not simulating, there is a chance we don't need to take this output
                if (ThreadLocalRandom.current().nextFloat() >= input.probability) {
                    continue;
                }
            }
            long remainingAmount = input.amount;
            for (ConfigurableFluidStack stack : stacks) {
                if (stack.getFluid() == input.fluid) {
                    long taken = Math.min(remainingAmount, stack.getAmount());
                    stack.decrement(taken);
                    remainingAmount -= taken;
                    if (remainingAmount == 0)
                        break;
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }
        return ok;
    }

    protected boolean putItemOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableItemStack> baseList = inventory.getItemOutputs();
        List<ConfigurableItemStack> stacks = simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Item> lockItems = new ArrayList<>();

        boolean ok = true;
        for (MachineRecipe.ItemOutput output : recipe.itemOutputs) {
            if (output.probability < 1) {
                if (simulate)
                    continue; // don't check output space for probabilistic recipes
                float randFloat = ThreadLocalRandom.current().nextFloat();
                if (randFloat > output.probability)
                    continue;
            }
            int remainingAmount = output.amount;
            // Try to insert in non-empty stacks or locked first, then also allow insertion
            // in empty stacks.
            for (int loopRun = 0; loopRun < 2; loopRun++) {
                int stackId = 0;
                for (ConfigurableItemStack stack : stacks) {
                    stackId++;
                    ItemKey key = stack.getItemKey();
                    if (key.getItem() == output.item || key.isEmpty()) {
                        int ins = Math.min(remainingAmount, output.item.getMaxCount() - stack.getCount());
                        if (key.isEmpty()) {
                            if ((stack.isMachineLocked() || stack.isPlayerLocked() || loopRun == 1) && stack.isValid(new ItemStack(output.item))) {
                                stack.setCount(ins);
                                stack.setItemKey(ItemKey.of(output.item));
                            } else {
                                ins = 0;
                            }
                        } else {
                            stack.increment(ins);
                        }
                        remainingAmount -= ins;
                        if (ins > 0) {
                            locksToToggle.add(stackId - 1);
                            lockItems.add(output.item);
                        }
                        if (remainingAmount == 0)
                            break;
                    }
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }

        if (toggleLock) {
            for (int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockItems.get(i));
            }
        }
        return ok;
    }

    protected int getMaxFluidOutputs() {
        return Integer.MAX_VALUE;
    }

    protected boolean putFluidOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableFluidStack> baseList = inventory.getFluidOutputs();
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Fluid> lockFluids = new ArrayList<>();

        boolean ok = true;
        for (int i = 0; i < Math.min(recipe.fluidOutputs.size(), getMaxFluidOutputs()); ++i) {
            MachineRecipe.FluidOutput output = recipe.fluidOutputs.get(i);
            if (output.probability < 1) {
                if (simulate)
                    continue; // don't check output space for probabilistic recipes
                float randFloat = ThreadLocalRandom.current().nextFloat();
                if (randFloat > output.probability)
                    continue;
            }
            // First, try to find a slot that contains the fluid. If we couldn't find one,
            // we insert in any stack
            outer: for (int tries = 0; tries < 2; ++tries) {
                for (int j = 0; j < stacks.size(); j++) {
                    ConfigurableFluidStack stack = stacks.get(j);
                    if (stack.isValid(output.fluid) && (tries == 1 || stack.getFluid() == output.fluid)) {
                        long inserted = Math.min(output.amount, stack.getRemainingSpace());
                        if (inserted > 0) {
                            stack.setFluid(output.fluid);
                            stack.increment(inserted);
                            locksToToggle.add(j);
                            lockFluids.add(output.fluid);

                            if (inserted < output.amount)
                                ok = false;
                            break outer;
                        }
                    }
                }
            }
        }

        if (toggleLock) {
            for (int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockFluids.get(i));
            }
        }
        return ok;
    }

    protected void clearLocks() {
        for (ConfigurableItemStack stack : inventory.getItemOutputs()) {
            if (stack.isMachineLocked())
                stack.disableMachineLock();
        }
        for (ConfigurableFluidStack stack : inventory.getFluidOutputs()) {
            if (stack.isMachineLocked())
                stack.disableMachineLock();
        }
    }
}
