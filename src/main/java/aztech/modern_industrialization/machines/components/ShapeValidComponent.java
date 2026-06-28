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

package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.machines.MachineComponent;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

/**
 * Syncing whether the multiblock shape is currently valid with the clients, to
 * decide if the BER should display something or not.
 */
public class ShapeValidComponent implements MachineComponent.ClientOnly {
    private boolean lastShapeValid = false;
    public boolean shapeValid = false;

    // Used to communicate what block entities are mismatching to the client, since it does not have full block entity awareness like the server does
    private Set<BlockPos> lastMismatchingBlockEntities = new HashSet<>();
    private Set<BlockPos> mismatchingBlockEntities = new HashSet<>();

    public boolean isBlockEntityMatchingAt(BlockPos pos) {
        return !mismatchingBlockEntities.contains(pos);
    }

    public void clearMismatchingBlockEntities() {
        mismatchingBlockEntities.clear();
    }

    public void addMismatchingBlockEntity(BlockPos pos) {
        mismatchingBlockEntities.add(pos);
    }

    /**
     * Return true if this component should be synced with the client.
     */
    public boolean update() {
        if (lastShapeValid != shapeValid || !lastMismatchingBlockEntities.equals(mismatchingBlockEntities)) {
            lastShapeValid = shapeValid;
            lastMismatchingBlockEntities = Set.copyOf(mismatchingBlockEntities);
            return true;
        }
        return false;
    }

    @Override
    public void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("shapeValid", shapeValid);

        ListTag mismatchingBlockEntitiesTag = new ListTag();
        for (BlockPos pos : mismatchingBlockEntities) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.put("pos", NbtUtils.writeBlockPos(pos));
            mismatchingBlockEntitiesTag.add(blockTag);
        }
        tag.put("mismatchingBlockEntities", mismatchingBlockEntitiesTag);
    }

    @Override
    public void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        shapeValid = tag.getBoolean("shapeValid");

        mismatchingBlockEntities.clear();
        ListTag mismatchingBlockEntitiesTag = tag.getList("mismatchingBlockEntities", Tag.TAG_COMPOUND);
        for (Tag blockTag : mismatchingBlockEntitiesTag) {
            if (blockTag instanceof CompoundTag blockCompoundTag) {
                NbtUtils.readBlockPos(blockCompoundTag, "pos").ifPresent(mismatchingBlockEntities::add);
            }
        }
    }
}
