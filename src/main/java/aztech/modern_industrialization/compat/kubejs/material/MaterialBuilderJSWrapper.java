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
package aztech.modern_industrialization.compat.kubejs.material;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MaterialItemPart;
import aztech.modern_industrialization.materials.part.NuclearFuelPart;
import aztech.modern_industrialization.materials.part.PartKey;
import aztech.modern_industrialization.materials.property.MaterialHardness;
import aztech.modern_industrialization.materials.property.MaterialProperty;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import aztech.modern_industrialization.materials.set.MaterialSet;
import aztech.modern_industrialization.nuclear.IsotopeFuelParams;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import com.google.gson.JsonObject;

class MaterialBuilderJSWrapper {

    protected MaterialBuilder materialBuilder;
    final private PartJsonCreator creator = new PartJsonCreator();

    public MaterialBuilderJSWrapper(MaterialBuilder materialBuilder) {
        this.materialBuilder = materialBuilder;
    }

    public MaterialBuilderJSWrapper hardness(String hardness) {
        materialBuilder.set(MaterialProperty.HARDNESS, MaterialHardness.valueOf(hardness.toUpperCase()));
        return this;
    }

    public MaterialBuilderJSWrapper materialSet(String materialSet) {
        materialBuilder.set(MaterialProperty.SET, MaterialSet.valueOf(materialSet.toUpperCase()));
        return this;
    }

    public MaterialBuilderJSWrapper setMainPart(String name) {
        materialBuilder.set(MaterialProperty.MAIN_PART, new PartKey(name));
        return this;
    }

    public MaterialBuilderJSWrapper addParts(String... name) {
        for (String s : name) {
            materialBuilder.addParts(creator.regularPart(s));
        }
        return this;
    }

    public MaterialBuilderJSWrapper addExternalPart(String part, String id, String tag) {
        materialBuilder.addMaterialItemParts(MaterialItemPart.external(new PartKey(part), tag, id));
        return this;
    }

    public MaterialBuilderJSWrapper addExternalPart(String part, String id) {
        return addExternalPart(part, id, id);
    }

    public MaterialBuilderJSWrapper battery(long energyCapacity) {
        materialBuilder.addParts(creator.batteryPart(energyCapacity));
        return this;
    }

    public MaterialBuilderJSWrapper barrel(int stackCapacity) {
        materialBuilder.addParts(creator.barrelPart(stackCapacity));
        return this;
    }

    public MaterialBuilderJSWrapper barrel(String englishName, String path, int stackCapacity) {
        materialBuilder.addParts(creator.barrelPart(englishName, path, stackCapacity));
        return this;
    }

    public MaterialBuilderJSWrapper block(String materialSet) {
        materialBuilder.addParts(creator.blockPart(materialSet));
        return this;
    }

    public MaterialBuilderJSWrapper cable(String tier) {
        materialBuilder.addParts(creator.cablePart(tier));
        return this;
    }

    public MaterialBuilderJSWrapper machineCasing(String englishName, String path) {
        materialBuilder.addParts(creator.machineCasing(englishName, path));
        return this;
    }

    public MaterialBuilderJSWrapper machineCasing(String englishName, String path, float resistance) {
        materialBuilder.addParts(creator.machineCasing(englishName, path, resistance));
        return this;
    }

    public MaterialBuilderJSWrapper machineCasing(float resistance) {
        materialBuilder.addParts(creator.machineCasing(resistance));
        return this;
    }

    public MaterialBuilderJSWrapper machineCasing() {
        materialBuilder.addParts(creator.machineCasing());
        return this;
    }

    public MaterialBuilderJSWrapper pipeCasing(float resistance) {
        materialBuilder.addParts(creator.pipeCasing(resistance));
        return this;
    }

    public MaterialBuilderJSWrapper pipeCasing() {
        materialBuilder.addParts(creator.pipeCasing());
        return this;
    }

    public MaterialBuilderJSWrapper specialCasing(String englishName, String path) {
        materialBuilder.addParts(creator.specialCasing(englishName, path));
        return this;
    }

    public MaterialBuilderJSWrapper specialCasing(String englishName, String path, float resistance) {
        materialBuilder.addParts(creator.specialCasing(englishName, path, resistance));
        return this;
    }

    public MaterialBuilderJSWrapper ore(JsonObject json, boolean deepslate) {
        materialBuilder.addParts(creator.orePart(json, deepslate));
        return this;
    }

    public MaterialBuilderJSWrapper ore(JsonObject json) {
        materialBuilder.addParts(creator.orePart(json, true));
        materialBuilder.addParts(creator.orePart(json, false));
        return this;
    }

    public MaterialBuilderJSWrapper rawMetal(String materialSet, boolean block) {
        materialBuilder.addParts(creator.rawMetalPart(materialSet, block));
        return this;
    }

    public MaterialBuilderJSWrapper rawMetal(String materialSet) {
        materialBuilder.addParts(creator.rawMetalPart(materialSet, true));
        materialBuilder.addParts(creator.rawMetalPart(materialSet, false));
        return this;
    }

    public MaterialBuilderJSWrapper tank(int bucketCapacity) {
        materialBuilder.addParts(creator.tankPart(bucketCapacity));
        return this;
    }

    public MaterialBuilderJSWrapper tank(String englishName, String path, int bucketCapacity) {
        materialBuilder.addParts(creator.tankPart(englishName, path, bucketCapacity));
        return this;
    }

    public MaterialBuilderJSWrapper customRegularPart(String englishName, String name) {
        materialBuilder.addParts(creator.customRegularPart(englishName, name));
        return this;
    }

    public MaterialBuilderJSWrapper defaultRecipes() {
        materialBuilder.addRecipes(StandardRecipes::apply);
        materialBuilder.addRecipes(SmeltingRecipes::apply);
        return this;
    }

    public MaterialBuilderJSWrapper forgeHammerRecipes() {
        materialBuilder.addRecipes(ForgeHammerRecipes::apply);
        return this;
    }

    public MaterialBuilderJSWrapper nuclearFuel(double thermalAbsorbProba, double thermalScatterings, int maxTemp, int tempLimitLow,
            int tempLimitHigh, double neutronsMultiplication, double directEnergyFactor) {
        materialBuilder.set(MaterialProperty.ISOTOPE, new IsotopeFuelParams(thermalAbsorbProba, thermalScatterings, maxTemp, tempLimitLow,
                tempLimitHigh, neutronsMultiplication, directEnergyFactor));
        return this;
    }

    public MaterialBuilderJSWrapper nuclearFuelMix(String a, String b, double factor) {
        materialBuilder.set(MaterialProperty.ISOTOPE, IsotopeFuelParams.mix(
                IsotopeFuelParams.of(MaterialRegistry.getMaterial(a)),
                IsotopeFuelParams.of(MaterialRegistry.getMaterial(b)),
                factor));
        return this;
    }

    public MaterialBuilderJSWrapper fuelRods() {
        materialBuilder.addParts(NuclearFuelPart.ofAll());
        return this;
    }

    public MaterialBuilderJSWrapper controlRod(int maxTemperature, double heatConduction, double thermalAbsorbProba, double fastAbsorbProba,
            double thermalScatteringProba, double fastScatteringProba, NuclearConstant.ScatteringType scatteringType, double size) {
        materialBuilder.addParts(creator.controlRodPart(maxTemperature, heatConduction, thermalAbsorbProba, fastAbsorbProba, thermalScatteringProba,
                fastScatteringProba, scatteringType, size));
        return this;
    }
}
