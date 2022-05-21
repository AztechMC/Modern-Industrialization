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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum MIText {

    ModernIndustrialization("Modern Industrialization", "itemGroup." +
            aztech.modern_industrialization.ModernIndustrialization.ITEM_GROUP.getRecipeFolderName(),
            "modern_industrialization"),
    AbsorptionProbability("Absorption Probability"),
    AdditionalTips("Pro-tips:"),
    AdditionalTipsShift("Pro-tips: [Press Shift]"),
    BarrelStack("Can store up to %d stacks"),
    BaseDurationSeconds("%s sec"),
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
    ClickToDisable("Click to disable"),
    ClickToEnable("Click to enable"),
    ClickToSwitch("Click to switch to %s"),
    ClickToToggleBlacklist("Click to enable blacklist mode"),
    ClickToToggleWhitelist("Click to enable whitelist mode"),
    ClickUrl("Click to open link"),
    ConfigurableSlotCapacity("Capacity: %s. Adjust with mouse scroll."),
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
    Eu("%s %sEU"),
    EuCable("%s - Max network transfer : %s"),
    EuGenerationMode("EU Generation"),
    EuInDieselGenerator("EU per mb : %d"),
    EuMaxed("%s / %s %sEU"),
    EuT("%s %sEU/t"),
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
    GunpowderUpgrade("MI : double Steam Machines speed for 2 minutes"),
    HeatConduction("Heat Conduction %s / °Ct"),
    ItemAutoExtractOff("Item auto-extraction disabled"),
    ItemAutoExtractOn("Item auto-extraction enabled"),
    ItemAutoInsertOff("Item auto-insertion disabled"),
    ItemAutoInsertOn("Item auto-insertion enabled"),
    JetpackDisabled("Jetpack disabled!"),
    JetpackFill("Fuel: %d %%"),
    JetpackEnabled("Jetpack enabled!"),
    LockingModeOff("Lock editing disabled"),
    LockingModeOn("Lock editing enabled"),
    LubricantTooltip("Right-Click on Electric Machine : consume %s mb for 1 efficiency tick"),
    MachineUpgrade("Electric Machine Upgrade : Max Overclock +%d EU/t"),
    MaxTemp("Max Temperature : %d C°"),
    MultiblockMaterials("Multiblock Materials"),
    MultiblockShapeInvalid("Shape Invalid"),
    MultiblockShapeValid("Shape Valid"),
    MultiblockStatusActive("Status : Active"),
    NetworkFluidHelpClear("Shift-click to clear the network of its fluid."),
    NetworkFluidHelpSet("Click with a container to set the fluid for the network."),
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
    NoToolRequired("No Tool Required"),
    NuclearFuelEfficiencyTooltip("%s produced for %s of fuel consumed"),
    NuclearFuelMode("Nuclear Fuel"),
    OreGenerationTooltipVeinFrequency("%d veins per chunk"),
    OreGenerationTooltipVeinSize("%d ores per vein"),
    OreGenerationTooltipY("Y level %d to %d"),
    PipeConnectionIn("IN"),
    PipeConnectionIO("I/O"),
    PipeConnectionOut("OUT"),
    PipeConnectionHelp("Click to change"),
    PipeConnectionTooltipInsertOnly("Insert only"),
    PipeConnectionTooltipInsertOrExtract("Insert or Extract"),
    PipeConnectionTooltipExtractOnly("Extract only"),
    PriorityExtract("Extract priority: %d"),
    PriorityExtractHelp("Lower priorities first, only into higher priorities."),
    PriorityInsert("Insert priority: %d"),
    PriorityInsertHelp("Higher priorities first."),
    PriorityTransfer("Transfer priority: %d"),
    PriorityTransferHelp("Pipes will interact with higher priorities first."),
    Probability("Probability: %s %%"),
    ProbabilityZero("Not consumed"),
    Progress("Progress : %s"),
    RemAbsorption("Remaining Absorption : %d / %d "),
    ScatteringProbability("Scattering Probability"),
    SecondsLeft("Seconds left: %s"),
    SingleNeutronCapture("Single Neutron Capture"),
    SteamDrillFuelHelp("2) Place fuel inside the drill (right click)."),
    SteamDrillProfit("3) Enjoy 3x3 Silk Touch."),
    SteamDrillWaterHelp("1) Right click still or flowing water to fill."),
    Temperature("Temperature : %d °C"),
    TemperatureMode("Temperature"),
    ThermalEfficiency("Thermal Efficiency %s %%"),
    ThermalInteraction("Thermal Interaction"),
    ThermalNeutron("Thermal Neutron"),
    ThermalNeutronFraction("Thermal Scattered Neutron Fraction"),
    ToolSwitchedFortune("Fortune Mode enabled!"),
    ToolSwitchedSilkTouch("Silk Touch Mode enabled!"),
    TooltipSpeedUpgrade("Item Pipe Speed Upgrade: +%d items / 3s."),
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
        return new TranslatableComponent(getTranslationKey());
    }

    public MutableComponent text(Object... args) {
        return new TranslatableComponent(getTranslationKey(), args);
    }

}
