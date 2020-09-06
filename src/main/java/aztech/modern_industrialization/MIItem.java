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

    public static final MIItem ITEM_SMALL_BRICK_DUST = new MIItem("brick_small_dust");
    public static final MIItem ITEM_BRICK_DUST = new MIItem("brick_dust");
    public static final MIItem ITEM_FIRE_CLAY_DUST = new MIItem("fire_clay_dust");
    public static final MIItem ITEM_FIRE_CLAY_BRICK = new MIItem("fire_clay_brick");
    public static final MIItem ITEM_COKE = new MIItem("coke");
    public static final MIItem ITEM_COKE_DUST = new MIItem("coke_dust");
    public static final MIItem ITEM_UNCOOKED_STEEL_DUST = new MIItem("uncooked_steel_dust");

    public static final MIItem ITEM_LV_MOTOR = new MIItem("lv_motor");
    public static final MIItem ITEM_LV_PISTON = new MIItem("lv_piston");
    public static final MIItem ITEM_LV_CONVEYOR = new MIItem("lv_conveyor");
    public static final MIItem ITEM_LV_ROBOT_ARM = new MIItem("lv_robot_arm");
    public static final MIItem ITEM_LV_CIRCUIT = new MIItem("lv_circuit");
    public static final MIItem ITEM_LV_CIRCUIT_BOARD = new MIItem("lv_circuit_board");
    public static final MIItem ITEM_LV_BATTERY = new MIItem("lv_battery");
    public static final MIItem ITEM_RESISTOR = new MIItem("resistor");
    public static final MIItem ITEM_CAPACITOR = new MIItem("capacitor");
    public static final MIItem ITEM_INDUCTOR = new MIItem("inductor");
    public static final MIItem ITEM_STEEL_ROD_MAGNETIC = new MIItem("steel_rod_magnetic");
    public static final MIItem ITEM_WOOD_PULP = new MIItem("wood_pulp");
    public static final MIItem ITEM_RUBBER_SHEET = new MIItem("rubber_sheet");

}
