package aztech.modern_industrialization.material;

public class MIMaterials {

    public static MIMaterial gold = new MIMaterial("gold", true).addItemType(new String [] { "dust","large_plate","double_ingot","crushed_dust","plate","rod","curved_plate","small_dust"});
    public static MIMaterial iron = new MIMaterial("iron", true).addItemType(new String [] { "blade","dust","large_plate","rotor","double_ingot","crushed_dust","plate","gear","bolt","rod","curved_plate","small_dust","ring"});
    public static MIMaterial copper = new MIMaterial("copper", false).addItemType(new String [] { "blade","nugget","ingot","dust","large_plate","rotor","double_ingot","crushed_dust","plate","gear","bolt","rod","curved_plate","small_dust","ring"}).addBlockType(new String [] { "ore","block" }).setupOreGenerator(20, 9, 128);
    public static MIMaterial bronze = new MIMaterial("bronze", false).addItemType(new String [] { "blade","nugget","ingot","dust","large_plate","rotor","curved_plate","double_ingot","plate","gear","rod","bolt","small_dust","ring"}).addBlockType(new String [] { "block" });
    public static MIMaterial tin = new MIMaterial("tin", false).addItemType(new String [] { "blade","nugget","ingot","dust","large_plate","rotor","double_ingot","crushed_dust","plate","gear","bolt","rod","curved_plate","small_dust","ring"}).addBlockType(new String [] { "ore","block" }).setupOreGenerator(8, 9, 64);
    public static MIMaterial steel = new MIMaterial("steel", false).addItemType(new String [] { "blade","nugget","ingot","dust","large_plate","rotor","curved_plate","double_ingot","plate","gear","rod","bolt","small_dust","ring"}).addBlockType(new String [] { "block" });
    public static MIMaterial aluminum = new MIMaterial("aluminum", false).addItemType(new String [] { "nugget","ingot","dust","large_plate","double_ingot","crushed_dust","plate","rod","curved_plate","small_dust"}).addBlockType(new String [] { "ore","block" }).setupOreGenerator(6, 6, 64);
    public static MIMaterial lignite_coal = new MIMaterial("lignite_coal", false).addItemType(new String [] { "lignite_coal","crushed_dust","dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(20, 17, 128);

}