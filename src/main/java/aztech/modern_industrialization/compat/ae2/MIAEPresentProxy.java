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

package aztech.modern_industrialization.compat.ae2;

import appeng.core.AEConfig;
import aztech.modern_industrialization.config.MIStartupConfig;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;

class MIAEPresentProxy extends MIAEProxy {
    static {
        if (!MIStartupConfig.INSTANCE.loadAe2Compat()) {
            throw new RuntimeException("AE2 compat is disabled. How did this get loaded?");
        }
    }

    @Override
    public void initialize() {
        MIAEAddon.onInitializePipes();
    }

    @Override
    public boolean isMeWireType(PipeNetworkType type) {
        return MIAEAddon.PIPES.contains(type);
    }

    @Override
    public int getMeWireChannelCount() {
        return 8 * AEConfig.instance().getChannelMode().getCableCapacityFactor();
    }
}
