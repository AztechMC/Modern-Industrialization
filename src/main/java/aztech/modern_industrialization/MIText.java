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
    ModernIndustrialization("Modern Industrialization"),
    AbsorptionProbability("Absorption Probability"),
    AcceptAnyFluidFuels("Consumes any Fluid Fuel (Check REI for EU/mb production)"),
    AcceptAnyItemFuels("Consumes any Furnace Fuel (Check Item tooltip for EU production)"),
    AcceptFollowingFluidEntry("  - %s → %s / mb"),
    AcceptFollowingItemEntry("  - %s → %s each"),
    AcceptLowAndHighPressure("Accepts %s, %s, %s and %s"),
    AcceptLowOrHighPressure("Only accepts %s and %s"),
    AcceptSingleFluid("Consumes %s and produces %s per mb"),
    AcceptSingleItem("Consumes %s and produces %s per item"),
    AcceptsHatches("Accepts hatches:"),
    AcceptsMachineHull("Change machine hull to connect higher tier cables."),
    AcceptsOverdriveModule("Accepts an overdrive module."),
    AcceptsRedstoneControlModule("Accepts a redstone control module."),
    AcceptsSteam("Accepts Steam (1 mb = 1 EU)"),
    AcceptsSteamToo("Also Accepts Steam (1 mb = 1 EU)"),
    AcceptsUpgrades("Add upgrades to increase max processing EU/t."),
    Activated("Activated"),
    AttributeInfiniteDamage("Infinite Damage"),
    AttributeQuantumArmor("Quantum Armor"),
    BarrelStack("Can store up to %d stacks"),
    BarrelStorageComponent("%s / %s (%s)"),
    BaseDurationSeconds("%s sec"),
    BaseEuRecipe("Recipe Base: %s"),
    BaseEuTotal("Total: %s"),
    BaseEuTotalStored("Total Energy Stored: %s"),
    BatteryInStorageUnit("Insert in a Portable Storage Unit to add %s of capacity"),
    Blacklist("Blacklist mode enabled"),
    BookSubtitle("Technology For Newbies"),
    Both("Both"),
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
    ConfigCardHelpCamouflage2("- Press %s + %s on a block in the world to select it as camouflage."),
    ConfigCardHelpCamouflage3("- Use %s on a pipe to update its camouflage."),
    ConfigCardHelpCamouflage4("- Press %s + %s with a Wrench to remove the camouflage."),
    ConfigCardHelpCamouflage5("- Use %s + %s to toggle transparent rendering of camouflage."),
    ConfigCardHelpClear("Clear using %s + %s on air."),
    ConfigCardHelpItems1("Item pipe configuration:"),
    ConfigCardHelpItems2("- Press %s + %s on an item pipe connection to save its settings in the card."),
    ConfigCardHelpItems3("- Use %s on an item pipe connection to apply the settings from the card."),
    ConfigCardNoCamouflageInInventory("No %s in inventory"),
    ConfigCardSet("Copied pipe settings to Config Card"),
    ConfigCardSetCamouflage("Copied %s camouflage to Config Card"),
    ConfigurableSlotCapacity("Capacity: %s. Adjust with mouse scroll."),
    ConsumesTheFollowing("Consumes the following for: "),
    ContinuousOperation("Maximum efficiency reached only under continuous operation"),
    CurrentEuRecipe("Recipe Current: %s"),
    CurseForge("[CurseForge]"),
    CustomOreGen("Customizable Ore Generation (Need Restart)"),
    Deactivated("Deactivated"),
    DieselToolFuelHelp1("- Press %s on a tank to fill or empty. Any fluid fuel can be used."),
    DieselToolFuelHelp2("- Speed of the tool is determined by the EU/mb of the fuel."),
    DieselToolToggle("- Press %s + %s to swap between Fortune and Silk Touch."),
    DirectEnergy("Direct Energy for one capture"),
    DirectHeatByDesintegration("Direct Heat for one capture"),
    Disabled("Disabled"),
    DoubleFluidFuelEfficiency("Double efficiency! (EU x2 per fuel used)"),
    DurabilityCost("Durability Cost: %d"),
    EbfMaxEu("Allows EBF recipes up to %d EU/t"),
    Efficiency("Efficiency"),
    EfficiencyDefaultMessage("No active recipe to overclock"),
    EfficiencyEu("Current consumption: %d EU/t"),
    EfficiencyFactor("Overclock: x%s"),
    EfficiencyMaxOverclock("Max Overclock: %d EU/t"),
    EfficiencyNuclear("Fuel Efficiency: %s %%"),
    EfficiencyTicks("Efficiency: %d / %d"),
    Empty("Empty"),
    EmptyWhitelistWarning("⚠ Empty Filter"),
    Enabled("Enabled"),
    EnergyFill("Energy: %s %%"),
    EnergyInputHatch("Energy Input Hatch"),
    EnergyOutputHatch("Energy Output Hatch"),
    EnergyStored("Energy Stored: %s"),
    Eu("%s%s EU"),
    EuCable("%s - Max network transfer: %s"),
    EuGenerationMode("EU Generation"),
    EuInDieselGenerator("EU per mb: %d"),
    EuMaxed("%s / %s %sEU"),
    EuT("%s%s EU/t"),
    EuTOverclocked("Overclock × %s | %s EU/t"),
    FastNeutron("Fast Neutron"),
    FastNeutronEnergy("Fast Neutron Energy"),
    FastNeutronFraction("Fast Scattered Neutron Fraction"),
    FluidAutoExtractOff("Fluid auto-eject disabled"),
    FluidAutoExtractOn("Fluid auto-eject enabled"),
    FluidAutoInsertOff("Fluid auto-pull disabled"),
    FluidAutoInsertOn("Fluid auto-pull enabled"),
    FluidFuels("Fluid Fuels"),
    FluidInputHatch("Fluid Input Hatch"),
    FluidOutputHatch("Fluid Output Hatch"),
    FluidSlotIO("Fluid IO, Left Click to Insert or Extract"),
    FluidSlotInput("Fluid Input, Left Click to Insert or Extract"),
    FluidSlotOutput("Fluid Output, Left Click to Extract"),
    GeneratedResources("MI Generated Resources"),
    GeneratedResourcesDescription("Resources from the modern_industrialization/generated_resources folder."),
    GravichestplateDisabled("Gravichestplate disabled!"),
    GravichestplateEnabled("Gravichestplate enabled!"),
    GunpowderTime("Overclock: %s"),
    GunpowderUpgrade("Double MI Steam Machines speed for 2 minutes"),
    GunpowderUpgradeMachine("Use Gunpowder to double this machine speed for 2 minutes"),
    HasBetterYieldAssemblerRecipe("Has an Assembler recipe with better yield."),
    HeatConduction("Heat Conduction %s/°kCt"),
    ItemAutoExtractOff("Item auto-eject disabled"),
    ItemAutoExtractOn("Item auto-eject enabled"),
    ItemAutoInsertOff("Item auto-pull disabled"),
    ItemAutoInsertOn("Item auto-pull enabled"),
    ItemInputHatch("Item Input Hatch"),
    ItemOutputHatch("Item Output Hatch"),
    JetpackDisabled("Jetpack disabled!"),
    JetpackEnabled("Jetpack enabled!"),
    JetpackFill("Fuel: %d %%"),
    KeyMouseScroll("Mouse Scroll"),
    LargeTankTooltips("Stores %s buckets of fluid per block in the structure"),
    Locked("Locked"),
    LockingModeOff("Lock editing disabled"),
    LockingModeOn("Lock editing enabled"),
    LubricantTooltip("%s on Electric Machine: consume %s mb for 1 efficiency tick"),
    MachineCasingVoltage("Allows machines to accept %s power"),
    MachineUpgrade("Electric Machine Upgrade: Max Overclock +%s"),
    MachineUpgradeStack("Total Stack Upgrade +%s"),
    MaxEuProduction("Can produce up to %s"),
    MaxEuProductionSteam("Can produce up to %s worth of %s"),
    MaxTemp("Max Temperature: %d °C"),
    MiningArea("Mining Area: %s"),
    MiningArea1x1("1x1"),
    MiningArea3x3("3x3"),
    MiningMode("Mining Mode: %s"),
    MultiblockMaterials("Multiblock Materials"),
    MultiblockShapeInvalid("Shape Invalid"),
    MultiblockShapeValid("Shape Valid"),
    MultiblockStatusActive("Status: Active"),
    NetworkAmount("Network Amount"),
    NetworkDelay("Network Delay"),
    NetworkEnergy("Network Energy"),
    NetworkFluid("Network Fluid"),
    NetworkFluidHelpClear("Shift-click to clear the network of its fluid."),
    NetworkFluidHelpSet("Click with a container to set the fluid for the network."),
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
    NoLargeTank("Not linked to a Large Tank"),
    NoToolRequired("No Tool Required"),
    NotConsumed("Not consumed"),
    NuclearFuelEfficiencyTooltip("%s produced for %s of fuel consumed"),
    NuclearFuelMode("Nuclear Fuel"),
    OreGenerationTooltipVeinFrequency("%d veins per chunk"),
    OreGenerationTooltipVeinSize("%d ores per vein"),
    OreGenerationTooltipY("Y level %d to %d"),
    OreNotGenerated("Not Generated but can be obtained with the Quarry"),
    OverclockMachine("Use %s to %fx this machine speed for %d ticks"),
    OverdriveModuleDescription1("Prevents electric machine efficiency from going down, at the cost of electricity."),
    OverdriveModuleDescription2("The recipe will be locked until the module is removed or the machine runs out of power."),
    PipeConnectionHelp("Click/Shift-Click to change"),
    PipeConnectionIO("I/O"),
    PipeConnectionIn("IN"),
    PipeConnectionOut("OUT"),
    PipeConnectionTooltipExtractOnly("Extract only"),
    PipeConnectionTooltipInsertOnly("Insert only"),
    PipeConnectionTooltipInsertOrExtract("Insert or Extract"),
    PipeHelp1("Can be instantly retrieved by pressing %s + %s with any Wrench."),
    PipeHelp2("Use %s + %s to connect directly the pipe to the target block."),
    PriorityExtract("Extract priority: %d"),
    PriorityInsert("Insert priority: %d"),
    PriorityItemHelp(
            "Items only move from lower extract (%s) priorities to equal or higher insert (%s) priorities. Greater differences take precedence."),
    PriorityNotApplicable("Not applicable in %s mode (%s)."),
    PriorityTransfer("Transfer priority: %d"),
    PriorityTransferHelp("Pipes will interact with higher priorities first."),
    Progress("Progress: %s"),
    PutMotorToUpgrade("Put any Motor here to improve Item Pipe Speed"),
    RedstoneControlModuleHelp("Insert in a machine to enable redstone control."),
    RedstoneControlModuleMachineRequires("Machine requires: %s"),
    RemAbsorption("Remaining Absorption: %d / %d "),
    RequiresBiome("Requires biome: %s"),
    RequiresBlockBehind("Requires block behind machine: %s"),
    RequiresBlockBelow("Requires block below machine: %s"),
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
    ShiftClickToSwitch("Shift-Click to switch to %s"),
    ShiftClickToUnlockAll("Shift-Click to unlock all"),
    SignalHigh("High Signal"),
    SignalLow("Low Signal"),
    SilkTouchState("Silk Touch: %s"),
    SingleNeutronCapture("Single Neutron Capture"),
    SteamDrillFuelHelp("- Place fuel inside the drill using %s."),
    SteamDrillProfit("- Press %s to toggle 3x3 mining."),
    SteamDrillToggle("- Toggle Silk Touch with %s + %s."),
    SteamDrillWaterHelp("- Press %s on still or flowing water to fill."),
    SuperconductorPowerOnly(
            "Can only connect to Superconductor cables and machines. Must be directly connected to a machine on at least one end of the tunnel."),
    Temperature("Temperature: %d °C"),
    TemperatureMode("Temperature"),
    ThermalEfficiency("Thermal Efficiency %s %%"),
    ThermalInteraction("Thermal Interaction"),
    ThermalNeutron("Thermal Neutron"),
    ThermalNeutronFraction("Thermal Scattered Neutron Fraction"),
    ToolConfiguration("Tool configuration:"),
    ToolSwitched3x3("3x3 Mining enabled!"),
    ToolSwitchedFortune("Fortune Mode enabled!"),
    ToolSwitchedNo3x3("3x3 Mining disabled!"),
    ToolSwitchedNoSilkTouch("Silk Touch Mode disabled!"),
    ToolSwitchedSilkTouch("Silk Touch Mode enabled!"),
    TooltipSpeedUpgrade("Item Pipe Speed Upgrade: +%d items / 3s."),
    TooltipSpeedUpgradeStack("Total Stack Upgrade +%d items / 3s."),
    TooltipsShiftRequired("Press [Shift] for info"),
    TransparentCamouflageDisabled("Disabled transparent camouflage rendering"),
    TransparentCamouflageEnabled("Enabled transparent camouflage rendering"),
    Unlocked("Unlocked"),
    UseItemToChange("%s to change."),
    WaterPercent("Water: %s %%"),
    Whitelist("Whitelist mode enabled");

    private final String root;
    private final String englishText;
    private final List<String> additionalTranslationsKey;

    MIText(String englishText, String... additionalTranslationKey) {
        this.root = "text." + MI.ID;
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
