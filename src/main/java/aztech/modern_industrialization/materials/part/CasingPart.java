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
import aztech.modern_industrialization.textures.MITextures;
import com.mojang.blaze3d.platform.NativeImage;

public class CasingPart extends Part implements BuildablePart {

    public final String englishName;

    public CasingPart(String englishName, String key) {
        super(key);
        this.englishName = englishName;
    }

    @Override
    public Part getPart() {
        return this;
    }

    public BuildablePart of(String englishNameFormatter, String path, float resistance) {
        RegularPart regPart = new RegularPart(englishNameFormatter, this.key).asBlock(5, resistance, 1)
                .withTextureRegister((mtm, partContext, part, itemPath) -> {
                    try {
                        NativeImage image = MITextures.generateTexture(mtm, part.key, partContext.getMaterialSet(), partContext.getColoramp());

                        if (part.equals(MIParts.MACHINE_CASING)) {
                            MITextures.casingFromTexture(mtm, partContext.getMaterialName(), image);
                        } else {
                            MITextures.casingFromTexture(mtm, itemPath, image);
                        }
                        MITextures.appendTexture(mtm, image, itemPath, true);
                        image.close();

                    } catch (Throwable throwable) {
                        MITextures.logTextureGenerationError(throwable, itemPath, partContext.getMaterialSet(), part.key);
                    }
                });
        if (path != null) {
            return regPart.withCustomPath(path, path);
        }
        return regPart;
    }

    @Override
    public MaterialPart build(MaterialBuilder.PartContext ctx) {
        return of(englishName, null, 6f).build(ctx);
    }

    public BuildablePart of(String englishName, String path) {
        return of(englishName + "!", path, 6f);
    }

    public BuildablePart of(float resistance) {
        return of(englishName, null, resistance);
    }

}
