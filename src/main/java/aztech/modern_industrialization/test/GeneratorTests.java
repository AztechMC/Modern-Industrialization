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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.machines.blockentities.GeneratorMachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.GeneratorMultiblockBlockEntity;
import aztech.modern_industrialization.machines.guicomponents.GeneratorMultiblockGui;
import aztech.modern_industrialization.machines.init.MultiblockHatches;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class GeneratorTests {
    /**
     * Regression test for <a href="https://github.com/AztechMC/Modern-Industrialization/issues/1152">issue 1152</a>:
     * "EU is left in a hidden buffer when Diesel Generator runs out of fuel".
     */
    @MIGameTest
    public void testGeneratorEmptiesEuBuffer(MIGameTestHelper helper) {
        var generatorPos = new BlockPos(0, 1, 0);
        var lvDieselGeneratorBlock = BuiltInRegistries.BLOCK.getOptional(MI.id("lv_diesel_generator")).orElseThrow();
        helper.setBlock(generatorPos, lvDieselGeneratorBlock);

        var dieselGenerator = (GeneratorMachineBlockEntity) helper.getBlockEntity(generatorPos);

        try (var tx = Transaction.openRoot()) {
            long inserted = dieselGenerator.getInventory().fluidStorage.insert(MIFluids.BIODIESEL.variant(), 1, tx);
            helper.assertValueEqual(inserted, 1L, "inserted biodiesel");
            tx.commit();
        }

        // Should produce 64 per tick: 448 after 7 ticks and only 500 (2x the biodiesel value) after 8 ticks
        helper.startSequence()
                .thenIdle(7)
                .thenExecute(() -> helper.assertEnergy(generatorPos, 448, Direction.NORTH))
                .thenIdle(1)
                .thenExecute(() -> helper.assertEnergy(generatorPos, 500, Direction.NORTH))
                .thenSucceed();
    }

    private static final BlockPos CONTROLLER_POS = new BlockPos(2, 2, 1);
    private static final BlockPos ENERGY_HATCH_POS = CONTROLLER_POS.offset(0, 0, 3);
    private static final BlockPos DIESEL_FLUID_HATCH_POS = CONTROLLER_POS.offset(-1, 0, 1);
    private static final BlockPos TURBINE_FLUID_HATCH_POS = CONTROLLER_POS.offset(-1, 0, 0);

    @MIGameTest(timeoutTicks = 200)
    public void largeDieselGeneratorReportsFuel(MIGameTestHelper helper) {
        var generator = buildGenerator(helper, MultiblockMachines.LARGE_DIESEL_GENERATOR.asBlock(), DIESEL_FLUID_HATCH_POS,
                MIFluids.DIESEL.variant());

        long dieselEu = FluidFuel.getEu(MIFluids.DIESEL.asFluid());
        helper.assertTrue(dieselEu > 0, "diesel should be a fluid fuel");
        var dieselName = MIFluids.DIESEL.asFluid().getFluidType().getDescription();

        helper.startSequence()
                .thenWaitUntil(() -> {
                    var data = getGuiData(generator);
                    helper.assertTrue(data.isShapeValid(), "shape should be valid");
                    helper.assertTrue(data.currentEuGeneration() > 0, "generator should be producing EU");
                })
                .thenExecute(() -> {
                    var fuel = getGuiData(generator).fuel();
                    helper.assertTrue(fuel.isPresent(), "a fuel should be burning");
                    helper.assertValueEqual(fuel.get().name(), dieselName, "fuel name");
                    helper.assertTrue(fuel.get().isFluid(), "fuel should be a fluid");
                    helper.assertValueEqual(fuel.get().euPerUnit(), dieselEu * 2, "EU per mb of fuel");
                    helper.assertValueEqual(fuel.get().efficiency(), Optional.of(2.0), "fuel efficiency");
                })
                .thenExecute(() -> {
                    var registries = helper.getLevel().registryAccess();
                    var tag = generator.saveWithoutMetadata(registries);
                    generator.loadWithComponents(tag, registries);
                    var fuel = getGuiData(generator).fuel();
                    helper.assertTrue(fuel.isPresent(), "fuel should survive a save and load");
                    helper.assertValueEqual(fuel.get().name(), dieselName, "reloaded fuel name");
                })
                .thenSucceed();
    }

    @MIGameTest(timeoutTicks = 200)
    public void largeSteamTurbineHidesEfficiencyMultiplier(MIGameTestHelper helper) {
        var turbine = buildGenerator(helper, MultiblockMachines.LARGE_STEAM_TURBINE.asBlock(), TURBINE_FLUID_HATCH_POS,
                MIFluids.HIGH_PRESSURE_STEAM.variant());
        var steamName = MIFluids.HIGH_PRESSURE_STEAM.asFluid().getFluidType().getDescription();

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(getGuiData(turbine).currentEuGeneration() > 0, "turbine should be producing EU");
                })
                .thenExecute(() -> {
                    var fuel = getGuiData(turbine).fuel();
                    helper.assertTrue(fuel.isPresent(), "a fuel should be burning");
                    helper.assertValueEqual(fuel.get().name(), steamName, "fuel name");
                    helper.assertValueEqual(fuel.get().euPerUnit(), 8L, "EU per mb of fuel");
                    helper.assertTrue(fuel.get().efficiency().isEmpty(), "a multiplier of 1 should not be shown");
                })
                .thenSucceed();
    }

    @MIGameTest(timeoutTicks = 200)
    public void generatorForgetsFuelWhenItRunsOut(MIGameTestHelper helper) {
        var generator = buildGenerator(helper, MultiblockMachines.LARGE_DIESEL_GENERATOR.asBlock(), DIESEL_FLUID_HATCH_POS,
                MIFluids.DIESEL.variant());
        HatchBlockEntity fuelHatch = helper.getBlockEntity(DIESEL_FLUID_HATCH_POS);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(getGuiData(generator).fuel().isPresent(), "a fuel should be burning");
                })
                .thenExecute(() -> {
                    helper.setBlock(ENERGY_HATCH_POS, BuiltInRegistries.BLOCK.getOptional(MI.id("solid_titanium_machine_casing")).orElseThrow());
                })
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(getGuiData(generator).isShapeValid(), "shape should still be valid without an energy hatch");
                    try (var tx = Transaction.openRoot()) {
                        fuelHatch.getInventory().fluidStorage.extractAllSlot(MIFluids.DIESEL.variant(), Long.MAX_VALUE, tx);
                        tx.commit();
                    }
                })
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(getGuiData(generator).fuel().isEmpty(), "fuel should be cleared once the hatch is empty");
                })
                .thenSucceed();
    }

    private static GeneratorMultiblockBlockEntity buildGenerator(MIGameTestHelper helper, Block controller, BlockPos fuelHatchPos,
            FluidVariant fuel) {
        helper.setBlock(CONTROLLER_POS, controller);
        GeneratorMultiblockBlockEntity generator = helper.getBlockEntity(CONTROLLER_POS);
        generator.createShapeMatcher().buildMultiblock(helper.getLevel());

        helper.setBlock(fuelHatchPos, MultiblockHatches.HIGHLY_ADVANCED_FLUID.input().asBlock());
        helper.setBlock(ENERGY_HATCH_POS, MultiblockHatches.EV_ENERGY.output().asBlock());

        HatchBlockEntity fuelHatch = helper.getBlockEntity(fuelHatchPos);
        try (var tx = Transaction.openRoot()) {
            long inserted = fuelHatch.getInventory().fluidStorage.insert(fuel, Long.MAX_VALUE, tx);
            helper.assertTrue(inserted > 0, "fuel should have been inserted");
            tx.commit();
        }

        return generator;
    }

    private static GeneratorMultiblockGui.Data getGuiData(GeneratorMultiblockBlockEntity generator) {
        for (var component : generator.guiComponents) {
            if (component instanceof GeneratorMultiblockGui gui) {
                return gui.extractData();
            }
        }
        throw new IllegalStateException("Generator has no GeneratorMultiblockGui");
    }
}
