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
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.util.Identifier;

public class MaterialHelper {
    public static boolean hasBlock(String part) {
        return part.equals("block") || part.equals("ore") || part.equals("coil") || part.equals("machine_casing")
                || part.equals("machine_casing_pipe") || part.equals("machine_casing_special");
    }

    public static boolean isOre(String part) {
        return part.equals("ore");
    }

    public static String getPartTag(String materialName, String part) {
        return "c:" + materialName + "_" + part + "s";
    }

    public static String partWithOverlay(String partWithMabyeOverlay) {
        int len = partWithMabyeOverlay.length();

        if (partWithMabyeOverlay.endsWith("_magnetic")) {
            return partWithMabyeOverlay.substring(0, len - 9);
        } else if (partWithMabyeOverlay.startsWith("n_doped_")) {
            return partWithMabyeOverlay.substring(8, len);
        } else if (partWithMabyeOverlay.startsWith("p_doped_")) {
            return partWithMabyeOverlay.substring(8, len);
        } else {
            return partWithMabyeOverlay;
        }
    }

    public static String overlayWithOverlay(String partWithMabyeOverlay) {

        if (partWithMabyeOverlay.endsWith("_magnetic")) {
            return "magnetic";
        } else if (partWithMabyeOverlay.startsWith("n_doped_")) {
            return "n_doped";
        } else if (partWithMabyeOverlay.startsWith("p_doped_")) {
            return "p_doped";
        } else {
            return null;
        }
    }

    public static String overrideItemPath(String itemPath) {
        if (itemPath.equals("fire_clay_ingot")) {
            return "fire_clay_brick";
        } else if (itemPath.equals("bronze_machine_casing_special")) {
            return "bronze_plated_bricks";
        } else if (itemPath.equals("aluminum_machine_casing")) {
            return "advanced_machine_casing";
        } else if (itemPath.equals("aluminum_machine_casing_special")) {
            return "frostproof_machine_casing";
        } else if (itemPath.equals("invar_machine_casing_special")) {
            return "heatproof_machine_casing";
        } else if (itemPath.equals("stainless_steel_machine_casing_special")) {
            return "clean_stainless_steel_machine_casing";
        } else if (itemPath.equals("stainless_steel_machine_casing")) {
            return "turbo_machine_casing";
        }
        return itemPath;
    }

    /**
     * Register a tag in the runtime data pack, and also create it if it doesn't
     * exist yet.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void registerItemTag(String tag, JTag content) {
        Identifier tagId = new Identifier(tag);
        ModernIndustrialization.RESOURCE_PACK.addTag(new Identifier(tagId.getNamespace(), "items/" + tagId.getPath()), content);
        TagRegistry.item(tagId);
    }
}
