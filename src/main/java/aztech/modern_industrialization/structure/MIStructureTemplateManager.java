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
package aztech.modern_industrialization.structure;

import static aztech.modern_industrialization.machines.multiblocks.ShapeMatcher.*;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.blocks.structure.StructureControllerBounds;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.FileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

public final class MIStructureTemplateManager {
    public static CompoundTag fromWorld(Level level, BlockPos controllerPos, Direction controllerDirection, StructureControllerBounds bounds) {
        if (bounds.isEmpty()) {
            throw new IllegalArgumentException("Invalid bounds: %s".formatted(bounds));
        }

        CompoundTag tag = new CompoundTag();

        BoundingBox boundsBox = bounds.boundingBox();
        BlockPos minPos = controllerPos.offset(boundsBox.minX(), boundsBox.minY(), boundsBox.minZ());
        BlockPos maxPos = controllerPos.offset(boundsBox.maxX(), boundsBox.maxY(), boundsBox.maxZ());

        List<BlockState> paletteStates = new ArrayList<>();
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = toTemplateState(level, pos, level.getBlockState(pos), controllerDirection);

            CompoundTag blockTag = new CompoundTag();

            blockTag.put("pos", NbtUtils.writeBlockPos(toTemplatePos(controllerPos, controllerDirection, pos)));

            if (!paletteStates.contains(state)) {
                paletteStates.add(state);
                palette.add(NbtUtils.writeBlockState(state));
            }
            int paletteIndex = paletteStates.indexOf(state);
            blockTag.putInt("state", paletteIndex);

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockTag.put("nbt", blockEntity.saveWithId(level.registryAccess()));
            }

            blocks.add(blockTag);
        }

        tag.put("palette", palette);
        tag.put("blocks", blocks);

        return tag;
    }

    public static ShapeTemplate deserialize(MachineCasing hatchCasing, CompoundTag tag) {
        var blockRegistry = BuiltInRegistries.BLOCK.asLookup();

        ShapeTemplate.Builder builder = new ShapeTemplate.Builder(hatchCasing);

        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);

        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompound(i);

            BlockPos pos = NbtUtils.readBlockPos(block, "pos").orElseThrow();

            int paletteIndex = block.getInt("state");
            BlockState state = NbtUtils.readBlockState(blockRegistry, palette.getCompound(paletteIndex));

            /*
             * TODO:
             * - get the simple member from the blockentity if its one of our structure blocks
             */

            builder.add(pos.getX(), pos.getY(), pos.getZ(), SimpleMember.forBlockState(state));
        }

        return builder.build();
    }

    private static Path path(ResourceLocation id) throws IOException {
        var miFolder = FMLPaths.GAMEDIR.get().resolve(MI.ID);
        var structuresFolder = miFolder
                .resolve("structures")
                .resolve(id.getNamespace());
        Files.createDirectories(structuresFolder);
        return FileUtil.createPathToResource(structuresFolder, id.getPath(), ".nbt");
    }

    public static void save(ResourceLocation id, CompoundTag tag) {
        try (OutputStream output = new FileOutputStream(path(id).toFile())) {
            NbtIo.writeCompressed(tag, output);
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to save structure '{}'", id, ex);
        }
    }

    @Nullable
    public static CompoundTag load(ResourceLocation id) {
        try {
            Path path = path(id);
            if (Files.exists(path)) {
                try (InputStream input = new FileInputStream(path.toFile());
                        InputStream fastInput = new FastBufferedInputStream(input)) {
                    return NbtIo.readCompressed(fastInput, NbtAccounter.unlimitedHeap());
                } catch (Exception ex) {
                    MI.LOGGER.error("Failed to load structure '{}'", id, ex);
                    return null;
                }
            }
            return null;
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to load structure '{}'", id, ex);
            return null;
        }
    }

    private MIStructureTemplateManager() {
    }
}
