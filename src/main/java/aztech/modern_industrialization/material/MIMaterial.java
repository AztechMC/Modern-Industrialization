package aztech.modern_industrialization.material;


import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.HashMap;

public class MIMaterial {

    private String id;
    private static HashMap<String, MIMaterial> map = new HashMap<String, MIMaterial>();
    private boolean hasOre = false;
    private boolean isVanilla = false; // for gold, iron

    private int veinsPerChunk, veinsSize, maxYLevel;
    private boolean isOverworld = false;
    private boolean isEnd = false;
    private boolean isNether = false;

    private float hardness = 5.0f;
    private float oreHardness = 3.0f;
    private int miningLevel = 2;

    private float blastResistance = 6.0f;
    private float oreBlastResistance = 3.0f;

    public static final String[] customItem = {"dust", "small_dust", "plate", "gear", "rod"};

    private HashMap<String, Item> itemMap = new HashMap<String, Item>();
    private HashMap<String, Block> blockMap = new HashMap<String, Block>();

    public MIMaterial(String id){
        this.id = id;
        if(map.containsKey(id)){
            throw new IllegalArgumentException("Material ID : "+ id + " already exists");
        }else{
            map.put(id, this);
        }
    }

    public static Iterable<MIMaterial> getAllMaterials(){
        return map.values();
    }

    public String getId() {
        return id;
    }

    public boolean hasOre() {
        return hasOre;
    }

    public boolean isVanilla() { return isVanilla; }

    public MIMaterial setIsVanilla(boolean isVanilla){
        this.isVanilla = isVanilla;
        return this;
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

    public MIMaterial setUpOreGenerator(int veinsPerChunk, int veinsSize, int maxYLevel, int type, float hardness){
        this.hasOre = true;
        this.veinsPerChunk = veinsPerChunk;
        this.veinsSize = veinsSize;
        this.maxYLevel = maxYLevel;
        if(type == 0){
            isOverworld = true;
        }else if(type == 1){
            isNether = true;
        }else if(type == 2){
            isEnd = true;
        }
        this.oreHardness = oreHardness;
        return this;
    }

    public MIMaterial setUpOreGenerator(int veinsPerChunk, int veinsSize, int maxYLevel, float hardness){
        return setUpOreGenerator(veinsPerChunk, veinsSize, maxYLevel, 0, hardness);
    }

    public MIMaterial setUpOreGenerator(int veinsPerChunk, int veinsSize, int maxYLevel, int type){
        return setUpOreGenerator(veinsPerChunk, veinsSize, maxYLevel, type, 3.0f);
    }

    public MIMaterial setUpOreGenerator(int veinsPerChunk, int veinsSize, int maxYLevel){
        return setUpOreGenerator(veinsPerChunk, veinsSize, maxYLevel, 0, 3.0f);
    }

    public boolean isOverworld() {
        return isOverworld;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public boolean isNether() {
        return isNether;
    }

    public float getHardness() {
        return hardness;
    }

    public MIMaterial setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    public float getOreHardness() {
        return oreHardness;
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public MIMaterial setMiningLevel(int miningLevel) {
        this.miningLevel = miningLevel;
        return this;
    }

    public float getBlastResistance() {
        return blastResistance;
    }

    public MIMaterial setBlastResistance(float blastResistance) {
        this.blastResistance = blastResistance;
        return this;
    }

    public float getOreBlastResistance() {
        return oreBlastResistance;
    }

    public MIMaterial setOreBlastResistance(float oreBlastResistance) {
        this.oreBlastResistance = oreBlastResistance;
        return this;
    }

    public void saveItem(String type, Item item) {
        this.itemMap.put(type, item);
    }

    public void saveBlock(String type, Block block) {
        this.blockMap.put(type, block);
    }

    public Item getItem(String type) {
        return itemMap.get(type);
    }

    public Block getBlock(String type) {
        return blockMap.get(type);
    }

    public static MIMaterial iron = new MIMaterial("iron").setIsVanilla(true);
    public static MIMaterial gold = new MIMaterial("gold").setIsVanilla(true);
    public static MIMaterial copper = new MIMaterial("copper").setUpOreGenerator(10, 8, 64);
    public static MIMaterial tin = new MIMaterial("tin").setUpOreGenerator(4, 6, 64);
    public static MIMaterial bronze = new MIMaterial("bronze");

}
