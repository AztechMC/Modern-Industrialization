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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.structure.MIStructureTemplateManager;
import aztech.modern_industrialization.machines.multiblocks.structure.member.HatchStructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.SimpleStructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.VariableStructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable description of a multiblock shape.
 */
@EventBusSubscriber(modid = MI.ID, bus = EventBusSubscriber.Bus.MOD)
public class ShapeTemplate {
    public static final Codec<ShapeTemplate> STRUCTURE_CODEC = InternalStructureTemplate.CODEC
            .xmap(structure -> {
                Builder template = new Builder(null);
                for (InternalStructureBlockPos block : structure.blocks()) {
                    BlockPos pos = block.pos();
                    int memberIndex = block.memberIndex();
                    StructureMember member = structure.members().get(memberIndex);
                    template.add(pos.getX(), pos.getY(), pos.getZ(), member,
                            member instanceof HatchStructureMember hatch ? hatch.hatchFlags() : null);
                }
                return template.build();
            }, template -> {
                List<StructureMember> members = new ArrayList<>();
                List<InternalStructureBlockPos> blocks = new ArrayList<>();
                for (var entry : template.simpleMembers.entrySet()) {
                    if (entry.getValue() instanceof StructureMember member) {
                        if (!members.contains(member)) {
                            members.add(member);
                        }
                        int memberIndex = members.indexOf(member);
                        blocks.add(new InternalStructureBlockPos(memberIndex, entry.getKey()));
                    } else {
                        throw new IllegalArgumentException(
                                "Cannot convert ShapeTemplate to a structure ShapeTemplate if all members aren't StructureMembers");
                    }
                }
                return new InternalStructureTemplate(blocks, members);
            });

