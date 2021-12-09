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
package aztech.modern_industrialization.datagen.advancement;

import aztech.modern_industrialization.MIIdentifier;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementsProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class MIAdvancementsProvider extends FabricAdvancementsProvider {
    public MIAdvancementsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @SuppressWarnings("unused")
    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        var forgeHammer = createBasic(consumer, "forge_hammer", null, AdvancementFrame.GOAL);
        var steamMiningDrill = createBasic(consumer, "steam_mining_drill", forgeHammer, AdvancementFrame.GOAL);
        var fireClayBricks = createBasic(consumer, "fire_clay_bricks", forgeHammer);
        var bronzeFurnace = createBasic(consumer, "bronze_furnace", fireClayBricks);
        var bronzeCompressor = createBasic(consumer, "bronze_compressor", forgeHammer);
        var bronzeMixer = createBasic(consumer, "bronze_mixer", forgeHammer);
        var bronzeMacerator = createBasic(consumer, "bronze_macerator", forgeHammer);
        var cokeOven = createBasic(consumer, "coke_oven", forgeHammer, AdvancementFrame.GOAL);
        var steamBlastFurnace = createBasic(consumer, "steam_blast_furnace", fireClayBricks);
        var steelMachineCasing = createBasic(consumer, "steel_machine_casing", steamBlastFurnace, AdvancementFrame.GOAL);
        var steelWiremill = createBasic(consumer, "steel_wiremill", steelMachineCasing);
        var steamQuarry = createBasic(consumer, "steam_quarry", steelMachineCasing);
        var steelPacket = createBasic(consumer, "steel_packer", steelMachineCasing);
        var inductor = createBasic(consumer, "inductor", steelWiremill);
        var resistor = createBasic(consumer, "resistor", steelWiremill);
        var capacitor = createBasic(consumer, "capacitor", steelWiremill);
        var analogCircuit = createBasic(consumer, "analog_circuit", resistor, AdvancementFrame.GOAL);
        var lvSteamTurbine = createBasic(consumer, "lv_steam_turbine", analogCircuit);
        var polarizer = createBasic(consumer, "polarizer", lvSteamTurbine);
        var largeSteamBoiler = createBasic(consumer, "large_steam_boiler", analogCircuit);
        var assembler = createBasic(consumer, "assembler", analogCircuit, AdvancementFrame.GOAL);
        var mvLvTransformer = createBasic(consumer, "mv_lv_transformer", analogCircuit);
        var electricBlastFurnace = createBasic(consumer, "electric_blast_furnace", lvSteamTurbine, AdvancementFrame.GOAL);
        var centrifuge = createBasic(consumer, "centrifuge", electricBlastFurnace);
        var electrolyzer = createBasic(consumer, "electrolyzer", electricBlastFurnace);
        var chemicalReactor = createBasic(consumer, "chemical_reactor", electricBlastFurnace);
        var distillery = createBasic(consumer, "distillery", electricBlastFurnace);
        var electricQuarry = createBasic(consumer, "electric_quarry", electricBlastFurnace, AdvancementFrame.CHALLENGE);
        var oilDrillingRig = createBasic(consumer, "oil_drilling_rig", electricBlastFurnace, AdvancementFrame.GOAL);
        var vacuumFreezer = createBasic(consumer, "vacuum_freezer", electricBlastFurnace, AdvancementFrame.GOAL);
        var mvSteamTurbine = createBasic(consumer, "mv_steam_turbine", electricBlastFurnace, AdvancementFrame.GOAL);
        var dieselGenerator = createBasic(consumer, "diesel_generator", distillery);
        var dieselJetpack = createBasic(consumer, "diesel_jetpack", distillery, AdvancementFrame.CHALLENGE);
        var dieselChainsaw = createBasic(consumer, "diesel_chainsaw", distillery, AdvancementFrame.CHALLENGE);
        var dieselMiningDrill = createBasic(consumer, "diesel_mining_drill", distillery, AdvancementFrame.CHALLENGE);
    }

    private static Advancement createBasic(Consumer<Advancement> consumer, String item, @Nullable Advancement parent) {
        return createBasic(consumer, item, parent, AdvancementFrame.TASK);
    }

    private static Advancement createBasic(Consumer<Advancement> consumer, String itemId, @Nullable Advancement parent, AdvancementFrame frame) {
        var item = Registry.ITEM.get(new MIIdentifier(itemId));
        var titleKey = "advancements.modern_industrialization." + itemId;
        var descKey = "advancements.modern_industrialization." + itemId + ".description";

        var advancementTask = Advancement.Task.create();
        Identifier background = null;

        if (parent != null) {
            advancementTask.parent(parent);
        } else {
            background = new MIIdentifier("textures/blocks/fire_clay_bricks.png");
        }

        advancementTask.display(
                item,
                new TranslatableText(titleKey),
                new TranslatableText(descKey),
                background,
                frame,
                true,
                true,
                false);
        advancementTask.criterion("checkInv", InventoryChangedCriterion.Conditions.items(item));

        var advancement = advancementTask.build(new MIIdentifier(itemId));
        consumer.accept(advancement);
        return advancement;
    }
}
