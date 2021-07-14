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

import aztech.modern_industrialization.machines.models.MachineCasingModel;
import aztech.modern_industrialization.machines.models.MachineCasings;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import java.util.HashMap;
import java.util.Map;
import net.oskarstrom.dashloader.DashRegistry;
import net.oskarstrom.dashloader.api.DataClass;
import net.oskarstrom.dashloader.model.components.DashMesh;

public class MachineCastingModelsData implements DataClass {
    @Serialize(order = 0)
    public final Map<String, DashMachineCasingModel> casingModels;

    public MachineCastingModelsData(@Deserialize("casingModels") Map<String, DashMachineCasingModel> casingModels) {
        this.casingModels = casingModels;
    }

    public MachineCastingModelsData() {
        casingModels = new HashMap<>();
    }

    @Override
    public void reload(DashRegistry registry) {
    }

    @Override
    public void apply(DashRegistry registry) {
        MachineCasings.registeredCasings.forEach((s, machineCasing) -> {
            casingModels.get(s).apply(registry, machineCasing.mcm);
        });
    }

    @Override
    public void serialize(DashRegistry registry) {
        MachineCasings.registeredCasings.forEach((s, machineCasing) -> {
            casingModels.put(s, new DashMachineCasingModel(machineCasing.mcm, registry));
        });
    }

    public static class DashMachineCasingModel {
        @Serialize(order = 0)
        public final int id;
        @Serialize(order = 1)
        public final DashMesh mesh;
        @Serialize(order = 2)
        public final int sideSprite;

        public DashMachineCasingModel(@Deserialize("id") int id, @Deserialize("mesh") DashMesh mesh, @Deserialize("sideSprite") int sideSprite) {
            this.id = id;
            this.mesh = mesh;
            this.sideSprite = sideSprite;
        }

        public DashMachineCasingModel(MachineCasingModel machineCasingModel, DashRegistry registry) {
            id = registry.createIdentifierPointer(machineCasingModel.getId());
            mesh = new DashMesh(machineCasingModel.getMesh());
            sideSprite = registry.createSpritePointer(machineCasingModel.getSideSprite());
        }

        public void apply(DashRegistry registry, MachineCasingModel machineCasingModel) {
            machineCasingModel.setId(registry.getIdentifier(id));
            machineCasingModel.setMesh(mesh.toUndash());
            machineCasingModel.setSideSprite(registry.getSprite(sideSprite));
        }

    }
}
