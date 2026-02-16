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

package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.MITags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public sealed interface PartItemPathFormatter {
    static String idFromPath(String path) {
        return "modern_industrialization:" + path;
    }

    String getPartItemPath(String materialName, PartKey partKey);

    @Nullable
    TagKey<Item> getPartItemTag(String materialName, PartKey partKey);

    default String getPartItemId(String materialName, PartKey partKey) {
        return "modern_industrialization:" + getPartItemPath(materialName, partKey);
    }

    record Default() implements PartItemPathFormatter {
        @Override
        public String getPartItemPath(String materialName, PartKey partKey) {
            return materialName + "_" + partKey.key;
        }

        @Override
        public @Nullable TagKey<Item> getPartItemTag(String materialName, PartKey partKey) {
            if (MIParts.TAGGED_PARTS.contains(partKey)) {
                return MITags.convention("%ss/%s".formatted(partKey.key, materialName));
            } else {
                return null;
            }
        }
    }

    record Overridden(String path, Function<String, @Nullable TagKey<Item>> materialNameToTag) implements PartItemPathFormatter {
        @Override
        public String getPartItemPath(String materialName, PartKey partKey) {
            if (path.contains("%s")) {
                return String.format(path, materialName);
            } else {
                return path;
            }
        }

        @Override
        public @Nullable TagKey<Item> getPartItemTag(String materialName, PartKey partKey) {
            return materialNameToTag.apply(materialName);
        }
    }
}
