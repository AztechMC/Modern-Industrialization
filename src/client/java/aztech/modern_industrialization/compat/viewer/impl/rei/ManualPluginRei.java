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
package aztech.modern_industrialization.compat.viewer.impl.rei;

import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

@REIPluginClient
public class ManualPluginRei implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof DieselToolItem) {
                if (item.builtInRegistryHolder().is(ItemTags.AXES)) {
                    registry.addWorkstations(BuiltinPlugin.STRIPPING, EntryStacks.of(item));
                }
                if (item.builtInRegistryHolder().is(ItemTags.SHOVELS)) {
                    registry.addWorkstations(BuiltinPlugin.PATHING, EntryStacks.of(item));
                }
            }
        }
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new MachineSlotLockingHandler());
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new MIDraggableStackVisitor());

        registry.registerClickArea(MachineScreen.class, new MachineClickAreaHandler());
        registry.registerFocusedStack(new MachineFocusedStackProvider());
        registry.exclusionZones().register(MachineScreen.class, screen -> {
            return screen.getExtraBoxes().stream().map(r -> new me.shedaniel.math.Rectangle(r.x(), r.y(), r.w(), r.h())).toList();
        });
    }
}
