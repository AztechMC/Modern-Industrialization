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
package aztech.modern_industrialization.pipes.impl;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class RotatedModelHelper {
    /**
     * The model rotation to rotate a model facing NORTH to the correct facing direction.
     * Rotations are indexed by {@link Direction} id.
     */
    public static final ModelState[] PIPE_BAKE_SETTINGS = new ModelState[] {
            preRotated(BlockModelRotation.X90_Y0, 270),
            BlockModelRotation.X270_Y0,
            BlockModelRotation.X0_Y0,
            preRotated(BlockModelRotation.X0_Y180, 90),
            preRotated(BlockModelRotation.X0_Y270, 90),
            BlockModelRotation.X0_Y90,
    };

    public static ModelState preRotated(BlockModelRotation rotation, float preAngle) {
        Transformation preRotation = new Transformation(null, Axis.ZP.rotationDegrees(preAngle), null, null);
        Transformation combined = rotation.getRotation().compose(preRotation);
        return new ModelState() {
            @Override
            public Transformation getRotation() {
                return combined;
            }
        };
    }

    public static BakedModel[] loadRotatedModels(ResourceLocation modelId, ModelBaker modelBaker,
            Function<Material, TextureAtlasSprite> spriteGetter) {
        // Load side models
        BakedModel[] models = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            models[i] = modelBaker.bake(modelId, PIPE_BAKE_SETTINGS[i], spriteGetter);
        }

        return models;
    }
}
