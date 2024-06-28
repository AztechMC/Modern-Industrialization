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
package aztech.modern_industrialization.compat.jade.client;

import aztech.modern_industrialization.compat.jade.server.MachineComponentProvider;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.ui.IDisplayHelper;

@WailaPlugin
public class MIJadeClientPlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new OverclockComponentProvider(), MachineBlock.class);

        registration.usePickedResult(MIPipes.BLOCK_PIPE.get());
        registration.registerBlockComponent(new PipeComponentProvider(), PipeBlock.class);

        registration.registerEnergyStorageClient(new MachineComponentProvider.Energy());
        registration.registerFluidStorageClient(new MachineComponentProvider.Fluids());
        registration.registerItemStorageClient(new MachineComponentProvider.Items());
        registration.registerProgressClient(new MachineComponentProvider.Progress());
    }

    static float ratio(double current, double max) {
        return (float) (current / max);
    }

    static String getUnicodeMillibuckets(long amount, boolean simplify) {
        return IDisplayHelper.get().humanReadableNumber((double) amount, "B", true);
    }

    static Component textAndRatio(Component text, String current, String max) {
        return Component.literal("")
                .append(text.copy().withStyle(ChatFormatting.WHITE))
                .append(" ")
                .append(Component.literal(current).withStyle(ChatFormatting.WHITE))
                .append(" / ")
                .append(Component.literal(max))
                .withStyle(ChatFormatting.GRAY);
    }
}
