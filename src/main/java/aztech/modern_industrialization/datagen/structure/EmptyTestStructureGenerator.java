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

package aztech.modern_industrialization.datagen.structure;

import aztech.modern_industrialization.MI;
import com.google.common.hash.HashCode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;

public class EmptyTestStructureGenerator implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public EmptyTestStructureGenerator(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        String structureIn;
        try (var in = getClass().getResourceAsStream("/data/modern_industrialization/structure/empty.snbt")) {
            structureIn = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        CompoundTag structureTag;
        try {
            structureTag = NbtUtils.snbtToStructure(structureIn);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        structureTag = NbtUtils.addCurrentDataVersion(structureTag);

        var out = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(structureTag, new DataOutputStream(out));
            output.writeIfNeeded(
                    pathProvider.file(MI.id("empty"), "nbt"),
                    out.toByteArray(),
                    HashCode.fromBytes(out.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Empty Test Structure";
    }
}
