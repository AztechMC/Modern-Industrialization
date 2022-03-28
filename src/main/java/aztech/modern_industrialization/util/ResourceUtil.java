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
package aztech.modern_industrialization.util;

import aztech.modern_industrialization.ModernIndustrialization;
import java.util.HashMap;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.resources.ResourceLocation;

public class ResourceUtil {
    private static final HashMap<ResourceLocation, JTag> tags = new HashMap<>();

    /**
     * Append a value to a tag. Will only work if all calls go through this
     * function.
     */
    public static synchronized void appendToTag(ResourceLocation tagId, ResourceLocation elementId) {
        // We use a copy-on-write strategy to update the JTag every time with the added
        // entry.
        JTag jtag = tags.computeIfAbsent(tagId, id -> JTag.tag());
        jtag.add(elementId);
        ModernIndustrialization.RESOURCE_PACK.addTag(tagId, jtag);
    }

    public static void appendToItemTag(ResourceLocation tagId, ResourceLocation elementId) {
        appendToTag(new ResourceLocation(tagId.getNamespace(), "items/" + tagId.getPath()), elementId);
    }

    public static synchronized void appendToTag(String tagId, String elementId) {
        appendToTag(new ResourceLocation(tagId), new ResourceLocation(elementId));
    }

    /**
     * Register a tag in the runtime data pack, and also create it if it doesn't
     * exist yet.
     */
    public static void registerItemTag(String tag, JTag content) {
        ResourceLocation tagId = new ResourceLocation(tag);
        ModernIndustrialization.RESOURCE_PACK.addTag(new ResourceLocation(tagId.getNamespace(), "items/" + tagId.getPath()), content);
    }
}
