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

package aztech.modern_industrialization.test;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.machines.blockentities.multiblocks.SteamBoilerMultiblockBlockEntity;
import aztech.modern_industrialization.machines.guicomponents.SteamBoilerMultiblockGui;
import aztech.modern_industrialization.machines.init.MachineDefinition;
import aztech.modern_industrialization.machines.init.MultiblockHatches;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluids;

public class SteamBoilerTests {
    @MIGameTest(timeoutTicks = 200)
    public void largeSteamBoilerReportsSteamAndFuel(MIGameTestHelper helper) {
        testBoilerInfo(helper, MultiblockMachines.LARGE_STEAM_BOILER, FluidVariant.of(Fluids.WATER), MIFluids.STEAM, 1, 256);
    }

    @MIGameTest(timeoutTicks = 200)
    public void highPressureLargeSteamBoilerReportsSteamAndFuel(MIGameTestHelper helper) {
        testBoilerInfo(helper, MultiblockMachines.HIGH_PRESSURE_LARGE_STEAM_BOILER, MIFluids.HIGH_PRESSURE_WATER.variant(),
                MIFluids.HIGH_PRESSURE_STEAM, 8, 256);
    }

    private static void testBoilerInfo(MIGameTestHelper helper, MachineDefinition<SteamBoilerMultiblockBlockEntity> boilerDefinition,
            FluidVariant water, FluidDefinition steam, int euPerSteamMb, long expectedMaxSteam) {
        var controllerPos = new BlockPos(2, 1, 1);
        helper.setBlock(controllerPos, boilerDefinition.asBlock());
        SteamBoilerMultiblockBlockEntity boiler = helper.getBlockEntity(controllerPos);
        boiler.createShapeMatcher().buildMultiblock(helper.getLevel());

        var waterPos = controllerPos.offset(-1, -1, 0);
        var fuelPos = controllerPos.offset(0, -1, 0);
        var steamPos = controllerPos.offset(1, -1, 0);
        helper.setBlock(waterPos, MultiblockHatches.HIGHLY_ADVANCED_FLUID.input().asBlock());
        helper.setBlock(fuelPos, MultiblockHatches.HIGHLY_ADVANCED_FLUID.input().asBlock());
        helper.setBlock(steamPos, MultiblockHatches.HIGHLY_ADVANCED_FLUID.output().asBlock());
        insertFluid(helper, waterPos, water);
        insertFluid(helper, fuelPos, MIFluids.DIESEL.variant());

        long dieselEu = FluidFuel.getEu(MIFluids.DIESEL.asFluid());
        helper.assertTrue(dieselEu > 0, "diesel should be a fluid fuel");
        var dieselName = MIFluids.DIESEL.asFluid().getFluidType().getDescription();

        long[] steamBefore = new long[1];

        helper.startSequence()
                .thenWaitUntil(() -> {
                    var data = getGuiData(boiler);
                    helper.assertTrue(data.isShapeValid(), "shape should be valid");
                    helper.assertTrue(data.steamProduction() > 0, "boiler should be producing steam");
                })
                .thenExecute(() -> {
                    var data = getGuiData(boiler);
                    helper.assertValueEqual(data.maxSteamProduction(), expectedMaxSteam, "max steam production");

                    helper.assertTrue(data.fuel().isPresent(), "a fuel should be burning");
                    var fuel = data.fuel().get();
                    helper.assertValueEqual(fuel.name(), dieselName, "fuel name");
                    helper.assertTrue(fuel.isFluid(), "fuel should be a fluid");
                    helper.assertValueEqual(fuel.euPerUnit(), dieselEu, "EU per mb of fuel");
                    helper.assertValueEqual(data.euPerSteamMb(), euPerSteamMb, "EU per mb of steam");
                    helper.assertTrue(fuel.efficiency().isEmpty(), "a multiplier of 1 should not be shown");

                    steamBefore[0] = getSteamAmount(helper, steamPos, steam);
                })
                .thenIdle(1)
                .thenExecute(() -> {
                    long produced = getSteamAmount(helper, steamPos, steam) - steamBefore[0];
                    helper.assertTrue(produced > 0, "steam should have been produced");
                    helper.assertValueEqual(getGuiData(boiler).steamProduction(), produced, "reported steam production");
                })
                .thenExecute(() -> {
                    var registries = helper.getLevel().registryAccess();
                    var tag = boiler.saveWithoutMetadata(registries);
                    boiler.loadWithComponents(tag, registries);
                    var fuel = getGuiData(boiler).fuel();
                    helper.assertTrue(fuel.isPresent(), "fuel should survive a save and load");
                    helper.assertValueEqual(fuel.get().name(), dieselName, "reloaded fuel name");
                })
                .thenSucceed();
    }

    private static SteamBoilerMultiblockGui.Data getGuiData(SteamBoilerMultiblockBlockEntity boiler) {
        for (var component : boiler.guiComponents) {
            if (component instanceof SteamBoilerMultiblockGui gui) {
                return gui.extractData();
            }
        }
        throw new IllegalStateException("Boiler has no SteamBoilerMultiblockGui");
    }

    private static void insertFluid(MIGameTestHelper helper, BlockPos pos, FluidVariant fluid) {
        HatchBlockEntity hatch = helper.getBlockEntity(pos);
        try (var tx = Transaction.openRoot()) {
            long inserted = hatch.getInventory().fluidStorage.insert(fluid, Long.MAX_VALUE, tx);
            helper.assertTrue(inserted > 0, "fluid should have been inserted");
            tx.commit();
        }
    }

    private static long getSteamAmount(MIGameTestHelper helper, BlockPos pos, FluidDefinition steam) {
        HatchBlockEntity hatch = helper.getBlockEntity(pos);
        long amount = 0;
        for (var stack : hatch.getInventory().getFluidStacks()) {
            if (stack.getResource().getFluid() == steam.asFluid()) {
                amount += stack.getAmount();
            }
        }
        return amount;
    }
}
