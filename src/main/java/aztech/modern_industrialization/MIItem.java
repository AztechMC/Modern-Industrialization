package aztech.modern_industrialization;

import net.minecraft.item.Item;

import java.util.HashMap;

public class MIItem extends Item {


    private String id;
    public static HashMap<String, MIItem> items = new HashMap<String, MIItem>();

    public MIItem(String id){
        super(new Item.Settings().group(ModernIndustrialization.ITEM_GROUP));
        if(items.containsKey(id)){
            throw new IllegalArgumentException("Item id already taken : " + id);
        }else{
            this.id = id;
            items.put(id, this);
        }
    }

    public String getId() {
        return id;
    }

    public static final MIItem ITEM_BRICK_DUST = new MIItem("brick_dust");
    public static final MIItem ITEM_FIRE_CLAY_DUST = new MIItem("fire_clay_dust");
    public static final MIItem ITEM_FIRE_CLAY = new MIItem("fire_clay");
    public static final MIItem ITEM_FIRE_CLAY_BRICK = new MIItem("fire_clay_brick");
    public static final MIItem ITEM_COKE = new MIItem("coke");
    public static final MIItem ITEM_COKE_DUST = new MIItem("coke_dust");
    public static final MIItem ITEM_UNCOOKED_STEEL_DUST = new MIItem("uncooked_steel_dust");



}
