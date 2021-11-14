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

import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.pipes.impl.PipeMeshCache;
import aztech.modern_industrialization.pipes.impl.PipeModel;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.DashRegistry;
import net.oskarstrom.dashloader.api.annotation.DashObject;
import net.oskarstrom.dashloader.data.serialization.PairMap;
import net.oskarstrom.dashloader.model.DashModel;
import net.oskarstrom.dashloader.model.components.DashModelTransformation;

@DashObject(PipeModel.class)
public class DashPipeModel implements DashModel {

    @Serialize(order = 0)
    public final int particleSprite;
    @Serialize(order = 1)
    public final PairMap<Integer, DashPipeMeshCache> renderers;
    @Serialize(order = 2)
    public final DashModelTransformation modelTransformation;

    public DashPipeModel(@Deserialize("particleSprite") int particleSprite, @Deserialize("renderers") PairMap<Integer, DashPipeMeshCache> renderers,
            @Deserialize("modelTransformation") DashModelTransformation modelTransformation) {
        this.particleSprite = particleSprite;
        this.renderers = renderers;
        this.modelTransformation = modelTransformation;
    }

    public DashPipeModel(PipeModel pipeModel, DashRegistry registry) {
        particleSprite = registry.createSpritePointer(pipeModel.getParticleSprite());
        renderers = new PairMap<>();
        pipeModel.getRenderers().forEach((factory, pipeRenderer) -> {
            Identifier factoryIdentifier = null;
            for (Map.Entry<Identifier, PipeNetworkType> entry : PipeNetworkType.getTypes().entrySet()) {
                if (factory == entry.getValue().getRenderer()) {
                    factoryIdentifier = entry.getKey();
                    break;
                }
            }
            renderers.put(registry.createIdentifierPointer(factoryIdentifier), new DashPipeMeshCache((PipeMeshCache) pipeRenderer));
        });
        modelTransformation = new DashModelTransformation(pipeModel.getTransformation());
    }

    @Override
    public PipeModel toUndash(DashRegistry registry) {
        Map<PipeRenderer.Factory, PipeRenderer> renderersOut = new HashMap<>();
        final Map<Identifier, PipeNetworkType> types = PipeNetworkType.getTypes();
        renderers.forEach((identifierPointer, dashPipeMeshCache) -> {
            renderersOut.put(types.get(registry.getIdentifier(identifierPointer)).getRenderer(), dashPipeMeshCache.toUndash());
        });
        return new PipeModel(registry.getSprite(particleSprite), renderersOut, modelTransformation.toUndash(),
                RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.CUTOUT).find());
    }

    @Override
    public int getStage() {
        return 0;
    }
}
