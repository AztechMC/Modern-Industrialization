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
package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidApi;
import net.fabricmc.fabric.api.transfer.v1.item.ItemApi;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

/**
 * The base block entity for the machine system. Contains components, and an
 * inventory.
 */
@SuppressWarnings("rawtypes")
public abstract class MachineBlockEntity extends FastBlockEntity
        implements ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity, BlockEntityClientSerializable {
    final List<SyncedComponent.Server> syncedComponents = new ArrayList<>();
    private final List<IComponent> icomponents = new ArrayList<>();
    private final MachineGuiParameters guiParams;
    /**
     * Server-side: true if the next call to sync() will trigger a remesh.
     * Client-side: true if fromClientTag() is being called for the first time.
     */
    private boolean syncCausesRemesh = true;

    public MachineBlockEntity(BlockEntityType<?> type, MachineGuiParameters guiParams) {
        super(type);
        this.guiParams = guiParams;
    }

    protected final void registerClientComponent(SyncedComponent.Server component) {
        syncedComponents.add(component);
    }

    protected final void registerComponents(IComponent... components) {
        for (IComponent c : components) {
            icomponents.add(c);
        }
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
        buf.writeInt(inv.itemStacks.size());
        buf.writeInt(inv.fluidStacks.size());
        CompoundTag tag = new CompoundTag();
        NbtHelper.putList(tag, "items", inv.itemStacks, ConfigurableItemStack::writeToTag);
        NbtHelper.putList(tag, "fluids", inv.fluidStacks, ConfigurableFluidStack::writeToTag);
        buf.writeCompoundTag(tag);
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
    protected abstract ActionResult onUse(PlayerEntity player, Hand hand, Direction face);

    protected abstract MachineModelClientData getModelData();

    public abstract void onPlaced(LivingEntity placer, ItemStack itemStack);

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
        BlockEntityClientSerializable.super.sync();
    }

    @Override
    public final void fromClientTag(CompoundTag tag) {
        boolean forceChunkRemesh = tag.getBoolean("remesh") || syncCausesRemesh;
        syncCausesRemesh = false;
        for (IComponent component : icomponents) {
            component.readClientNbt(tag);
        }
        if (forceChunkRemesh) {
            RenderHelper.forceChunkRemesh((ClientWorld) world, pos);
        }

    }

    @Override
    public final CompoundTag toClientTag(CompoundTag tag) {
        tag.putBoolean("remesh", syncCausesRemesh);
        syncCausesRemesh = false;
        for (IComponent component : icomponents) {
            component.writeClientNbt(tag);
        }
        return tag;
    }

    @Override
    public final CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        for (IComponent component : icomponents) {
            component.writeNbt(tag);
        }
        return tag;
    }

    @Override
    public final void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        for (IComponent component : icomponents) {
            component.readNbt(tag);
        }
    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        ItemApi.SIDED.registerForBlockEntities((be, direction) -> ((MachineBlockEntity) be).getInventory().itemStorage, bet);
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        FluidApi.SIDED.registerForBlockEntities((be, direction) -> ((MachineBlockEntity) be).getInventory().fluidStorage, bet);
    }

    public List<ItemStack> dropExtra() {
        return new ArrayList<>();
    }
}
