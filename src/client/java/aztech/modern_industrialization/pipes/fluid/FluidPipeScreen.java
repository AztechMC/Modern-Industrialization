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
package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.DynamicTooltip;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.network.pipes.SetNetworkFluidPacket;
import aztech.modern_industrialization.pipes.gui.PipeScreen;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.*;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FluidPipeScreen extends PipeScreen<FluidPipeScreenHandler> {
    private static final ResourceLocation TEXTURE = new MIIdentifier("textures/gui/pipe/fluid.png");

    public FluidPipeScreen(FluidPipeScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, FluidPipeScreenHandler.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        addNetworkFluidButton();
        addConnectionTypeButton(148, 22, menu.iface);
        addPriorityWidgets(33, 42, menu.iface, "transfer", 0);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }

    private void addNetworkFluidButton() {
        addRenderableWidget(
                new NetworkFluidButton(72 + this.leftPos, 20 + this.topPos, widget -> updateNetworkFluid(), () -> {
                    List<Component> lines = new ArrayList<>();
                    lines.add(FluidHelper.getFluidName(menu.iface.getNetworkFluid(), false));
                    if (!menu.iface.getNetworkFluid().isBlank()) {
                        lines.add(MIText.NetworkFluidHelpClear.text().setStyle(TextHelper.GRAY_TEXT));
                    } else {
                        lines.add(MIText.NetworkFluidHelpSet.text().setStyle(TextHelper.GRAY_TEXT));
                    }
                    return lines;
                }, menu.iface));
    }

    private void updateNetworkFluid() {
        FluidPipeInterface iface = menu.iface;
        FluidVariant targetFluid = null;
        if (iface.getNetworkFluid().isBlank()) {
            // Want to set the fluid
            FluidVariant fluid = FluidVariant.of(FluidUtil.getFluidContained(menu.getCarried()).orElse(FluidStack.EMPTY));
            if (fluid != null && !fluid.isBlank()) {
                targetFluid = fluid;
            }
        } else if (hasShiftDown()) {
            targetFluid = FluidVariant.blank();
        }
        if (targetFluid != null) {
            setNetworkFluid(targetFluid);
        }
    }

    private void setNetworkFluid(FluidVariant fluidKey) {
        menu.iface.setNetworkFluid(fluidKey);
        new SetNetworkFluidPacket(menu.containerId, fluidKey).sendToServer();
    }

    private class NetworkFluidButton extends Button implements ReiDraggable {
        private final FluidPipeInterface iface;

        public NetworkFluidButton(int x, int y, OnPress onPress, Supplier<List<Component>> tooltipSupplier, FluidPipeInterface iface) {
            super(x, y, 16, 16, null, onPress, Button.DEFAULT_NARRATION);
            setTooltip(new DynamicTooltip(tooltipSupplier));
            this.iface = iface;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            // Render fluid slot
            guiGraphics.blit(MachineScreen.SLOT_ATLAS, getX() - 1, getY() - 1, 18, 0, 18, 18);
            // Render the fluid itself
            if (!iface.getNetworkFluid().isBlank()) {
                RenderHelper.drawFluidInGui(guiGraphics, iface.getNetworkFluid(), getX(), getY());
            }
            // Render the white hover effect
            if (isHoveredOrFocused()) {
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                guiGraphics.fillGradient(getX(), getY(), getX() + 16, getY() + 16, -2130706433, -2130706433);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
            }
        }

        @Override
        public boolean dragFluid(FluidVariant fluidKey, Simulation simulation) {
            if (simulation.isActing()) {
                setNetworkFluid(fluidKey);
            }
            return true;
        }

        @Override
        public boolean dragItem(ItemVariant itemKey, Simulation simulation) {
            return false;
        }
    }
}
