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
import aztech.modern_industrialization.materials.part.*;
import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

public interface PartJsonCreator {

    default PartTemplate regularPart(String name) {
        try {
            return (PartTemplate) MIParts.class.getField(name.toUpperCase()).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("No such default Part Template: " + name.toUpperCase());
        }
    }

    default PartTemplate barrelPart(JsonObject json) {
        if (json.has("english_name")) {
            return MIParts.BARREL.of(
                    json.get("english_name").getAsString(),
                    json.get("path").getAsString(),
                    json.get("stack_capacity").getAsInt());
        } else {
            return MIParts.BARREL.of(
                    json.get("stack_capacity").getAsInt());
        }
    }

    default PartTemplate blockPart(String materialSet) {
        MaterialBlockSet blockSet = MaterialBlockSet.getByName(materialSet);
        if (blockSet == null) {
            throw new IllegalArgumentException("No such Material Block Set: " + materialSet);
        }
        return MIParts.BLOCK.of(blockSet);
    }

    default PartTemplate cablePart(String tier) {
        CableTier cableTier = CableTier.getByName(tier);
        if (cableTier == null) {
            throw new IllegalArgumentException("No such Cable Tier: " + tier);
        }
        return MIParts.CABLE.of(cableTier);
    }

    default PartTemplate casingPart(@NotNull JsonObject json) {
        String type = json.get("type").getAsString();
        CasingPart act = switch (type) {
        case "default" -> MIParts.MACHINE_CASING;
        case "pipe" -> MIParts.MACHINE_CASING_PIPE;
        case "special" -> MIParts.MACHINE_CASING_SPECIAL;
        default -> throw new IllegalArgumentException("No such Casing Type: " + type);
        };

        if (json.has("english_name")) {
            if (json.has("resistance")) {
                return act.of(json.get("english_name").getAsString(), json.get("path").getAsString(), json.get("resistance").getAsFloat());
            }
            return act.of(json.get("english_name").getAsString(), json.get("path").getAsString());
        } else if (json.has("resistance")) {
            return act.of(json.get("resistance").getAsFloat());
        } else {
            return act.of();
        }

    }

    default PartTemplate orePart(JsonObject json, boolean deepslate) {
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

    default List<PartTemplate> oreParts(JsonObject json) {
        return List.of(orePart(json, false), orePart(json, true));
    }

    default PartTemplate rawMetalPart(String materialSet, boolean block) {
        RawMetalPart act;
        if (block) {
            act = MIParts.RAW_METAL_BLOCK;
        } else {
            act = MIParts.RAW_METAL;
        }
        return act.of(MaterialRawSet.getByName(materialSet));
    }

    default List<PartTemplate> rawMetalParts(String materialSet) {
        return List.of(rawMetalPart(materialSet, false), rawMetalPart(materialSet, true));
    }

    default PartTemplate tankPart(JsonObject json) {
        if (json.has("english_name")) {
            return MIParts.TANK.of(
                    json.get("english_name").getAsString(),
                    json.get("path").getAsString(),
                    json.get("bucket_capacity").getAsInt());
        } else {
            return MIParts.TANK.of(
                    json.get("bucket_capacity").getAsInt());
        }
    }
}
