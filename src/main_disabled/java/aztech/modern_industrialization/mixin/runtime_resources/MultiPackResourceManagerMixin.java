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
package aztech.modern_industrialization.mixin.runtime_resources;

import aztech.modern_industrialization.misc.runtime_datagen.RuntimeResourcesHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {
    @Final
    @Mutable
    @Shadow
    private List<PackResources> packs;

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "java/util/List.copyOf (Ljava/util/Collection;)Ljava/util/List;", shift = At.Shift.AFTER, by = 1), method = "<init>")
    private void injectRuntimeResources(PackType packType, List<PackResources> list, CallbackInfo ci) {
        if (RuntimeResourcesHelper.IS_CREATING_SERVER_RELOAD_PACK.get() != null) {
            RuntimeResourcesHelper.IS_CREATING_SERVER_RELOAD_PACK.remove();
            packs = new ArrayList<>(packs);
            RuntimeResourcesHelper.injectPack(packType, packs);
        }
    }
}
