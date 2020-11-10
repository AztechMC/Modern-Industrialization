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
package aztech.modern_industrialization.machines.impl;

import static alexiil.mc.lib.attributes.Simulation.ACTION;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProviderBlockEntity;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyAttributes;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableInventory;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

// TODO: refactor
public class MachineBlockEntity extends AbstractMachineBlockEntity
        implements Tickable, ExtendedScreenHandlerFactory, MachineInventory, AttributeProviderBlockEntity {
    protected static final FluidKey STEAM_KEY = MIFluids.STEAM.key;

    protected List<ConfigurableItemStack> itemStacks;
    protected List<ConfigurableFluidStack> fluidStacks;

    protected long storedEu = 0;

    protected long getMaxStoredEu() {
        return factory.tier == null ? -1 : factory.tier.getMaxStoredEu();
    }

    protected MachineFactory factory;
    protected MachineRecipe activeRecipe = null;
    protected Identifier delayedActiveRecipe;

    protected int usedEnergy;
    protected int recipeEnergy;
    protected int recipeMaxEu;

    protected int efficiencyTicks;
    protected int maxEfficiencyTicks;

    private PropertyDelegate propertyDelegate;

    public MachineBlockEntity(MachineFactory factory) {
        super(factory.blockEntityType, Direction.NORTH);
        this.factory = factory;
        itemStacks = new ArrayList<>();
        for (int i = 0; i < factory.getInputSlots(); ++i) {
            itemStacks.add(ConfigurableItemStack.standardInputSlot());
        }
        for (int i = 0; i < factory.getOutputSlots(); ++i) {
            itemStacks.add(ConfigurableItemStack.standardOutputSlot());
        }
        fluidStacks = new ArrayList<>();
        for (int i = 0; i < factory.getLiquidInputSlots(); ++i) {
            if (i == 0 && factory instanceof SteamMachineFactory) {
                fluidStacks.add(ConfigurableFluidStack.lockedInputSlot(((SteamMachineFactory) factory).getSteamBucketCapacity() * 1000, STEAM_KEY));
            } else {
                fluidStacks.add(ConfigurableFluidStack.standardInputSlot(factory.getInputBucketCapacity() * 1000));
            }
        }
        for (int i = 0; i < factory.getLiquidOutputSlots(); ++i) {
            fluidStacks.add(ConfigurableFluidStack.standardOutputSlot(factory.getOutputBucketCapacity() * 1000));
        }

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if (index == 0)
                    return isActive ? 1 : 0;
                else if (index == 1)
                    return usedEnergy;
                else if (index == 2)
                    return recipeEnergy;
                else if (index == 3)
                    return efficiencyTicks;
                else if (index == 4)
                    return maxEfficiencyTicks;
                else if (index == 5)
                    return (int) storedEu;
                else if (index == 6)
                    return activeRecipe != null && recipeEnergy != 0 ? activeRecipe.eu : 0;
                else if (index == 7)
                    return (int) getMaxStoredEu();
                else if (index == 8)
                    return recipeMaxEu;
                else
                    return -1;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0)
                    isActive = value == 1;
                else if (index == 1)
                    usedEnergy = value;
                else if (index == 2)
                    recipeEnergy = value;
                else if (index == 3)
                    efficiencyTicks = value;
                else if (index == 4)
                    maxEfficiencyTicks = value;
                else if (index == 5)
                    storedEu = value;
                else
                    throw new UnsupportedOperationException();
            }

            @Override
            public int size() {
                return 9;
            }
        };

    }

    @Override
    public List<ConfigurableItemStack> getItemStacks() {
        return itemStacks;
    }

    @Override
    public List<ConfigurableFluidStack> getFluidStacks() {
        return fluidStacks;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(factory.getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new MachineScreenHandler(syncId, inv, this, this.propertyDelegate, this.factory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        writeToTag(tag);
        tag.putInt("usedEnergy", this.usedEnergy);
        tag.putInt("recipeEnergy", this.recipeEnergy);
        tag.putInt("recipeMaxEu", this.recipeMaxEu);
        if (activeRecipe != null) {
            tag.putString("activeRecipe", this.activeRecipe.getId().toString());
        } else if (delayedActiveRecipe != null) {
            tag.putString("activeRecipe", this.delayedActiveRecipe.toString());
        }
        tag.putInt("efficiencyTicks", this.efficiencyTicks);
        tag.putInt("maxEfficiencyTicks", this.maxEfficiencyTicks);
        tag.putLong("storedEu", this.storedEu);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        {
            // This is a failsafe in case the number of slots in a machine changed
            // When this happens, we destroy all items/fluids, but at least we don't crash
            // the world.
            // TODO: find a better solution?
            List<ConfigurableItemStack> itemStackCopy = ConfigurableItemStack.copyList(itemStacks);
            List<ConfigurableFluidStack> fluidStackCopy = ConfigurableFluidStack.copyList(fluidStacks);
            readFromTag(tag);
            if (itemStackCopy.size() != itemStacks.size()) {
                itemStacks = itemStackCopy;
            }
            if (fluidStackCopy.size() != fluidStacks.size()) {
                fluidStacks = fluidStackCopy;
            }
        }
        this.usedEnergy = tag.getInt("usedEnergy");
        this.recipeEnergy = tag.getInt("recipeEnergy");
        this.recipeMaxEu = tag.getInt("recipeMaxEu");
        this.delayedActiveRecipe = tag.contains("activeRecipe") ? new Identifier(tag.getString("activeRecipe")) : null;
        if (delayedActiveRecipe == null && factory.recipeType != null && usedEnergy > 0) {
            usedEnergy = 0;
            ModernIndustrialization.LOGGER.error("Had to set the usedEnergy of a machine to 0, but that should never happen!");
        }
        this.efficiencyTicks = tag.getInt("efficiencyTicks");
        this.maxEfficiencyTicks = tag.getInt("maxEfficiencyTicks");
        this.storedEu = tag.getLong("storedEu");
    }

    public MachineFactory getFactory() {
        return factory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        MachineInventories.toBuf(packetByteBuf, this);
        packetByteBuf.writeInt(propertyDelegate.size());
        packetByteBuf.writeString(factory.getID());
    }

    protected void loadDelayedActiveRecipe() {
        if (delayedActiveRecipe != null) {
            activeRecipe = factory.recipeType.getRecipe((ServerWorld) world, delayedActiveRecipe);
            delayedActiveRecipe = null;
            if (activeRecipe == null) { // If a recipe got removed, we need to reset the efficiency and the used energy
                                        // to allow the machine to resume processing.
                efficiencyTicks = 0;
                usedEnergy = 0;
            }
        }
    }

    protected Iterable<MachineRecipe> getRecipes() {
        if (efficiencyTicks > 0) {
            return Collections.singletonList(activeRecipe);
        } else {
            ServerWorld serverWorld = (ServerWorld) world;
            MachineRecipeType recipeType = factory.recipeType;
            List<MachineRecipe> recipes = new ArrayList<>(recipeType.getFluidOnlyRecipes(serverWorld));
            for (ConfigurableItemStack stack : getItemInputStacks()) {
                if (!stack.getStack().isEmpty()) {
                    recipes.addAll(recipeType.getMatchingRecipes(serverWorld, stack.getStack().getItem()));
                }
            }
            return recipes;
        }
    }

    public MachineTier getTier() {
        return factory.tier;
    }

    /**
     * Try to start a recipe. Return true if success, false otherwise. If false,
     * nothing was changed.
     */
    private boolean tryStartRecipe(MachineRecipe recipe, IntArrayList cachedItemCounts) {
        if (takeItemInputs(recipe, true, cachedItemCounts) && takeFluidInputs(recipe, true) && putItemOutputs(recipe, true, false)
                && putFluidOutputs(recipe, true, false)) {
            takeItemInputs(recipe, false, cachedItemCounts);
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

    private static int getRecipeMaxEu(MachineTier tier, int recipeEu, int totalEu, int efficiencyTicks) {
        int baseEu = Math.max(tier.getBaseEu(), recipeEu);
        return Math.min(totalEu, Math.min((int) Math.floor(baseEu * getEfficiencyOverclock(efficiencyTicks)), tier.getMaxEu()));
    }

    private int getRecipeMaxEfficiencyTicks(int eu, int totalEu) {
        if (efficiencyTicks != 0)
            throw new RuntimeException("Illegal state");
        for (int ticks = 0; true; ++ticks) {
            if (getRecipeMaxEu(getTier(), eu, totalEu, ticks) == Math.min(getTier().getMaxEu(), totalEu))
                return ticks;
        }
    }

    protected boolean banRecipe(MachineRecipe recipe) {
        return recipe.eu > getTier().getMaxEu();
    }

    protected boolean updateActiveRecipe() {
        // We need to setup the item counts before calling takeItemInputs
        IntArrayList cachedItemCounts = getCachedItemCounts();
        prepareItemInputs(cachedItemCounts);

        // Only then can we run the iteration over the recipes
        for (MachineRecipe recipe : getRecipes()) {
            if (banRecipe(recipe))
                continue;
            if (tryStartRecipe(recipe, cachedItemCounts)) {
                // Make sure we recalculate the max efficiency ticks if the recipe changes or if
                // the efficiency has reached 0 (the latter is to recalculate the efficiency for
                // 0.3.6 worlds without having to break and replace the machines)
                if (activeRecipe != recipe || efficiencyTicks == 0) {
                    maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(recipe.eu, recipe.eu * recipe.duration);
                }
                activeRecipe = recipe;
                usedEnergy = 0;
                recipeEnergy = recipe.eu * recipe.duration;
                recipeMaxEu = getRecipeMaxEu(getTier(), recipe.eu, recipeEnergy, efficiencyTicks);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (world.isClient)
            return;
        loadDelayedActiveRecipe();

        boolean wasActive = isActive;

        // START RECIPE IF NECESSARY
        // usedEnergy == 0 means that no recipe is currently started
        boolean recipeStarted = false;
        if (usedEnergy == 0 && canRecipeStart()) {
            if (getEu(1, true) == 1) {
                recipeStarted = updateActiveRecipe();
            }
        }

        // PROCESS RECIPE TICK
        int eu = 0;
        boolean finishedRecipe = false; // whether the recipe finished this tick
        if (activeRecipe != null && canRecipeProgress() && (usedEnergy > 0 || recipeStarted)) {
            eu = getEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), false);
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

        if (wasActive != isActive) {
            sync();
        }
        markDirty();

        autoExtract();
    }

    protected boolean canRecipeStart() {
        return true;
    }

    protected void autoExtract() {
        if (outputDirection != null) {
            if (extractItems)
                autoExtractItems(world, pos, outputDirection);
            if (extractFluids)
                autoExtractFluids(world, pos, outputDirection);
        }
    }

    // Must be true if canRecipeStart is true!
    protected boolean canRecipeProgress() {
        return true;
    }

    public List<ConfigurableItemStack> getItemInputStacks() {
        return itemStacks.subList(0, factory.getInputSlots());
    }

    public List<ConfigurableFluidStack> getFluidInputStacks() {
        return fluidStacks.subList(factory instanceof SteamMachineFactory ? 1 : 0, factory.getLiquidInputSlots());
    }

    public List<ConfigurableItemStack> getItemOutputStacks() {
        return itemStacks.subList(factory.getInputSlots(), itemStacks.size());
    }

    public List<ConfigurableFluidStack> getFluidOutputStacks() {
        return fluidStacks.subList(factory.getLiquidInputSlots(), fluidStacks.size());
    }

    /**
     * This allows not having to copy the input stacks when trying to match a
     * recipe. Just keeping track of the counts of the various stacks is enough, and
     * that's exactly what this shared array does. We need the ThreadLocal because
     * machines in different worlds may be ticked at different times.
     */
    private static ThreadLocal<IntArrayList> cachedItemCounts = new ThreadLocal<>();

    private IntArrayList getCachedItemCounts() {
        // Note: Not using the default constructor, because empty fastutils lists don't
        // resize properly.
        if (cachedItemCounts.get() == null)
            cachedItemCounts.set(new IntArrayList(1));
        return cachedItemCounts.get();
    }

    private void prepareItemInputs(IntArrayList itemCounts) {
        List<ConfigurableItemStack> baseList = getItemInputStacks();
        itemCounts.size(baseList.size());
        for (int i = 0; i < baseList.size(); i++) {
            itemCounts.set(i, baseList.get(i).getStack().getCount());
        }
    }

    /**
     * cachedItemCounts must be correct when this function is called, and are
     * guaranteed to be correct after this call
     */
    private boolean takeItemInputs(MachineRecipe recipe, boolean simulate, IntArrayList itemCounts) {
        List<ConfigurableItemStack> baseList = getItemInputStacks();

        boolean changedItems = false;
        boolean ok = true;
        for (MachineRecipe.ItemInput input : recipe.itemInputs) {
            if (!simulate && input.probability < 1) { // if we are not simulating, there is a chance we don't need to take this output
                if (ThreadLocalRandom.current().nextFloat() >= input.probability) {
                    continue;
                }
            }
            int remainingAmount = input.amount;
            for (int i = 0; i < baseList.size(); i++) {
                ConfigurableItemStack stack = baseList.get(i);
                if (itemCounts.getInt(i) > 0 && input.matches(stack.getStack())) {
                    int taken = Math.min(itemCounts.getInt(i), remainingAmount);
                    if (!simulate)
                        stack.getStack().decrement(taken);
                    itemCounts.set(i, itemCounts.getInt(i) - taken);
                    changedItems = true;
                    remainingAmount -= taken;
                    if (remainingAmount == 0)
                        break;
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }

        if (changedItems) {
            prepareItemInputs(itemCounts);
        }

        return ok;
    }

    protected boolean takeFluidInputs(MachineRecipe recipe, boolean simulate) {
        List<ConfigurableFluidStack> baseList = getFluidInputStacks();
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        boolean ok = true;
        for (MachineRecipe.FluidInput input : recipe.fluidInputs) {
            if (!simulate && input.probability < 1) { // if we are not simulating, there is a chance we don't need to take this output
                if (ThreadLocalRandom.current().nextFloat() >= input.probability) {
                    continue;
                }
            }
            int remainingAmount = input.amount;
            for (ConfigurableFluidStack stack : stacks) {
                if (stack.getFluid().getRawFluid() == input.fluid) {
                    int taken = Math.min(remainingAmount, stack.getAmount());
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
        List<ConfigurableItemStack> baseList = getItemOutputStacks();
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
                    ItemStack st = stack.getStack();
                    if (st.getItem() == output.item || st.isEmpty()) {
                        int ins = Math.min(remainingAmount, output.item.getMaxCount() - st.getCount());
                        if (st.isEmpty()) {
                            if ((stack.isMachineLocked() || stack.isPlayerLocked() || loopRun == 1) && stack.canInsert(new ItemStack(output.item))) {
                                stack.setStack(new ItemStack(output.item, ins));
                            } else {
                                ins = 0;
                            }
                        } else {
                            st.increment(ins);
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

    protected boolean putFluidOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableFluidStack> baseList = getFluidOutputStacks();
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<FluidKey> lockFluids = new ArrayList<>();

        boolean ok = true;
        for (MachineRecipe.FluidOutput output : recipe.fluidOutputs) {
            if (output.probability < 1) {
                if (simulate)
                    continue; // don't check output space for probabilistic recipes
                float randFloat = ThreadLocalRandom.current().nextFloat();
                if (randFloat > output.probability)
                    continue;
            }
            FluidKey key = FluidKeys.get(output.fluid);
            int remainingAmount = ConfigurableInventory.internalInsert(stacks, key, output.amount, ACTION, s -> true, index -> {
                locksToToggle.add(index);
                lockFluids.add(key);
            }, () -> {
                if (!simulate)
                    markDirty();
            });
            if (remainingAmount > 0)
                ok = false;
        }

        if (toggleLock) {
            for (int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockFluids.get(i));
            }
        }
        return ok;
    }

    protected void clearLocks() {
        for (ConfigurableItemStack stack : getItemOutputStacks()) {
            if (stack.isMachineLocked())
                stack.disableMachineLock();
        }
        for (ConfigurableFluidStack stack : getFluidOutputStacks()) {
            if (stack.isMachineLocked())
                stack.disableMachineLock();
        }
    }

    protected List<ConfigurableFluidStack> getSteamInputStacks() {
        return fluidStacks.subList(0, 1);
    }

    public int getEu(int maxEu, boolean simulate) {
        if (factory instanceof SteamMachineFactory) {
            int totalRem = 0;
            for (ConfigurableFluidStack stack : getSteamInputStacks()) {
                if (stack.getFluid() == STEAM_KEY) {
                    int amount = stack.getAmount();
                    int rem = Math.min(maxEu, amount);
                    if (!simulate) {
                        stack.decrement(rem);
                    }
                    maxEu -= rem;
                    totalRem += rem;
                }
            }
            return totalRem;
        } else {
            int ext = (int) Math.min(storedEu, maxEu);
            if (!simulate) {
                storedEu -= ext;
            }
            return ext;
        }
    }

    @Override
    public void setItemExtract(boolean extract) {
        extractItems = extract;
        if (!world.isClient)
            sync();
        markDirty();
    }

    @Override
    public void setFluidExtract(boolean extract) {
        extractFluids = extract;
        if (!world.isClient)
            sync();
        markDirty();
    }

    @Override
    public boolean getItemExtract() {
        return extractItems;
    }

    @Override
    public boolean getFluidExtract() {
        return extractFluids;
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if (getTier() != null && getTier().isElectric()) {
            to.offer(buildInsertable(CableTier.LV)); // TODO: cache this to prevent allocation
        }
    }

    protected EnergyInsertable buildInsertable(CableTier cableTier) {
        return new EnergyInsertable() {
            @Override
            public long insertEnergy(long amount) {
                long ins = Math.min(amount, getMaxStoredEu() - storedEu);
                storedEu += ins;
                markDirty();
                return amount - ins;
            }

            @Override
            public boolean canInsert(CableTier tier) {
                return tier == cableTier;
            }
        };
    }

    protected EnergyExtractable buildExtractable(CableTier cableTier) {
        return new EnergyExtractable() {
            @Override
            public long extractEnergy(long maxAmount) {
                long ext = Math.min(maxAmount, storedEu);
                storedEu -= ext;
                markDirty();
                return ext;
            }

            @Override
            public boolean canExtract(CableTier tier) {
                return tier == cableTier;
            }
        };
    }

    protected void autoExtractEnergy(Direction direction, CableTier extractTier) {
        EnergyInsertable insertable = EnergyAttributes.INSERTABLE.getFirstOrNull(world, pos.offset(direction), SearchOptions.inDirection(direction));
        if (insertable != null && insertable.canInsert(extractTier)) {
            storedEu = insertable.insertEnergy(storedEu);
        }
    }

    void lockRecipe(MachineRecipe recipe, PlayerInventory inventory) {
        // ITEM INPUTS
        outer: for (MachineRecipe.ItemInput input : recipe.itemInputs) {
            for (ConfigurableItemStack stack : getItemInputStacks()) {
                if (input.matches(new ItemStack(stack.getLockedItem())))
                    continue outer;
            }
            Item targetItem = null;
            // Find the first match in the player inventory (useful for logs for example)
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack playerStack = inventory.getStack(i);
                if (!playerStack.isEmpty() && input.matches(new ItemStack(playerStack.getItem()))) {
                    targetItem = playerStack.getItem();
                    break;
                }
            }
            if (targetItem == null) {
                // Find the first match that is an item from MI (useful for ingots for example)
                for (Item item : input.getInputItems()) {
                    Identifier id = Registry.ITEM.getId(item);
                    if (id.getNamespace().equals(ModernIndustrialization.MOD_ID)) {
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
                for (ConfigurableItemStack stack : getItemInputStacks()) {
                    if (stack.playerLock(targetItem)) {
                        markDirty();
                        break;
                    }
                }
            }
        }
        // ITEM OUTPUTS
        outer: for (MachineRecipe.ItemOutput output : recipe.itemOutputs) {
            for (ConfigurableItemStack stack : getItemOutputStacks()) {
                if (stack.getLockedItem() == output.item)
                    continue outer;
            }
            for (ConfigurableItemStack stack : getItemOutputStacks()) {
                if (stack.playerLock(output.item)) {
                    markDirty();
                    break;
                }
            }
        }

        // FLUID INPUTS
        outer: for (MachineRecipe.FluidInput input : recipe.fluidInputs) {
            FluidKey fluid = FluidKeys.get(input.fluid);
            for (ConfigurableFluidStack stack : getFluidInputStacks()) {
                if (stack.getLockedFluid() == fluid)
                    continue outer;
            }
            for (ConfigurableFluidStack stack : getFluidInputStacks()) {
                if (stack.playerLock(fluid)) {
                    markDirty();
                    break;
                }
            }
        }
        // FLUID OUTPUTS
        outer: for (MachineRecipe.FluidOutput output : recipe.fluidOutputs) {
            FluidKey fluid = FluidKeys.get(output.fluid);
            for (ConfigurableFluidStack stack : getFluidOutputStacks()) {
                if (stack.getLockedFluid() == fluid)
                    continue outer;
            }
            for (ConfigurableFluidStack stack : getFluidOutputStacks()) {
                if (stack.playerLock(fluid)) {
                    markDirty();
                    break;
                }
            }
        }
    }
}
