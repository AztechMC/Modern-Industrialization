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
package aztech.modern_industrialization.mixin_client;

import aztech.modern_industrialization.mixin_impl.BasicDisplayExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Remove outputs of crafting recipes starting with namespace
 * {@code mi_output_hidden}. I'm sorry.
 */
@Mixin(value = BasicDisplay.class, remap = false)
public abstract class ReiBasicDisplayMixin implements BasicDisplayExtension {
    @Shadow
    protected List<EntryIngredient> outputs;

    @Shadow
    public abstract Optional<Identifier> getDisplayLocation();

    @Overwrite
    public List<EntryIngredient> getOutputEntries() {
        if (getDisplayLocation().isPresent() && getDisplayLocation().get().getNamespace().equals("mi_output_hidden")) {
            return Collections.emptyList(); // Magic!
        } else {
            return outputs;
        }
    }

    @Override
    public List<EntryIngredient> getActualOutputs() {
        return outputs;
    }
}
