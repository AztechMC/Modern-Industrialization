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

import static aztech.modern_industrialization.machines.multiblocks.ShapeMatcher.toTemplatePos;
import static aztech.modern_industrialization.machines.multiblocks.ShapeMatcher.toTemplateState;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.blocks.structure.StructureMemberOverride;
import aztech.modern_industrialization.blocks.structure.controller.StructureControllerBounds;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.structure.member.HatchStructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.FileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
        Objects.requireNonNull(id);
        return STRUCTURE_TEMPLATES.containsKey(id);
    }

    public static ShapeTemplate get(ResourceLocation id) {
        assertLoaded();
        Objects.requireNonNull(id);
        ShapeTemplate template = STRUCTURE_TEMPLATES.get(id);
        if (template != null) {
            return template;
        } else {
            throw new IllegalArgumentException("Structure shape template \"" + id + "\" does not exist.");
        }
    }

    public static void register(ResourceLocation id, ShapeTemplate template) {
        assertLoaded();
        Objects.requireNonNull(id);
        Objects.requireNonNull(template);
        STRUCTURE_TEMPLATES.put(id, template);
    }

    @Nullable
    public static CompoundTag maybeTag(@Nullable BlockEntity blockEntity) {
        return blockEntity == null ? null : blockEntity.saveCustomOnly(blockEntity.getLevel().registryAccess());
    }

    public static StructureResult fromWorld(ResourceLocation id, Level level,
            BlockPos controllerPos, Direction controllerDirection,
            StructureControllerBounds bounds, boolean includeBlockEntities,
            List<BlockPos> ignoreNBTPositions, List<BlockPos> weakNBTPositions) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(level);
        Objects.requireNonNull(controllerPos);
        Objects.requireNonNull(controllerDirection);
        Objects.requireNonNull(bounds);
        Objects.requireNonNull(ignoreNBTPositions);
        Objects.requireNonNull(weakNBTPositions);
        if (bounds.isEmpty()) {
            return new StructureResult.InvalidBounds();
        }

        List<BlockPos> misconfiguredBlocks = new ArrayList<>();
        List<BlockPos> controllerBlocks = new ArrayList<>();
        List<BlockPos> hatchBlocks = new ArrayList<>();

        ShapeTemplate.Builder template = new ShapeTemplate.Builder(null);

        BoundingBox boundsBox = bounds.boundingBox();
        BlockPos minPos = controllerPos.offset(boundsBox.minX(), boundsBox.minY(), boundsBox.minZ());
        BlockPos maxPos = controllerPos.offset(boundsBox.maxX(), boundsBox.maxY(), boundsBox.maxZ());

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = toTemplateState(level, pos, level.getBlockState(pos), controllerDirection);
            if (state.isAir()) {
                continue;
            }
            if (state.is(Blocks.STRUCTURE_VOID)) {
                state = Blocks.AIR.defaultBlockState();
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);

            boolean isIgnoreNBT = ignoreNBTPositions.contains(pos);
            boolean isWeakNBT = weakNBTPositions.contains(pos);
            StructureNBTMode nbtMode = !includeBlockEntities || isIgnoreNBT ? StructureNBTMode.IGNORE
                    : isWeakNBT ? StructureNBTMode.WEAK : StructureNBTMode.STRONG;

            StructureMember member = StructureMember.literal(state, includeBlockEntities && !isIgnoreNBT ? blockEntity : null, nbtMode);

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
                HatchFlags hatchFlags = null;
                if (member instanceof HatchStructureMember hatch && hatch.hatchFlags() != null) {
                    hatchBlocks.add(pos.immutable());
                    hatchFlags = hatch.hatchFlags();
                }
                BlockPos templatePos = toTemplatePos(controllerPos, controllerDirection, pos);
                template.add(templatePos.getX(), templatePos.getY(), templatePos.getZ(), member, hatchFlags);
            }
        }

        if (controllerBlocks.isEmpty()) {
            return new StructureResult.NoController();
        }
        if (controllerBlocks.size() > 1) {
            return new StructureResult.TooManyControllers(controllerBlocks);
        }
        if (!misconfiguredBlocks.isEmpty()) {
            return new StructureResult.MisconfiguredBlocks(misconfiguredBlocks);
        }
        if (hatchBlocks.isEmpty()) {
            return new StructureResult.NoHatches();
        }

        return new StructureResult.Success(id, template.build());
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
        return FileUtil.createPathToResource(structuresFolder, id.getPath(), ".json");
    }

    private static JsonElement serialize(ShapeTemplate template) {
        Objects.requireNonNull(template);
        return ShapeTemplate.STRUCTURE_CODEC.encodeStart(JsonOps.INSTANCE, template).getOrThrow();
    }

    private static boolean save(Path path, JsonElement json) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(json);
        try {
            try (OutputStream output = Files.newOutputStream(path)) {
                output.write(new GsonBuilder().setPrettyPrinting().create().toJson(json).getBytes(StandardCharsets.UTF_8));
                return true;
            } catch (Exception ex) {
                MI.LOGGER.error("Failed to save structure at \"{}\"", path, ex);
            }
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to save structure at \"{}\"", path, ex);
        }
        return false;
    }

    private static boolean save(Path path, ShapeTemplate template) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(template);
        try {
            JsonElement json = serialize(template);
            return save(path, json);
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to save structure at \"{}\"", path, ex);
        }
        return false;
    }

    public static boolean save(ResourceLocation id, ShapeTemplate template) {
        Objects.requireNonNull(id);
        try {
            return save(path(id), template);
        } catch (IOException ex) {
            MI.LOGGER.error("Failed to save structure \"{}\"", id, ex);
        }
        return false;
    }

    public static void exportAll() {
        try {
            for (IModFileInfo modFile : ModList.get().getModFiles()) {
                Path modStructuresPath = modFile.getFile().findResource("mi_structures");
                iterateStructureFiles(modStructuresPath, (id, pathIn) -> {
                    try {
                        JsonElement json = load(pathIn);
                        Path pathOut = path(id);
                        if (Files.exists(pathOut)) {
                            JsonElement fileJson = load(pathOut);
                            if (!json.equals(fileJson)) {
                                save(pathOut, json);
                            }
                        } else {
                            save(pathOut, json);
                        }
                    } catch (IOException ex) {
                        MI.LOGGER.error("Failed to export structure \"{}\"", id, ex);
                    }
                });
            }
        } catch (IOException ex) {
            MI.LOGGER.error("Failed to export structures", ex);
        }
    }

    @Nullable
    private static JsonElement load(Path path) {
        Objects.requireNonNull(path);
        try {
            if (Files.exists(path)) {
                try (InputStream input = Files.newInputStream(path);
                        InputStream fastInput = new FastBufferedInputStream(input)) {
                    return JsonParser.parseString(new String(fastInput.readAllBytes(), StandardCharsets.UTF_8));
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
                        if (file.toString().endsWith(".json")) {
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
        JsonElement structureJson = load(path);
        if (structureJson != null) {
            try {
                ShapeTemplate structure = ShapeTemplate.STRUCTURE_CODEC.decode(JsonOps.INSTANCE, structureJson).getOrThrow().getFirst();
                register(id, structure);
            } catch (Exception ex) {
                MI.LOGGER.error("Failed to load structure with id \"{}\"", id, ex);
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
                    if (!STRUCTURE_TEMPLATES.containsKey(id)) {
                        register(id, path);
                    }
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
