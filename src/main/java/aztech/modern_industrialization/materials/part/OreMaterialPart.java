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

import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.util.ResourceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.function.Function;
import net.devtech.arrp.json.loot.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.item.Item;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class OreMaterialPart implements MaterialPart {

    protected final MaterialOreSet oreSet;
    protected final boolean deepslate;

    protected final String materialName;
    protected final String part;
    protected final String itemPath;
    protected final String itemId;
    protected final String itemTag;
    protected final UniformIntProvider xpDropped;
    protected final Coloramp coloramp;
    protected MIBlock block;
    protected Item item;
    protected String mainPart;

    protected OreMaterialPart(String materialName, Coloramp coloramp, MaterialOreSet oreSet, boolean deepslate, UniformIntProvider xpDropped,
            String mainPart) {
        this.materialName = materialName;
        this.coloramp = coloramp;
        this.part = deepslate ? MIParts.ORE : MIParts.ORE_DEEPLSATE;
        this.itemPath = (deepslate ? "deepslate_" : "") + materialName + "_ore";
        this.itemId = "modern_industrialization:" + itemPath;
        this.itemTag = "#c:" + materialName + "_ores";
        this.oreSet = oreSet;
        this.deepslate = deepslate;
        this.mainPart = mainPart;
        this.xpDropped = xpDropped;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(MaterialOreSet oreSet, UniformIntProvider xpDropped) {
        Function<MaterialBuilder.PartContext, MaterialPart>[] array = new Function[2];
        for (int i = 0; i < 2; i++) {
            final int j = i;
            Function<MaterialBuilder.PartContext, MaterialPart> function = ctx -> new OreMaterialPart(ctx.getMaterialName(), ctx.getColoramp(),
                    oreSet, j == 0, xpDropped, ctx.getMainPart());
            array[i] = function;
        }
        return array;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(MaterialOreSet oreSet) {
        return of(oreSet, UniformIntProvider.create(0, 0));
    }

    @Override
    public String getPart() {
        return deepslate ? MIParts.ORE_DEEPLSATE : MIParts.ORE;
    }

    @Override
    public String getTaggedItemId() {
        return itemTag;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        block = new OreBlock(itemPath, FabricBlockSettings.of(STONE_MATERIAL).hardness(deepslate ? 4.5f : 3.0f).resistance(3.0f)
                .breakByTool(FabricToolTags.PICKAXES, 1).requiresTool(), xpDropped);
        item = block.blockItem;

        String loot = switch (mainPart) {
        case MIParts.INGOT -> context.getMaterialPart(MIParts.RAW_METAL).getItemId();
        case MIParts.DUST -> context.getMaterialPart(MIParts.DUST).getItemId();
        case MIParts.GEM -> context.getMaterialPart(MIParts.GEM).getItemId();
        default -> "";
        };

        block.setLootTables(JLootTable.loot("minecraft:block")
                .pool(new JPool().rolls(1).bonus(0).entry(new JEntry().type("minecraft:alternatives").child(new JEntry().type("minecraft:item")
                        .condition(new JCondition("minecraft:match_tool").parameter("predicate", new Gson().fromJson("""
                                {
                                "enchantments": [
                                  {
                                    "enchantment": "minecraft:silk_touch",
                                    "levels": {
                                      "min": 1
                                    }
                                  }
                                ]
                                }
                                """, JsonElement.class))).name(itemId)

                ).child(new JEntry().type("minecraft:item").function(new JFunction("minecraft:apply_bonus")
                        .parameter("enchantment", "minecraft:fortune").parameter("formula", "minecraft:ore_drops"))
                        .function(new JFunction("minecraft:explosion_decay")).name(loot)))));

        ResourceUtil.appendToTag("c:items/" + materialName + "_ores", getItemId());

    }

    public String getTranslationKey() {
        return item.getTranslationKey();
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        String template = String.format("modern_industrialization:textures/materialsets/ores/%s.png", oreSet.name);
        try {

            String from =

                    switch (oreSet) {
                    case IRON -> deepslate ? "deepslate_iron_ore" : "iron_ore";
                    case COPPER -> deepslate ? "deepslate_copper_ore" : "copper_ore";
                    case LAPIS -> deepslate ? "deepslate_lapis_ore" : "lapis_ore";
                    case REDSTONE -> deepslate ? "deepslate" : "redstone_ore";
                    case DIAMOND -> deepslate ? "deepslate" : "diamond_ore";
                    case GOLD -> deepslate ? "deepslate_gold_ore" : "gold_ore";
                    case EMERALD -> deepslate ? "deepslate_emerald_ore" : "emerald_ore";
                    case COAL -> deepslate ? "deepslate_coal_ore" : "coal_ore";
                    default -> deepslate ? "deepslate" : "stone";
                    };

            NativeImage image = mtm.getAssetAsTexture(String.format("minecraft:textures/block/%s.png", from));
            NativeImage top = mtm.getAssetAsTexture(template);
            TextureHelper.colorize(top, coloramp);
            TextureHelper.blend(image, top);
            top.close();
            String texturePath = String.format("modern_industrialization:textures/blocks/%s.png", itemPath);
            mtm.addTexture(texturePath, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Item getItem() {
        return item;
    }

}
