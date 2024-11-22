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

import static aztech.modern_industrialization.machines.multiblocks.ShapeMatcher.*;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.structure.StructureControllerBounds;
import aztech.modern_industrialization.blocks.structure.StructureMemberOverride;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
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
import net.minecraft.FileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

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

    public static FromWorldResult fromWorld(Level level, BlockPos controllerPos, Direction controllerDirection,
            MachineCasing hatchCasing, StructureControllerBounds bounds) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(controllerPos);
        Objects.requireNonNull(controllerDirection);
        Objects.requireNonNull(bounds);
        if (hatchCasing == null) {
            return new FromWorldResult(ValidationResult.MISCONFIGURED_BLOCK, controllerPos.toShortString());
        }
        if (bounds.isEmpty()) {
            return new FromWorldResult(ValidationResult.INVALID_BOUNDS);
        }

        CompoundTag tag = new CompoundTag();

        tag.putString("hatch_casing", hatchCasing.key.toString());

        BoundingBox boundsBox = bounds.boundingBox();
        BlockPos minPos = controllerPos.offset(boundsBox.minX(), boundsBox.minY(), boundsBox.minZ());
        BlockPos maxPos = controllerPos.offset(boundsBox.maxX(), boundsBox.maxY(), boundsBox.maxZ());

        List<StructureMember> members = new ArrayList<>();
        ListTag membersTag = new ListTag();
        ListTag blocksTag = new ListTag();

        boolean hasController = false;
        boolean hasHatch = false;

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = toTemplateState(level, pos, level.getBlockState(pos), controllerDirection);

            if (state.isAir() || state.is(Blocks.STRUCTURE_VOID)) {
                continue;
            }

            CompoundTag blockTag = new CompoundTag();

            blockTag.put("pos", NbtUtils.writeBlockPos(toTemplatePos(controllerPos, controllerDirection, pos)));

            StructureMember member = new StructureMember(() -> state);

            BlockEntity blockEntity = level.getBlockEntity(pos);
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
                member = override.getMemberOverride();
            }

            if (member != null) {
                if (member.hatchFlags() != null) {
                    hasHatch = true;
                }
                CompoundTag memberTag = new CompoundTag();
                member.save(memberTag);
                if (!members.contains(member)) {
                    members.add(member);
                    membersTag.add(memberTag);
                }
                blockTag.putInt("member_index", members.indexOf(member));

                blocksTag.add(blockTag);
            }
        }

        if (!hasController) {
            return new FromWorldResult(ValidationResult.NO_CONTROLLER);
        }
        if (!hasHatch) {
            return new FromWorldResult(ValidationResult.NO_HATCHES);
        }

        tag.put("members", membersTag);
        tag.put("blocks", blocksTag);

        return new FromWorldResult(tag);
    }

    @Nullable
    public static ShapeTemplate deserialize(CompoundTag tag) {
        Objects.requireNonNull(tag);

        ResourceLocation hatchCasingId = ResourceLocation.tryParse(tag.getString("hatch_casing"));
        if (hatchCasingId == null || !MachineCasings.registeredCasings.containsKey(hatchCasingId)) {
            return null;
        }
        MachineCasing hatchCasing = MachineCasings.get(hatchCasingId);

        ShapeTemplate.Builder builder = new ShapeTemplate.Builder(hatchCasing);

        ListTag members = tag.getList("members", Tag.TAG_COMPOUND);
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);

        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag blockTag = blocks.getCompound(i);

            BlockPos pos = NbtUtils.readBlockPos(blockTag, "pos").orElseThrow();

            if (blockTag.contains("member_index", Tag.TAG_INT)) {
                int memberIndex = blockTag.getInt("member_index");
                StructureMember member = StructureMember.from(members.getCompound(memberIndex));
                builder.add(pos.getX(), pos.getY(), pos.getZ(), member, member.hatchFlags());
            }
        }

        return builder.build();
    }

    private static Path path(ResourceLocation id) throws IOException {
        Objects.requireNonNull(id);
        var miFolder = FMLPaths.GAMEDIR.get().resolve(MI.ID);
        var structuresFolder = miFolder
                .resolve("structures")
                .resolve(id.getNamespace());
        Files.createDirectories(structuresFolder);
        return FileUtil.createPathToResource(structuresFolder, id.getPath(), ".nbt");
    }

    public static boolean save(ResourceLocation id, CompoundTag tag) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tag);
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
        Objects.requireNonNull(id);
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
