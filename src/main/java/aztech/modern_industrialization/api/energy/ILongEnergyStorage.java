package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.MI;
import com.google.common.primitives.Ints;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.stream.StreamSupport;

public interface ILongEnergyStorage extends IEnergyStorage {
    BlockCapability<ILongEnergyStorage, @Nullable Direction> BLOCK =
            BlockCapability.createSided(MI.id("long_energy_storage"), ILongEnergyStorage.class);
    ItemCapability<ILongEnergyStorage, Void> ITEM =
            ItemCapability.createVoid(MI.id("long_energy_storage"), ILongEnergyStorage.class);

    long receive(long maxReceive, boolean simulate);

    long extract(long maxExtract, boolean simulate);

    long getAmount();

    long getCapacity();

    // Default implementations below, do not override!

    @Override
    @ApiStatus.NonExtendable
    default int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) receive(maxReceive, simulate);
    }

    @Override
    @ApiStatus.NonExtendable
    default int extractEnergy(int maxExtract, boolean simulate) {
        return (int) extract(maxExtract, simulate);
    }

    @Override
    @ApiStatus.NonExtendable
    default int getEnergyStored() {
        return Ints.saturatedCast(getAmount());
    }

    @Override
    @ApiStatus.NonExtendable
    default int getMaxEnergyStored() {
        return Ints.saturatedCast(getCapacity());
    }

    // Internal implementation below

    @ApiStatus.Internal
    static void init(RegisterCapabilitiesEvent event, Block[] allBlocks, Item[] allItems) {
        ThreadLocal<Boolean> inCompat = ThreadLocal.withInitial(() -> false);

        event.registerBlock(BLOCK, (level, pos, state, be, direction) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                var nonLongHandler = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, direction);
                return nonLongHandler == null ? null : new NonLongWrapper(nonLongHandler);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);

        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, be, direction) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                return level.getCapability(BLOCK, pos, state, be, direction);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);

        event.registerItem(ITEM, (stack, ignored) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                var nonLongHandler = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                return nonLongHandler == null ? null : new NonLongWrapper(nonLongHandler);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);

        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ignored) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                return stack.getCapability(ITEM);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);
    }
}
