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

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import java.util.function.Function;
import net.minecraft.item.Item;

public class PipeMaterialPart implements MaterialPart {
    protected final String materialName;
    private final PipeType type;
    private final String part;
    private final String itemId;
    protected final int color;
    private PipeNetworkType pipeType;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(PipeType type) {
        return ctx -> new PipeMaterialPart(ctx.getMaterialName(), type, ctx.getColor());
    }

    protected PipeMaterialPart(String materialName, PipeType type, int color) {
        this.materialName = materialName;
        this.type = type;
        this.part = type.partName;
        this.itemId = "modern_industrialization:pipe_" + type.internalName + "_" + materialName;
        this.color = color;
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public String getTaggedItemId() {
        return itemId;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register() {
        if (type == PipeType.ITEM) {
            MIPipes.INSTANCE.registerItemPipeType(materialName, color | 0xff000000);
        } else if (type == PipeType.FLUID) {
            MIPipes.INSTANCE.registerFluidPipeType(materialName, color | 0xff000000, 81000);
        }
    }

    @Override
    public Item getItem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerTextures(TextureManager textureManager) {
    }
}
