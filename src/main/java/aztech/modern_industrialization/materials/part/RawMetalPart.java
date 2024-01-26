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

import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import java.util.List;

public record RawMetalPart(boolean isBlock) implements PartKeyProvider {

    public PartTemplate of(MaterialRawSet set) {
        PartTemplate part = new PartTemplate(isBlock ? "Block of Raw %s" : "Raw %s", key());

        if (isBlock) {
            return part
                    .asBlock(SortOrder.RAW_ORE_BLOCKS, new TextureGenParams.RawMetal(true, set), 5, 6, 1)
                    .withCustomPath("raw_%s_block", "storage_blocks/raw_%s");
        } else {
            return part
                    .withTexture(new TextureGenParams.RawMetal(false, set))
                    .withCustomPath("raw_%s", "raw_materials/%s");
        }
    }

    public List<PartTemplate> ofAll(MaterialRawSet set) {
        return List.of(RAW_METAL.of(set), RAW_METAL_BLOCK.of(set));
    }

    @Override
    public PartKey key() {
        return new PartKey("raw_metal" + (isBlock ? "_block" : ""));
    }
}
