package aztech.modern_industrialization.material;

public class MIMaterials {

    public static MIMaterial gold = new MIMaterial("gold", true).addItemType(new String [] { "dust","crushed_dust","large_plate","rod","curved_plate","small_dust","plate"});
    public static MIMaterial iron = new MIMaterial("iron", true).addItemType(new String [] { "dust","crushed_dust","ring","large_plate","rod","rotor","double_ingot","curved_plate","blade","small_dust","bolt","gear","plate"});
    public static MIMaterial copper = new MIMaterial("copper", false).addItemType(new String [] { "dust","crushed_dust","ring","nugget","ingot","large_plate","rod","rotor","double_ingot","curved_plate","blade","small_dust","bolt","gear","plate"}).addBlockType(new String [] { "ore","block" }).setupOreGenerator(20, 9, 128);
    public static MIMaterial bronze = new MIMaterial("bronze", false).addItemType(new String [] { "dust","ring","nugget","ingot","large_plate","rod","rotor","double_ingot","curved_plate","blade","small_dust","bolt","gear","plate"}).addBlockType(new String [] { "block" });
    public static MIMaterial tin = new MIMaterial("tin", false).addItemType(new String [] { "dust","crushed_dust","ring","nugget","ingot","large_plate","rod","rotor","double_ingot","curved_plate","blade","small_dust","bolt","gear","plate"}).addBlockType(new String [] { "ore","block" }).setupOreGenerator(8, 9, 64);
    public static MIMaterial steel = new MIMaterial("steel", false).addItemType(new String [] { "dust","ring","nugget","ingot","large_plate","rod","rotor","double_ingot","curved_plate","blade","small_dust","bolt","gear","plate"}).addBlockType(new String [] { "block" });
    public static MIMaterial aluminum = new MIMaterial("aluminum", false).addItemType(new String [] { "dust","crushed_dust","nugget","ingot","large_plate","rod","curved_plate","small_dust","plate"}).addBlockType(new String [] { "ore","block" }).setupOreGenerator(6, 6, 64);
    public static MIMaterial lignite_coal = new MIMaterial("lignite_coal", false).addItemType(new String [] { "dust","lignite_coal","crushed_dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(30, 12, 128);

}