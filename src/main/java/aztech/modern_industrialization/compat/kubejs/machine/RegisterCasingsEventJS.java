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
package aztech.modern_industrialization.compat.kubejs.machine;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.datagen.model.MachineCasingImitations;
import aztech.modern_industrialization.machines.models.MachineCasings;
import dev.latvian.mods.kubejs.event.KubeEvent;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public class RegisterCasingsEventJS implements KubeEvent {
    @Deprecated
    public void register(String... names) {
        MI.LOGGER.warn("register is deprecated, use registerNamed instead");
        for (var name : names) {
            registerNamed(name, null);
        }
    }

    public void registerNamed(String name, String englishName) {
        if (name.contains(":")) {
            throw new IllegalArgumentException("Casing name cannot contain ':'.");
        }
        MachineCasings.create(name, englishName);
    }

    @Deprecated
    public void registerBlockImitation(String name, ResourceLocation block) {
        MI.LOGGER.warn("registerBlockImitation is deprecated, use registerNamedBlockImitation instead");
        registerNamedBlockImitation(name, null, block);
    }

    public void registerNamedBlockImitation(String name, String englishName, ResourceLocation block) {
        Objects.requireNonNull(block, "block may not be null");
        if (name.contains(":")) {
            throw new IllegalArgumentException("Casing name cannot contain ':'.");
        }

        MachineCasingImitations.imitationsToGenerate.put(MachineCasings.create(name, englishName), block);
    }
}
