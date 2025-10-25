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

package aztech.modern_industrialization.machines.gui;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.ComponentStorage;
import aztech.modern_industrialization.machines.GuiComponentsClient;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class MachineMenuClient extends MachineMenuCommon {
    @SuppressWarnings("ConstantConditions")
    public static MachineMenuClient create(int syncId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        // Inventory
        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
        CompoundTag tag = buf.readNbt();
        NbtHelper.getList(tag, "items", itemStacks, t -> new ConfigurableItemStack(t, buf.registryAccess()));
        NbtHelper.getList(tag, "fluids", fluidStacks, t -> new ConfigurableFluidStack(t, buf.registryAccess()));
        // Slot positions
        SlotPositions itemPositions = SlotPositions.read(buf);
        SlotPositions fluidPositions = SlotPositions.read(buf);
        MIInventory inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);
        // Components
        ComponentStorage<GuiComponentClient> components = new ComponentStorage<>();
        int componentCount = buf.readInt();
        for (int i = 0; i < componentCount; ++i) {
            ResourceLocation id = buf.readResourceLocation();
            components.register(GuiComponentsClient.get(id).createFromInitialData(buf));
        }
        // GUI params
        MachineGuiParameters guiParams = MachineGuiParameters.read(buf);

        return new MachineMenuClient(syncId, playerInventory, inventory, components, guiParams);
    }

    public final ComponentStorage<GuiComponentClient> components;

    private MachineMenuClient(int syncId, Inventory playerInventory, MIInventory inventory, ComponentStorage<GuiComponentClient> components,
            MachineGuiParameters guiParams) {
        super(syncId, playerInventory, inventory, guiParams, components);
        this.components = components;
    }

    @Nullable
    public <T extends GuiComponentClient> T getComponent(Class<T> klass) {
        return components.get(klass);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void readClientComponentSyncData(int componentIndex, RegistryFriendlyByteBuf buf) {
        components.get(componentIndex).readCurrentData(buf);
    }
}
