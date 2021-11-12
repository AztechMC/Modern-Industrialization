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
import aztech.modern_industrialization.mixin.ResourceImplAccessor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

public class ResourceUtil {
    public static byte[] getBytes(Resource resource) throws IOException {
        InputStream is = resource.getInputStream();
        byte[] textureBytes = IOUtils.toByteArray(is);
        ((ResourceImplAccessor) resource).setInputStream(new ByteArrayInputStream(textureBytes));
        return textureBytes;
    }

    private static final HashMap<Identifier, JTag> tags = new HashMap<>();

    /**
     * Append a value to a tag. Will only work if all calls go through this
     * function.
     */
    public static synchronized void appendToTag(Identifier tagId, Identifier elementId) {
        // We use a copy-on-write strategy to update the JTag every time with the added
        // entry.
        JTag jtag = tags.computeIfAbsent(tagId, id -> JTag.tag());
        jtag.add(elementId);
        ModernIndustrialization.RESOURCE_PACK.addTag(tagId, jtag);
    }

    public static void appendToItemTag(Identifier tagId, Identifier elementId) {
        appendToTag(new Identifier(tagId.getNamespace(), "items/" + tagId.getPath()), elementId);
    }

    public static synchronized void appendTagToTag(Identifier tagId, Identifier subtag) {
        // We use a copy-on-write strategy to update the JTag every time with the added
        // entry.
        JTag jtag = tags.computeIfAbsent(tagId, id -> JTag.tag());
        jtag.tag(subtag);
        ModernIndustrialization.RESOURCE_PACK.addTag(tagId, jtag);
    }

    public static synchronized void appendTagToTag(String tagId, String subtag) {
        appendTagToTag(new Identifier((tagId)), new Identifier(subtag));
    }

    public static synchronized void appendToTag(String tagId, String elementId) {
        appendToTag(new Identifier(tagId), new Identifier(elementId));
    }
}
