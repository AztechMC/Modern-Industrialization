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
package aztech.modern_industrialization.misc.runtime_datagen;

import aztech.modern_industrialization.resource.GeneratedPathPackResources;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

public class RuntimeResourcesHelper {
    public static final ThreadLocal<Object> IS_CREATING_SERVER_RELOAD_PACK = new ThreadLocal<>();

    public static PackResources createPack(PackType packType) {
        var generatedDirectory = FabricLoader.getInstance().getGameDir().resolve("modern_industrialization/generated_resources");
        return new GeneratedPathPackResources(generatedDirectory, packType);
    }

    public static void injectPack(PackType packType, List<PackResources> list) {
        // Try to inject right after "Fabric Mods" pack
        for (int i = 0; i < list.size(); ++i) {
            var pack = list.get(i);
            if (pack.packId().equals("fabric")) {
                list.add(i + 1, createPack(packType));
                return;
            }
        }
        // No "Fabric Mods" pack - inject after vanilla pack
        for (int i = 0; i < list.size(); ++i) {
            var pack = list.get(i);
            if (pack.packId().equals("vanilla")) {
                list.add(i + 1, createPack(packType));
                return;
            }
        }
        // Otherwise inject at end
        list.add(createPack(packType));
    }
}
