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

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.parts.PartModel;
import aztech.modern_industrialization.MI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class P2PModels {

    public static final ResourceLocation MODEL_STATUS_OFF = AppEng.makeId("part/p2p/p2p_tunnel_status_off");
    public static final ResourceLocation MODEL_STATUS_ON = AppEng.makeId("part/p2p/p2p_tunnel_status_on");
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = AppEng.makeId("part/p2p/p2p_tunnel_status_has_channel");
    public static final ResourceLocation MODEL_FREQUENCY = AppEng.makeId("part/p2p/p2p_tunnel_frequency");

    private final IPartModel modelsOff;
    private final IPartModel modelsOn;
    private final IPartModel modelsHasChannel;

    public P2PModels(String frontModelPath) {
        ResourceLocation frontModel = MI.id(frontModelPath);

        this.modelsOff = new PartModel(MODEL_STATUS_OFF, MODEL_FREQUENCY, frontModel);
        this.modelsOn = new PartModel(MODEL_STATUS_ON, MODEL_FREQUENCY, frontModel);
        this.modelsHasChannel = new PartModel(MODEL_STATUS_HAS_CHANNEL, MODEL_FREQUENCY, frontModel);
    }

    public IPartModel getModel(boolean hasPower, boolean hasChannel) {
        if (hasPower && hasChannel) {
            return this.modelsHasChannel;
        } else if (hasPower) {
            return this.modelsOn;
        } else {
            return this.modelsOff;
        }
    }

    public List<IPartModel> getModels() {
        List<IPartModel> result = new ArrayList<>();
        result.add(this.modelsOff);
        result.add(this.modelsOn);
        result.add(this.modelsHasChannel);
        return result;
    }
}
