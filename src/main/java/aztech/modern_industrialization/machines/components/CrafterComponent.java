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
package aztech.modern_industrialization.machines.components;

import static aztech.modern_industrialization.util.Simulation.ACT;
import static aztech.modern_industrialization.util.Simulation.SIMULATE;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.api.machine.component.CrafterAccess;
import aztech.modern_industrialization.api.machine.component.InventoryAccess;
import aztech.modern_industrialization.inventory.AbstractConfigurableStack;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.stats.PlayerStatistics;
import aztech.modern_industrialization.stats.PlayerStatisticsData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class CrafterComponent implements IComponent.ServerOnly, CrafterAccess {
    private final MachineProcessCondition.Context conditionContext;

    public CrafterComponent(MachineBlockEntity blockEntity, Inventory inventory, Behavior behavior) {
        this.inventory = inventory;
        this.behavior = behavior;
        this.conditionContext = () -> blockEntity;
    }

    public interface Inventory extends InventoryAccess {
        List<ConfigurableItemStack> getItemInputs();

        List<ConfigurableItemStack> getItemOutputs();

        List<ConfigurableFluidStack> getFluidInputs();

        List<ConfigurableFluidStack> getFluidOutputs();

        int hash();
    }

    public interface Behavior {
        default boolean isEnabled() {
            return true;
        }

        long consumeEu(long max, Simulation simulation);

        default boolean banRecipe(MachineRecipe recipe) {
            return recipe.eu > getMaxRecipeEu();
        }

        MachineRecipeType recipeType();

        long getBaseRecipeEu();

        long getMaxRecipeEu();

        // can't use getWorld() or the remapping will fail
        Level getCrafterWorld();

        default int getMaxFluidOutputs() {
            return Integer.MAX_VALUE;
        }

        @Nullable
        UUID getOwnerUuid();

        default PlayerStatistics getStatsOrDummy() {
            var uuid = getOwnerUuid();
            if (uuid == null) {
                return PlayerStatistics.DUMMY;
            } else {
                return PlayerStatisticsData.get(getCrafterWorld().getServer()).get(uuid);
            }
        }
    }

    private final Inventory inventory;
    private final Behavior behavior;

    private RecipeHolder<MachineRecipe> activeRecipe = null;
    private ResourceLocation delayedActiveRecipe;

    private long usedEnergy;
    private long recipeEnergy;
    private long recipeMaxEu;

    private int efficiencyTicks;
    private int maxEfficiencyTicks;

    private long previousBaseEu = -1;
    private long previousMaxEu = -1;

    private int lastInvHash = 0;
    private int lastForcedTick = 0;

    @Override
    public float getProgress() {
        return (float) usedEnergy / recipeEnergy;
    }

    @Override
    public int getEfficiencyTicks() {
        return efficiencyTicks;
    }

    @Override
    public int getMaxEfficiencyTicks() {
        return maxEfficiencyTicks;
    }

    @Override
    public boolean hasActiveRecipe() {
        return activeRecipe != null;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public void decreaseEfficiencyTicks() {
        efficiencyTicks = Math.max(efficiencyTicks - 1, 0);
        clearActiveRecipeIfPossible();
    }

    public void increaseEfficiencyTicks(int increment) {
        efficiencyTicks = Math.min(efficiencyTicks + increment, maxEfficiencyTicks);
    }

    @Override
    public long getCurrentRecipeEu() {
        Preconditions.checkArgument(hasActiveRecipe());
        return recipeMaxEu;
    }

    @Override
    public long getBaseRecipeEu() {
        Preconditions.checkArgument(hasActiveRecipe());
        return activeRecipe.value().eu;
    }

    /**
     * Perform a crafter tick, and return whether the crafter is active after the
     * tick.
     */
    public boolean tickRecipe() {
        if (behavior.getCrafterWorld().isClientSide()) {
            throw new IllegalStateException("May not call client side.");
        }
        boolean isActive;
        boolean isEnabled = behavior.isEnabled();

        loadDelayedActiveRecipe();

        // START RECIPE IF NECESSARY
        // usedEnergy == 0 means that no recipe is currently started
        boolean recipeStarted = false;
        if (usedEnergy == 0 && isEnabled) {
            if (behavior.consumeEu(1, SIMULATE) == 1) {
                recipeStarted = updateActiveRecipe();
            }
        }

        if (activeRecipe != null) {
            lastForcedTick = 0;
        }

        // PROCESS RECIPE TICK
        long eu = 0;
        boolean finishedRecipe = false; // whether the recipe finished this tick
        if (activeRecipe != null && (usedEnergy > 0 || recipeStarted) && isEnabled) {
            recipeMaxEu = getRecipeMaxEu(activeRecipe.value().eu, recipeEnergy, efficiencyTicks);
            eu = activeRecipe.value().conditionsMatch(conditionContext) ? behavior.consumeEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), ACT)
                    : 0;
            isActive = eu > 0;
            usedEnergy += eu;

            if (usedEnergy == recipeEnergy) {
                putItemOutputs(activeRecipe.value(), false, false);
                putFluidOutputs(activeRecipe.value(), false, false);
                clearLocks();
                usedEnergy = 0;
                finishedRecipe = true;
            }
        } else {
            isActive = false;
        }

        if (activeRecipe != null) {
            if (previousBaseEu != behavior.getBaseRecipeEu() || previousMaxEu != behavior.getMaxRecipeEu()) {
                previousBaseEu = behavior.getBaseRecipeEu();
                previousMaxEu = behavior.getMaxRecipeEu();
                maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(activeRecipe.value());
                efficiencyTicks = Math.min(efficiencyTicks, maxEfficiencyTicks);
            }

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

        // If the recipe is done, allow starting another one when the efficiency reaches zero
        clearActiveRecipeIfPossible();

        return isActive;
    }

    private void clearActiveRecipeIfPossible() {
        if (efficiencyTicks == 0 && usedEnergy == 0) {
            activeRecipe = null;
        }
    }

    /**
     * Attempt to re-lock hatches to continue the active recipe.
     *
     * @return True if there is no current recipe or if the hatches could be locked
     *         for it, false otherwise.
     */
    public boolean tryContinueRecipe() {
        loadDelayedActiveRecipe();

        if (activeRecipe != null) {
            if (putItemOutputs(activeRecipe.value(), true, false) && putFluidOutputs(activeRecipe.value(), true, false)) {
                // Relock stacks
                putItemOutputs(activeRecipe.value(), true, true);
                putFluidOutputs(activeRecipe.value(), true, true);
            } else {
                return false;
            }
        }

        return true;
    }

    private void loadDelayedActiveRecipe() {
        if (delayedActiveRecipe != null) {
            activeRecipe = behavior.recipeType().getRecipe(behavior.getCrafterWorld(), delayedActiveRecipe);
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
        for (RecipeHolder<MachineRecipe> recipe : getRecipes()) {
            if (behavior.banRecipe(recipe.value()))
                continue;
            if (tryStartRecipe(recipe.value())) {
                // Make sure we recalculate the max efficiency ticks if the recipe changes or if
                // the efficiency has reached 0 (the latter is to recalculate the efficiency for
                // 0.3.6 worlds without having to break and replace the machines)
                if (activeRecipe != recipe || efficiencyTicks == 0) {
                    maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(recipe.value());
                }
                activeRecipe = recipe;
                usedEnergy = 0;
                recipeEnergy = recipe.value().getTotalEu();
                recipeMaxEu = getRecipeMaxEu(recipe.value().eu, recipeEnergy, efficiencyTicks);
                return true;
            }
        }
        return false;
    }

    private Iterable<RecipeHolder<MachineRecipe>> getRecipes() {
        if (efficiencyTicks > 0) {
            return Collections.singletonList(activeRecipe);
        } else {
            int currentHash = inventory.hash();
            if (currentHash == lastInvHash) {
                if (lastForcedTick == 0) {
                    lastForcedTick = 100;
                } else {
                    --lastForcedTick;
                    return Collections.emptyList();
                }
            } else {
                lastInvHash = currentHash;
            }

            ServerLevel serverWorld = (ServerLevel) behavior.getCrafterWorld();
            MachineRecipeType recipeType = behavior.recipeType();
            List<RecipeHolder<MachineRecipe>> recipes = new ArrayList<>(recipeType.getFluidOnlyRecipes(serverWorld));
            for (ConfigurableItemStack stack : inventory.getItemInputs()) {
                if (!stack.isEmpty()) {
                    recipes.addAll(recipeType.getMatchingRecipes(serverWorld, stack.getResource().getItem()));
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
                && putFluidOutputs(recipe, true, false) && recipe.conditionsMatch(conditionContext)) {
            takeItemInputs(recipe, false);
            takeFluidInputs(recipe, false);
            putItemOutputs(recipe, true, true);
            putFluidOutputs(recipe, true, true);
            return true;
        } else {
            return false;
        }
    }

    public static double getEfficiencyOverclock(int efficiencyTicks) {
        return Math.pow(2.0, efficiencyTicks / 32.0);
    }

    private long getRecipeMaxEu(long recipeEu, long totalEu, int efficiencyTicks) {
        long baseEu = Math.max(behavior.getBaseRecipeEu(), recipeEu);
        return Math.min(totalEu, Math.min((int) Math.floor(baseEu * getEfficiencyOverclock(efficiencyTicks)), behavior.getMaxRecipeEu()));
    }

    private int getRecipeMaxEfficiencyTicks(MachineRecipe recipe) {
        long eu = recipe.eu;
        long totalEu = recipe.getTotalEu();
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
            tag.putString("activeRecipe", this.activeRecipe.id().toString());
        } else if (delayedActiveRecipe != null) {
            tag.putString("activeRecipe", this.delayedActiveRecipe.toString());
        }
        tag.putInt("efficiencyTicks", this.efficiencyTicks);
        tag.putInt("maxEfficiencyTicks", this.maxEfficiencyTicks);
    }

    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        this.usedEnergy = tag.getInt("usedEnergy");
        this.recipeEnergy = tag.getInt("recipeEnergy");
        this.recipeMaxEu = tag.getInt("recipeMaxEu");
        this.delayedActiveRecipe = tag.contains("activeRecipe") ? new ResourceLocation(tag.getString("activeRecipe")) : null;
        if (delayedActiveRecipe == null && usedEnergy > 0) {
            usedEnergy = 0;
            MI.LOGGER.error("Had to set the usedEnergy of CrafterComponent to 0, but that should never happen!");
        }
        this.efficiencyTicks = tag.getInt("efficiencyTicks");
        this.maxEfficiencyTicks = tag.getInt("maxEfficiencyTicks");
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
            for (ConfigurableItemStack stack : stacks) {
                if (stack.getAmount() > 0 && input.matches(stack.getResource().toStack())) { // TODO: ItemStack creation slow?
                    int taken = Math.min((int) stack.getAmount(), remainingAmount);
                    if (taken > 0 && !simulate) {
                        behavior.getStatsOrDummy().addUsedItems(stack.getResource().getItem(), taken);
                    }
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
                if (stack.getResource().equals(FluidVariant.of(input.fluid))) {
                    long taken = Math.min(remainingAmount, stack.getAmount());
                    if (taken > 0 && !simulate) {
                        behavior.getStatsOrDummy().addUsedFluids(stack.getResource().getFluid(), taken);
                    }
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
                    ItemVariant key = stack.getResource();
                    if (key.getItem() == output.item || key.isBlank()) {
                        // If simulating or chanced output, respect the adjusted capacity.
                        // If putting the output, don't respect the adjusted capacity in case it was
                        // reduced during the processing.
                        int remainingCapacity = simulate || output.probability < 1 ? (int) stack.getRemainingCapacityFor(ItemVariant.of(output.item))
                                : output.item.getMaxStackSize() - (int) stack.getAmount();
                        int ins = Math.min(remainingAmount, remainingCapacity);
                        if (key.isBlank()) {
                            if ((stack.isMachineLocked() || stack.isPlayerLocked() || loopRun == 1) && stack.isValid(new ItemStack(output.item))) {
                                stack.setAmount(ins);
                                stack.setKey(ItemVariant.of(output.item));
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
                            if (!simulate) {
                                behavior.getStatsOrDummy().addProducedItems(behavior.getCrafterWorld(), output.item, ins);
                            }
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

    protected boolean putFluidOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableFluidStack> baseList = inventory.getFluidOutputs();
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Fluid> lockFluids = new ArrayList<>();

        boolean ok = true;
        for (int i = 0; i < Math.min(recipe.fluidOutputs.size(), behavior.getMaxFluidOutputs()); ++i) {
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
                    FluidVariant outputKey = FluidVariant.of(output.fluid);
                    if (stack.isResourceAllowedByLock(outputKey)
                            && ((tries == 1 && stack.isResourceBlank()) || stack.getResource().equals(outputKey))) {
                        long inserted = Math.min(output.amount, stack.getRemainingSpace());
                        if (inserted > 0) {
                            stack.setKey(outputKey);
                            stack.increment(inserted);
                            locksToToggle.add(j);
                            lockFluids.add(output.fluid);
                            if (!simulate) {
                                behavior.getStatsOrDummy().addProducedFluids(output.fluid, inserted);
                            }
                        }
                        if (inserted < output.amount) {
                            ok = false;
                        }
                        break outer;
                    }
                }
                if (tries == 1) {
                    ok = false;
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

    public void lockRecipe(ResourceLocation recipeId, net.minecraft.world.entity.player.Inventory inventory) {
        // Find MachineRecipe
        Optional<RecipeHolder<MachineRecipe>> optionalMachineRecipe = behavior.recipeType().getRecipes(behavior.getCrafterWorld()).stream()
                .filter(recipe -> recipe.id().equals(recipeId)).findFirst();
        if (optionalMachineRecipe.isEmpty())
            return;
        var recipe = optionalMachineRecipe.get();
        // ITEM INPUTS
        outer: for (MachineRecipe.ItemInput input : recipe.value().itemInputs) {
            for (ConfigurableItemStack stack : this.inventory.getItemInputs()) {
                if (stack.getLockedInstance() != null && input.matches(new ItemStack(stack.getLockedInstance())))
                    continue outer;
            }
            Item targetItem = null;
            // Find the first match in the player inventory (useful for logs for example)
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack playerStack = inventory.getItem(i);
                if (!playerStack.isEmpty() && input.matches(new ItemStack(playerStack.getItem()))) {
                    targetItem = playerStack.getItem();
                    break;
                }
            }
            if (targetItem == null) {
                // Find the first match that is an item from MI (useful for ingots for example)
                for (Item item : input.getInputItems()) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                    if (id.getNamespace().equals(MI.ID)) {
                        targetItem = item;
                        break;
                    }
                }
            }
            if (targetItem == null) {
                // If there is only one value in the tag, pick that one
                if (input.getInputItems().size() == 1) {
                    targetItem = input.getInputItems().get(0);
                }
            }

            if (targetItem != null) {
                AbstractConfigurableStack.playerLockNoOverride(targetItem, this.inventory.getItemInputs());
            }
        }
        // ITEM OUTPUTS
        outer: for (MachineRecipe.ItemOutput output : recipe.value().itemOutputs) {
            for (ConfigurableItemStack stack : this.inventory.getItemOutputs()) {
                if (stack.getLockedInstance() == output.item)
                    continue outer;
            }
            AbstractConfigurableStack.playerLockNoOverride(output.item, this.inventory.getItemOutputs());
        }

        // FLUID INPUTS
        outer: for (MachineRecipe.FluidInput input : recipe.value().fluidInputs) {
            for (ConfigurableFluidStack stack : this.inventory.getFluidInputs()) {
                if (stack.isLockedTo(input.fluid))
                    continue outer;
            }
            AbstractConfigurableStack.playerLockNoOverride(input.fluid, this.inventory.getFluidInputs());
        }
        // FLUID OUTPUTS
        outer: for (MachineRecipe.FluidOutput output : recipe.value().fluidOutputs) {
            for (ConfigurableFluidStack stack : this.inventory.getFluidOutputs()) {
                if (stack.isLockedTo(output.fluid))
                    continue outer;
            }
            AbstractConfigurableStack.playerLockNoOverride(output.fluid, this.inventory.getFluidOutputs());
        }

        // LOCK ITEMS
        if (recipe.value().itemInputs.size() > 0 || recipe.value().itemOutputs.size() > 0) {
            lockAll(this.inventory.getItemInputs());
            lockAll(this.inventory.getItemOutputs());
        }
        // LOCK FLUIDS
        if (recipe.value().fluidInputs.size() > 0 || recipe.value().fluidOutputs.size() > 0) {
            lockAll(this.inventory.getFluidInputs());
            lockAll(this.inventory.getFluidOutputs());
        }
    }

    private static void lockAll(List<? extends AbstractConfigurableStack<?, ?>> stacks) {
        for (var stack : stacks) {
            if (stack.isEmpty() && stack.getLockedInstance() == null) {
                stack.togglePlayerLock();
            }
        }
    }
}
