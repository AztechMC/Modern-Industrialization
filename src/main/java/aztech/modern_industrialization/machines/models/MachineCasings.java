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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("Convert2MethodRef")
public class MachineCasings {

    public static final Map<ResourceLocation, MachineCasing> registeredCasings = new HashMap<>();

    public record CasingName(MachineCasing casing, String englishName) {
    }

    public static final List<CasingName> translations = new ArrayList<>();

    public static final MachineCasing BRICKED_BRONZE = create(MI.id("bricked_bronze"), "Bricked Bronze");
    public static final MachineCasing BRICKED_STEEL = create(MI.id("bricked_steel"), "Bricked Steel");
    public static final MachineCasing BRICKS = createBlockImitation(MI.id("bricks"), () -> Blocks.BRICKS);
    public static final MachineCasing BRONZE = createBlockImitation(MI.id("bronze"),
            () -> MIMaterials.BRONZE.getPart(MIParts.MACHINE_CASING).asBlock());
    public static final MachineCasing BRONZE_PLATED_BRICKS = createBlockImitation(MI.id("bronze_plated_bricks"),
            () -> MIMaterials.BRONZE.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
    public static final MachineCasing CLEAN_STAINLESS_STEEL = createBlockImitation(MI.id("clean_stainless_steel_machine_casing"),
            () -> MIMaterials.STAINLESS_STEEL.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
    public static final MachineCasing CONFIGURABLE_TANK = create(MI.id("configurable_tank"), "Configurable Tank");
    public static final MachineCasing STAINLESS_STEEL_PIPE = createBlockImitation(MI.id("stainless_steel_machine_casing_pipe"),
            () -> MIMaterials.STAINLESS_STEEL.getPart(MIParts.MACHINE_CASING_PIPE).asBlock());
    public static final MachineCasing FIREBRICKS = createBlockImitation(MI.id("firebricks"), () -> MIBlock.BLOCK_FIRE_CLAY_BRICKS.asBlock());
    public static final MachineCasing FROSTPROOF = createBlockImitation(MI.id("frostproof_machine_casing"),
            () -> MIMaterials.ALUMINUM.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
    public static final MachineCasing HEATPROOF = createBlockImitation(MI.id("heatproof_machine_casing"),
            () -> MIMaterials.INVAR.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
    public static final MachineCasing STEEL = createBlockImitation(MI.id("steel"), () -> MIMaterials.STEEL.getPart(MIParts.MACHINE_CASING).asBlock());
    public static final MachineCasing STEEL_CRATE = create(MI.id("steel_crate"), "Steel Crate");
    public static final MachineCasing TITANIUM = createBlockImitation(MI.id("titanium"),
            () -> MIMaterials.TITANIUM.getPart(MIParts.MACHINE_CASING).asBlock());
    public static final MachineCasing TITANIUM_PIPE = createBlockImitation(MI.id("titanium_machine_casing_pipe"),
            () -> MIMaterials.TITANIUM.getPart(MIParts.MACHINE_CASING_PIPE).asBlock());
    public static final MachineCasing SOLID_TITANIUM = createBlockImitation(MI.id("solid_titanium_machine_casing"),
            () -> MIMaterials.TITANIUM.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
    public static final MachineCasing NUCLEAR = createBlockImitation(MI.id("nuclear_casing"),
            () -> MIMaterials.NUCLEAR_ALLOY.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
    public static final MachineCasing PLASMA_HANDLING_IRIDIUM = createBlockImitation(MI.id("plasma_handling_iridium_machine_casing"),
            () -> MIMaterials.IRIDIUM.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());

    static {
        KubeJSProxy.instance.fireRegisterMachineCasingsEvent();
    }

    public static MachineCasing createBlockImitation(ResourceLocation key, Supplier<? extends Block> block) {
        Objects.requireNonNull(block);
        return register(key, block);
    }

    public static MachineCasing create(ResourceLocation key, String englishName) {
        Objects.requireNonNull(englishName);
        var casing = register(key, null);
        translations.add(new CasingName(casing, englishName));
        return casing;
    }

    private static MachineCasing register(ResourceLocation key, @Nullable Supplier<? extends Block> blockImitation) {
        if (registeredCasings.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate machine casing definition: " + key);
        }

        MachineCasing casing = new MachineCasing(key, blockImitation);
        registeredCasings.put(key, casing);
        return casing;
    }

    public static MachineCasing get(ResourceLocation key) {
        MachineCasing casing = registeredCasings.get(key);
        if (casing != null) {
            return casing;
        } else {
            throw new IllegalArgumentException("Machine casing model \"" + key + "\" does not exist.");
        }
    }

    public static MachineCasing get(String name) {
        return get(ResourceLocation.isValidPath(name) ? MI.id(name) : ResourceLocation.parse(name));
    }
}
