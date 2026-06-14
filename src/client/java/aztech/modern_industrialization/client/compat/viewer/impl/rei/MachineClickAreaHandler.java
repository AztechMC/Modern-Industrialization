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

package aztech.modern_industrialization.client.compat.viewer.impl.rei;

import aztech.modern_industrialization.client.compat.viewer.impl.MachineScreenPredicateTest;
import aztech.modern_industrialization.client.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.util.Rectangle;
import java.util.Collections;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.resources.ResourceLocation;

class MachineClickAreaHandler implements ClickArea<MachineScreen> {
    @Override
    public Result handle(ClickAreaContext<MachineScreen> context) {
        MachineMenuClient screenHandler = context.getScreen().getMenu();
        ResourceLocation blockId = screenHandler.guiParams.blockId;
        List<ReiMachineRecipes.ClickAreaCategory> categories = ReiMachineRecipes.machineToClickAreaCategory.getOrDefault(blockId,
                Collections.emptyList());
        Rectangle rectangle = ReiMachineRecipes.machineToClickArea.get(blockId);
        Point point = context.getMousePosition().clone();
        point.translate(-context.getScreen().x(), -context.getScreen().y());
        if (categories.size() > 0 && rectangle != null && contains(rectangle, point)) {
            ClickArea.Result result = ClickArea.Result.success();
            boolean foundSome = false;
            for (ReiMachineRecipes.ClickAreaCategory cac : categories) {
                if (!MachineScreenPredicateTest.test(cac.predicate, context.getScreen()))
                    continue;
                List<Display> displays = DisplayRegistry.getInstance().get(CategoryIdentifier.of(cac.category));
                if (displays.size() > 0) {
                    result.category(CategoryIdentifier.of(cac.category));
                    foundSome = true;
                }
            }
            return foundSome ? result : ClickArea.Result.fail();
        } else {
            return ClickArea.Result.fail();
        }
    }

    private static boolean contains(Rectangle rectangle, Point mousePosition) {
        return rectangle.x() <= mousePosition.x && mousePosition.x <= rectangle.x() + rectangle.w() && rectangle.y() <= mousePosition.y
                && mousePosition.y <= rectangle.y() + rectangle.h();
    }
}
