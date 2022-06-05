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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.MaterialBuilder;

public class MIItemPart {

    public static MaterialPart of(Part part, String englishName, String itemPath) {

        String itemId = "modern_industrialization:" + itemPath;

        return new MaterialPart() {
            @Override
            public Part getPart() {
                return part;
            }

            @Override
            public String getTaggedItemId() {
                return itemId;
            }

            @Override
            public String getItemId() {
                return itemId;
            }

            @Override
            public void register(MaterialBuilder.PartContext context) {
                MIItem.item(englishName, itemPath, SortOrder.MATERIALS.and(context.getMaterialName()));
            }
        };
    }

}
