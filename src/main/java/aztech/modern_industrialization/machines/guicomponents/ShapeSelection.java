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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ShapeSelection implements GuiComponentServer<List<ShapeSelection.LineInfo>, List<Integer>> {
    public static final Type<List<ShapeSelection.LineInfo>, List<Integer>> TYPE = new Type<>(
            MI.id("shape_selection"),
            LineInfo.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()));

    public final Behavior behavior;
    private final List<LineInfo> lines;

    public ShapeSelection(Behavior behavior, LineInfo... lines) {
        Preconditions.checkArgument(lines.length > 0);

        this.behavior = behavior;
        this.lines = List.of(lines);
    }

    @Override
    public List<LineInfo> getParams() {
        return lines;
    }

    @Override
    public List<Integer> extractData() {
        return IntStream.range(0, lines.size()).map(behavior::getCurrentIndex).boxed().toList();
    }

    @Override
    public Type<List<LineInfo>, List<Integer>> getType() {
        return TYPE;
    }

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
    public record LineInfo(List<Component> translations, boolean useArrows) {
        public static final StreamCodec<RegistryFriendlyByteBuf, LineInfo> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),
                LineInfo::translations,
                ByteBufCodecs.BOOL,
                LineInfo::useArrows,
                LineInfo::new);

        public int numValues() {
            return translations.size();
        }
    }
}
