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
package aztech.modern_industrialization;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Class to work around Fabric not allowing ordering between loaded mods.
 * If KubeJS is loaded and the integration is enabled, we only init MI from the KubeJS plugin.
 */
public class MIStartup implements ModInitializer {
    private static boolean initialized = false;

    private static void initialize() {
        if (initialized) {
            throw new IllegalStateException("MIStartup#initialize should only be called once");
        }

        initialized = true;
        ModernIndustrialization.initialize();
    }

    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("kubejs")) {
            initialize();
        }
    }

    public static void onClientStartup() {
        // Sanity check
        if (!initialized) {
            throw new IllegalStateException("MI client init is called but MI hasn't been initialized yet?");
        }
    }

    public static void onKubejsPluginLoaded() {
        initialize();
    }
}
