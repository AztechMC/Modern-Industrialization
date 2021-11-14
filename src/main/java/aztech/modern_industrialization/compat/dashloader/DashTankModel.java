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

import aztech.modern_industrialization.blocks.storage.tank.TankModel;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.oskarstrom.dashloader.DashRegistry;
import net.oskarstrom.dashloader.api.annotation.DashObject;
import net.oskarstrom.dashloader.model.DashModel;
import net.oskarstrom.dashloader.model.components.DashMesh;
import net.oskarstrom.dashloader.model.components.DashModelTransformation;

@DashObject(TankModel.class)
public class DashTankModel implements DashModel {

    @Serialize(order = 0)
    public final DashModelTransformation transformation;
    @Serialize(order = 1)
    public final int tankSprite;
    @Serialize(order = 2)
    public final DashMesh tankMesh;

    public DashTankModel(@Deserialize("transformation") DashModelTransformation transformation, @Deserialize("tankSprite") int tankSprite,
            @Deserialize("tankMesh") DashMesh tankMesh) {
        this.transformation = transformation;
        this.tankSprite = tankSprite;
        this.tankMesh = tankMesh;
    }

    public DashTankModel(TankModel model, DashRegistry registry) {
        transformation = new DashModelTransformation(model.getTransformation());
        tankSprite = registry.createSpritePointer(model.getParticleSprite());
        tankMesh = new DashMesh(model.getTankMesh());
    }

    @Override
    public TankModel toUndash(DashRegistry registry) {
        return new TankModel(transformation.toUndash(), null, registry.getSprite(tankSprite),
                RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find(), tankMesh.toUndash());
    }

    @Override
    public int getStage() {
        return 0;
    }
}
