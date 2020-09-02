package aztech.modern_industrialization.material;

public class MIMaterials {

    public static MIMaterial gold = new MIMaterial("gold", true).addItemType(new String [] { "large_plate","plate","small_dust","dust","curved_plate","crushed_dust","double_ingot","rod"});
    public static MIMaterial iron = new MIMaterial("iron", true).addItemType(new String [] { "gear","blade","ring","large_plate","plate","small_dust","dust","curved_plate","crushed_dust","bolt","double_ingot","rod","rotor"});
    public static MIMaterial copper = new MIMaterial("copper", false).addItemType(new String [] { "gear","blade","ingot","ring","large_plate","plate","small_dust","nugget","dust","curved_plate","crushed_dust","bolt","double_ingot","rod","rotor"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(20, 9, 128);
    public static MIMaterial bronze = new MIMaterial("bronze", false).addItemType(new String [] { "gear","blade","ingot","ring","large_plate","plate","small_dust","nugget","dust","curved_plate","bolt","double_ingot","rod","rotor"}).addBlockType(new String [] { "block" });
    public static MIMaterial tin = new MIMaterial("tin", false).addItemType(new String [] { "gear","blade","ingot","ring","large_plate","plate","small_dust","nugget","dust","curved_plate","crushed_dust","bolt","double_ingot","rod","rotor"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(8, 9, 64);
    public static MIMaterial steel = new MIMaterial("steel", false).addItemType(new String [] { "gear","blade","ingot","ring","large_plate","plate","small_dust","nugget","dust","curved_plate","bolt","double_ingot","rod","rotor"}).addBlockType(new String [] { "block" });
    public static MIMaterial aluminum = new MIMaterial("aluminum", false).addItemType(new String [] { "ingot","large_plate","plate","small_dust","nugget","dust","curved_plate","crushed_dust","double_ingot","rod"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(6, 6, 64);
    public static MIMaterial lignite_coal = new MIMaterial("lignite_coal", false).addItemType(new String [] { "crushed_dust","lignite_coal","dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(20, 17, 128);

}