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
package aztech.modern_industrialization.compat.waila.server;

import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;

public class MIWailaServerPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar r) {
        r.addBlockData(new MachineComponentProvider(), MachineBlockEntity.class);
        r.addBlockData(new TankFluidProvider(), AbstractTankBlockEntity.class);
        r.addBlockData(new StorageItemProvider(), AbstractStorageBlockEntity.class);

        r.addBlockData(new PipeDataProvider(), PipeBlockEntity.class);
        r.addBlockData(new OverclockComponentProvider(), MachineBlockEntity.class);
    }
}
