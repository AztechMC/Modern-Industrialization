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
package aztech.modern_industrialization.machines.multiblocks.structure;

import aztech.modern_industrialization.MIText;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum StructureNBTMode implements StringRepresentable {
    STRONG(MIText.StructureMultiblockGuiNBTModeStrong, MIText.StructureMultiblockWrenchNBTModeStrong),
    WEAK(MIText.StructureMultiblockGuiNBTModeWeak, MIText.StructureMultiblockWrenchNBTModeWeak),
    IGNORE(null, MIText.StructureMultiblockWrenchNBTModeIgnore);

    public static final Codec<StructureNBTMode> CODEC = Codec.STRING.xmap(name -> StructureNBTMode.valueOf(name.toUpperCase(Locale.ROOT)),
            mode -> mode.toString().toLowerCase(Locale.ROOT));

    public static final StreamCodec<ByteBuf, StructureNBTMode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(name -> StructureNBTMode.valueOf(name.toUpperCase(Locale.ROOT)), mode -> mode.toString().toLowerCase(Locale.ROOT));

    private final MIText textGui, textTooltip;

    StructureNBTMode(MIText textGui, MIText textTooltip) {
        this.textGui = textGui;
        this.textTooltip = textTooltip;
    }

    public boolean isStrong() {
        return this == STRONG;
    }

    public boolean isWeak() {
        return this == WEAK;
    }

    public boolean isIgnore() {
        return this == IGNORE;
    }

    public MutableComponent textGui() {
        return textGui.text();
    }

    public MutableComponent textTooltip() {
        return textTooltip.text();
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
