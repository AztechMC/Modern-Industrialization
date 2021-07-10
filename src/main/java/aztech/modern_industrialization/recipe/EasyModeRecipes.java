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
package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.MIRuntimeResourcePack;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.util.ResourceUtil;
import java.io.IOException;
import java.util.Collection;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class EasyModeRecipes {

    public static void yes(MIRuntimeResourcePack pack, ResourceManager manager) {
        Collection<Identifier> possibleReplacements = manager.findResources("easy_mode/recipes", path -> path.endsWith(".json"));
        for (Identifier pathId : possibleReplacements) {
            // ModernIndustrialization.LOGGER.info(String.format("Easy Mode Recipe Found :
            // %s ", pathId.getPath()));
            try {
                byte[] bytes = ResourceUtil.getBytes(manager.getResource(pathId));
                String newPath = pathId.getPath().replaceFirst("easy_mode/recipes", "modern_industrialization/recipes");
                pack.addData(newPath, bytes);
                Identifier recipeId = new Identifier(newPath.replaceFirst("/", ":"));
                if (AssemblerRecipes.shouldConvertToAssembler(recipeId)) {
                    // ModernIndustrialization.LOGGER.info("ASSEMBLER {} ", newPath);
                    AssemblerRecipes.convertToAssembler(pack, recipeId, bytes, true);
                }
            } catch (IOException e) {
                ModernIndustrialization.LOGGER.warn("Failed to load easy mode recipe {}. Error: {}", pathId, e);
            }
        }

    }
}
