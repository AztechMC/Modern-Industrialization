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
package aztech.modern_industrialization.material;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class MIMaterial {

    private String id;
    private static Map<String, MIMaterial> map = new TreeMap<>();

    public static Iterable<MIMaterial> getAllMaterials() {
        return map.values();
    }

    private boolean hasOre = false;
    private boolean isVanilla; // for gold, iron

    private int veinsPerChunk, veinsSize, maxYLevel;

    private ArrayList<String> itemType = new ArrayList<String>();
    private ArrayList<String> blockType = new ArrayList<String>();

    private Map<String, Item> itemMap = new HashMap<String, Item>();
    private Map<String, Block> blockMap = new HashMap<String, Block>();

    public MIMaterial(String id, boolean isVanilla) {
        this.id = id;

        if (map.containsKey(id)) {
            throw new IllegalArgumentException("Material ID : " + id + " already exists");
        } else {
            map.put(id, this);
        }
        this.isVanilla = isVanilla;
    }

    public MIMaterial(String id) {
        this(id, false);
    }

    public Iterable<String> getItemType() {
        return itemType;
    }

    public Iterable<String> getBlockType() {
        return blockType;
    }

    public MIMaterial addItemType(String itemType) {
        if (!this.itemType.contains(itemType)) {
            this.itemType.add(itemType);
        }
        return this;
    }

    public MIMaterial addItemType(String itemTypes[]) {
        for (String s : itemTypes) {
            addItemType(s);
        }
        return this;
    }

    public MIMaterial addBlockType(String blockType) {
        if (!this.blockType.contains(itemType)) {
            this.blockType.add(blockType);
        }
        return this;
    }

    public MIMaterial addBlockType(String blockTypes[]) {
        for (String s : blockTypes) {
            addBlockType(s);
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public boolean hasOre() {
        return hasOre;
    }

    public int getVeinsPerChunk() {
        return veinsPerChunk;
    }

    public int getVeinsSize() {
        return veinsSize;
    }

    public int getMaxYLevel() {
        return maxYLevel;
    }

    public MIMaterial setupOreGenerator(int veinsPerChunk, int veinsSize, int maxYLevel) {
        this.hasOre = true;

        this.veinsPerChunk = veinsPerChunk;
        this.veinsSize = veinsSize;
        this.maxYLevel = maxYLevel;
        return this;
    }

    public void saveItem(String type, Item item) {
        this.itemMap.put(type, item);
    }

    public void saveBlock(String type, Block block) {
        this.blockMap.put(type, block);
    }

    public Item getItem(String type) {
        if (type.equals("pipe")) {
            // TODO : Remove this eldricht abomination
            return MIPipes.INSTANCE.getPipeItem(PipeNetworkType.get(new MIIdentifier("fluid_" + id)));
        }

        if (isVanilla) { // TODO : refactor this
            if (type.equals("ingot")) {
                if (id.equals("iron")) {
                    return Items.IRON_INGOT;
                } else if (id.equals("gold")) {
                    return Items.GOLD_INGOT;
                }
            } else if (type.equals("nugget")) {
                if (id.equals("iron")) {
                    return Items.IRON_NUGGET;
                } else if (id.equals("gold")) {
                    return Items.GOLD_NUGGET;
                }
            }
        }
        return itemMap.get(type);
    }

    public Block getBlock(String type) {
        return blockMap.get(type);
    }

}
