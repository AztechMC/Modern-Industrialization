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
package aztech.modern_industrialization.util;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

/**
 * Base class for all MI saved data to make them more resistant to crashes while writing. Thank you RS for the idea!
 */
// TODO remove when NeoForge implements robust saving
public abstract class MISavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void save(File file) {
        if (!this.isDirty()) {
            return;
        }

        File tempFile = file.toPath().getParent().resolve(file.getName() + ".temp").toFile();

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", this.save(new CompoundTag()));
        NbtUtils.addCurrentDataVersion(compoundTag);
        try {
            // Write to temp file first.
            NbtIo.writeCompressed(compoundTag, tempFile.toPath());
            // Delete old file.
            if (file.exists()) {
                if (!file.delete()) {
                    LOGGER.error("Could not delete old file {}", file);
                }
            }
            // Rename temp file to the correct name.
            if (!tempFile.renameTo(file)) {
                LOGGER.error("Could not rename file {} to {}", tempFile, file);
            }
        } catch (IOException iOException) {
            LOGGER.error("Could not save data {}", this, iOException);
        }
        this.setDirty(false);
    }
}
