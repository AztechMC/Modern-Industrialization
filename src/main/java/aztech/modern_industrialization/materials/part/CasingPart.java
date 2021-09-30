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

import aztech.modern_industrialization.textures.MITextures;
import net.minecraft.client.texture.NativeImage;

public class CasingPart extends UnbuildablePartWithSomeDefaultParams<CasingPart.SpecialCasingPartParams, String> {

    public record SpecialCasingPartParams(float resistance, String itemPath) {
    };

    public CasingPart(String key) {
        super(key);
    }

    @Override
    public SpecialCasingPartParams getDefaultParams(String itemPath) {
        return new SpecialCasingPartParams(6, itemPath);
    }

    @Override
    public BuildablePart of(SpecialCasingPartParams materialParams) {
        RegularPart regPart = new RegularPart(this.key).asBlock(5, materialParams.resistance, 1)
                .withTextureRegister((mtm, partContext, part, itemPath) -> {
                    try {
                        NativeImage image = MITextures.generateTexture(mtm, part.key, partContext.getMaterialSet(), partContext.getColoramp());
                        if (part.equals(MIParts.MACHINE_CASING)) {
                            MITextures.casingFromTexture(mtm, partContext.getMaterialName(), image);
                            MITextures.tankFromTexture(mtm, partContext.getMaterialName(), image);
                        } else if (part.equals(MIParts.MACHINE_CASING_PIPE) || part.equals(MIParts.MACHINE_CASING_SPECIAL)) {
                            MITextures.casingFromTexture(mtm, itemPath, image);
                        }
                        MITextures.appendTexture(mtm, image, itemPath, true);
                        image.close();

                    } catch (Throwable throwable) {
                        MITextures.logTextureGenerationError(throwable, itemPath, partContext.getMaterialSet(), part.key);
                    }
                });
        if (materialParams.itemPath != null) {
            return regPart.withCustomPath(materialParams.itemPath, materialParams.itemPath);
        }
        return regPart;
    }

    public BuildablePart ofDefault() {
        return of(new SpecialCasingPartParams(6, null));
    }

    public BuildablePart of(float resistance, String itemPath) {
        return of(new SpecialCasingPartParams(resistance, itemPath));
    }

    public BuildablePart of(float resistance) {
        return of(new SpecialCasingPartParams(resistance, null));
    }

}
