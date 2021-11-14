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

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.api.ICacheableApiHost;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.RenderHelper;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtNull;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * The base block entity for the machine system. Contains components, and an
 * inventory.
 */
@SuppressWarnings("rawtypes")
public abstract class MachineBlockEntity extends FastBlockEntity
        implements ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity, ICacheableApiHost, WrenchableBlockEntity {
    final List<SyncedComponent.Server> syncedComponents = new ArrayList<>();
    private final List<IComponent> icomponents = new ArrayList<>();
    private final MachineGuiParameters guiParams;
    /**
     * Server-side: true if the next call to sync() will trigger a remesh.
     * Client-side: true if fromClientTag() is being called for the first time.
     */
    private boolean syncCausesRemesh = true;
    private final Set<Runnable> cacheInvalidateCallbacks = new ReferenceOpenHashSet<>();

    /**
     * Every machine has an orientation component: this is the only one that is
     * here, the others are in subclasses.
     */
    protected final OrientationComponent orientation;

    public MachineBlockEntity(BEP bep, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(bep.type(), bep.pos(), bep.state());
        this.guiParams = guiParams;
        this.orientation = new OrientationComponent(orientationParams);

        registerComponents(orientation);
    }

    protected final void registerClientComponent(SyncedComponent.Server component) {
        syncedComponents.add(component);
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
    public <S extends SyncedComponent.Server> S getComponent(Identifier componentId) {
        for (SyncedComponent.Server component : syncedComponents) {
            if (component.getId().equals(componentId)) {
                return (S) component;
            }
        }
        throw new RuntimeException("Couldn't find component " + componentId);
    }

    @Override
    public final Text getDisplayName() {
        return new TranslatableText("block.modern_industrialization." + guiParams.blockId);
    }

    @Override
    public final ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new MachineScreenHandlers.Server(syncId, inv, this, guiParams);
    }

    @Override
    public final void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        // Write inventory
        MIInventory inv = getInventory();
        buf.writeInt(inv.getItemStacks().size());
        buf.writeInt(inv.getFluidStacks().size());
        NbtCompound tag = new NbtCompound();
        NbtHelper.putList(tag, "items", inv.getItemStacks(), ConfigurableItemStack::toNbt);
        NbtHelper.putList(tag, "fluids", inv.getFluidStacks(), ConfigurableFluidStack::toNbt);
        buf.writeNbt(tag);
        // Write slot positions
        inv.itemPositions.write(buf);
        inv.fluidPositions.write(buf);
        buf.writeInt(syncedComponents.size());
        // Write components
        for (SyncedComponent.Server component : syncedComponents) {
            buf.writeIdentifier(component.getId());
            component.writeInitialData(buf);
        }
        // Write GUI params
        guiParams.write(buf);
    }

    /**
     * @param face The face that was targeted, taking the overlay into account.
     */
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return ActionResult.PASS;
    }

    protected abstract MachineModelClientData getModelData();

    public abstract void onPlaced(LivingEntity placer, ItemStack itemStack);

    @Override
    public boolean useWrench(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (orientation.useWrench(player, hand, MachineOverlay.findHitSide(hitResult))) {
            markDirty();
            if (!getWorld().isClient()) {
                sync();
            }
            return true;
        }
        return false;
    }

    @Override
    public final Object getRenderAttachmentData() {
        return getModelData();
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
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("remesh", syncCausesRemesh);
        syncCausesRemesh = false;
        for (IComponent component : icomponents) {
            component.writeClientNbt(tag);
        }
        return tag;
    }

    @Override
    public final void writeNbt(NbtCompound tag) {
        for (IComponent component : icomponents) {
            component.writeNbt(tag);
        }
        tag.put("s", NbtNull.INSTANCE); // mark server-side
    }

    @Override
    public final void readNbt(NbtCompound tag) {
        if (tag.contains("s")) {
            for (IComponent component : icomponents) {
                component.readNbt(tag);
            }
        } else {
            boolean forceChunkRemesh = tag.getBoolean("remesh") || syncCausesRemesh;
            syncCausesRemesh = false;
            for (IComponent component : icomponents) {
                component.readClientNbt(tag);
            }
            if (forceChunkRemesh) {
                RenderHelper.forceChunkRemesh(world, pos);
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        invalidateCache();
    }

    protected void invalidateCache() {
        this.cacheInvalidateCallbacks.forEach(Runnable::run);
        this.cacheInvalidateCallbacks.clear();
    }

    @Override
    public <A, C> boolean canCache(BlockApiLookup<A, C> lookup, A apiInstance, Runnable invalidateCallback) {
        if (lookup == ItemStorage.SIDED || lookup == FluidStorage.SIDED) {
            this.cacheInvalidateCallbacks.add(invalidateCallback);
            return true;
        }
        return false;
    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        ItemStorage.SIDED.registerForBlockEntities((be, direction) -> ((MachineBlockEntity) be).getInventory().itemStorage, bet);
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        FluidStorage.SIDED.registerForBlockEntities((be, direction) -> ((MachineBlockEntity) be).getInventory().fluidStorage, bet);
    }

    public List<ItemStack> dropExtra() {
        return new ArrayList<>();
    }
}
