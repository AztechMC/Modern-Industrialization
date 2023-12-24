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
package aztech.modern_industrialization.datagen.tag;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class TagsToGenerate {

    static final Map<String, List<ItemLike>> tagToItemMap = new HashMap<>();
    static final Set<String> optionalTags = new HashSet<>();
    public static final Map<String, String> tagTranslations = new HashMap<>();
    static final Map<String, Set<String>> tagToBeAddedToAnotherTag = new HashMap<>();

    private static void addTranslation(String tag, String tagEnglishName) {
        var tagId = new ResourceLocation(tag);
        tagTranslations.put("tag.%s.%s".formatted(tagId.getNamespace(), tagId.getPath()).replace('/', '.'), tagEnglishName);
    }

    public static void generateTag(String tag, ItemLike item, String tagEnglishName) {
        if (tag.startsWith("#")) {
            throw new IllegalArgumentException("Tag must not start with #: " + tag);
        }
        tagToItemMap.computeIfAbsent(tag, t -> new ArrayList<>()).add(item);
        addTranslation(tag, tagEnglishName);
    }

    public static void addTagToTag(String tagTobeAdded, String tagTarget, String targetEnglishName) {
        if (tagTobeAdded.startsWith("#")) {
            throw new IllegalArgumentException("Tag must not start with #: " + tagTobeAdded);
        }
        if (tagTarget.startsWith("#")) {
            throw new IllegalArgumentException("Tag must not start with #: " + tagTarget);
        }

        tagToBeAddedToAnotherTag.computeIfAbsent(tagTarget, t -> new TreeSet<>()).add(tagTobeAdded);
        addTranslation(tagTarget, targetEnglishName);
    }

    public static void generateTag(TagKey<Item> tag, ItemLike item, String tagEnglishName) {
        generateTag(tag.location().toString(), item, tagEnglishName);
    }

    public static void markTagOptional(TagKey<Item> tag) {
        optionalTags.add(tag.location().toString());
    }
}
