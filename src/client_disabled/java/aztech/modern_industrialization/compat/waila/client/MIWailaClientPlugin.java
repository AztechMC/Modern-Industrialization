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
package aztech.modern_industrialization.compat.waila.client;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaHelper;
import mcp.mobius.waila.api.data.EnergyData;

public class MIWailaClientPlugin implements IWailaPlugin {
    public static final int ENERGY_COLOR = 0xFFB70000;

    @Override
    public void register(IRegistrar r) {
        EnergyData.describe(ModernIndustrialization.MOD_ID).color(ENERGY_COLOR).unit("EU");

        PipeComponentProvider pipeComponentProvider = new PipeComponentProvider();
        r.addComponent(pipeComponentProvider, TooltipPosition.HEAD, PipeBlockEntity.class);
        r.addComponent(pipeComponentProvider, TooltipPosition.BODY, PipeBlockEntity.class);

        OverclockComponentProvider overclockComponentProvider = new OverclockComponentProvider();
        r.addComponent(overclockComponentProvider, TooltipPosition.BODY, MachineBlockEntity.class);
    }

    static float ratio(double current, double max) {
        return (float) (current / max);
    }

    static String fraction(double current, double max) {
        return WailaHelper.suffix((long) current) + "/" + WailaHelper.suffix((long) max);
    }
}
