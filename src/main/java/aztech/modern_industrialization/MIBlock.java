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

import aztech.modern_industrialization.material.MIMaterialSetup;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.SortedMap;
import java.util.TreeMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class MIBlock extends Block {

    private String id;
    public static SortedMap<String, MIBlock> blocks = new TreeMap<String, MIBlock>();
    private BlockItem blockItem;

    public MIBlock(String id, Settings settings) {
        super(settings);
        if (blocks.containsKey(id)) {
            throw new IllegalArgumentException("Block id already taken : " + id);
        } else {
            this.id = id;
            blocks.put(id, this);
            blockItem = new BlockItem(this, new Item.Settings().group(ModernIndustrialization.ITEM_GROUP));
        }
    }

    public MIBlock(String id) {
        this(id, FabricBlockSettings.of(MIMaterialSetup.METAL_MATERIAL).hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool()
                .allowsSpawning(MobSpawning.NO_SPAWN));
    }

    public String getId() {
        return id;
    }

    public BlockItem getItem() {
        return blockItem;
    }

    public static final MIBlock BLOCK_FIRE_CLAY_BRICKS = new MIBlock("fire_clay_bricks", FabricBlockSettings.of(MIMaterialSetup.STONE_MATERIAL)
            .hardness(2.0f).resistance(6.0f).breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());

    public static final MIBlock STEEL_MACHINE_CASING = new MIBlock("steel_machine_casing");
    public static final MIBlock STEEL_MACHINE_CASING_PIPE = new MIBlock("steel_machine_casing_pipe");
    public static final MIBlock BASIC_MACHINE_HULL = new MIBlock("lv_machine_hull");
    public static final MIBlock BRONZE_PLATED_BRICKS = new MIBlock("bronze_plated_bricks");
    public static final MIBlock BRONZE_MACHINE_CASING = new MIBlock("bronze_machine_casing");
    public static final MIBlock BRONZE_MACHINE_CASING_PIPE = new MIBlock("bronze_machine_casing_pipe");
    public static final MIBlock ADVANCED_MACHINE_CASING = new MIBlock("advanced_machine_casing");
    public static final MIBlock HEATPROOF_MACHINE_CASING = new MIBlock("heatproof_machine_casing");
    public static final MIBlock ADVANCED_MACHINE_HULL = new MIBlock("advanced_machine_hull");

    public static final MIBlock TURBO_MACHINE_CASING = new MIBlock("turbo_machine_casing");
    public static final MIBlock TURBO_MACHINE_HULL = new MIBlock("turbo_machine_hull");
    public static final MIBlock FROSTPROOF_MACHINE_CASING = new MIBlock("frostproof_machine_casing");
    public static final MIBlock CLEAN_STAINLESS_STEEL_MACHINE_CASING = new MIBlock("clean_stainless_steel_machine_casing");
    public static final MIBlock NUCLEAR_MACHINE_CASING = new MIBlock("nuclear_machine_casing");
}
