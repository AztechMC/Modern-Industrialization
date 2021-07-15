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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.io.IOException;
import java.util.function.Function;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

public class GemMaterialPart implements MaterialPart {

    private final String materialName;
    private final String itemPath;
    private final String itemId;
    private final String itemTag;
    private final Coloramp coloramp;

    public GemMaterialPart(String materialName, Coloramp coloramp) {
        this.materialName = materialName;
        this.coloramp = coloramp;
        this.itemPath = materialName;
        this.itemId = "modern_industrialization:" + itemPath;
        this.itemTag = "#c:" + itemPath;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of() {
        return ctx -> new GemMaterialPart(ctx.getMaterialName(), ctx.getColoramp());
    }

    @Override
    public String getPart() {
        return MIParts.GEM;
    }

    @Override
    public String getTaggedItemId() {
        return itemTag;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        MIItem.of(itemPath);
        MaterialHelper.registerItemTag("c:" + itemPath, JTag.tag().add(new Identifier(getItemId())));
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        String template = String.format("modern_industrialization:textures/materialsets/gems/%s.png", materialName);
        try {
            NativeImage image = mtm.getAssetAsTexture(template);
            TextureHelper.colorize(image, coloramp);
            String texturePath = String.format("modern_industrialization:textures/items/%s.png", itemPath);
            mtm.addTexture(texturePath, image);
            image.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
