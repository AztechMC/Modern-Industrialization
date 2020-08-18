package aztech.modern_industrialization.blockentity;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blockentity.factory.MachineFactory;
import aztech.modern_industrialization.blockentity.factory.MachineSlotType;
import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.fluid.FluidSlotIO;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.gui.MachineScreenHandler;
import aztech.modern_industrialization.gui.SteamBoilerScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import static aztech.modern_industrialization.blockentity.factory.MachineSlotType.LIQUID_INPUT_SLOT;
import static aztech.modern_industrialization.blockentity.factory.MachineSlotType.LIQUID_OUTPUT_SLOT;



public abstract class MachineBlockEntity extends AbstractMachineBlockEntity
        implements SidedInventory, Tickable, ExtendedScreenHandlerFactory, FluidInventory {

    private MachineFactory factory;


    private int tickProgress;
    private int tickRecipe;
    private boolean isActive;

    private PropertyDelegate propertyDelegate;

    private int availableSlot[];

    protected MachineBlockEntity(BlockEntityType<?> blockEntityType, MachineFactory factory) {
        super(blockEntityType, factory.getSlots(), Direction.NORTH);
        this.factory = factory;

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

        availableSlot = new int[factory.getInputSlots()];

        for(int i = 0; i < factory.getInputSlots(); i++) {
            availableSlot[i] = i;
        }

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0) return isActive ? 1 : 0;
                else if(index == 1) return tickProgress;
                else if(index == 2) return tickRecipe;
                else return -1;
            }

            @Override
            public void set(int index, int value) {
                if(index == 0) isActive = value == 1;
                else if(index == 1) tickProgress = value;
                else if(index == 2) tickRecipe = value;
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
        return factory.getSlotType(slot) == MachineSlotType.INPUT_SLOT || factory.getSlotType(slot) == MachineSlotType.OUTPUT_SLOT;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("tickProgress", this.tickProgress);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.tickProgress = tag.getInt("tickProgress");
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
    public int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate){
        return 0;
    }

    @Override
    public int extract(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        return 0;
    }

    @Override
    public Fluid[] getExtractableFluids(Direction direction) {
        return new Fluid[] {};
    }

}