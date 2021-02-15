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
package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.textures.MITextures;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import aztech.modern_industrialization.recipe.MIRecipes;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin implements ReloadableResourceManager {
    @Shadow
    @Final
    private ResourceType type;

    @Shadow
    public abstract void addPack(ResourcePack pack);

    @Inject(method = "beginMonitoredReload", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs,
            CallbackInfoReturnable<?> cir) {
        if (this.type == ResourceType.CLIENT_RESOURCES) {
            ModernIndustrialization.LOGGER.info("Creating generated texture resource pack.");
            long millis1 = System.currentTimeMillis();
            ResourcePack pack = MITextures.buildResourcePack(this);
            long millis2 = System.currentTimeMillis();
            ModernIndustrialization.LOGGER.info("Injecting generated texture resource pack. Took " + (millis2 - millis1) + " ms to build.");
            addPack(pack);
        } else if (this.type == ResourceType.SERVER_DATA) {
            ModernIndustrialization.LOGGER.info("Creating generated recipes pack.");
            long millis1 = System.currentTimeMillis();
            addPack(MIRecipes.buildGeneratedRecipesPack(this));
            long millis2 = System.currentTimeMillis();
            ModernIndustrialization.LOGGER.info("Injecting generated recipes pack. Took " + (millis2 - millis1) + " ms to build.");
        }
    }
}
