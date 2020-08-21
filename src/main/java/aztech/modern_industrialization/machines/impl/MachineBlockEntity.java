package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableInventories;
import aztech.modern_industrialization.inventory.ConfigurableInventory;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.client.tutorial.OpenInventoryTutorialStepHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
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

import java.util.ArrayList;
import java.util.List;

public class MachineBlockEntity extends AbstractMachineBlockEntity
        implements Tickable, ExtendedScreenHandlerFactory, ConfigurableInventory {

    private final List<ConfigurableItemStack> itemStacks;
    private final List<ConfigurableFluidStack> fluidStacks; // TODO: sync with player
    private int openCount = 0; // TODO: sync with player

    private MachineFactory factory;
    private MachineRecipeType recipeType;
    private MachineRecipe activeRecipe = null; // TODO: efficiency progress bar
    private Identifier delayedActiveRecipe;

    private int usedEnergy;
    private int recipeEnergy;
    private int recipeMaxEu;

    private PropertyDelegate propertyDelegate;

    public MachineBlockEntity(MachineFactory factory, MachineRecipeType recipeType) {
        super(factory.blockEntityType, Direction.NORTH);
        this.factory = factory;
        this.recipeType = recipeType;
        itemStacks = new ArrayList<>();
        for(int i = 0; i < factory.getInputSlots(); ++i) {
            itemStacks.add(ConfigurableItemStack.standardInputSlot());
        }
        for(int i = 0; i < factory.getOutputSlots(); ++i) {
            itemStacks.add(ConfigurableItemStack.standardOutputSlot());
        }
        fluidStacks = new ArrayList<>();
        for(int i = 0; i < factory.getLiquidInputSlots(); ++i) {
            if(i == 0 && factory instanceof SteamMachineFactory) {
                fluidStacks.add(ConfigurableFluidStack.steamInputSlot(this, ((SteamMachineFactory) factory).getSteamBucketCapacity() * FluidUnit.DROPS_PER_BUCKET));
            } else {
                fluidStacks.add(ConfigurableFluidStack.standardInputSlot(this, factory.getInputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET));
            }
        }
        for(int i = 0; i < factory.getLiquidOutputSlots(); ++i) {
            fluidStacks.add(ConfigurableFluidStack.standardOutputSlot(this, factory.getOutputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET));
        }

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0) return isActive ? 1 : 0;
                else if(index == 1) return usedEnergy;
                else if(index == 2) return recipeEnergy;
                else return -1;
            }

            @Override
            public void set(int index, int value) {
                if(index == 0) isActive = value == 1;
                else if(index == 1) usedEnergy = value;
                else if(index == 2) recipeEnergy = value;
            }

            @Override
            public int size() {
                return 3;
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
    public void onOpen(PlayerEntity player) {
        openCount++;
        for(ConfigurableFluidStack stack : fluidStacks) {
            stack.updateDisplayedItem();
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        openCount--;
    }

    @Override
    public boolean isOpen() {
        return openCount > 0;
    }

    @Override
    public boolean providesFluidExtractionForce(Direction direction, Fluid fluid) {
        return false; // TODO auto extract
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
        if(activeRecipe != null) {
            tag.putString("activeRecipe", this.activeRecipe.getId().toString());
        }
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        readFromTag(tag);
        this.usedEnergy = tag.getInt("usedEnergy");
        this.recipeEnergy = tag.getInt("recipeEnergy");
        this.recipeMaxEu = tag.getInt("recipeMaxEu");
        this.delayedActiveRecipe = tag.contains("activeRecipe") ? new Identifier(tag.getString("activeRecipe")) : null;
    }

    public MachineFactory getFactory() {
        return factory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        ConfigurableInventories.toBuf(packetByteBuf, this);
        packetByteBuf.writeInt(propertyDelegate.size());
        packetByteBuf.writeString(factory.getID());
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        if(delayedActiveRecipe != null) {
            activeRecipe = recipeType.getRecipe((ServerWorld) world, delayedActiveRecipe);
            delayedActiveRecipe = null;
        }

        boolean wasActive = isActive;

        if(activeRecipe == null) {
            if(getEu(1, true) == 1) {
                for (MachineRecipe recipe : recipeType.getRecipes((ServerWorld) world)) {
                    if (takeItemInputs(recipe, true) && takeFluidInputs(recipe, true) && putItemOutputs(recipe, true, false) && putFluidOutputs(recipe, true, false)) {
                        takeItemInputs(recipe, false);
                        takeFluidInputs(recipe, false);
                        putItemOutputs(recipe, true, true);
                        putFluidOutputs(recipe, true, true);
                        activeRecipe = recipe;
                        usedEnergy = 0;
                        recipeEnergy = recipe.eu * recipe.duration;
                        recipeMaxEu = recipe.eu;
                        break;
                    }
                }
            }
        }
        if(activeRecipe != null) {
            int eu = getEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), false);
            isActive = eu > 0;
            usedEnergy += eu;

            if(usedEnergy == recipeEnergy) {
                putItemOutputs(activeRecipe, false, false);
                putFluidOutputs(activeRecipe, false, false);
                clearLocks();
                activeRecipe = null; // TODO: reuse recipe
                usedEnergy = 0;
            }
        } else {
            isActive = false;
        }

        if(wasActive != isActive) {
            sync();
        }
        markDirty();
    }

    private boolean takeItemInputs(MachineRecipe recipe, boolean simulate) {
        List<ConfigurableItemStack> baseList = itemStacks.subList(0, factory.getInputSlots());
        List<ConfigurableItemStack> stacks = simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

        boolean ok = true;
        for(MachineRecipe.ItemInput input : recipe.itemInputs) {
            int remainingAmount = input.amount;
            for(ConfigurableItemStack stack : stacks) {
                if(stack.getStack().getItem() == input.item) {
                    ItemStack taken = stack.splitStack(remainingAmount);
                    remainingAmount -= taken.getCount();
                    if(remainingAmount == 0) break;
                }
            }
            if(remainingAmount > 0) ok = false;
        }
        return ok;
    }

    private boolean takeFluidInputs(MachineRecipe recipe, boolean simulate) {
        List<ConfigurableFluidStack> baseList = fluidStacks.subList(0, factory.getLiquidInputSlots());
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        boolean ok = true;
        for(MachineRecipe.FluidInput input : recipe.fluidInputs) {
            int remainingAmount = input.amount;
            for(ConfigurableFluidStack stack : stacks) {
                if(stack.isSteamInput()) continue;
                if(stack.getFluid() == input.fluid) {
                    int taken = Math.min(remainingAmount, stack.getAmount());
                    stack.decrement(taken);
                    remainingAmount -= taken;
                    if(remainingAmount == 0) break;
                }
            }
            if(remainingAmount > 0) ok = false;
        }
        return ok;
    }

    private boolean putItemOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableItemStack> baseList = itemStacks.subList(factory.getInputSlots(), itemStacks.size());
        List<ConfigurableItemStack> stacks = simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Item> lockItems = new ArrayList<>();

        boolean ok = true;
        for(MachineRecipe.ItemOutput output : recipe.itemOutputs) {
            int remainingAmount = output.amount;
            // Try to insert in non-empty stacks or locked first, then also allow insertion in empty stacks.
            for(int loopRun = 0; loopRun < 2; loopRun++) {
                int stackId = 0;
                for (ConfigurableItemStack stack : stacks) {
                    stackId++;
                    ItemStack st = stack.getStack();
                    if(st.getItem() == output.item || st.isEmpty()) {
                        int ins = Math.min(remainingAmount, Math.min(getMaxCountPerStack(), output.item.getMaxCount()) - st.getCount());
                        if (st.isEmpty()) {
                            if ((stack.isMachineLocked() || stack.isPlayerLocked() || loopRun == 1) && stack.canInsert(new ItemStack(output.item), null)) {
                                stack.setStack(new ItemStack(output.item, ins));
                            } else {
                                ins = 0;
                            }
                        } else {
                            st.increment(ins);
                        }
                        remainingAmount -= ins;
                        if(ins > 0) {
                            locksToToggle.add(stackId-1);
                            lockItems.add(output.item);
                        }
                        if (remainingAmount == 0) break;
                    }
                }
            }
            if(remainingAmount > 0) ok = false;
        }

        if(toggleLock) {
            for(int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockItems.get(i));
            }
        }
        return ok;
    }

    private boolean putFluidOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableFluidStack> baseList = fluidStacks.subList(factory.getLiquidInputSlots(), fluidStacks.size());
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Fluid> lockFluids = new ArrayList<>();

        boolean ok = true;
        for(MachineRecipe.FluidOutput output : recipe.fluidOutputs) {
            int remainingAmount = output.amount;
            // Try to insert in non-empty stacks first, then also allow insertion in empty stacks if there was no empty stack.
            outerLoop:
            for(int loopRun = 0; loopRun < 2; loopRun++) {
                int stackId = 0;
                for (ConfigurableFluidStack stack : stacks) {
                    stackId++;
                    if(stack.getFluid() == output.fluid || stack.getFluid() == Fluids.EMPTY) {
                        int ins = Math.min(remainingAmount, stack.getRemainingSpace());
                        if (stack.getFluid() == Fluids.EMPTY) {
                            if ((stack.isPlayerLocked() || stack.isMachineLocked() || loopRun == 1) && stack.canInsertFluid(output.fluid)) {
                                stack.setFluid(output.fluid);
                                stack.setAmount(ins);
                            } else {
                                ins = 0;
                            }
                        } else {
                            stack.increment(ins);
                        }
                        remainingAmount -= ins;
                        if(ins > 0) {
                            locksToToggle.add(stackId-1);
                            lockFluids.add(output.fluid);
                        }
                        // only allow one insertion
                        if(ins > 0) break outerLoop;
                    }
                }
            }
            if(remainingAmount > 0) ok = false;
        }

        if(toggleLock) {
            for(int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockFluids.get(i));
            }
        }
        return ok;
    }

    protected void clearLocks() {
        for(ConfigurableItemStack stack : itemStacks) {
            if (stack.isMachineLocked()) stack.disableMachineLock();
        }
        for(ConfigurableFluidStack stack : fluidStacks) {
            if (stack.isMachineLocked()) stack.disableMachineLock();
        }
    }

    private int getEu(int maxEu, boolean simulate) {
        if(factory instanceof SteamMachineFactory) {
            ConfigurableFluidStack steam = fluidStacks.get(0);
            int amount = steam.getAmount();
            int rem = Math.min(maxEu, amount);
            if(!simulate) {
                steam.decrement(rem);
            }
            return rem;
        } else {
            throw new UnsupportedOperationException("Only steam machines are supported");
        }
    }
}