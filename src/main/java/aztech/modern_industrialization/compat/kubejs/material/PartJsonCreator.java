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
package aztech.modern_industrialization.compat.kubejs.material;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.OrePart;
import aztech.modern_industrialization.materials.part.PartTemplate;
import aztech.modern_industrialization.materials.part.RawMetalPart;
import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import com.google.gson.JsonObject;
import net.minecraft.util.valueproviders.UniformInt;

public class PartJsonCreator {

    public PartTemplate regularPart(String name) {
        try {
            return (PartTemplate) MIParts.class.getField(name.toUpperCase()).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("No such default Part Template: " + name.toUpperCase());
        }
    }

    public PartTemplate customRegularPart(String englishName, String name) {
        return new PartTemplate(englishName, name);
    }

    public PartTemplate batteryPart(long energyCapacity) {
        return MIParts.BATTERY.of(energyCapacity);
    }

    public PartTemplate barrelPart(int stackCapacity) {
        return MIParts.BARREL.of(stackCapacity);
    }

    public PartTemplate barrelPart(String englishName, String path, int stackCapacity) {
        return MIParts.BARREL.of(englishName, path, stackCapacity);
    }

    public PartTemplate blockPart(String materialSet) {
        MaterialBlockSet blockSet = MaterialBlockSet.getByName(materialSet);
        if (blockSet == null) {
            throw new IllegalArgumentException("No such Material Block Set: " + materialSet);
        }
        return MIParts.BLOCK.of(blockSet);
    }

    public PartTemplate cablePart(String tier) {
        CableTier cableTier = CableTier.getTier(tier);
        // TODO: remove this if CableTier#getTier should be non-null
        if (cableTier == null) {
            throw new IllegalArgumentException("No such Cable Tier: " + tier);
        }
        return MIParts.CABLE.of(cableTier);
    }

    public PartTemplate machineCasing(String englishName, String path) {
        return MIParts.MACHINE_CASING.of(englishName, path);
    }

    public PartTemplate machineCasing(String englishName, String path, float resistance) {
        return MIParts.MACHINE_CASING.of(englishName, path, resistance);
    }

    public PartTemplate machineCasing(float resistance) {
        return MIParts.MACHINE_CASING.of(resistance);
    }

    public PartTemplate machineCasing() {
        return MIParts.MACHINE_CASING.of();
    }

    public PartTemplate pipeCasing(float resistance) {
        return MIParts.MACHINE_CASING_PIPE.of(resistance);
    }

    public PartTemplate pipeCasing() {
        return MIParts.MACHINE_CASING_PIPE.of();
    }

    public PartTemplate specialCasing(String englishName, String path) {
        return MIParts.MACHINE_CASING_SPECIAL.of(englishName, path);
    }

    public PartTemplate specialCasing(String englishName, String path, float resistance) {
        return MIParts.MACHINE_CASING_SPECIAL.of(englishName, path, resistance);
    }

    public PartTemplate orePart(JsonObject json, boolean deepslate) {
        OrePart act;
        if (deepslate) {
            act = MIParts.ORE_DEEPSLATE;
        } else {
            act = MIParts.ORE;
        }

        int minXp = json.has("min_xp") ? json.get("min_xp").getAsInt() : 0;
        int maxXp = json.has("max_xp") ? json.get("max_xp").getAsInt() : 0;
        boolean generate = !json.has("generate") || json.get("generate").getAsBoolean();
        MaterialOreSet oreSet = MaterialOreSet.getByName(json.get("ore_set").getAsString());

        if (oreSet == null) {
            throw new IllegalArgumentException("No such Material Ore Set: " + json.get("ore_set").getAsString());
        }

        if (generate) {
            int veinSize = json.get("vein_size").getAsInt();
            int veinPerChunk = json.get("veins_per_chunk").getAsInt();
            int maxY = json.get("max_y").getAsInt();
            return act.of(UniformInt.of(minXp, maxXp), veinSize, veinPerChunk, maxY, oreSet);
        } else {
            return act.of(UniformInt.of(minXp, maxXp), oreSet);
        }

    }

    public PartTemplate rawMetalPart(String materialSet, boolean block) {
        RawMetalPart act;
        if (block) {
            act = MIParts.RAW_METAL_BLOCK;
        } else {
            act = MIParts.RAW_METAL;
        }
        return act.of(MaterialRawSet.getByName(materialSet));
    }

    public PartTemplate tankPart(int bucketCapacity) {
        return MIParts.TANK.of(bucketCapacity);
    }

    public PartTemplate tankPart(String englishName, String path, int bucketCapacity) {
        return MIParts.TANK.of(englishName, path, bucketCapacity);
    }

    public PartTemplate controlRodPart(int maxTemperature, double heatConduction, double thermalAbsorbProba, double fastAbsorbProba,
            double thermalScatteringProba, double fastScatteringProba, NuclearConstant.ScatteringType scatteringType, double size) {
        return MIParts.CONTROL_ROD.of(maxTemperature, heatConduction, thermalAbsorbProba, fastAbsorbProba, thermalScatteringProba,
                fastScatteringProba, scatteringType, size);

    }
}
