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
package aztech.modern_industrialization.test.framework;

import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;

public class PipeBuilder {
    private final MIGameTestHelper helper;
    private final PipeBlockEntity pipe;
    private final PipeNetworkType type;

    PipeBuilder(MIGameTestHelper helper, PipeBlockEntity pipe, PipeNetworkType type) {
        this.helper = helper;
        this.pipe = pipe;
        this.type = type;
    }

    public void addConnection(Direction direction) {
        pipe.addConnection(helper.makeMockPlayer(GameType.SURVIVAL), type, direction);
    }

    public void removeConnection(Direction direction) {
        pipe.removeConnection(type, direction);
    }
}
