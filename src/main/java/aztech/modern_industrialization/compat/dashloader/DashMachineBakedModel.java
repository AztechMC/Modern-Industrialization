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
package aztech.modern_industrialization.compat.dashloader;

import aztech.modern_industrialization.machines.models.MachineBakedModel;
import aztech.modern_industrialization.machines.models.MachineCasingModel;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.DashRegistry;
import net.oskarstrom.dashloader.api.annotation.DashObject;
import net.oskarstrom.dashloader.model.DashModel;
import net.oskarstrom.dashloader.model.components.DashModelTransformation;

@DashObject(MachineBakedModel.class)
public class DashMachineBakedModel implements DashModel {

    @Serialize(order = 0)
    public final DashModelTransformation blockTransformation;
    @Serialize(order = 1)
    @SerializeNullable(path = { 0 })
    public final Integer[] sprites;
    @Serialize(order = 2)
    public final int defaultCasingModel;

    public DashMachineBakedModel(@Deserialize("blockTransformation") DashModelTransformation blockTransformation,
            @Deserialize("sprites") Integer[] sprites, @Deserialize("defaultCasingModel") int defaultCasingModel) {
        this.blockTransformation = blockTransformation;

        this.sprites = sprites;
        this.defaultCasingModel = defaultCasingModel;
    }

    public DashMachineBakedModel(MachineBakedModel machineBakedModel, DashRegistry registry) {
        blockTransformation = new DashModelTransformation(machineBakedModel.getTransformation());
        final Sprite[] sprites = machineBakedModel.getSprites();
        this.sprites = new Integer[sprites.length];
        for (int i = 0; i < sprites.length; i++) {
            this.sprites[i] = sprites[i] == null ? null : registry.createSpritePointer(sprites[i]);
        }
        defaultCasingModel = registry.createModelPointer(machineBakedModel.getDefaultCasing());
    }

    @Override
    public MachineBakedModel toUndash(DashRegistry registry) {
        Sprite[] spritesOut = new Sprite[sprites.length];
        for (int i = 0; i < sprites.length; i++) {
            spritesOut[i] = sprites[i] == null ? null : registry.getSprite(sprites[i]);
        }
        MachineBakedModel machineBakedModel = new MachineBakedModel(blockTransformation.toUndash(),
                RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find(), spritesOut,
                (MachineCasingModel) registry.getModel(defaultCasingModel));
        return machineBakedModel;
    }

    @Override
    public int getStage() {
        return 1;
    }
}
