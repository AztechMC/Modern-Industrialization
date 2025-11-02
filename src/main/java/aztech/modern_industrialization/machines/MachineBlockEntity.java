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
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.components.DropableComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.PlacedByComponent;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.WorldHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
import org.jspecify.annotations.Nullable;

/**
 * The base block entity for the machine system. Contains components, and an
 * inventory.
 */
@SuppressWarnings("rawtypes")
public abstract class MachineBlockEntity extends FastBlockEntity
        implements MenuProvider, WrenchableBlockEntity {
    public final ComponentStorage.GuiServer guiComponents = new ComponentStorage.GuiServer();
    public final ComponentStorage.Server components = new ComponentStorage.Server();
    public final MachineGuiParameters guiParams;
    /**
     * Server-side only: true if the next call to sync() will trigger a remesh.
     */
    private boolean syncCausesRemesh = true;
    /**
     * Caches the current redstone status. Invalidated by {@link MachineBlock}.
     * {@code null} if the current status is not known.
     */
    @Nullable
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

    protected final void registerGuiComponent(GuiComponentServer... components) {
        guiComponents.register(components);
    }

    protected final void registerComponents(MachineComponent... components) {
        this.components.register(components);
    }

    /**
     * @return The inventory that will be synced with the client.
     */
    public abstract MIInventory getInventory();

    @Override
    public final Component getDisplayName() {
        return Component.translatable(Util.makeDescriptionId("block", guiParams.blockId));
    }

    @Override
    public final AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new MachineMenuServer(syncId, inv, this, guiParams);
    }

    public final void writeScreenOpeningData(RegistryFriendlyByteBuf buf) {
        // Write inventory
        MIInventory inv = getInventory();
        CompoundTag tag = new CompoundTag();
        NbtHelper.putList(tag, "items", inv.getItemStacks(), configurableItemStack -> configurableItemStack.toNbt(buf.registryAccess()));
        NbtHelper.putList(tag, "fluids", inv.getFluidStacks(), configurableFluidStack -> configurableFluidStack.toNbt(buf.registryAccess()));
        buf.writeNbt(tag);
        // Write slot positions
        inv.itemPositions.write(buf);
        inv.fluidPositions.write(buf);
        buf.writeInt(guiComponents.size());
        // Write components
        for (GuiComponentServer<?, ?> component : guiComponents) {
            writeInitialGuiComponent(buf, component);
        }
        // Write GUI params
        guiParams.write(buf);
    }

    private static <P, D> void writeInitialGuiComponent(RegistryFriendlyByteBuf buf, GuiComponentServer<P, D> component) {
        var type = component.getType();
        buf.writeResourceLocation(type.id());
        type.paramsCodec().encode(buf, component.getParams());
        type.dataCodec().encode(buf, component.extractData());
    }

    /**
     * @param face The face that was targeted, taking the overlay into account.
     */
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(this, this::writeScreenOpeningData);
    }

    public abstract MachineModelClientData getMachineModelData();

    @MustBeInvokedByOverriders
    public void onPlaced(@Nullable LivingEntity placer, ItemStack itemStack) {
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("remesh", syncCausesRemesh);
        syncCausesRemesh = false;
        for (MachineComponent component : components) {
            component.writeClientNbt(tag, registries);
        }
        return tag;
    }

    @Override
    public final void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        for (MachineComponent component : components) {
            component.writeNbt(tag, registries);
        }
    }

    @Override
    public final void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        load(tag, registries, false);
    }

    public final void load(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        if (!tag.contains("remesh")) {
            for (MachineComponent component : components) {
                component.readNbt(tag, registries, isUpgradingMachine);
            }
        } else {
            boolean forceChunkRemesh = tag.getBoolean("remesh");
            for (MachineComponent component : components) {
                component.readClientNbt(tag, registries);
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
        components.forType(DropableComponent.class, u -> drops.add(u.getDrop()));
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
