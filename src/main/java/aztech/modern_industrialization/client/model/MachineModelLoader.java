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
package aztech.modern_industrialization.client.model;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.MachineBlock;
import java.io.IOException;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import org.jetbrains.annotations.Nullable;

/**
 * Allows loading MI's custom machine JSONs.
 */
public class MachineModelLoader implements ModelVariantProvider {
    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(MachineModelLoader::new);
    }

    private final ResourceManager resourceManager;

    public MachineModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    @Nullable
    public UnbakedModel loadModelVariant(ModelIdentifier modelIdentifier, ModelProviderContext context) throws ModelProviderException {
        if (!modelIdentifier.getNamespace().equals(ModernIndustrialization.MOD_ID)) {
            return null;
        }

        String path = modelIdentifier.getPath();
        var casing = MachineBlock.REGISTERED_MACHINES.get(path);
        if (casing != null) {
            // This is a machine, load its json model.
            try (var resource = this.resourceManager.getResource(new MIIdentifier("models/machine/" + path + ".json"))) {
                return MachineUnbakedModel.deserialize(casing, resource);
            } catch (IOException exception) {
                throw new ModelProviderException("Failed to find machine model json for machine " + modelIdentifier);
            } catch (RuntimeException runtimeException) {
                throw new ModelProviderException("Failed to load machine model json for machine " + modelIdentifier, runtimeException);
            }
        }

        return null;
    }
}
