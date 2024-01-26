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
package aztech.modern_industrialization.compat.viewer.impl.emi;

import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.compat.viewer.usage.ForgeHammerCategory;
import aztech.modern_industrialization.network.machines.ForgeHammerMoveRecipePacket;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

class ForgeHammerRecipeHandler implements EmiRecipeHandler<ForgeHammerScreenHandler> {
    @Override
    public List<Slot> getInputSources(ForgeHammerScreenHandler handler) {
        List<Slot> inputs = new ArrayList<>();
        // Player inventory
        for (int i = 0; i < 36; ++i) {
            inputs.add(handler.getSlot(i));
        }
        // Extra
        inputs.add(handler.input);
        inputs.add(handler.tool);
        return inputs;
    }

    @Override
    public List<Slot> getCraftingSlots(ForgeHammerScreenHandler handler) {
        return List.of(handler.input, handler.tool);
    }

    @Override
    @Nullable
    public Slot getOutputSlot(ForgeHammerScreenHandler handler) {
        return handler.output;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory().getId().equals(ForgeHammerCategory.ID);
    }

    @Override
    public boolean performFill(EmiRecipe recipe, AbstractContainerScreen<ForgeHammerScreenHandler> screen, EmiFillAction action, int amount) {
        new ForgeHammerMoveRecipePacket(
                screen.getMenu().containerId,
                recipe.getId(),
                switch (action) {
                case FILL -> 0;
                case CURSOR -> 1;
                case QUICK_MOVE -> 2;
                },
                amount).sendToServer();

        Minecraft.getInstance().setScreen(screen);

        return true;
    }
}
