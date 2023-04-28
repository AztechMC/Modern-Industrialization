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

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum MIText {

    ModernIndustrialization("Modern Industrialization", "itemGroup." +
            aztech.modern_industrialization.ModernIndustrialization.ITEM_GROUP.getRecipeFolderName(),
            "modern_industrialization"),
    AbsorptionProbability("Absorption Probability"),
    AcceptsMachineHull("Change machine hull to connect higher tier cables."),
    AcceptsSteam("Accepts Steam (1 mb = 1 EU)"),
    AcceptsSteamToo("Also Accepts Steam (1 mb = 1 EU)"),
    AcceptsUpgrades("Add upgrades to increase max processing EU/t."),
    AcceptAnyFluidFuels("Consumes any Fluid Fuels (Check REI for EU/mb production)"),
    AcceptSingleFluid("Consumes %s and produces %s per mb"),
    AcceptFollowingFluid("Consumes the following for : "),
    AcceptFollowingFluidEntry("  - %s → %s / mb"),
    AcceptLowOrHighPressure("Only accepts %s and %s"),
    AcceptLowAndHighPressure("Accepts %s, %s, %s and %s"),
    AllowCreativeFligth("Allow Creative Flight"),
    BarrelStack("Can store up to %d stacks"),
    BaseDurationSeconds("%s sec"),
    BarrelStorageComponent("%s / %s (%s)"),
    BaseEuRecipe("Recipe Base : %s"),
    BaseEuTotal("Total : %s"),
    BaseEuTotalStored("Total Energy Stored : %s"),
    Blacklist("Blacklist mode enabled"),
    Both("Both"),
    BookSubtitle("Technology For Newbies"),
    CableTierEV("Extreme Voltage"),
    CableTierHV("High Voltage"),
    CableTierLV("Low Voltage"),
    CableTierMV("Medium Voltage"),
    CableTierSuperconductor("Superconductor"),
    ChanceConsumption("Consumption Chance: %s %%"),
    ChanceProduction("Production Chance: %s %%"),
    ClickToDisable("Click to disable"),
    ClickToEnable("Click to enable"),
    ClickToSwitch("Click to switch to %s"),
    ClickToToggleBlacklist("Click to enable blacklist mode"),
    ClickToToggleWhitelist("Click to enable whitelist mode"),
    ClickUrl("Click to open link"),
    ConfigCardApplied("Applied pipe settings from Config Card"),
    ConfigCardCleared("Cleared Config Card"),
    ConfigCardConfiguredCamouflage("Configured (%s camouflage)"),
    ConfigCardConfiguredItems("Configured (%d items)"),
    ConfigCardConfiguredNoItems("Configured (no items)"),
    ConfigCardHelpCamouflage1("Camouflage application:"),
    ConfigCardHelpCamouflage2("- Shift right-click a block in the world to"),
    ConfigCardHelpCamouflage3("  select it as camouflage."),
    ConfigCardHelpCamouflage4("- Right-click a pipe to update its camouflage."),
    ConfigCardHelpCamouflage5("- Shift right-click with a Wrench to remove"),
    ConfigCardHelpCamouflage6("  the camouflage."),
    ConfigCardHelpCamouflage7("- Shift mouse scroll to toggle transparent"),
    ConfigCardHelpCamouflage8("  rendering of camouflage."),
    ConfigCardHelpClear("Clear using shift right-click on air."),
    ConfigCardHelpItems1("Item pipe configuration:"),
    ConfigCardHelpItems2("- Shift right-click an item pipe connection to"),
    ConfigCardHelpItems3("  save its settings in the card."),
    ConfigCardHelpItems4("- Right-click an item pipe connection to apply"),
    ConfigCardHelpItems5("  the settings from the card."),
    ConfigCardNoCamouflageInInventory("No %s in inventory"),
    ConfigCardSet("Copied pipe settings to Config Card"),
    ConfigCardSetCamouflage("Copied %s camouflage to Config Card"),
    ConfigurableSlotCapacity("Capacity: %s. Adjust with mouse scroll."),
    ContinuousOperation("Maximum efficiency reached only under continuous operation"),
    CurrentEuRecipe("Recipe Current : %s"),
    CurseForge("[CurseForge]"),
    CustomOreGen("Customizable Ore Generation (Need Restart)"),
    DirectEnergy("Direct Energy for one capture"),
    DirectHeatByDesintegration("Direct Heat for one capture"),
    Disabled("Disabled"),
    DoubleFluidFuelEfficiency("Double efficiency! (EU x2 per fuel used)"),
    DurabilityCost("Durability Cost : %d"),
    EbfMaxEu("Allows EBF recipes up to %d EU/t"),
    EfficiencyDefaultMessage("No active recipe to overclock"),
    EfficiencyEu("Current consumption : %d EU/t"),
    EfficiencyFactor("Overclock : x%s"),
    EfficiencyMaxOverclock("Max Overclock : %d EU/t"),
    EfficiencyNuclear("Fuel Efficiency : %s %%"),
    EfficiencyTicks("Efficiency : %d / %d"),
    Empty("Empty"),
    EmptyWhitelistWarning("⚠ Empty Filter"),
    Enabled("Enabled"),
    EnergyFill("Energy: %s %%"),
    Eu("%s%s EU"),
    EuCable("%s - Max network transfer : %s"),
    EuGenerationMode("EU Generation"),
    EuInDieselGenerator("EU per mb : %d"),
    EuMaxed("%s / %s %sEU"),
    EuT("%s%s EU/t"),
    FastNeutron("Fast Neutron"),
    FastNeutronEnergy("Fast Neutron Energy"),
    FastNeutronFraction("Fast Scattered Neutron Fraction"),
    FluidAutoExtractOff("Fluid auto-extraction disabled"),
    FluidAutoExtractOn("Fluid auto-extraction enabled"),
    FluidAutoInsertOff("Fluid auto-insertion disabled"),
    FluidAutoInsertOn("Fluid auto-insertion enabled"),
    FluidFuels("Fluid Fuels"),
    FluidSlotIO("Fluid IO, Left Click to Insert or Extract"),
    FluidSlotInput("Fluid Input, Left Click to Insert or Extract"),
    FluidSlotOutput("Fluid Output, Left Click to Extract"),
    GravichestplateDisabled("Gravichestplate disabled!"),
    GravichestplateEnabled("Gravichestplate enabled!"),
    GunpowderTime("Overclock : %s"),
    GunpowderUpgrade("Double MI Steam Machines speed for 2 minutes"),
    GunpowderUpgradeMachine("Use Gunpowder to double this machine speed for 2 minutes"),
    HeatConduction("Heat Conduction %s/°kCt"),
    ItemAutoExtractOff("Item auto-extraction disabled"),
    ItemAutoExtractOn("Item auto-extraction enabled"),
    ItemAutoInsertOff("Item auto-insertion disabled"),
    ItemAutoInsertOn("Item auto-insertion enabled"),
    JetpackDisabled("Jetpack disabled!"),
    JetpackFill("Fuel: %d %%"),
    JetpackEnabled("Jetpack enabled!"),
    LargeTankTooltips("Stores %s buckets of fluid per block in the structure"),
    LockingModeOff("Lock editing disabled"),
    LockingModeOn("Lock editing enabled"),
    Locked("Locked"),
    LubricantTooltip("Right-Click on Electric Machine : consume %s mb for 1 efficiency tick"),
    MachineUpgrade("Electric Machine Upgrade : Max Overclock +%s"),
    MachineUpgradeStack("Total Stack Upgrade +%s"),
    MaxTemp("Max Temperature : %d C°"),
    MaxEuProduction("Can produce up to %s"),
    MaxEuProductionSteam("Can produce up to %s worth of %s"),
    MultiblockMaterials("Multiblock Materials"),
    MultiblockShapeInvalid("Shape Invalid"),
    MultiblockShapeValid("Shape Valid"),
    MultiblockStatusActive("Status : Active"),
    NetworkAmount("Network Amount"),
    NetworkFluidHelpClear("Shift-click to clear the network of its fluid."),
    NetworkFluidHelpSet("Click with a container to set the fluid for the network."),
    NetworkDelay("Network Delay"),
    NetworkEnergy("Network Energy"),
    NetworkFluid("Network Fluid"),
    NetworkMovedItems("Network Moved Items"),
    NetworkTier("Network Tier"),
    NetworkTransfer("Network Transfer"),
    Neutron("%d Neutron"),
    NeutronAbsorption("Neutron Absorption"),
    NeutronAbsorptionMode("Neutron Absorption"),
    NeutronFluxMode("Neutron Flux"),
    NeutronGenerationMode("Neutron Generation"),
    NeutronInteraction("Neutron Interaction"),
    NeutronProductionTemperatureEffect("Neutrons Emitted with T°"),
    NeutronTemperatureVariation("Decrease when the Temperature increase"),
    Neutrons("%d Neutrons"),
    NeutronsMultiplication("Max %s neutrons emitted"),
    NeutronsRate("%s neutrons/t"),
    NewVersion("A new version of Modern Industrialization (%s) is available on %s !"),
    NoEmi("WARNING: To play with Modern Industrialization, we STRONGLY RECOMMEND using EMI (Item and Recipe Viewer). Otherwise you won't be able to view the recipes in-game, and you will not be able to deal with conflicting machine recipes. Just Enough Items and Roughly Enough Items are also supported. This message can be disabled in the config."),
    NoToolRequired("No Tool Required"),
    NuclearFuelEfficiencyTooltip("%s produced for %s of fuel consumed"),
    NuclearFuelMode("Nuclear Fuel"),
    OreGenerationTooltipVeinFrequency("%d veins per chunk"),
    OreGenerationTooltipVeinSize("%d ores per vein"),
    OreGenerationTooltipY("Y level %d to %d"),
    OreNotGenerated("Not Generated but can be obtained with the Quarry"),
    OverclockMachine("Use %s to %fx this machine speed for %d ticks"),
    PipeConnectionIn("IN"),
    PipeConnectionIO("I/O"),
    PipeConnectionOut("OUT"),
    PipeConnectionHelp("Click/Shift-Click to change"),
    PipeConnectionTooltipInsertOnly("Insert only"),
    PipeConnectionTooltipInsertOrExtract("Insert or Extract"),
    PipeConnectionTooltipExtractOnly("Extract only"),
    PriorityExtract("Extract priority: %d"),
    PriorityExtractHelp("Lower priorities first, only into higher priorities."),
    PriorityInsert("Insert priority: %d"),
    PriorityInsertHelp("Higher priorities first."),
    PriorityTransfer("Transfer priority: %d"),
    PriorityTransferHelp("Pipes will interact with higher priorities first."),
    PutMotorToUpgrade("Put any Motor here to improve Item Pipe Speed"),
    NotConsumed("Not consumed"),
    Progress("Progress : %s"),
    RemAbsorption("Remaining Absorption : %d / %d "),
    RequiresBiome("Requires biome: %s"),
    RequiresBlockBelow("Requires block below machine: %s"),
    RequiresBlockBehind("Requires block behind machine: %s"),
    RequiresDimension("Requires dimension: %s"),
    RequiresSteelHatch0("Steam Only:"),
    RequiresSteelHatch1("- Requires at least one Steel Hatch (Any)"),
    RequiresUpgrades("Requires at least +%s of upgrades."),
    ScatteringProbability("Scattering Probability"),
    SecondsLeft("Seconds left: %s"),
    ShapeSelectionDescription("Click to open shape selection panel."),
    ShapeSelectionTitle("Select Multiblock Shape"),
    ShapeTextDepth("Depth: %d"),
    ShapeTextExtreme("Extreme"),
    ShapeTextHeight("Height: %d"),
    ShapeTextLarge("Large"),
    ShapeTextMedium("Medium"),
    ShapeTextSmall("Small"),
    ShapeTextWidth("Width: %d"),
    ShiftClickToLockAll("Shift-Click to lock all"),
    ShiftClickToUnlockAll("Shift-Click to unlock all"),
    ShiftClickToSwitch("Shift-Click to switch to %s"),
    SingleNeutronCapture("Single Neutron Capture"),
    SteamDrillFuelHelp("2) Place fuel inside the drill (right click)."),
    SteamDrillProfit("3) Enjoy 3x3 Silk Touch."),
    SteamDrillToggle("4) Toggle Silk Touch with shift-right click."),
    SteamDrillWaterHelp("1) Right click still or flowing water to fill."),
    Temperature("Temperature : %d °C"),
    TemperatureMode("Temperature"),
    ThermalEfficiency("Thermal Efficiency %s %%"),
    ThermalInteraction("Thermal Interaction"),
    ThermalNeutron("Thermal Neutron"),
    ThermalNeutronFraction("Thermal Scattered Neutron Fraction"),
    ToolSwitchedFortune("Fortune Mode enabled!"),
    ToolSwitchedNoSilkTouch("Silk Touch Mode disabled!"),
    ToolSwitchedSilkTouch("Silk Touch Mode enabled!"),
    TooltipSpeedUpgrade("Item Pipe Speed Upgrade: +%d items / 3s."),
    TooltipsShiftRequired("Press [Shift] for info"),
    TransparentCamouflageDisabled("Disabled transparent camouflage rendering"),
    TransparentCamouflageEnabled("Enabled transparent camouflage rendering"),
    Unlocked("Unlocked"),
    WaterPercent("Water: %s %%"),
    Whitelist("Whitelist mode enabled");

    private final String root;
    private final String englishText;
    private final List<String> additionalTranslationsKey;

    MIText(String englishText, String... additionalTranslationKey) {
        this.root = "text." + aztech.modern_industrialization.ModernIndustrialization.MOD_ID;
        this.englishText = englishText;
        this.additionalTranslationsKey = List.of(additionalTranslationKey);
    }

    public List<String> getAdditionalTranslationKey() {
        return this.additionalTranslationsKey;
    }

    public String getEnglishText() {
        return englishText;
    }

    public String getTranslationKey() {
        return this.root + '.' + name();
    }

    public MutableComponent text() {
        return Component.translatable(getTranslationKey());
    }

    public MutableComponent text(Object... args) {
        return Component.translatable(getTranslationKey(), args);
    }

}
