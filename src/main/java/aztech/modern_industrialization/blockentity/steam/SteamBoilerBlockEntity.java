package aztech.modern_industrialization.blockentity.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blockentity.AbstractMachineBlockEntity;
import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.fluid.FluidSlotIO;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.gui.SteamBoilerScreenHandler;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

/**
 * Steam boiler BlockEntity.
 * Slots: 0 is the burnable fuel, 1 is the input water.
 */
public class SteamBoilerBlockEntity extends AbstractMachineBlockEntity implements SidedInventory, Tickable, FluidInventory {
    private static final int WATER_CONSUMPTION = 1;
    private static final int STEAM_PRODUCTION = 8;
    private static final int BURN_TIME_MULTIPLIER = 10;
    public static final int MIN_TEMPERATURE = 25;
    public static final int BOILING_TEMPERATURE = 100;
    public static final int MAX_TEMPERATURE = 1100;
    private int burnTime = 0;
    private int totalBurnTime = 1;
    private int temperature = MIN_TEMPERATURE;
    private PropertyDelegate propertyDelegate;

    public SteamBoilerBlockEntity() {
        super(ModernIndustrialization.BLOCK_ENTITY_STEAM_BOILER, 3, Direction.NORTH);

        ItemStack waterFluidStack = FluidStackItem.getEmptyStack();
        FluidStackItem.setCapacity(waterFluidStack, 4 * FluidUnit.DROPS_PER_BUCKET);
        FluidStackItem.setIO(waterFluidStack, FluidSlotIO.INPUT_ONLY);
        this.inventory.set(1, waterFluidStack);

        ItemStack steamFluidStack = FluidStackItem.getEmptyStack();
        FluidStackItem.setCapacity(steamFluidStack, 4 * FluidUnit.DROPS_PER_BUCKET);
        FluidStackItem.setIO(steamFluidStack, FluidSlotIO.OUTPUT_ONLY);
        this.inventory.set(2, steamFluidStack);

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0) return burnTime;
                else if(index == 1) return totalBurnTime;
                else if(index == 2) return temperature;
                else if(index == 3) return isActive ? 1 : 0;
                else return -1;
            }

            @Override
            public void set(int index, int value) {
                if(index == 0) burnTime = value;
                else if(index == 1) totalBurnTime = value;
                else if(index == 2) temperature = value;
                else if(index == 3) isActive = value == 1;
            }

            @Override
            public int size() {
                return 4;
            }
        };
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("machine.steam_boiler");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new SteamBoilerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot == 0 && canInsertItem(stack.getItem());
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    public static boolean canInsertItem(ItemConvertible item) {
        return FuelRegistryImpl.INSTANCE.get(item) != null;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);

        tag.putInt("burnTime", this.burnTime);
        tag.putInt("totalBurnTime", this.totalBurnTime);
        tag.putInt("temperature", this.temperature);

        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        this.burnTime = tag.getInt("burnTime");
        this.totalBurnTime = tag.getInt("totalBurnTime");
        this.temperature = tag.getInt("temperature");
    }

    @Override
    public void tick() {
        if(this.world.isClient) return;

        boolean wasActive = this.isActive;

        this.isActive = false;
        if(this.burnTime == 0) {
            ItemStack fuel = this.inventory.get(0);
            if(fuel.getCount() > 0) {
                this.totalBurnTime = FuelRegistryImpl.INSTANCE.get(fuel.getItem()) * BURN_TIME_MULTIPLIER;
                this.burnTime = totalBurnTime;
                fuel.decrement(1);
            }
        }

        if(this.burnTime > 0) {
            this.isActive = true;
            this.burnTime--;
        }

        if(this.isActive) {
            this.temperature = Math.min(this.temperature+1, MAX_TEMPERATURE);
        } else {
            this.temperature = Math.max(this.temperature-1, MIN_TEMPERATURE);
        }

        if(this.temperature >= BOILING_TEMPERATURE) {
            ItemStack waterInput = inventory.get(1), steamOutput = inventory.get(2);
            int waterAmount = FluidStackItem.getAmount(waterInput);
            if(waterAmount > 0) {
                int steamAmount = STEAM_PRODUCTION * (this.temperature - MIN_TEMPERATURE) / (MAX_TEMPERATURE - MIN_TEMPERATURE);
                FluidStackItem.setAmount(waterInput, waterAmount - WATER_CONSUMPTION);
                FluidStackItem.setFluid(steamOutput, ModernIndustrialization.FLUID_STEAM);
                FluidStackItem.setAmount(steamOutput, FluidStackItem.getAmount(steamOutput) + steamAmount);
            }
        }

        if(isActive != wasActive) {
            sync();
        }
        markDirty();
    }

    @Override
    public int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        if(!canFluidContainerConnect(direction)) return 0;

        if(fluid == Fluids.WATER) {
            int waterAmount = FluidStackItem.getAmount(inventory.get(1));
            int waterCapacity = FluidStackItem.getCapacity(inventory.get(1));
            int amount = waterCapacity - waterAmount;
            int inserted = Math.min(amount, maxAmount);
            if(!simulate) {
                int newAmount = amount + inserted;
                FluidStackItem.setAmount(inventory.get(1), newAmount);
                if(amount == 0 && newAmount > 0) {
                    FluidStackItem.setFluid(inventory.get(1), fluid);
                }
            }
            return inserted;
        } else {
            return 0;
        }
    }

    @Override
    public int extract(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        if(!canFluidContainerConnect(direction)) return 0;

        if(fluid == ModernIndustrialization.FLUID_STEAM) {
            int amount = FluidStackItem.getAmount(inventory.get(2));
            int extracted = Math.min(amount, maxAmount);
            if(!simulate) {
                FluidStackItem.setAmount(inventory.get(2), amount - extracted);
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public Fluid[] getExtractableFluids(Direction direction) {
        return new Fluid[] { ModernIndustrialization.FLUID_STEAM };
    }

    @Override
    public boolean canFluidContainerConnect(Direction direction) {
        return direction != facingDirection;
    }

    @Override
    public boolean providesFluidExtractionForce(Direction direction, Fluid fluid) {
        return true;
    }
}
