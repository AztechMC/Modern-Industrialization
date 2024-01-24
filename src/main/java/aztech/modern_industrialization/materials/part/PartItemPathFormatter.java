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

public sealed interface PartItemPathFormatter {

    static String idFromPath(String path) {
        return "modern_industrialization:" + path;
    }

    String getPartItemPath(String materialName, PartKey partKey);

    String getPartItemTag(String materialName, PartKey partKey);

    default String getPartItemId(String materialName, PartKey partKey) {
        return "modern_industrialization:" + getPartItemPath(materialName, partKey);
    }

    record Default() implements PartItemPathFormatter {

        @Override
        public String getPartItemPath(String materialName, PartKey partKey) {
            return materialName + "_" + partKey.key;
        }

        @Override
        public String getPartItemTag(String materialName, PartKey partKey) {
            if (MIParts.TAGGED_PARTS.contains(partKey)) {
                return "#forge:%ss/%s".formatted(partKey.key, materialName);
            } else {
                return idFromPath(getPartItemPath(materialName, partKey));
            }
        }
    }

    record Overridden(String path, String tag) implements PartItemPathFormatter {

        @Override
        public String getPartItemPath(String materialName, PartKey partKey) {
            if (path.contains("%s")) {
                return String.format(path, materialName);
            } else {
                return path;
            }
        }

        @Override
        public String getPartItemTag(String materialName, PartKey partKey) {
            if (MIParts.TAGGED_PARTS.contains(partKey)) {
                if (tag.contains("%s")) {
                    return "#forge:" + String.format(tag, materialName);
                } else {
                    return tag;
                }
            } else {
                return idFromPath(getPartItemPath(materialName, partKey));
            }
        }
    }

}
