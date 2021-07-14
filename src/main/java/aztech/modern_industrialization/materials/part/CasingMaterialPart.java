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

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.util.function.Function;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.texture.NativeImage;

public class CasingMaterialPart implements MaterialPart {

    private final Coloramp coloramp;
    private final String path;
    private final String id;
    private final String part;
    private final float blastResistance;
    private final String materialName;
    private final String materialSet;

    private CasingMaterialPart(String materialName, String part, String path, float blastResistance, String materialSet, Coloramp coloramp) {
        this.materialName = materialName;
        this.coloramp = coloramp;
        this.path = path;
        this.id = "modern_industrialization:" + path;
        this.blastResistance = blastResistance;
        this.part = part;
        this.materialSet = materialSet;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part, float blastResistance) {
        return ctx -> new CasingMaterialPart(ctx.getMaterialName(), part, ctx.getMaterialName() + "_" + part, blastResistance, ctx.getMaterialSet(),
                ctx.getColoramp());
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part, String path, float blastResistance) {
        return ctx -> new CasingMaterialPart(ctx.getMaterialName(), part, path, blastResistance, ctx.getMaterialSet(), ctx.getColoramp());
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part) {
        return of(part, 6.0f);
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part, String path) {
        return of(part, path, 6.0f);
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public String getTaggedItemId() {
        return id;
    }

    @Override
    public String getItemId() {
        return id;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        new MIBlock(path, FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f).resistance(blastResistance).breakByTool(FabricToolTags.PICKAXES, 0)
                .requiresTool());
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        try {
            NativeImage image = MITextures.generateTexture(mtm, part, materialSet, coloramp);
            if (part.equals(MIParts.MACHINE_CASING)) {
                MITextures.casingFromTexture(mtm, materialName, image);
                MITextures.tankFromTexture(mtm, materialName, image);
            } else if (part.equals(MIParts.MACHINE_CASING_SPECIAL) || part.equals(MIParts.MACHINE_CASING_PIPE)) {
                MITextures.casingFromTexture(mtm, path, image);
            }
            MITextures.appendTexture(mtm, image, path, true);
            image.close();

        } catch (Throwable throwable) {
            MITextures.logTextureGenerationError(throwable, path, materialSet, part);
        }

    }

}