    private record InternalStructureBlockPos(int memberIndex, BlockPos pos) {
        private static final Codec<InternalStructureBlockPos> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.INT.fieldOf("member_index").forGetter(InternalStructureBlockPos::memberIndex),
                        BlockPos.CODEC.fieldOf("pos").forGetter(InternalStructureBlockPos::pos))
                .apply(instance, InternalStructureBlockPos::new));
    }

    private record InternalStructureTemplate(List<InternalStructureBlockPos> blocks, List<StructureMember> members) {
        private static final Codec<InternalStructureTemplate> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        InternalStructureBlockPos.CODEC.listOf().fieldOf("blocks").forGetter(InternalStructureTemplate::blocks),
                        StructureMember.CODEC.listOf().fieldOf("members").forGetter(InternalStructureTemplate::members))
                .apply(instance, InternalStructureTemplate::new));
    }

    private static final List<ShapeTemplate> TEMPLATES = new ArrayList<>();

    public final Map<BlockPos, SimpleMember> simpleMembers = new HashMap<>();
    public final Map<BlockPos, HatchFlags> hatchFlags = new HashMap<>();
    @Nullable
    public final MachineCasing hatchCasing;

    /**
     * A null hatchCasing for a shape is only permitted if the shape is a structure as the hatch casing is determined by the hatch members.
     */
    private ShapeTemplate(@Nullable MachineCasing hatchCasing) {
        this.hatchCasing = hatchCasing;
        TEMPLATES.add(this);
    }

    /**
     * Forces all lazy block states in structures to get loaded during startup as soon as blocks are in the registry. This prevents any delay later
     * where we otherwise would need to be parsing json elements during play.
     */
    @SubscribeEvent
    private static void onSetup(FMLCommonSetupEvent event) {
        for (ShapeTemplate template : TEMPLATES) {
            for (SimpleMember member : template.simpleMembers.values()) {
                if (member instanceof SimpleStructureMember simpleMember) {
                    simpleMember.getPreviewState();
                    for (StructureMemberTest test : simpleMember.tests()) {
                        if (test instanceof StateStructureMemberTest stateTest) {
                            stateTest.blockState();
                        }
                    }
                }
            }
        }
    }

    public static class Builder {
        private final ShapeTemplate template;

        public Builder(MachineCasing hatchCasing) {
            template = new ShapeTemplate(hatchCasing);
        }

        public Builder add3by3Levels(int minY, int maxY, SimpleMember member, @Nullable HatchFlags flags) {
            for (int y = minY; y <= maxY; ++y) {
                add3by3(y, member, y != minY, (y == minY || y == maxY) ? flags : null);
            }

            return this;
        }

        public Builder add3by3LevelsRoofed(int minY, int maxY, SimpleMember member, @Nullable HatchFlags flags) {
            for (int y = minY; y <= maxY; ++y) {
                add3by3(y, member, y != minY && y != maxY, (y == minY || y == maxY) ? flags : null);
            }

            return this;
        }

        public Builder add(int x, int y, int z, SimpleMember member, @Nullable HatchFlags flags) {
            BlockPos pos = new BlockPos(x, y, z);
            template.simpleMembers.put(pos, member);
            if (flags != null) {
                template.hatchFlags.put(pos, flags);
            }
            return this;
        }

        public Builder add(int x, int y, int z, SimpleMember member) {
            return add(x, y, z, member, null);
        }

        public Builder add3by3(int y, SimpleMember member, boolean hollow, @Nullable HatchFlags flags) {
            for (int x = -1; x <= 1; x++) {
                for (int z = 0; z <= 2; z++) {
                    if (hollow && x == 0 && z == 1) {
                        continue;
                    }
                    add(x, y, z, member, flags);
                }
            }
            return this;
        }

        public Builder remove(int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            template.simpleMembers.remove(pos);
            template.hatchFlags.remove(pos);
            return this;
        }

        public ShapeTemplate build() {
            remove(0, 0, 0);
            return template;
        }
    }

    public static class LayeredBuilder {
        private final ShapeTemplate.Builder innerBuilder;
        private final String[][] layers;
        private final Set<Character> missingKeys = new HashSet<>();
        private final Map<Character, KeyDefinition> keyDefinitions = new HashMap<>();

        private int iController, jController, kController;

        private record KeyDefinition(SimpleMember member, @Nullable HatchFlags flags) {
        }

        public LayeredBuilder(MachineCasing hatchCasing, String[][] layers) {
            innerBuilder = new Builder(hatchCasing);

            // Find layout size
            if (layers.length == 0) {
                throw new IllegalArgumentException("No layers provided");
            }
            if (layers[0].length == 0) {
                throw new IllegalArgumentException("Layer 0 cannot have size 0");
            }
            if (layers[0][0].length() == 0) {
                throw new IllegalArgumentException("Layer 0 cannot have size 0");
            }

            int dim1 = layers.length;
            int dim2 = layers[0].length;
            int dim3 = layers[0][0].length();

            // Validate size and find controller
            boolean foundController = false;
            for (int i = 0; i < dim1; ++i) {
                if (layers[i].length != dim2) {
                    throw new IllegalArgumentException("Layer %d has invalid size, expected %d".formatted(i, dim2));
                }
                for (int j = 0; j < dim2; ++j) {
                    if (layers[i][j].length() != dim3) {
                        throw new IllegalArgumentException("Layer %d entry %d has invalid size, expected %d".formatted(i, j, dim3));
                    }

                    for (int k = 0; k < dim3; ++k) {
                        char c = layers[i][j].charAt(k);
                        if (c == '#') {
                            if (foundController) {
                                throw new IllegalArgumentException("Multiple controllers found (character #)");
                            }
                            foundController = true;
                            iController = i;
                            jController = j;
                            kController = k;
                        } else if (c != ' ') {
                            missingKeys.add(c);
                        }
                    }
                }
            }

            this.layers = layers;
        }

        public LayeredBuilder key(char key, SimpleMember member, @Nullable HatchFlags flags) {
            if (keyDefinitions.containsKey(key)) {
                throw new IllegalArgumentException("Key '%c' was already defined".formatted(key));
            }
            if (!missingKeys.contains(key)) {
                throw new IllegalArgumentException("Key '%c' it not part of the shape layers".formatted(key));
            }
            missingKeys.remove(key);
            keyDefinitions.put(key, new KeyDefinition(member, flags));
            return this;
        }

        public ShapeTemplate build() {
            if (!missingKeys.isEmpty()) {
                throw new IllegalArgumentException("Missing keys: " + missingKeys);
            }

            for (int i = 0; i < layers.length; ++i) {
                for (int j = 0; j < layers[i].length; ++j) {
                    for (int k = 0; k < layers[i][j].length(); ++k) {
                        char c = layers[i][j].charAt(k);
                        if (c != ' ' && c != '#') {
                            KeyDefinition def = keyDefinitions.get(c);
                            int iAdjusted = i - iController;
                            int jAdjusted = j - jController;
                            int kAdjusted = k - kController;

                            innerBuilder.add(kAdjusted, jAdjusted, -iAdjusted, def.member, def.flags);
                        }
                    }
                }
            }

            return innerBuilder.build();
        }
    }

    public static class Structure {
        private final ShapeTemplate template;

        public Structure(ResourceLocation id) {
            ShapeTemplate referenceTemplate = MIStructureTemplateManager.get(id);
            template = new ShapeTemplate(null);
            template.simpleMembers.putAll(referenceTemplate.simpleMembers);
            template.hatchFlags.putAll(referenceTemplate.hatchFlags);
        }

        public Structure replace(String name, SimpleMember member, @Nullable HatchFlags flags) {
            List<BlockPos> positions = new ArrayList<>();
            for (var entry : template.simpleMembers.entrySet()) {
                SimpleMember other = entry.getValue();
                if (other instanceof VariableStructureMember structureMember && name.equals(structureMember.name())) {
                    positions.add(entry.getKey());
                }
            }
            if (positions.isEmpty()) {
                throw new IllegalArgumentException("No members with the name \"" + name + "\" could be found");
            }
            for (BlockPos pos : positions) {
                template.simpleMembers.put(pos, member);
                if (flags != null) {
                    template.hatchFlags.put(pos, flags);
                }
            }
            return this;
        }

        public Structure replace(String name, SimpleMember member) {
            return replace(name, member, null);
        }

        public ShapeTemplate build() {
            for (SimpleMember member : template.simpleMembers.values()) {
                if (member instanceof VariableStructureMember structureMember) {
                    throw new IllegalArgumentException(
                            "Tried to build structure template without replacing member with the name \"" + structureMember.name() + "\"");
                }
            }
            return template;
        }
    }
}
