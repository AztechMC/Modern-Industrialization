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
package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.Part;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.util.Identifier;

public class MaterialHelper {

    public static boolean hasBlock(Part part) {
        for (Part s : MIParts.BLOCKS) {
            if (s.equals(part)) {
                return true;
            }
        }
        return false;
    }

    public static String getPartTag(String materialName, String part) {
        return "c:" + materialName + "_" + part + "s";
    }

    /**
     * Register a tag in the runtime data pack, and also create it if it doesn't
     * exist yet.
     */
    public static void registerItemTag(String tag, JTag content) {
        Identifier tagId = new Identifier(tag);
        ModernIndustrialization.RESOURCE_PACK.addTag(new Identifier(tagId.getNamespace(), "items/" + tagId.getPath()), content);
        TagRegistry.item(tagId);
    }

}
