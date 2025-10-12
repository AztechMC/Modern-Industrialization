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

import aztech.modern_industrialization.machines.models.MachineCasing;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable description of a multiblock shape.
 */
public class ShapeTemplate {
    public final Map<BlockPos, SimpleMember> simpleMembers = new HashMap<>();
    public final Map<BlockPos, HatchFlags> hatchFlags = new HashMap<>();
    public final MachineCasing hatchCasing;

    private ShapeTemplate(MachineCasing hatchCasing) {
        this.hatchCasing = hatchCasing;
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
}
