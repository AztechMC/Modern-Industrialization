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
package aztech.modern_industrialization;

import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.GuideBookItem;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.armor.GraviChestPlateItem;
import aztech.modern_industrialization.items.armor.JetpackItem;
import aztech.modern_industrialization.items.armor.QuantumArmorItem;
import aztech.modern_industrialization.items.armor.RubberArmorMaterial;
import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.items.tools.CrowbarItem;
import aztech.modern_industrialization.items.tools.QuantumSword;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;

@SuppressWarnings("unused")
public final class MIItem {

    public static SortedMap<String, Item> items = new TreeMap<>();
    public static SortedMap<String, Consumer<Item>> registrationEvents = new TreeMap<>();
    public static SortedSet<String> handhelds = new TreeSet<>();

    public static Item of(String id) {
        return of(Item::new, id, 64);
    }

    public static Item of(String id, Rarity rarity) {
        return of(Item::new, id, 64, null, false, rarity);
    }

    public static Item of(String id, int maxCount, Rarity rarity) {
        return of(Item::new, id, maxCount, null, false, rarity);
    }

    public static Item of(String id, Consumer<Item> registrationEvent) {
        return of(Item::new, id, 64, registrationEvent);
    }

    public static Item of(String id, int maxCount) {
        return of(Item::new, id, maxCount);
    }

    public static Item of(String id, int maxCount, boolean handheld) {
        return of(Item::new, id, maxCount, null, handheld);
    }

    public static <T extends Item> T of(Function<? super FabricItemSettings, T> ctor, String id, int maxCount) {
        return of(ctor, id, maxCount, null);
    }

    public static <T extends Item> T of(Function<? super FabricItemSettings, T> ctor, String id, int maxCount, Consumer<Item> registrationEvent) {
        return of(ctor, id, maxCount, registrationEvent, false);
    }

    public static <T extends Item> T of(Function<? super FabricItemSettings, T> ctor, String id, int maxCount, boolean handheld) {
        return of(ctor, id, maxCount, null, handheld);
    }

    public static <T extends Item> T of(Function<? super FabricItemSettings, T> ctor, String id, int maxCount, Consumer<Item> registrationEvent,
            boolean handheld) {
        return of(ctor, id, maxCount, registrationEvent, handheld, Rarity.COMMON);
    }

    public static <T extends Item> T of(Function<? super FabricItemSettings, T> ctor, String id, int maxCount, Consumer<Item> registrationEvent,
            boolean handheld, Rarity rarity) {
        T item = ctor.apply(new FabricItemSettings().maxCount(maxCount).group(ModernIndustrialization.ITEM_GROUP).rarity(rarity));
        if (items.put(id, item) != null) {
            throw new IllegalArgumentException("Item id already taken : " + id);
        }
        if (registrationEvent != null) {
            registrationEvents.put(id, registrationEvent);
        }
        if (handheld) {
            handhelds.add(id);
        }
        return item;
    }

    public static final Item ITEM_GUIDE_BOOK = of(GuideBookItem::new, "guidebook", 64);
    public static final Item ITEM_UNCOOKED_STEEL_DUST = of("uncooked_steel_dust");

    public static final Item RUBBER_HELMET = of(s -> new ArmorItem(RubberArmorMaterial.INSTANCE, EquipmentSlot.HEAD, s), "rubber_helmet", 1);
    public static final Item RUBBER_BOOTS = of(s -> new ArmorItem(RubberArmorMaterial.INSTANCE, EquipmentSlot.FEET, s), "rubber_boots", 1);

    public static final Item ITEM_MOTOR = of("motor", (item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 2, item));
    public static final Item ITEM_PISTON = of("piston");
    public static final Item ITEM_CONVEYOR = of("conveyor");
    public static final Item ITEM_ROBOT_ARM = of("robot_arm");
    public static final Item ITEM_CIRCUIT = of("analog_circuit");
    public static final Item ITEM_CIRCUIT_BOARD = of("analog_circuit_board");
    public static final Item ITEM_PUMP = of("pump");
    public static final Item ITEM_RESISTOR = of("resistor");
    public static final Item ITEM_CAPACITOR = of("capacitor");
    public static final Item ITEM_INDUCTOR = of("inductor");
    public static final Item ITEM_WOOD_PULP = of("wood_pulp");
    public static final Item ITEM_RUBBER_SHEET = of("rubber_sheet");
    public static final Item ITEM_INVAR_ROTARY_BLADE = of("invar_rotary_blade");

    public static final Item ITEM_ELECTRONIC_CIRCUIT = of("electronic_circuit");
    public static final Item ITEM_DIODE = of("diode");
    public static final Item ITEM_ELECTRONIC_CIRCUIT_BOARD = of("electronic_circuit_board");
    public static final Item ITEM_TRANSISTOR = of("transistor");
    public static final Item ITEM_LARGE_MOTOR = of("large_motor", (item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 8, item));

    public static final Item ITEM_LARGE_PUMP = of("large_pump");

