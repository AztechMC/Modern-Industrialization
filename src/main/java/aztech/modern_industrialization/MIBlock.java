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

import aztech.modern_industrialization.blocks.TrashCanBlock;
import aztech.modern_industrialization.blocks.creativestorageunit.CreativeStorageUnitBlock;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankBlock;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankItem;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerBlock;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.loot.JCondition;
import net.devtech.arrp.json.loot.JEntry;
import net.devtech.arrp.json.loot.JLootTable;
import net.devtech.arrp.json.loot.JPool;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class MIBlock extends Block {

    public static SortedMap<String, MIBlock> blocks = new TreeMap<>();

    public final Item blockItem;
    public final String id;

    private JLootTable lootTables;
    private JModel blockModel;
    private JModel itemModel;
    private JState blockState;
    private boolean pickaxeMineable = false;
    private int miningLevel = 0;

    public final int FLAGS;

    public static final int FLAG_BLOCK_LOOT = 1;
    public static final int FLAG_BLOCK_MODEL = 1 << 1;
    public static final int FLAG_BLOCK_ITEM_MODEL = 1 << 2;

    public MIBlock(String id, Properties settings, int registrationFlag) {
        this(id, settings, null, registrationFlag);
    }

    public MIBlock(String id, Properties settings, Function<MIBlock, BlockItem> blockItemCtor, int registrationFlag) {
        super(settings);
        this.id = id;

        if (blocks.containsKey(id)) {
            throw new IllegalArgumentException("Block id already taken : " + this.id);
        } else {
            blocks.put(id, this);
            if (blockItemCtor != null) {
                this.blockItem = blockItemCtor.apply(this);
            } else {
                this.blockItem = MIItem.of(itemSettings -> new BlockItem(this, itemSettings), this.id, 64);
            }
        }

        this.setLootTables(JLootTable.loot("minecraft:block")
                .pool(new JPool().rolls(1).entry(new JEntry().type("minecraft:item").name(ModernIndustrialization.MOD_ID + ":" + this.id))
                        .condition(new JCondition("minecraft:survives_explosion"))));

        this.setBlockState(JState.state().add(new JVariant().put("", new JBlockModel(ModernIndustrialization.MOD_ID + ":block/" + id))));
        this.setBlockModel(
                JModel.model().parent("block/cube_all").textures(new JTextures().var("all", ModernIndustrialization.MOD_ID + ":blocks/" + id)));

        this.setItemModel(JModel.model().parent(ModernIndustrialization.MOD_ID + ":block/" + id));

        this.FLAGS = registrationFlag;
    }

    public MIBlock(String id, Properties settings) {
        this(id, settings, FLAG_BLOCK_LOOT | FLAG_BLOCK_MODEL | FLAG_BLOCK_ITEM_MODEL);
    }

    public MIBlock(String id, Properties settings, Function<MIBlock, BlockItem> blockItemCtor) {
        this(id, settings, blockItemCtor, FLAG_BLOCK_LOOT | FLAG_BLOCK_MODEL | FLAG_BLOCK_ITEM_MODEL);
    }

    public MIBlock(String id) {
        this(id, FabricBlockSettings.of(METAL_MATERIAL).destroyTime(4.0f).requiresCorrectToolForDrops()
                .isValidSpawn(MobSpawning.NO_SPAWN));
        pickaxeMineable = true;
    }

    public MIBlock(String id, float resistance) {
        this(id, FabricBlockSettings.of(METAL_MATERIAL).destroyTime(4.0f).explosionResistance(resistance)
                .requiresCorrectToolForDrops()
                .isValidSpawn(MobSpawning.NO_SPAWN));
        pickaxeMineable = true;
    }

    // hull
    public static final MIBlock BASIC_MACHINE_HULL = new MIBlock("basic_machine_hull");
    public static final MIBlock ADVANCED_MACHINE_HULL = new MIBlock("advanced_machine_hull");
    public static final MIBlock TURBO_MACHINE_HULL = new MIBlock("turbo_machine_hull");
    public static final MIBlock HIGHLY_ADVANCED_MACHINE_HULL = new MIBlock("highly_advanced_machine_hull");
    public static final MIBlock QUANTUM_MACHINE_HULL = new MIBlock("quantum_machine_hull", 6000);

    // Multiblock
    public static final MIBlock FUSION_CHAMBER = new MIBlock("fusion_chamber");

    // other
    public static final MIBlock INDUSTRIAL_TNT = new MIBlock("industrial_tnt",
            Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS));

    public static final MIBlock NUKE = new MIBlock("nuke", Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS));

    public static final MIBlock BLOCK_FIRE_CLAY_BRICKS = new MIBlock("fire_clay_bricks",
            FabricBlockSettings.of(STONE_MATERIAL).destroyTime(2.0f).explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()).setPickaxeMineable();

    public static final Block FORGE_HAMMER = new ForgeHammerBlock();
    public static final TrashCanBlock TRASH_CAN = new TrashCanBlock();

    public static final CreativeTankBlock CREATIVE_TANK_BLOCK = new CreativeTankBlock();
    public static final CreativeStorageUnitBlock CREATIVE_STORAGE_UNIT = new CreativeStorageUnitBlock();

    static {
        // Extra setup
        FluidStorage.ITEM.registerForItems(CreativeTankItem.TankItemStorage::new, CREATIVE_TANK_BLOCK.blockItem);
    }

    public MIBlock setLootTables(JLootTable lootTables) {
        this.lootTables = lootTables;
        return this;
    }

    public MIBlock setBlockState(JState blockState) {
        this.blockState = blockState;
        return this;
    }

    public MIBlock setBlockModel(JModel blockModel) {
        this.blockModel = blockModel;
        return this;
    }

    public MIBlock asColumn() {
        return this.setBlockModel(JModel.model().parent("block/cube_column")
                .textures(new JTextures().var("end", ModernIndustrialization.MOD_ID + ":blocks/" + id + "_end").var("side",
                        ModernIndustrialization.MOD_ID + ":blocks/" + id + "_side")));
    }

    public MIBlock setItemModel(JModel itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public MIBlock setPickaxeMineable() {
        pickaxeMineable = true;
        return this;
    }

    public MIBlock setMiningLevel(int level) {
        miningLevel = level;
        return this;
    }

    public void onRegister(Block block, Item blockItem) {

    }

    public JLootTable getLootTables() {
        return lootTables;
    }

    public JModel getBlockModel() {
        return blockModel;
    }

    public JModel getItemModel() {
        return itemModel;
    }

    public JState getBlockState() {
        return blockState;
    }

    public boolean isPickaxeMineable() {
        return pickaxeMineable;
    }

    public int getMiningLevel() {
        return miningLevel;
    }
}
