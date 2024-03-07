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
package aztech.modern_industrialization.compat.jade.server;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.fluid.JadeFluidObject;

@WailaPlugin
public class MIJadeCommonPlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new OverclockComponentProvider(), MachineBlockEntity.class);

        registration.registerBlockDataProvider(new PipeDataProvider(), PipeBlockEntity.class);

        registration.registerEnergyStorage(new MachineComponentProvider.Energy(), MachineBlockEntity.class);
        registration.registerFluidStorage(new MachineComponentProvider.Fluids(), MachineBlockEntity.class);
        registration.registerItemStorage(new MachineComponentProvider.Items(), MachineBlockEntity.class);
        registration.registerProgress(new MachineComponentProvider.Progress(), MachineBlockEntity.class);
    }

    public static JadeFluidObject fluidStack(FluidVariant variant, long amount) {
        return JadeFluidObject.of(variant.getFluid(), amount, variant.copyNbt());
    }
}
