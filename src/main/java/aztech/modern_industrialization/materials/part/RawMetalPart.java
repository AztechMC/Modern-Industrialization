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
package aztech.modern_industrialization.materials.part;

import static aztech.modern_industrialization.materials.part.MIParts.RAW_METAL;
import static aztech.modern_industrialization.materials.part.MIParts.RAW_METAL_BLOCK;

import aztech.modern_industrialization.materials.set.MaterialRawSet;
import aztech.modern_industrialization.textures.TextureHelper;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.List;

public class RawMetalPart extends UnbuildablePart<MaterialRawSet> {

    public final boolean isBlock;

    public RawMetalPart(boolean isBlock) {
        super(isBlock ? "raw_metal_block" : "raw_metal");
        this.isBlock = isBlock;
    }

    @Override
    public BuildablePart of(MaterialRawSet set) {
        RegularPart part = new RegularPart(isBlock ? "Block of Raw %s" : "Raw %s", key);

        if (isBlock) {
            part = part.asBlock(5, 6, 1);
        }

        part = part.withTextureRegister((mtm, partContext, part1, itemPath) -> {
            String template = String.format("modern_industrialization:textures/materialsets/raw/%s.png", set.name + (isBlock ? "_block" : ""));
            try {
                NativeImage image = mtm.getAssetAsTexture(template);
                TextureHelper.colorize(image, partContext.getColoramp());
                String texturePath;
                if (isBlock) {
                    texturePath = String.format("modern_industrialization:textures/block/%s.png", itemPath);
                } else {
                    texturePath = String.format("modern_industrialization:textures/item/%s.png", itemPath);
                }
                mtm.addTexture(texturePath, image);
                image.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (isBlock) {
            return part.withCustomFormattablePath("raw_%s_block", "raw_%s_blocks");
        } else {
            return part.withCustomFormattablePath("raw_%s", "raw_%s_ores");
        }

    }

    public List<BuildablePart> ofAll(MaterialRawSet set) {
        return List.of(RAW_METAL.of(set), RAW_METAL_BLOCK.of(set));
    }
}
