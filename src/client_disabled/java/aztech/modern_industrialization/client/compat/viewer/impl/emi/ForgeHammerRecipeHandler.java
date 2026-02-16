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

package aztech.modern_industrialization.client.compat.viewer.impl.emi;

import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.client.compat.viewer.usage.ForgeHammerCategory;
import aztech.modern_industrialization.network.machines.ForgeHammerMoveRecipePacket;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

class ForgeHammerRecipeHandler implements StandardRecipeHandler<ForgeHammerScreenHandler> {
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
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ForgeHammerScreenHandler> context) {
        new ForgeHammerMoveRecipePacket(
                context.getScreenHandler().containerId,
                recipe.getId(),
                switch (context.getDestination()) {
                    case NONE -> 0;
                    case CURSOR -> 1;
                    case INVENTORY -> 2;
                },
                context.getAmount()).sendToServer();

        Minecraft.getInstance().setScreen(context.getScreen());
        return true;
    }
}
