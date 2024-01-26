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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.util.ModelHelper;
import java.util.*;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

public class MachineCasingModel {
    public static MachineCasingModel get(MachineCasing casing) {
        return (MachineCasingModel) casing.model;
    }

    /**
     * <ol>
     * <li>Top texture</li>
     * <li>Side texture</li>
     * <li>Bottom texture</li>
     * </ol>
     */
    private static final String[] SIDES = new String[] { "top", "side", "bottom" };

    private final BakedQuad[] quads;
    private final TextureAtlasSprite sideSprite;

    public MachineCasingModel(String folder, Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[3];
        for (int i = 0; i < 3; ++i) {
            var spriteId = new Material(InventoryMenu.BLOCK_ATLAS,
                    new MIIdentifier("block/casings/" + folder + "/" + SIDES[i]));
            sprites[i] = spriteGetter.apply(spriteId);
        }
        this.sideSprite = sprites[1];
        BakedQuad[] quads = new BakedQuad[6];
        var vc = new QuadBakingVertexConsumer.Buffered();
        for (int i = 0; i < 6; ++i) {
            Direction direction = Direction.from3DDataValue(i);
            int spriteIdx = direction == Direction.UP ? 0 : direction == Direction.DOWN ? 2 : 1;
            ModelHelper.emitSprite(vc, direction, sprites[spriteIdx], 0.0f);
            quads[i] = vc.getQuad();
        }
        this.quads = quads;
    }

    public BakedQuad getQuad(Direction side) {
        return quads[side.get3DDataValue()];
    }

    public TextureAtlasSprite getSideSprite() {
        return sideSprite;
    }
}
