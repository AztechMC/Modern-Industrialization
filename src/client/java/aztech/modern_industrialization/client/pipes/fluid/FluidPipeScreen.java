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

package aztech.modern_industrialization.client.pipes.fluid;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.DynamicTooltip;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import aztech.modern_industrialization.client.pipes.gui.PipeScreen;
import aztech.modern_industrialization.client.util.RenderHelper;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.network.pipes.SetNetworkFluidPacket;
import aztech.modern_industrialization.pipes.fluid.FluidPipeInterface;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreenHandler;
import aztech.modern_industrialization.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class FluidPipeScreen extends PipeScreen<FluidPipeScreenHandler> {
    private static final Identifier TEXTURE = MI.id("textures/gui/pipe/fluid.png");

    public FluidPipeScreen(FluidPipeScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, FluidPipeScreenHandler.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        addNetworkFluidButton();
        addConnectionTypeButton(148, 22, menu.iface);
        addPriorityWidgets(33, 42, menu.iface, 0, () -> {
            List<Component> lines = new ArrayList<>();

            MIText priorityText = MIText.PriorityTransfer;
            lines.add(priorityText.text(menu.iface.getPriority(0)));

            MIText priorityTextHelp = MIText.PriorityTransferHelp;
            lines.add(priorityTextHelp.text().setStyle(TextHelper.GRAY_TEXT));

            return lines;
        }, () -> true);
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return TEXTURE;
    }

    private void addNetworkFluidButton() {
        addRenderableWidget(
                new NetworkFluidButton(72 + this.leftPos, 20 + this.topPos, widget -> updateNetworkFluid(), () -> {
                    List<Component> lines = new ArrayList<>();
                    lines.add(FluidHelper.getFluidName(menu.iface.getNetworkFluid(), false));
                    if (!menu.iface.getNetworkFluid().isEmpty()) {
                        lines.add(MIText.NetworkFluidHelpClear.text().setStyle(TextHelper.GRAY_TEXT));
                    } else {
                        lines.add(MIText.NetworkFluidHelpSet.text().setStyle(TextHelper.GRAY_TEXT));
                    }
                    return lines;
                }, menu.iface));
    }

    private void updateNetworkFluid() {
        FluidPipeInterface iface = menu.iface;
        FluidResource targetFluid = null;
        if (iface.getNetworkFluid().isEmpty()) {
            // Want to set the fluid
            FluidResource fluid = FluidResource.of(FluidUtil.getFluidContained(menu.getCarried()).orElse(FluidStack.EMPTY));
            if (!fluid.isEmpty()) {
                targetFluid = fluid;
            }
        } else if (Minecraft.getInstance().hasShiftDown()) {
            targetFluid = FluidResource.EMPTY;
        }
        if (targetFluid != null) {
            setNetworkFluid(targetFluid);
        }
    }

    private void setNetworkFluid(FluidResource fluidKey) {
        menu.iface.setNetworkFluid(fluidKey);
        new SetNetworkFluidPacket(menu.containerId, fluidKey).sendToServer();
    }

    private class NetworkFluidButton extends Button implements ReiDraggable {
        private final Supplier<List<Component>> tooltipSupplier;
        private final FluidPipeInterface iface;

        public NetworkFluidButton(int x, int y, OnPress onPress, Supplier<List<Component>> tooltipSupplier, FluidPipeInterface iface) {
            super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.tooltipSupplier = tooltipSupplier;
            setTooltip(new DynamicTooltip(tooltipSupplier));
            this.iface = iface;
        }

        @Override
        public Component getMessage() {
            return tooltipSupplier.get().getFirst();
        }

        @Override
        public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            // Render fluid slot
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MachineScreen.SLOT_ATLAS, getX() - 1, getY() - 1, 18, 0, 18, 18, 256, 256);
            // Render the fluid itself
            if (!iface.getNetworkFluid().isEmpty()) {
                RenderHelper.drawFluidInGui(guiGraphics, iface.getNetworkFluid(), getX(), getY());
            }
            // Render the white hover effect
            if (isHoveredOrFocused()) {
                // TODO 26.1
//                RenderSystem.disableDepthTest();
//                RenderSystem.colorMask(true, true, true, false);
                guiGraphics.fillGradient(getX(), getY(), getX() + 16, getY() + 16, -2130706433, -2130706433);
//                RenderSystem.colorMask(true, true, true, true);
//                RenderSystem.enableDepthTest();
            }
        }

        @Override
        public boolean dragFluid(FluidResource fluidResource, Simulation simulation) {
            if (simulation.isActing()) {
                setNetworkFluid(fluidResource);
            }
            return true;
        }

        @Override
        public boolean dragItem(ItemResource itemResource, Simulation simulation) {
            return false;
        }
    }
}
