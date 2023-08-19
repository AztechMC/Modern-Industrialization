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
package aztech.modern_industrialization.compat.ae2;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.inventories.PartApiLookup;
import appeng.api.parts.PartModels;
import appeng.api.util.AEColor;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import aztech.modern_industrialization.*;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.compat.ae2.pipe.MENetwork;
import aztech.modern_industrialization.compat.ae2.pipe.MENetworkData;
import aztech.modern_industrialization.compat.ae2.pipe.MENetworkNode;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.PipeColor;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class MIAEAddon {
    static {
        if (!MIConfig.loadAe2Compat()) {
            throw new RuntimeException("AE2 compat is disabled. How did this get loaded?");
        }
    }

    public static final Item ENERGY_P2P_TUNNEL = new PartItem<>(
            new FabricItemSettings(), EnergyP2PTunnelPart.class, EnergyP2PTunnelPart::new);
    public static final List<PipeNetworkType> PIPES = new ArrayList<>();

    public static void init() {
        PartModels.registerModels(PartModelsHelper.createModels(EnergyP2PTunnelPart.class));
        var item = Registry.register(BuiltInRegistries.ITEM, new MIIdentifier("energy_p2p_tunnel"), ENERGY_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(item);
        PartApiLookup.register(EnergyApi.SIDED, (part, context) -> part.getExposedApi(), EnergyP2PTunnelPart.class);
    }

    public static void onInitializePipes() {
        for (var color : PipeColor.values()) {
            registerMEPipeType(color);
        }

        TagsToGenerate.markTagOptional(MITags.ME_WIRES);
    }

    private static void registerMEPipeType(PipeColor color) {
        var aeColor = switch (color) {
        case REGULAR -> AEColor.TRANSPARENT;
        default -> {
            for (var candidate : AEColor.values()) {
                if (candidate.registryPrefix.equals(color.name)) {
                    yield candidate;
                }
            }
            throw new UnsupportedOperationException("No AE color for " + color.name);
        }
        };

        var pipeId = color.prefix + "me_wire";
        var type = PipeNetworkType.register(new MIIdentifier(pipeId), (id, data) -> new MENetwork(id, data, aeColor),
                MENetworkNode::new, color.color, false);
        PIPES.add(type);
        var itemDef = MIItem.itemNoModel(color.englishNamePrefix + "ME Wire", pipeId,
                prop -> new PipeItem(prop, type, new MENetworkData()), SortOrder.PIPES);
        var item = itemDef.asItem();
        MIPipes.INSTANCE.register(type, item);
        MIPipes.ITEM_PIPE_MODELS.add(new MIIdentifier("item/" + pipeId));
        TagsToGenerate.generateTag(MITags.ME_WIRES, item, "ME Wires");
    }
}