    public static final Item ITEM_DIGITAL_CIRCUIT = of("digital_circuit");
    public static final Item ITEM_DIGITAL_CIRCUIT_BOARD = of("digital_circuit_board");
    public static final Item ITEM_OP_AMP = of("op_amp");
    public static final Item ITEM_AND_GATE = of("and_gate");
    public static final Item ITEM_OR_GATE = of("or_gate");
    public static final Item ITEM_NOT_GATE = of("not_gate");

    public static final Item ITEM_PROCESSING_UNIT = of("processing_unit");
    public static final Item ITEM_PROCESSING_UNIT_BOARD = of("processing_unit_board");
    public static final Item ITEM_ARITHMETIC_LOGIC_UNIT = of("arithmetic_logic_unit");
    public static final Item ITEM_RANDOM_ACCESS_MEMORY = of("random_access_memory");
    public static final Item ITEM_MEMORY_MANAGEMENT_UNIT = of("memory_management_unit");

    public static final Item ITEM_QUANTUM_CIRCUIT_BOARD = of("quantum_circuit_board", Rarity.RARE);
    public static final Item ITEM_QUANTUM_CIRCUIT = of("quantum_circuit", Rarity.RARE);
    public static final Item ITEM_QBIT = of("qbit", Rarity.RARE);

    public static final Item ITEM_MONOCRYSTALLINE_SILICON = of("monocrystalline_silicon");
    public static final Item ITEM_SILICON_WAFER = of("silicon_wafer");

    public static final Item BASIC_UPGRADE = of("basic_upgrade");
    public static final Item ADVANCED_UPGRADE = of("advanced_upgrade");
    public static final Item TURBO_UPGRADE = of("turbo_upgrade", Rarity.UNCOMMON);
    public static final Item HIGHLY_ADVANCED_UPGRADE = of("highly_advanced_upgrade", Rarity.RARE);
    public static final Item QUANTUM_UPGRADE = of("quantum_upgrade", 1, Rarity.EPIC);

    public static final Item ADVANCED_MOTOR = of("advanced_motor", (item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 32, item));
    public static final Item LARGE_ADVANCED_MOTOR = of("large_advanced_motor",
            (item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 64, item));
    public static final Item ADVANCED_PUMP = of("advanced_pump");
    public static final Item LARGE_ADVANCED_PUMP = of("large_advanced_pump");

    public static final Item MIXED_INGOT_BLASTPROOF = of("mixed_ingot_blastproof");
    public static final Item MIXED_INGOT_IRIDIUM = of("mixed_ingot_iridium");

    public static final Item MIXED_PLATE_NUCLEAR = of("mixed_plate_nuclear");

    public static final Item AIR_INTAKE = of("air_intake", 1);

    public static final Item ITEM_PACKER_BLOCK_TEMPLATE = of("packer_block_template", 1, Rarity.RARE);
    public static final Item ITEM_PACKER_DOUBLE_INGOT_TEMPLATE = of("packer_double_ingot_template", 1, Rarity.RARE);

    public static final Item ITEM_SCREWDRIVER = of("screwdriver", 1, true);
    public static final Item ITEM_WRENCH = of("wrench", 1, true);
    public static final JetpackItem ITEM_DIESEL_JETPACK = of(JetpackItem::new, "diesel_jetpack", 1,
            (item) -> FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(JetpackItem.CAPACITY, ctx), item));
    public static final DieselToolItem ITEM_DIESEL_CHAINSAW = of(s -> new DieselToolItem(s, 12), "diesel_chainsaw", 1,
            (item) -> FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(DieselToolItem.CAPACITY, ctx), item),
            true);

    public static final DieselToolItem ITEM_DIESEL_MINING_DRILL = of(s -> new DieselToolItem(s, 7), "diesel_mining_drill", 1,
            (item) -> FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(DieselToolItem.CAPACITY, ctx), item),
            true);

    public static final SteamDrillItem ITEM_STEAM_MINING_DRILL = of(SteamDrillItem::new, "steam_mining_drill", 1, true);
    public static final Item ITEM_CROWBAR = of(CrowbarItem::new, "crowbar", 1, true);

    public static final Item COOLING_CELL = of("cooling_cell");

    public static final GraviChestPlateItem GRAVI_CHEST_PLATE = of(GraviChestPlateItem::new, "gravichestplate", 1);
    public static final QuantumArmorItem QUANTUM_BOOTS = of(s -> new QuantumArmorItem(EquipmentSlot.FEET, s), "quantum_boots", 1);
    public static final QuantumArmorItem QUANTUM_LEGGINGS = of(s -> new QuantumArmorItem(EquipmentSlot.LEGS, s), "quantum_leggings", 1);
    public static final QuantumArmorItem QUANTUM_CHESTPLATE = of(s -> new QuantumArmorItem(EquipmentSlot.CHEST, s), "quantum_chestplate", 1);
    public static final QuantumArmorItem QUANTUM_HELMET = of(s -> new QuantumArmorItem(EquipmentSlot.HEAD, s), "quantum_helmet", 1);
    public static final QuantumSword QUANTUM_SWORD = of(QuantumSword::new, "quantum_sword", 1, true);

    public static final Item ULTRADENSE_METAL_BALL = of("ultradense_metal_ball");
    public static final Item SINGULARITY = of("singularity", Rarity.EPIC);
}
