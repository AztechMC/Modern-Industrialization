package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.NuclearAbsorbable;
import aztech.modern_industrialization.nuclear.NuclearConstant;

import static aztech.modern_industrialization.materials.part.MIParts.FUEL_ROD;

public class ControlRodPart implements PartKeyProvider {

    @Override
    public PartKey key() {
        return FUEL_ROD.key;
    }

    public PartTemplate of(int maxTemperature, double heatConduction, double thermalAbsorbProba, double fastAbsorbProba, double thermalScatteringProba, double fastScatteringProba, NuclearConstant.ScatteringType scatteringType, double size) {
        return new PartTemplate("Control Rod", key()).withRegister((partContext, part, itemPath1, itemId, itemTag, itemEnglishName) -> NuclearAbsorbable
                        .of(partContext.getMaterialEnglishName() + " Control Rod", itemPath1, maxTemperature, heatConduction * NuclearConstant.BASE_HEAT_CONDUCTION,
                                INeutronBehaviour.of(scatteringType,
                                        new NuclearConstant.IsotopeParams(thermalAbsorbProba, fastAbsorbProba, thermalScatteringProba, fastScatteringProba), size),
                                NuclearConstant.DESINTEGRATION_BY_ROD))
                .withCustomPath("%s_control_rod");
    }

    public PartTemplate of(int maxTemperature, double heatConduction, NuclearConstant.ScatteringType scatteringType, NuclearConstant.IsotopeParams params, double size) {
        return of(maxTemperature, heatConduction,
                params.thermalAbsorption, params.fastAbsorption, params.thermalScattering, params.fastScattering,
                scatteringType, size);
    }

}
