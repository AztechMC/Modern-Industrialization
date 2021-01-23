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
package aztech.modern_industrialization.blocks;

import aztech.modern_industrialization.util.MobSpawning;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageFunction;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;

@SuppressWarnings("rawtypes")
public class TrashCanBlock extends Block {
    public TrashCanBlock() {
        super(FabricBlockSettings.of(Material.METAL).hardness(6.0f).resistance(1200).sounds(BlockSoundGroup.METAL)
                .allowsSpawning(MobSpawning.NO_SPAWN));
    }

    @SuppressWarnings("unchecked")
    public static <T> Storage<T> trashStorage() {
        return TRASH;
    }

    private static final Storage TRASH = new TrashStorage();

    private static class TrashStorage implements Storage {
        @Override
        public StorageFunction insertionFunction() {
            return StorageFunction.identity();
        }

        @Override
        public boolean forEach(Visitor visitor) {
            return false;
        }

        @Override
        public int getVersion() {
            return 0;
        }
    }
}
