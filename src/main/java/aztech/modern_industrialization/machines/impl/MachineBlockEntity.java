package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.fluid.FluidSlotIO;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SidedInventory;
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
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

import static aztech.modern_industrialization.machines.impl.MachineSlotType.*;



public abstract class MachineBlockEntity extends AbstractMachineBlockEntity
        implements SidedInventory, Tickable, ExtendedScreenHandlerFactory, FluidInventory {

    private MachineFactory factory;
    private MachineRecipeType recipeType;
    private MachineRecipe activeRecipe = null;

    private int usedEnergy;
    private int recipeEnergy;
    private int recipeMaxEu;

    private PropertyDelegate propertyDelegate;

    private int availableSlot[];

    protected MachineBlockEntity(MachineFactory factory, MachineRecipeType recipeType) {
        super(factory.blockEntityType, factory.getSlots(), Direction.NORTH);
        this.factory = factory;
        this.recipeType = recipeType;

        for(int i = 0; i < factory.getSlots(); i++){
            if(factory.getSlotType(i) == LIQUID_INPUT_SLOT){
                ItemStack fluidInput = FluidStackItem.getEmptyStack();
                FluidStackItem.setCapacity(fluidInput, factory.getInputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET);
                FluidStackItem.setIO(fluidInput, FluidSlotIO.INPUT_ONLY);
                this.inventory.set(i, fluidInput);
            }else if(factory.getSlotType(i) == LIQUID_OUTPUT_SLOT){
                ItemStack fluidOutput = FluidStackItem.getEmptyStack();
                FluidStackItem.setCapacity(fluidOutput, factory.getOutputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET);
                FluidStackItem.setIO(fluidOutput, FluidSlotIO.OUTPUT_ONLY);
                this.inventory.set(i, fluidOutput);
            }
        }

        availableSlot = new int[factory.getSlots()];

        for(int i = 0; i < factory.getSlots(); i++) {
            availableSlot[i] = i;
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
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new MachineScreenHandler(syncId, playerInventory, this, this.propertyDelegate, this.factory);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText(factory.getTranslationKey());
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return availableSlot;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return factory.getSlotType(slot) == MachineSlotType.INPUT_SLOT;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return factory.getSlotType(slot) == MachineSlotType.OUTPUT_SLOT;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("tickProgress", this.usedEnergy);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.usedEnergy = tag.getInt("tickProgress");
    }

    public MachineFactory getFactory() {
        return factory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeInt(factory.getSlots());
        packetByteBuf.writeInt(propertyDelegate.size());
        packetByteBuf.writeString(factory.getID());
    }

    @Override
    public int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate){ // TODO: steam machine with steam input slot
        for(int i = 0; i < factory.getLiquidInputSlots(); i++) {
            int idx = factory.getInputSlots() + i;
            ItemStack stack = inventory.get(idx);
            if(FluidStackItem.getFluid(stack) == fluid) {
                int ins = Math.min(FluidStackItem.getCapacity(stack) - FluidStackItem.getAmount(stack), maxAmount);
                if(!simulate) {
                    FluidStackItem.increment(stack, ins);
                }
                return ins;
            } else if(FluidStackItem.getFluid(stack) == Fluids.EMPTY) {
                int ins = Math.min(FluidStackItem.getCapacity(stack), maxAmount);
                if(!simulate) {
                    FluidStackItem.setFluid(stack, fluid);
                    FluidStackItem.setAmount(stack, ins);
                }
                return ins;
            }
        }
        return 0;
    }

    @Override
    public int extract(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        for(int i = 0; i < factory.getLiquidOutputSlots(); i++) {
            int idx = factory.getInputSlots() + factory.getLiquidInputSlots() + factory.getOutputSlots();
            ItemStack stack = inventory.get(idx);
            if(FluidStackItem.getFluid(stack) == fluid) {
                int ext = Math.min(FluidStackItem.getAmount(stack) - FluidStackItem.getCapacity(stack), maxAmount);
                if(!simulate) {
                    FluidStackItem.decrement(stack, ext);
                }
                return ext;
            }
        }
        return 0;
    }

    @Override
    public Fluid[] getExtractableFluids(Direction direction) {
        List<Fluid> fluids = new ArrayList<>();
        for(int i = 0; i < factory.getLiquidOutputSlots(); i++) {
            int idx = factory.getInputSlots() + factory.getLiquidInputSlots() + factory.getOutputSlots();
            ItemStack stack = inventory.get(idx);
            Fluid fluid = FluidStackItem.getFluid(stack);
            if(fluid != Fluids.EMPTY) {
                fluids.add(fluid);
            }
        }
        return fluids.toArray(new Fluid[0]);
    }

    @Override
    public boolean providesFluidExtractionForce(Direction direction, Fluid fluid) {
        return false; // TODO: auto-extract
    }

    @Override
    public boolean canFluidContainerConnect(Direction direction) {
        return factory.getLiquidInputSlots() + factory.getLiquidOutputSlots() > 0;
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        boolean wasActive = isActive;

        if(activeRecipe == null) { // TODO: don't start recipe if no energy
            for(MachineRecipe recipe : recipeType.getRecipes((ServerWorld) world)) {
                if(takeItemInputs(recipe, true) && takeFluidInputs(recipe, true) && putItemOutputs(recipe, true) && putFluidOutputs(recipe, true)) {
                    takeItemInputs(recipe, false);
                    takeFluidInputs(recipe, false);
                    activeRecipe = recipe;
                    usedEnergy = 0;
                    recipeEnergy = recipe.eu * recipe.duration;
                    recipeMaxEu = recipe.eu;
                    break;
                }
            }
        }
        if(activeRecipe != null) {
            int eu = getEu(Math.min(recipeMaxEu, recipeEnergy - usedEnergy), false);
            isActive = eu > 0;
            usedEnergy += eu;

            if(usedEnergy == recipeEnergy) {
                putItemOutputs(activeRecipe, false);
                putFluidOutputs(activeRecipe, false);
                activeRecipe = null; // TODO: reuse recipe
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
        int[] itemCounts = new int[recipe.itemInputs.size()];
        for (int i = 0; i < recipe.itemInputs.size(); i++) {
            itemCounts[i] = recipe.itemInputs.get(i).amount;
        }

        for (int j = 0; j < factory.getInputSlots(); j++) {
            ItemStack stack = inventory.get(j);
            int count = stack.getCount();
            for (int i = 0; i < recipe.itemInputs.size(); i++) {
                MachineRecipe.ItemInput inp = recipe.itemInputs.get(i);
                if (stack.getItem() == inp.item) {
                    int removedCount = Math.min(itemCounts[i], count);
                    if (!simulate) {
                        stack.decrement(removedCount);
                    }
                    count -= removedCount;
                    itemCounts[i] -= removedCount;
                }
            }
        }

        for (int i = 0; i < recipe.itemInputs.size(); ++i) {
            if (itemCounts[i] != 0) return false;
        }
        return true;
    }

    private boolean takeFluidInputs(MachineRecipe recipe, boolean simulate) {
        int[] fluidCounts = new int[recipe.fluidInputs.size()];
        for(int i = 0; i < recipe.fluidInputs.size(); i++) {
            fluidCounts[i] = recipe.fluidInputs.get(i).amount;
        }

        int firstSlot = factory instanceof SteamMachineFactory ? factory.getInputSlots() + 1 : factory.getInputSlots();
        int lastSlot = factory.getInputSlots() + factory.getLiquidInputSlots();
        for(int j = firstSlot; j < lastSlot; j++) {
            ItemStack stack = inventory.get(j);
            int count = FluidStackItem.getAmount(stack);
            for(int i = 0; i < recipe.fluidInputs.size(); i++) {
                MachineRecipe.FluidInput inp = recipe.fluidInputs.get(i);
                if(FluidStackItem.getFluid(stack) == inp.fluid) {
                    int removedCount = Math.min(fluidCounts[i], count);
                    if(!simulate) {
                        FluidStackItem.decrement(stack, removedCount);
                    }
                    count -= removedCount;
                    fluidCounts[i] -= removedCount;
                }
            }
        }
        for(int i = 0; i < recipe.fluidInputs.size(); ++i) {
            if(fluidCounts[i] != 0) return false;
        }
        return true;
    }

    private boolean putItemOutputs(MachineRecipe recipe, boolean simulate) {
        List<MachineRecipe.ItemOutput> itemOutputs = recipe.itemOutputs;
        int[] outputCount = new int[itemOutputs.size()];
        for(int i = 0; i < itemOutputs.size(); i++) {
            outputCount[i] = itemOutputs.get(i).amount;
        }
        int firstSlot = factory.getInputSlots() + factory.getLiquidInputSlots();
        for(int j = firstSlot; j < firstSlot + factory.getOutputSlots(); ++j) {
            ItemStack stack = inventory.get(j);
            if(stack.isEmpty()) continue;
            int remCount = Math.min(stack.getMaxCount(), getMaxCountPerStack()) - stack.getCount();
            for(int i = 0; i < itemOutputs.size(); i++) {
                if(stack.getItem() == itemOutputs.get(i).item) {
                    int addedCount = Math.min(remCount, outputCount[i]);
                    if(!simulate) {
                        stack.increment(addedCount);
                    }
                    remCount -= addedCount;
                    outputCount[i] -= addedCount;
                }
            }
        }
        firstSlot = factory.getInputSlots() + factory.getLiquidInputSlots();
        for(int j = firstSlot; j < firstSlot + factory.getOutputSlots(); ++j) {
            ItemStack stack = inventory.get(j);
            if(!stack.isEmpty()) continue;
            stack = null;
            Item newItem = null;
            int remCount = 0;
            for(int i = 0; i < itemOutputs.size(); i++) {
                if(newItem == null && outputCount[i] > 0) {
                    newItem = itemOutputs.get(i).item;
                    remCount = Math.min(getMaxCountPerStack(), newItem.getMaxCount());
                }
                if(newItem == itemOutputs.get(i).item) {
                    int addedCount = Math.min(remCount, outputCount[i]);
                    if(!simulate) {
                        if(stack == null) {
                            stack = new ItemStack(newItem, addedCount);
                            inventory.set(j, stack);
                        } else {
                            stack.increment(addedCount);
                        }
                    }
                    remCount -= addedCount;
                    outputCount[i] -= addedCount;
                }
            }
        }
        for(int i = 0; i < recipe.fluidInputs.size(); ++i) {
            if(outputCount[i] != 0) return false;
        }
        return true;
    }

    private boolean putFluidOutputs(MachineRecipe recipe, boolean simulate) {
        List<MachineRecipe.FluidOutput> fluidOutputs = recipe.fluidOutputs;
        int[] outputCount = new int[fluidOutputs.size()];
        for(int i = 0; i < fluidOutputs.size(); i++) {
            outputCount[i] = fluidOutputs.get(i).amount;
        }
        int firstSlot = factory.getInputSlots() + factory.getLiquidInputSlots() + factory.getOutputSlots();
        for(int j = firstSlot; j < firstSlot + factory.getLiquidOutputSlots(); ++j) {
            ItemStack stack = inventory.get(j);
            int amount = FluidStackItem.getAmount(stack);
            if(amount == 0) continue;
            int remCount = FluidStackItem.getCapacity(stack) - amount;
            for(int i = 0; i < fluidOutputs.size(); i++) {
                if(FluidStackItem.getFluid(stack) == fluidOutputs.get(i).fluid) {
                    int addedCount = Math.min(remCount, outputCount[i]);
                    if(!simulate) {
                        FluidStackItem.increment(stack, addedCount);
                    }
                    remCount -= addedCount;
                    outputCount[i] -= addedCount;
                }
            }
        }
        firstSlot = factory.getInputSlots() + factory.getLiquidInputSlots() + factory.getOutputSlots();
        for(int j = firstSlot; j < firstSlot + factory.getLiquidOutputSlots(); ++j) {
            ItemStack stack = inventory.get(j);
            int amount = FluidStackItem.getAmount(stack);
            if(amount != 0) continue;
            Fluid newFluid = null;
            boolean stackUpdated = false;
            int remCount = 0;
            for(int i = 0; i < fluidOutputs.size(); i++) {
                if(newFluid == null && outputCount[i] > 0) {
                    newFluid = fluidOutputs.get(i).fluid;
                    remCount = FluidStackItem.getCapacity(stack);
                }
                if(newFluid == fluidOutputs.get(i).fluid) {
                    int addedCount = Math.min(remCount, outputCount[i]);
                    if(!simulate) {
                        if(!stackUpdated) {
                            stackUpdated = true;
                            FluidStackItem.setFluid(stack, newFluid);
                            FluidStackItem.setAmount(stack, addedCount);
                        } else {
                            FluidStackItem.increment(stack, addedCount);
                        }
                    }
                    remCount -= addedCount;
                    outputCount[i] -= addedCount;
                }
            }
        }
        for(int i = 0; i < recipe.fluidInputs.size(); ++i) {
            if(outputCount[i] != 0) return false;
        }
        return true;
    }

    private int getEu(int maxEu, boolean simulate) {
        if(factory instanceof SteamMachineFactory) {
            ItemStack steamSlot = inventory.get(factory.getInputSlots());
            int amount = FluidStackItem.getAmount(steamSlot);
            int remAmount = Math.min(maxEu, amount);
            if(!simulate) {
                FluidStackItem.decrement(steamSlot, remAmount);
            }
            return remAmount;
        } else {
            throw new UnsupportedOperationException("Only steam machines are supported");
        }
    }
}