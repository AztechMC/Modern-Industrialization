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

import static aztech.modern_industrialization.materials.part.NuclearFuelPart.Type.*;

import aztech.modern_industrialization.materials.GemPart;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.coloramp.HotIngotColoramp;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MIParts {

    public static final RegularPart BATTERY = new RegularPart("Battery", "battery");
    public static final BarrelPart BARREL = new BarrelPart();
    public static final RegularPart BLADE = new RegularPart("Blade", "blade");
    public static final BlockPart BLOCK = new BlockPart();
    public static final RegularPart BOLT = new RegularPart("Bolt", "bolt");
    public static final CablePart CABLE = new CablePart();
    public static final RegularPart COIL = new RegularPart("Coil", "coil").asColumnBlock();
    public static final RegularPart CRUSHED_DUST = new RegularPart("Crushed Dust", "crushed_dust");
    public static final RegularPart CURVED_PLATE = new RegularPart("Curved Plate", "curved_plate");
    public static final RegularPart DOUBLE_INGOT = new RegularPart("Double Ingot", "double_ingot")
            .withTextureRegister((mtm, partContext, part, itemPath) -> mtm.runAtEnd(() -> {
                try {
                    MITextures.generateDoubleIngot(mtm, partContext.getMaterialName());
                } catch (Throwable throwable) {
                    MITextures.logTextureGenerationError(throwable, partContext.getMaterialName(), partContext.getMaterialSet(), part.key);
                }
            }));

    public static final RegularPart DRILL_HEAD = new RegularPart("Drill Head", "drill_head");

    public static final RegularPart DRILL = new RegularPart("Drill", "drill").appendTextureRegister((mtm, partContext, part, itemPath) -> {
        String template = "modern_industrialization:textures/materialsets/common/drill.png";
        String templateOverlay = "modern_industrialization:textures/materialsets/common/mining_drill_overlay.png";
        String texturePath = String.format("modern_industrialization:textures/item/%s.png", partContext.getMaterialName() + "_mining_drill");
        try {
            NativeImage image = mtm.getAssetAsTexture(template);
            NativeImage overlay = mtm.getAssetAsTexture(templateOverlay);
            TextureHelper.colorize(image, partContext.getColoramp());
            mtm.addTexture(texturePath, TextureHelper.blend(image, overlay), true);
            image.close();
            overlay.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    public static final RegularPart DUST = new RegularPart("Dust", "dust");
    public static final RegularPart FINE_WIRE = new RegularPart("Fine Wire", "fine_wire");
    public static final RegularPart GEAR = new RegularPart("Gear", "gear");
    public static final GemPart GEM = new GemPart();

    public static final RegularPart HAMMER = new RegularPart("Hammer", "hammer");

    public static final RegularPart HOT_INGOT = new RegularPart("Hot Ingot", "hot_ingot")
            .withTextureRegister((mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm, MIParts.INGOT.key,
                    partContext.getMaterialSet(), itemPath, false, new HotIngotColoramp(partContext.getColoramp(), 0.1, 0.5)));
    public static final RegularPart INGOT = new RegularPart("Ingot", "ingot");
    public static final RegularPart LARGE_PLATE = new RegularPart("Large Plate", "large_plate");
    public static final CasingPart MACHINE_CASING = new CasingPart("Machine Casing", "machine_casing");
    public static final CasingPart MACHINE_CASING_PIPE = new CasingPart("Pipe Machine Casing", "machine_casing_pipe");
    public static final CasingPart MACHINE_CASING_SPECIAL = new CasingPart("Special Casing", "machine_casing_special");
    public static final RegularPart NUGGET = new RegularPart("Nugget", "nugget");
    public static final OrePart ORE = new OrePart(false);
    public static final OrePart ORE_DEEPLSATE = new OrePart(true);
    public static final RegularPart PLATE = new RegularPart("Plate", "plate");
    public static final RawMetalPart RAW_METAL = new RawMetalPart(false);
    public static final RawMetalPart RAW_METAL_BLOCK = new RawMetalPart(true);
    public static final RegularPart RING = new RegularPart("Ring", "ring");
    public static final RegularPart ROD = new RegularPart("Rod", "rod");
    public static final RegularPart ROD_MAGNETIC = new RegularPart("Magnetic %s Rod", "rod_magnetic").withOverlay(ROD, "magnetic");
    public static final RegularPart ROTOR = new RegularPart("Rotor", "rotor");
    public static final TankPart TANK = new TankPart();
    public static final RegularPart TINY_DUST = new RegularPart("Tiny Dust", "tiny_dust");
    public static final RegularPart WIRE = new RegularPart("Wire", "wire");
    public static final RegularPart WIRE_MAGNETIC = new RegularPart("Magnetic %s Wire", "wire_magnetic").withOverlay(WIRE, "magnetic");

    public static final NuclearFuelPart FUEL_ROD = new NuclearFuelPart(SIMPLE);
    public static final NuclearFuelPart FUEL_ROD_DEPLETED = new NuclearFuelPart(DEPLETED);
    public static final NuclearFuelPart FUEL_ROD_DOUBLE = new NuclearFuelPart(DOUBLE);
    public static final NuclearFuelPart FUEL_ROD_QUAD = new NuclearFuelPart(QUAD);

    public static final RegularPart N_DOPED_PLATE = new RegularPart("N-Doped %s Plate", "n_doped_plate").withOverlay(PLATE, "n_doped");
    public static final RegularPart P_DOPED_PLATE = new RegularPart("P-Doped %s Plate", "p_doped_plate").withOverlay(PLATE, "p_doped");

    public static final BuildablePart[] ITEM_PURE_NON_METAL = new BuildablePart[] { TINY_DUST, DUST, CRUSHED_DUST };
    public static final BuildablePart[] ITEM_PURE_METAL = new BuildablePart[] { INGOT, NUGGET, TINY_DUST, DUST };

    public static final List<Part> TAGGED_PARTS_LIST = Arrays.asList(BLOCK, DUST, GEAR, INGOT, NUGGET, ORE, PLATE, TINY_DUST, RAW_METAL,
            RAW_METAL_BLOCK);
    public static final Set<Part> TAGGED_PARTS = new HashSet<>(TAGGED_PARTS_LIST);

    public static final Part[] BLOCKS = { ORE, BARREL, BLOCK, COIL, MACHINE_CASING, MACHINE_CASING_SPECIAL, MACHINE_CASING_PIPE, RAW_METAL_BLOCK };

    public static String idOfPart(Part part, String materialName) {
        return "modern_industrialization:" + materialName + "_" + part.key;
    }

}
