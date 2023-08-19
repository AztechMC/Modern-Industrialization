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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.machines.blockentities.multiblocks.LargeTankMultiblockBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockTankBER;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class MachineRendering {
    public static void init() {
        for (var casing : MachineCasings.registeredCasings.values()) {
            casing.model = new MachineCasingModel(casing.name);
        }

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new MachineModelProvider());
        ModelLoadingRegistry.INSTANCE.registerModelProvider(new MachineModelProvider());

        for (var blockDef : MIBlock.BLOCKS.values()) {
            if (blockDef.asBlock() instanceof MachineBlock machine) {
                registerBer(machine);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void registerBer(MachineBlock machine) {
        var blockEntity = machine.getBlockEntityInstance();
        BlockEntityType type = blockEntity.getType();

        if (blockEntity instanceof LargeTankMultiblockBlockEntity) {
            BlockEntityRenderers.register(type, MultiblockTankBER::new);
        } else if (blockEntity instanceof MultiblockMachineBlockEntity) {
            BlockEntityRenderers.register(type, MultiblockMachineBER::new);
        } else {
            BlockEntityRenderers.register(type, c -> new MachineBlockEntityRenderer(c));
        }
    }

    private MachineRendering() {
    }
}
