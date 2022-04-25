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

import appeng.api.IAEAddonEntrypoint;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.inventories.PartApiLookup;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.energy.EnergyApi;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;

public class MIAEAddon implements IAEAddonEntrypoint {

    @Override
    public void onAe2Initialized() {
        if (!MIConfig.getConfig().enableAe2Integration) {
            return;
        }

        PartModels.registerModels(PartModelsHelper.createModels(EnergyP2PTunnelPart.class));
        var item = Registry.register(Registry.ITEM, new MIIdentifier("energy_p2p_tunnel"), new PartItem<>(
                new FabricItemSettings().tab(ModernIndustrialization.ITEM_GROUP), EnergyP2PTunnelPart.class, EnergyP2PTunnelPart::new));
        P2PTunnelAttunement.addItemByTag(MITags.miItem("energy_p2p_attunement"), item);
        PartApiLookup.register(EnergyApi.MOVEABLE, (part, context) -> part.getExposedApi(), EnergyP2PTunnelPart.class);
    }
}
