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
package aztech.modern_industrialization.compat.rei;

import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreen;
import dev.architectury.fluid.FluidStack;
import java.util.Optional;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class MIREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        for (Item item : Registry.ITEM) {
            if (item instanceof DieselToolItem) {
                if (FabricToolTags.AXES.contains(item)) {
                    registry.addWorkstations(BuiltinPlugin.STRIPPING, EntryStacks.of(item));
                }
                if (FabricToolTags.SHOVELS.contains(item)) {
                    registry.addWorkstations(BuiltinPlugin.PATHING, EntryStacks.of(item));
                }
            }
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registerFluidPipeDragging(registry);
    }

    private void registerFluidPipeDragging(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new DraggableStackVisitor<FluidPipeScreen>() {
            @Override
            public Optional<Acceptor> visitDraggedStack(DraggingContext<FluidPipeScreen> context, DraggableStack stack) {
                if (context.getScreen().canSetNetworkFluid() && stack.getStack().getValue() instanceof FluidStack) {
                    return Optional.of(s -> {
                        FluidStack fs = s.getStack().<FluidStack>cast().getValue();
                        context.getScreen().setNetworkFluid(FluidKey.of(fs.getFluid(), fs.getTag()));
                    });
                }
                return Optional.empty();
            }

            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return screen instanceof FluidPipeScreen;
            }
        });
    }
}
