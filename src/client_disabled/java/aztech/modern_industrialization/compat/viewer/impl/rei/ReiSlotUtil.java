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

import aztech.modern_industrialization.compat.viewer.impl.ViewerUtil;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariantAttributes;
import aztech.modern_industrialization.util.FluidHelper;
import com.google.common.primitives.Ints;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class ReiSlotUtil {
    private ReiSlotUtil() {
    }

    public static EntryStack<?> createFluidEntryStack(FluidVariant fluid, long amount, float probability, boolean input) {
        @Nullable
        Component probabilityText = ViewerUtil.getProbabilityTooltip(probability, input);
        return EntryStacks.of(FluidStackHooksForge.fromForge(fluid.toStack(Ints.saturatedCast(amount))))
                .setting(EntryStack.Settings.TOOLTIP_PROCESSOR, (stack, oldTooltip) -> {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(FluidVariantAttributes.getName(fluid));
                    tooltip.add(FluidHelper.getFluidAmount(amount));
                    if (probabilityText != null) {
                        tooltip.add(probabilityText);
                    }
                    return Tooltip.create(tooltip);
                });
    }

    public static EntryStack<?> createFluidNoAmount(FluidVariant fluid) {
        return EntryStacks.of(FluidStackHooksForge.fromForge(fluid.toStack(FluidType.BUCKET_VOLUME))).setting(EntryStack.Settings.TOOLTIP_PROCESSOR,
                (stack, oldTooltip) -> {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(FluidVariantAttributes.getName(fluid));
                    return Tooltip.create(tooltip);
                });
    }

    public static Function<EntryStack<?>, List<Component>> getProbabilitySetting(float probability, boolean input) {
        @Nullable
        Component tooltip = ViewerUtil.getProbabilityTooltip(probability, input);
        return es -> tooltip == null ? List.of() : List.of(tooltip);
    }
}
