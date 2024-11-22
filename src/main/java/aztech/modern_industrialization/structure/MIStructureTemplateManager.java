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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.structure.StructureControllerBounds;
import aztech.modern_industrialization.blocks.structure.StructureMemberOverride;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static aztech.modern_industrialization.machines.multiblocks.ShapeMatcher.*;

public final class MIStructureTemplateManager {
    public enum ValidationResult {
        INVALID_BOUNDS(false, MIText.StructureMultiblockSaveFailInvalidBounds),
        MISCONFIGURED_BLOCK(false, MIText.StructureMultiblockSaveFailMisconfiguredBlock),
        TOO_MANY_CONTROLLERS(false, MIText.StructureMultiblockSaveFailTooManyControllers),
        NO_CONTROLLER(false, MIText.StructureMultiblockSaveFailNoController),
        NO_HATCHES(false, MIText.StructureMultiblockSaveFailNoHatches),
        SUCCESS(true, MIText.StructureMultiblockSaveSuccess);

        private final boolean success;
        private final MIText text;

        ValidationResult(boolean success, MIText text) {
            this.success = success;
            this.text = text;
        }

        public boolean isSuccess() {
            return success;
        }

        public MIText text() {
            return text;
        }
    }

    public record FromWorldResult(CompoundTag tag, ValidationResult result, Object... args) {
        public FromWorldResult {
            Objects.requireNonNull(result);
            if (tag == null && result.isSuccess()) {
                throw new IllegalArgumentException("Cannot create successful result with no tag");
            }
        }

        public FromWorldResult(ValidationResult result, Object... args) {
            this(null, result, args);
        }

        public FromWorldResult(CompoundTag tag, Object... args) {
            this(tag, ValidationResult.SUCCESS, args);
        }

        public boolean isSuccess() {
            return result.isSuccess();
        }

        public MutableComponent text(Object... args) {
            return result.text().text(args);
        }

        public MutableComponent text() {
            return this.text(args);
        }
    }

    public static FromWorldResult fromWorld(Level level, BlockPos controllerPos, Direction controllerDirection, StructureControllerBounds bounds) {
        if (bounds.isEmpty()) {
            return new FromWorldResult(ValidationResult.INVALID_BOUNDS);
        }

        CompoundTag tag = new CompoundTag();

        BoundingBox boundsBox = bounds.boundingBox();
        BlockPos minPos = controllerPos.offset(boundsBox.minX(), boundsBox.minY(), boundsBox.minZ());
        BlockPos maxPos = controllerPos.offset(boundsBox.maxX(), boundsBox.maxY(), boundsBox.maxZ());

        List<BlockState> paletteStates = new ArrayList<>();
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();

        boolean hasController = false;
        boolean hasHatch = false;

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

            // TODO replace this direct member and hatch data
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                if (blockEntity instanceof StructureMemberOverride override) {
                    if (!override.isConfigurationValid()) {
                        return new FromWorldResult(ValidationResult.MISCONFIGURED_BLOCK, pos.toShortString());
                    }
                    if (override.isController()) {
                        if (hasController) {
                            return new FromWorldResult(ValidationResult.TOO_MANY_CONTROLLERS);
                        }
                        hasController = true;
                    }
                    if (override.getHatchFlagsOverride() != null) {
                        hasHatch = true;
                    }
                }
                blockTag.put("nbt", blockEntity.saveWithId(level.registryAccess()));
            }

            blocks.add(blockTag);
        }

        if (!hasController) {
            return new FromWorldResult(ValidationResult.NO_CONTROLLER);
        }
        if (!hasHatch) {
            return new FromWorldResult(ValidationResult.NO_HATCHES);
        }

        tag.put("palette", palette);
        tag.put("blocks", blocks);

        return new FromWorldResult(tag);
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

            SimpleMember member = SimpleMember.forBlockState(state);
            HatchFlags flags = null;

            // TODO replace this direct member and hatch data
            if (block.contains("nbt", Tag.TAG_COMPOUND)) {
                CompoundTag nbt = block.getCompound("nbt");
                if (state.getBlock() instanceof EntityBlock entityBlock) {
                    BlockEntity blockEntity = entityBlock.newBlockEntity(pos, state);
                    if (blockEntity instanceof StructureMemberOverride override) {
                        override.loadStructureData(nbt);
                        member = override.getMemberOverride();
                        flags = override.getHatchFlagsOverride();
                    }
                }
            }

            if (member != null) {
                builder.add(pos.getX(), pos.getY(), pos.getZ(), member, flags);
            }
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

    public static boolean save(ResourceLocation id, CompoundTag tag) {
        try {
            try (OutputStream output = new FileOutputStream(path(id).toFile())) {
                NbtIo.writeCompressed(tag, output);
                return true;
            } catch (Exception ex) {
                MI.LOGGER.error("Failed to save structure '{}'", id, ex);
            }
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to save structure '{}'", id, ex);
        }
        return false;
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
