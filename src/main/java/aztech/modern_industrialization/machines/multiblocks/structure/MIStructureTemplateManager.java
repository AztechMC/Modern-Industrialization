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
import aztech.modern_industrialization.blocks.structure.StructureControllerBounds;
import aztech.modern_industrialization.blocks.structure.StructureMemberOverride;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.FileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.jetbrains.annotations.Nullable;

public final class MIStructureTemplateManager {
    private static Map<ResourceLocation, ShapeTemplate> STRUCTURE_TEMPLATES;

    private static void assertLoaded() {
        if (STRUCTURE_TEMPLATES == null) {
            throw new IllegalStateException("Structure templates have not yet been loaded");
        }
    }

    public static boolean exists(ResourceLocation id) {
        assertLoaded();
        return STRUCTURE_TEMPLATES.containsKey(id);
    }

    public static ShapeTemplate get(ResourceLocation id) {
        assertLoaded();
        ShapeTemplate template = STRUCTURE_TEMPLATES.get(id);
        if (template != null) {
            return template;
        } else {
            throw new IllegalArgumentException("Structure shape template \"" + id.toString() + "\" does not exist.");
        }
    }

    public static StructureResult fromWorld(ResourceLocation id, Level level,
            BlockPos controllerPos, Direction controllerDirection,
            MachineCasing hatchCasing, StructureControllerBounds bounds) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(level);
        Objects.requireNonNull(controllerPos);
        Objects.requireNonNull(controllerDirection);
        Objects.requireNonNull(bounds);
        if (bounds.isEmpty()) {
            return new StructureResult.InvalidBounds();
        }

        List<BlockPos> misconfiguredBlocks = new ArrayList<>();
        List<BlockPos> controllerBlocks = new ArrayList<>();
        List<BlockPos> hatchBlocks = new ArrayList<>();

        if (hatchCasing == null) {
            misconfiguredBlocks.add(controllerPos);
        }

        CompoundTag tag = new CompoundTag();

        if (hatchCasing != null) {
            tag.putString("hatch_casing", hatchCasing.key.toString());
        }

        BoundingBox boundsBox = bounds.boundingBox();
        BlockPos minPos = controllerPos.offset(boundsBox.minX(), boundsBox.minY(), boundsBox.minZ());
        BlockPos maxPos = controllerPos.offset(boundsBox.maxX(), boundsBox.maxY(), boundsBox.maxZ());

        List<StructureMember> members = new ArrayList<>();
        ListTag membersTag = new ListTag();
        ListTag blocksTag = new ListTag();

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
                    misconfiguredBlocks.add(pos.immutable());
                    continue;
                }
                if (override.isController()) {
                    controllerBlocks.add(pos.immutable());
                    if (controllerBlocks.size() > 1) {
                        continue;
                    }
                }
                member = override.getMemberOverride();
            }

            if (member != null) {
                if (member.hatchFlags() != null) {
                    hatchBlocks.add(pos.immutable());
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

        if (controllerBlocks.isEmpty()) {
            return new StructureResult.NoController();
        }
        if (hatchBlocks.isEmpty()) {
            return new StructureResult.NoHatches();
        }
        if (controllerBlocks.size() > 1) {
            return new StructureResult.TooManyControllers(controllerBlocks);
        }
        if (!misconfiguredBlocks.isEmpty()) {
            return new StructureResult.MisconfiguredBlocks(misconfiguredBlocks);
        }

        tag.put("members", membersTag);
        tag.put("blocks", blocksTag);

        return new StructureResult.Success(id, tag);
    }

    @Nullable
    private static ShapeTemplate deserialize(CompoundTag tag) {
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

    private static Path structuresPath() {
        return FMLPaths.GAMEDIR.get()
                .resolve(MI.ID)
                .resolve("structures");
    }

    private static Path path(ResourceLocation id) throws IOException {
        Objects.requireNonNull(id);
        var structuresFolder = structuresPath().resolve(id.getNamespace());
        Files.createDirectories(structuresFolder);
        return FileUtil.createPathToResource(structuresFolder, id.getPath(), ".snbt");
    }

    public static boolean save(ResourceLocation id, CompoundTag tag) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tag);
        try {
            try (OutputStream output = Files.newOutputStream(path(id))) {
                output.write(new SnbtPrinterTagVisitor().visit(tag).getBytes(StandardCharsets.UTF_8));
                return true;
            } catch (Exception ex) {
                MI.LOGGER.error("Failed to save structure \"{}\"", id, ex);
            }
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to save structure \"{}\"", id, ex);
        }
        return false;
    }

    @Nullable
    private static CompoundTag load(Path path) {
        Objects.requireNonNull(path);
        try {
            if (Files.exists(path)) {
                try (InputStream input = Files.newInputStream(path);
                        InputStream fastInput = new FastBufferedInputStream(input)) {
                    return TagParser.parseTag(new String(fastInput.readAllBytes(), StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    MI.LOGGER.error("Failed to load structure at \"{}\"", path, ex);
                    return null;
                }
            }
            MI.LOGGER.error("Could not find structure at \"{}\"", path);
            return null;
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to load structure at \"{}\"", path, ex);
            return null;
        }
    }

    private static void iterateStructureFiles(Path origin, BiConsumer<ResourceLocation, Path> action) throws IOException {
        try (DirectoryStream<Path> subdirectories = Files.newDirectoryStream(origin, Files::isDirectory)) {
            for (Path subdirectory : subdirectories) {
                String namespace = subdirectory.getFileName().toString();
                try (DirectoryStream<Path> files = Files.newDirectoryStream(subdirectory)) {
                    for (Path file : files) {
                        if (file.toString().endsWith(".snbt")) {
                            String rawFileName = file.getFileName().toString();
                            String path = rawFileName.substring(0, rawFileName.lastIndexOf('.'));
                            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
                            action.accept(id, file);
                        }
                    }
                }
            }
        }
    }

    private static void register(ResourceLocation id, Path path) {
        CompoundTag structureTag = load(path);
        if (structureTag != null) {
            ShapeTemplate structure = deserialize(structureTag);
            if (structure != null) {
                STRUCTURE_TEMPLATES.put(id, structure);
            } else {
                MI.LOGGER.error("Failed to load structure with id \"{}\"", id);
            }
        }
    }

    public static void init() {
        if (STRUCTURE_TEMPLATES != null) {
            throw new IllegalStateException("Structures have already been loaded");
        }

        STRUCTURE_TEMPLATES = new HashMap<>();

        try {
            var structuresPath = structuresPath();
            Files.createDirectories(structuresPath);
            iterateStructureFiles(structuresPath, MIStructureTemplateManager::register);

            for (IModFileInfo modFile : ModList.get().getModFiles()) {
                Path modStructuresPath = modFile.getFile().findResource("mi_structures");
                iterateStructureFiles(modStructuresPath, (id, path) -> {
                    if (STRUCTURE_TEMPLATES.containsKey(id)) {
                        return;
                    }
                    register(id, path);
                });
            }

            MI.LOGGER.info("Loaded {} structures.", STRUCTURE_TEMPLATES.size());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private MIStructureTemplateManager() {
    }
}
