package aztech.modern_industrialization.material;

import aztech.modern_industrialization.MIConfig;

public class MIMaterials {

    public static MIMaterial aluminum = new MIMaterial("aluminum", false).addItemType(new String [] { "blade","bolt","crushed_dust","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust","wire"}).addBlockType(new String [] { "block" });
    public static MIMaterial antimony = new MIMaterial("antimony", false).addItemType(new String [] { "crushed_dust","dust","ingot","nugget","tiny_dust"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(4, 6, 64, MIConfig.getConfig().ores.generateAntimony);
    public static MIMaterial battery_alloy = new MIMaterial("battery_alloy", false).addItemType(new String [] { "curved_plate","double_ingot","dust","ingot","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial bauxite = new MIMaterial("bauxite", false).addItemType(new String [] { "crushed_dust","dust","tiny_dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(8, 7, 32, MIConfig.getConfig().ores.generateBauxite);
    public static MIMaterial beryllium = new MIMaterial("beryllium", false).addItemType(new String [] { "blade","bolt","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial beryllium_hydroxide = new MIMaterial("beryllium_hydroxide", false).addItemType(new String [] { "dust"});
    public static MIMaterial beryllium_oxide = new MIMaterial("beryllium_oxide", false).addItemType(new String [] { "dust"});
    public static MIMaterial bronze = new MIMaterial("bronze", false).addItemType(new String [] { "blade","bolt","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial chrome = new MIMaterial("chrome", false).addItemType(new String [] { "crushed_dust","double_ingot","dust","ingot","large_plate","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial coal = new MIMaterial("coal", true).addItemType(new String [] { "crushed_dust","dust","tiny_dust"});
    public static MIMaterial copper = new MIMaterial("copper", false).addItemType(new String [] { "blade","bolt","crushed_dust","curved_plate","double_ingot","dust","fine_wire","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust","wire"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(30, 9, 128, MIConfig.getConfig().ores.generateCopper);
    public static MIMaterial cupronickel = new MIMaterial("cupronickel", false).addItemType(new String [] { "double_ingot","dust","ingot","nugget","plate","tiny_dust","wire"}).addBlockType(new String [] { "block","coil" });
    public static MIMaterial electrum = new MIMaterial("electrum", false).addItemType(new String [] { "curved_plate","double_ingot","dust","fine_wire","ingot","large_plate","nugget","plate","tiny_dust","wire"}).addBlockType(new String [] { "block" });
    public static MIMaterial emerald = new MIMaterial("emerald", false).addItemType(new String [] { "crushed_dust","dust","tiny_dust"});
    public static MIMaterial fluorite = new MIMaterial("fluorite", false).addItemType(new String [] { "crushed_dust","dust","tiny_dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(0, 0, 64, MIConfig.getConfig().ores.generateFluorite);
    public static MIMaterial gold = new MIMaterial("gold", true).addItemType(new String [] { "crushed_dust","curved_plate","double_ingot","dust","large_plate","plate","tiny_dust"});
    public static MIMaterial invar = new MIMaterial("invar", false).addItemType(new String [] { "double_ingot","dust","gear","ingot","large_plate","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial iron = new MIMaterial("iron", true).addItemType(new String [] { "crushed_dust","curved_plate","double_ingot","dust","large_plate","plate","tiny_dust"});
    public static MIMaterial lead = new MIMaterial("lead", false).addItemType(new String [] { "crushed_dust","curved_plate","double_ingot","dust","ingot","large_plate","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(4, 8, 64, MIConfig.getConfig().ores.generateLead);
    public static MIMaterial lignite_coal = new MIMaterial("lignite_coal", false).addItemType(new String [] { "crushed_dust","dust","lignite_coal","tiny_dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(10, 17, 128, MIConfig.getConfig().ores.generateLigniteCoal);
    public static MIMaterial manganese = new MIMaterial("manganese", false).addItemType(new String [] { "crushed_dust","dust","ingot","nugget","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial nickel = new MIMaterial("nickel", false).addItemType(new String [] { "crushed_dust","curved_plate","double_ingot","dust","ingot","large_plate","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(7, 6, 64, MIConfig.getConfig().ores.generateNickel);
    public static MIMaterial quartz = new MIMaterial("quartz", false).addItemType(new String [] { "crushed_dust","dust","tiny_dust"});
    public static MIMaterial redstone = new MIMaterial("redstone", false).addItemType(new String [] { "crushed_dust","tiny_dust"});
    public static MIMaterial salt = new MIMaterial("salt", false).addItemType(new String [] { "crushed_dust","dust","tiny_dust"}).addBlockType(new String [] { "ore" }).setupOreGenerator(2, 7, 32, MIConfig.getConfig().ores.generateSalt);
    public static MIMaterial silicon = new MIMaterial("silicon", false).addItemType(new String [] { "double_ingot","dust","ingot","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial silver = new MIMaterial("silver", false).addItemType(new String [] { "crushed_dust","curved_plate","double_ingot","dust","ingot","large_plate","nugget","plate","tiny_dust"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(4, 6, 64, MIConfig.getConfig().ores.generateSilver);
    public static MIMaterial sodium = new MIMaterial("sodium", false).addItemType(new String [] { "dust","ingot","nugget","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial sodium_fluoroberyllate = new MIMaterial("sodium_fluoroberyllate", false).addItemType(new String [] { "crushed_dust"});
    public static MIMaterial sodium_fluorosilicate = new MIMaterial("sodium_fluorosilicate", false).addItemType(new String [] { "dust","tiny_dust"});
    public static MIMaterial stainless_steel = new MIMaterial("stainless_steel", false).addItemType(new String [] { "blade","bolt","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial steel = new MIMaterial("steel", false).addItemType(new String [] { "blade","bolt","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust"}).addBlockType(new String [] { "block" });
    public static MIMaterial tin = new MIMaterial("tin", false).addItemType(new String [] { "blade","bolt","crushed_dust","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust","wire"}).addBlockType(new String [] { "block","ore" }).setupOreGenerator(8, 9, 64, MIConfig.getConfig().ores.generateTin);
    public static MIMaterial titanium = new MIMaterial("titanium", false).addItemType(new String [] { "blade","bolt","curved_plate","double_ingot","dust","gear","ingot","large_plate","nugget","plate","ring","rod","rotor","tiny_dust"}).addBlockType(new String [] { "block" });
}