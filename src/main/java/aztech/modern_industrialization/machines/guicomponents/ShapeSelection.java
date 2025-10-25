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

package aztech.modern_industrialization.machines.guicomponents;

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public class ShapeSelection {
    public interface Behavior {
        /**
         * @param delta +1 if clicked on right button, -1 if clicked on left button
         */
        void handleClick(int clickedLine, int delta);

        int getCurrentIndex(int line);
    }

    /**
     * @param useArrows True for arrows {@code < and >}, false for +/-.
     */
    public record LineInfo(int numValues, List<? extends Component> translations, boolean useArrows) {
        public LineInfo {
            Preconditions.checkArgument(numValues == translations.size());
        }
    }

    public static class Server implements GuiComponent.Server<int[]> {
        public final Behavior behavior;
        private final List<LineInfo> lines;

        public Server(Behavior behavior, LineInfo... lines) {
            Preconditions.checkArgument(lines.length > 0);

            this.behavior = behavior;
            this.lines = List.of(lines);
        }

        @Override
        public int[] copyData() {
            return IntStream.range(0, lines.size()).map(behavior::getCurrentIndex).toArray();
        }

        @Override
        public boolean needsSync(int[] cachedData) {
            for (int i = 0; i < lines.size(); ++i) {
                if (cachedData[i] != behavior.getCurrentIndex(i)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void writeInitialData(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(lines.size());
            for (var line : lines) {
                buf.writeVarInt(line.numValues);
                for (var component : line.translations) {
                    ComponentSerialization.STREAM_CODEC.encode(buf, component);
                }
                buf.writeBoolean(line.useArrows);
            }
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(RegistryFriendlyByteBuf buf) {
            for (int i = 0; i < lines.size(); ++i) {
                buf.writeVarInt(behavior.getCurrentIndex(i));
            }
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.SHAPE_SELECTION;
        }
    }
}
