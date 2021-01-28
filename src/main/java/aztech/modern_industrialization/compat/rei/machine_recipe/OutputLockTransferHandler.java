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
package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachinePackets;
import aztech.modern_industrialization.machines.impl.MachineScreen;
import aztech.modern_industrialization.machines.impl.MachineScreenHandler;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.AutoTransferHandler;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

public class OutputLockTransferHandler implements AutoTransferHandler {
    @Override
    public @NotNull Result handle(@NotNull Context context) {
        if (!(context.getRecipe() instanceof MachineRecipeDisplay))
            return Result.createNotApplicable();
        if (!(context.getContainerScreen() instanceof MachineScreen))
            return Result.createNotApplicable();
        MachineRecipeDisplay display = (MachineRecipeDisplay) context.getRecipe();
        MachineScreen screen = (MachineScreen) context.getContainerScreen();
        MachineScreenHandler handler = screen.getScreenHandler();
        MachineFactory factory = handler.getMachineFactory();
        if (factory.recipeType != display.recipe.getType())
            return Result.createNotApplicable();
        // Try to lock output slots
        if (context.isActuallyCrafting()) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(handler.syncId);
            buf.writeIdentifier(display.getRecipeCategory());
            buf.writeIdentifier(display.recipe.getId());
            ClientSidePacketRegistry.INSTANCE.sendToServer(MachinePackets.C2S.LOCK_RECIPE, buf);
        }
        return Result.createSuccessful().blocksFurtherHandling(true);
    }
}
