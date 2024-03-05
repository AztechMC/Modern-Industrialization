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
package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.blocks.WrenchableBlockEntity;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.components.DropableComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.PlacedByComponent;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.WorldHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * The base block entity for the machine system. Contains components, and an
 * inventory.
 */
@SuppressWarnings("rawtypes")
public abstract class MachineBlockEntity extends FastBlockEntity
        implements MenuProvider, WrenchableBlockEntity {
    public final List<GuiComponent.Server> guiComponents = new ArrayList<>();
    private final List<IComponent> icomponents = new ArrayList<>();
    public final MachineGuiParameters guiParams;
    /**
     * Server-side only: true if the next call to sync() will trigger a remesh.
     */
    private boolean syncCausesRemesh = true;
    /**
     * Caches the current redstone status. Invalidated by {@link MachineBlock}.
     * {@code null} if the current status is not known.
     */
    private Boolean hasRedstoneHighSignal = null;

    public final OrientationComponent orientation;
    public final PlacedByComponent placedBy;

    public MachineBlockEntity(BEP bep, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(bep.type(), bep.pos(), bep.state());
        this.guiParams = guiParams;
        this.orientation = new OrientationComponent(orientationParams, this);
        this.placedBy = new PlacedByComponent();

        registerComponents(orientation, placedBy);
    }

    protected final void registerGuiComponent(GuiComponent.Server... components) {
        Collections.addAll(guiComponents, components);
    }

    protected final void registerComponents(IComponent... components) {
        Collections.addAll(icomponents, components);
    }

    /**
     * @return The inventory that will be synced with the client.
     */
    public abstract MIInventory getInventory();

    /**
     * @throws RuntimeException if the component doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <S extends GuiComponent.Server> S getComponent(ResourceLocation componentId) {
        for (GuiComponent.Server component : guiComponents) {
            if (component.getId().equals(componentId)) {
                return (S) component;
            }
        }
        throw new RuntimeException("Couldn't find component " + componentId);
    }

    private <T> List<T> tryGetComponent(Class<T> clazz) {
        List<T> components = new ArrayList<>();
        for (var component : icomponents) {
            if (clazz.isInstance(component)) {
                components.add((T) component);
            }
        }
        return components;
    }

    public final <T> void forComponentType(Class<T> clazz, Consumer<? super T> action) {
        List<T> component = tryGetComponent(clazz);
        for (T c : component) {
            action.accept(c);
        }
    }

    public <T, R> R mapComponentOrDefault(Class<T> clazz, Function<? super T, ? extends R> action, R defaultValue) {
        List<T> components = tryGetComponent(clazz);
        if (components.isEmpty()) {
            return defaultValue;
        } else if (components.size() == 1) {
            return action.apply(components.get(0));
        } else {
            throw new RuntimeException("Multiple components of type " + clazz.getName() + " found");
        }
    }

    @Override
    public final Component getDisplayName() {
        return Component.translatable("block.modern_industrialization." + guiParams.blockId);
    }

    @Override
    public final AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new MachineMenuServer(syncId, inv, this, guiParams);
    }

    public final void writeScreenOpeningData(FriendlyByteBuf buf) {
        // Write inventory
        MIInventory inv = getInventory();
        CompoundTag tag = new CompoundTag();
        NbtHelper.putList(tag, "items", inv.getItemStacks(), ConfigurableItemStack::toNbt);
        NbtHelper.putList(tag, "fluids", inv.getFluidStacks(), ConfigurableFluidStack::toNbt);
        buf.writeNbt(tag);
        // Write slot positions
        inv.itemPositions.write(buf);
        inv.fluidPositions.write(buf);
        buf.writeInt(guiComponents.size());
        // Write components
        for (GuiComponent.Server component : guiComponents) {
            buf.writeResourceLocation(component.getId());
            component.writeInitialData(buf);
        }
        // Write GUI params
        guiParams.write(buf);
    }

    /**
     * @param face The face that was targeted, taking the overlay into account.
     */
    protected InteractionResult onUse(Player player, InteractionHand hand, Direction face) {
        return InteractionResult.PASS;
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(this, this::writeScreenOpeningData);
    }

    protected abstract MachineModelClientData getMachineModelData();

    @MustBeInvokedByOverriders
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
        placedBy.onPlaced(placer);
    }

    @Override
    public boolean useWrench(Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (orientation.useWrench(player, hand, MachineOverlay.findHitSide(hitResult))) {
            getLevel().blockUpdated(getBlockPos(), Blocks.AIR);
            setChanged();
            if (!getLevel().isClientSide()) {
                sync();
            }
            return true;
        }
        return false;
    }

    @Override
    public final ModelData getModelData() {
        return ModelData.builder()
                .with(MachineModelClientData.KEY, getMachineModelData())
                .build();
    }

    @Override
    public void sync() {
        sync(true);
    }

    public void sync(boolean forceRemesh) {
        syncCausesRemesh = syncCausesRemesh || forceRemesh;
        super.sync();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("remesh", syncCausesRemesh);
        syncCausesRemesh = false;
        for (IComponent component : icomponents) {
            component.writeClientNbt(tag);
        }
        return tag;
    }

    @Override
    public final void saveAdditional(CompoundTag tag) {
        for (IComponent component : icomponents) {
            component.writeNbt(tag);
        }
    }

    @Override
    public final void load(CompoundTag tag) {
        load(tag, false);
    }

    public final void load(CompoundTag tag, boolean isUpgradingMachine) {
        if (!tag.contains("remesh")) {
            for (IComponent component : icomponents) {
                component.readNbt(tag, isUpgradingMachine);
            }
        } else {
            boolean forceChunkRemesh = tag.getBoolean("remesh");
            for (IComponent component : icomponents) {
                component.readClientNbt(tag);
            }
            if (forceChunkRemesh) {
                WorldHelper.forceChunkRemesh(level, worldPosition);
                requestModelDataUpdate();
            }
        }
    }

    @Override
    protected final boolean shouldSkipComparatorUpdate() {
        return !hasComparatorOutput();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, bet,
                    (be, direction) -> ((MachineBlockEntity) be).getInventory().itemStorage.itemHandler);
        });
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, bet,
                    (be, direction) -> ((MachineBlockEntity) be).getInventory().fluidStorage.fluidHandler);
        });
    }

    public List<ItemStack> dropExtra() {
        List<ItemStack> drops = new ArrayList<>();
        forComponentType(DropableComponent.class, u -> drops.add(u.getDrop()));
        return drops;
    }

    public List<Component> getTooltips() {
        return List.of();
    }

    protected boolean hasComparatorOutput() {
        return false;
    }

    protected int getComparatorOutput() {
        return 0;
    }

    public boolean hasRedstoneHighSignal() {
        if (this.hasRedstoneHighSignal == null) {
            refreshRedstoneStatus();
        }
        return this.hasRedstoneHighSignal;
    }

    void refreshRedstoneStatus() {
        this.hasRedstoneHighSignal = level.hasNeighborSignal(worldPosition);
    }
}
