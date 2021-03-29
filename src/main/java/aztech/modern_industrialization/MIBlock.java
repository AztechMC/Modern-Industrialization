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

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;
import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.SortedMap;
import java.util.TreeMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class MIBlock extends Block {
    public static SortedMap<String, MIBlock> blocks = new TreeMap<String, MIBlock>();
    public final Item blockItem;
    public final boolean arrpModel;
    private final String id;

    public MIBlock(String id, Settings settings, boolean arrpModel) {
        super(settings);
        this.id = id;
        this.arrpModel = arrpModel;
        if (blocks.containsKey(id)) {
            throw new IllegalArgumentException("Block id already taken : " + id);
        } else {
            blocks.put(id, this);
            blockItem = MIItem.of(itemSettings -> new BlockItem(this, itemSettings), id, 64);
        }
    }

    public String getFullId() {
        return "modern_industrialization:" + id;
    }

    public MIBlock(String id, Settings settings) {
        this(id, settings, true);
    }

    public MIBlock(String id) {
        this(id, FabricBlockSettings.of(METAL_MATERIAL).hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool()
                .allowsSpawning(MobSpawning.NO_SPAWN));
    }

    public static final MIBlock BLOCK_FIRE_CLAY_BRICKS = new MIBlock("fire_clay_bricks",
            FabricBlockSettings.of(STONE_MATERIAL).hardness(2.0f).resistance(6.0f).breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());

    public static final MIBlock QUARTZ_ORE = new OreBlock("quartz_ore",
            FabricBlockSettings.of(STONE_MATERIAL).hardness(3.0f).resistance(3.0f).breakByTool(FabricToolTags.PICKAXES, 1).requiresTool());

    // hull
    public static final MIBlock BASIC_MACHINE_HULL = new MIBlock("lv_machine_hull");
    public static final MIBlock ADVANCED_MACHINE_HULL = new MIBlock("advanced_machine_hull");
    public static final MIBlock TURBO_MACHINE_HULL = new MIBlock("turbo_machine_hull");
    public static final MIBlock HIGHLY_ADVANCED_MACHINE_HULL = new MIBlock("highly_advanced_machine_hull");

    // other
    public static final MIBlock NUCLEAR_MACHINE_CASING = new MIBlock("nuclear_machine_casing");

    // public static final MIBlock CREATIVE_ENERGY_SOURCE = new
    // MIBlock("creative_energy_source");
}
