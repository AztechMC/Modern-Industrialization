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
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidApi;
import net.fabricmc.fabric.api.transfer.v1.item.ItemApi;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

/**
 * The base block entity for the machine system. Contains components, and an
 * inventory.
 */
@SuppressWarnings("rawtypes")
public abstract class MachineBlockEntity extends FastBlockEntity implements ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity {
    final List<SyncedComponent.Server> syncedComponents = new ArrayList<>();
    private final MachineGuiParameters guiParams;
    @Environment(EnvType.CLIENT)
    private final MachineModelClientData clientData = null;

    public MachineBlockEntity(BlockEntityType<?> type, MachineGuiParameters guiParams) {
        super(type);
        this.guiParams = guiParams;
    }

    protected final void registerClientComponent(SyncedComponent.Server component) {
        syncedComponents.add(component);
    }

    /**
     * @return The inventory that will be synced with the client.
     */
    public abstract MIInventory getInventory();

    @Override
    public final Text getDisplayName() {
        return guiParams.title;
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

    protected abstract ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit);

    protected abstract MachineModelClientData getModelData();

    @Override
    public final Object getRenderAttachmentData() {
        return getModelData();
    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        ItemApi.SIDED.registerForBlockEntities((be, direction) -> ((MachineBlockEntity) be).getInventory().itemStorage, bet);
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        FluidApi.SIDED.registerForBlockEntities((be, direction) -> ((MachineBlockEntity) be).getInventory().fluidStorage, bet);
    }
}
