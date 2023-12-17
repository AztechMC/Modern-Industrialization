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
package aztech.modern_industrialization.compat.sodium;

import aztech.modern_industrialization.ModernIndustrialization;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

public class SodiumCompat {
    @Nullable
    private static final MethodHandle METHOD_HANDLE;

    static {
        MethodHandle handle = null;
        try {
            handle = MethodHandles.lookup().findStatic(
                    Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil"),
                    "markSpriteActive",
                    MethodType.methodType(void.class, TextureAtlasSprite.class));
            ModernIndustrialization.LOGGER.info("Loaded Modern Industrialization Sodium active sprite compat.");
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            if (FabricLoader.getInstance().isModLoaded("sodium")) {
                ModernIndustrialization.LOGGER.error("Failed to load Modern Industrialization Sodium active sprite compat.", e);
            }
        }

        METHOD_HANDLE = handle;
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (sprite != null && METHOD_HANDLE != null) {
            try {
                METHOD_HANDLE.invokeExact(sprite);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to invoke SpriteUtil#markSpriteActive", e);
            }
        }
    }
}
