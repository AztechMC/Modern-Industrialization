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
import appeng.api.parts.PartModels;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.util.AEColor;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import aztech.modern_industrialization.*;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.compat.ae2.pipe.MENetwork;
import aztech.modern_industrialization.compat.ae2.pipe.MENetworkData;
import aztech.modern_industrialization.compat.ae2.pipe.MENetworkNode;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.PipeColor;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class MIAEAddon {
    static {
        if (!MIConfig.loadAe2Compat()) {
            throw new RuntimeException("AE2 compat is disabled. How did this get loaded?");
        }
    }

    public static final ItemDefinition<PartItem<EnergyP2PTunnelPart>> ENERGY_P2P_TUNNEL = MIItem.item(
            "EU P2P Tunnel",
            "energy_p2p_tunnel",
            p -> new PartItem<>(new Item.Properties(), EnergyP2PTunnelPart.class, EnergyP2PTunnelPart::new),
            (item, gen) -> {
            },
            SortOrder.CABLES.and(CableTier.SUPERCONDUCTOR).and("extra"));
    public static final List<PipeNetworkType> PIPES = new ArrayList<>();

    public static void init(IEventBus modBus) {
        PartModels.registerModels(PartModelsHelper.createModels(EnergyP2PTunnelPart.class));

        modBus.addListener(MIAEAddon::commonSetup);
        modBus.addListener(MIAEAddon::registerPartCapabilities);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        P2PTunnelAttunement.registerAttunementTag(ENERGY_P2P_TUNNEL);
    }

    public static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(EnergyApi.SIDED, (part, context) -> part.getExposedApi(), EnergyP2PTunnelPart.class);
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
        var type = PipeNetworkType.register(
                new MIIdentifier(pipeId),
                (id, data) -> new MENetwork(id, data, aeColor),
                MENetworkNode::new,
                color.color,
                false);
        PIPES.add(type);
        var itemDef = MIItem.item(
                color.englishNamePrefix + "ME Wire",
                pipeId,
                prop -> new PipeItem(prop, type, new MENetworkData()),
                MIPipes.ITEM_MODEL_GENERATOR,
                SortOrder.PIPES);
        MIPipes.INSTANCE.register(type, itemDef::asItem);
        TagsToGenerate.generateTag(MITags.ME_WIRES, itemDef, "ME Wires");
    }
}
