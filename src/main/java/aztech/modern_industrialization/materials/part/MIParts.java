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
package aztech.modern_industrialization.materials.part;

import static aztech.modern_industrialization.materials.part.NuclearFuelPart.Type.*;

import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.items.SortOrder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

public class MIParts {

    public static final BatteryPart BATTERY = new BatteryPart();
    public static final BarrelPart BARREL = new BarrelPart();
    public static final PartTemplate BLADE = new PartTemplate("Blade", "blade");
    public static final BlockPart BLOCK = new BlockPart();
    public static final PartTemplate BOLT = new PartTemplate("Bolt", "bolt");
    public static final CablePart CABLE = new CablePart();
    public static final PartTemplate COIL = new PartTemplate("Coil", "coil").asColumnBlock(SortOrder.COILS);
    public static final PartTemplate CRUSHED_DUST = new PartTemplate("Crushed Dust", "crushed_dust");
    public static final PartTemplate CURVED_PLATE = new PartTemplate("Curved Plate", "curved_plate");
    public static final PartTemplate DOUBLE_INGOT = new PartTemplate("Double Ingot", "double_ingot")
            .withTexture(new TextureGenParams.DoubleIngot());

    public static final PartTemplate DRILL_HEAD = new PartTemplate("Drill Head", "drill_head");

    public static final PartTemplate DRILL = new PartTemplate("Drill", "drill");

    public static final PartTemplate DUST = new PartTemplate("Dust", "dust");
    public static final PartTemplate FINE_WIRE = new PartTemplate("Fine Wire", "fine_wire");
    public static final PartTemplate GEAR = new PartTemplate("Gear", "gear");
    public static final PartTemplate GEM = new PartTemplate("%s", "gem").withRegister((partContext, part, itemPath, itemId, itemTag, englishName) -> {
        var item = PartTemplate.createSimpleItem(englishName, itemPath, partContext, part);
        TagsToGenerate.generateTag("forge:gems/" + itemPath, item, englishName + "s");
    })
            .withTexture(new TextureGenParams.Gem())
            .withCustomPath("%s", "%s");

    public static final PartTemplate HAMMER = new PartTemplate("Hammer", "hammer");

    public static final PartTemplate HOT_INGOT = new PartTemplate("Hot Ingot", "hot_ingot")
            .withTexture(new TextureGenParams.HotIngot());
    public static final PartTemplate INGOT = new PartTemplate("Ingot", "ingot");
    public static final PartTemplate LARGE_PLATE = new PartTemplate("Large Plate", "large_plate");
    public static final CasingPart MACHINE_CASING = new CasingPart("Machine Casing", "machine_casing");
    public static final CasingPart MACHINE_CASING_PIPE = new CasingPart("Pipe Machine Casing", "machine_casing_pipe");
    public static final CasingPart MACHINE_CASING_SPECIAL = new CasingPart("Special Casing", "machine_casing_special");
    public static final PartTemplate NUGGET = new PartTemplate("Nugget", "nugget");
    public static final OrePart ORE = new OrePart(false);
    public static final OrePart ORE_DEEPSLATE = new OrePart(true);
    public static final PartTemplate PLATE = new PartTemplate("Plate", "plate");
    public static final RawMetalPart RAW_METAL = new RawMetalPart(false);
    public static final RawMetalPart RAW_METAL_BLOCK = new RawMetalPart(true);
    public static final PartTemplate RING = new PartTemplate("Ring", "ring");
    public static final PartTemplate ROD = new PartTemplate("Rod", "rod");
    public static final PartTemplate ROD_MAGNETIC = new PartTemplate("Magnetic %s Rod", "rod_magnetic").withOverlay(ROD, "magnetic");
    public static final PartTemplate ROTOR = new PartTemplate("Rotor", "rotor");
    public static final TankPart TANK = new TankPart();
    public static final PartTemplate TINY_DUST = new PartTemplate("Tiny Dust", "tiny_dust");
    public static final PartTemplate WIRE = new PartTemplate("Wire", "wire");
    public static final PartTemplate WIRE_MAGNETIC = new PartTemplate("Magnetic %s Wire", "wire_magnetic").withOverlay(WIRE, "magnetic");

    public static final NuclearFuelPart FUEL_ROD = new NuclearFuelPart(SIMPLE);
    public static final NuclearFuelPart FUEL_ROD_DEPLETED = new NuclearFuelPart(DEPLETED);
    public static final NuclearFuelPart FUEL_ROD_DOUBLE = new NuclearFuelPart(DOUBLE);
    public static final NuclearFuelPart FUEL_ROD_QUAD = new NuclearFuelPart(QUAD);

    public static final ControlRodPart CONTROL_ROD = new ControlRodPart();

    public static final PartTemplate N_DOPED_PLATE = new PartTemplate("N-Doped %s Plate", "n_doped_plate").withOverlay(PLATE, "n_doped");
    public static final PartTemplate P_DOPED_PLATE = new PartTemplate("P-Doped %s Plate", "p_doped_plate").withOverlay(PLATE, "p_doped");

    public static final PartTemplate[] ITEM_PURE_NON_METAL = new PartTemplate[] { TINY_DUST, DUST, CRUSHED_DUST };
    public static final PartTemplate[] ITEM_PURE_METAL = new PartTemplate[] { INGOT, NUGGET, TINY_DUST, DUST };

    public static final List<PartKey> TAGGED_PARTS_LIST = PartKeyProvider.of(BLOCK, DUST, GEAR, INGOT, NUGGET, ORE, PLATE, TINY_DUST, RAW_METAL,
            RAW_METAL_BLOCK);
    public static final Set<PartKey> TAGGED_PARTS = new HashSet<>(TAGGED_PARTS_LIST);

    public static final Map<PartKey, CategoryTag> CATEGORY_TAGS = Map.of(
            DUST.key(), new CategoryTag(Tags.Items.DUSTS, "Dusts"),
            GEAR.key(), new CategoryTag("forge:gears", "Gears"),
            INGOT.key(), new CategoryTag(Tags.Items.INGOTS, "Ingots"),
            NUGGET.key(), new CategoryTag(Tags.Items.NUGGETS, "Nuggets"),
            PLATE.key(), new CategoryTag("forge:plates", "Plates"),
            RAW_METAL.key(), new CategoryTag(Tags.Items.RAW_MATERIALS, "Raw Ores"),
            TINY_DUST.key(), new CategoryTag("forge:tiny_dusts", "Tiny Dusts"));

    public record CategoryTag(String tag, String englishName) {
        public CategoryTag(TagKey<Item> tag, String englishName) {
            this(tag.location().toString(), englishName);
        }
    }

    public static final List<PartKey> BLOCKS = PartKeyProvider.of(ORE, BARREL, BLOCK, COIL, MACHINE_CASING, MACHINE_CASING_SPECIAL,
            MACHINE_CASING_PIPE, RAW_METAL_BLOCK);

}
