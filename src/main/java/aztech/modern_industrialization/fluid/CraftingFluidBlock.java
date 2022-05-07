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
package aztech.modern_industrialization.fluid;

import aztech.modern_industrialization.util.FluidHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

/**
 * Allows the transfer API to get the name of the fluid by calling fluid.getBlockState().getBlock().getTranslationKey().
 */
public class CraftingFluidBlock extends Block {
    private final String translationKey;
    private final int color;

    public CraftingFluidBlock(String name, int color) {
        super(FabricBlockSettings.of(Material.WATER));
        this.translationKey = "block.modern_industrialization." + name;
        this.color = FluidHelper.getColorMinLuminance(color);
    }

    public int getColor() {
        return color;
    }

    @Override
    public String getDescriptionId() {
        return translationKey;
    }

    @Override
    public MutableComponent getName() {
        return new TranslatableComponent(this.translationKey).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }
}
