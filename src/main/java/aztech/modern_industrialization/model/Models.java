package aztech.modern_industrialization.model;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.model.block.MachineModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Models {
    public static class BlockModels {
        public static final MachineModel STEAM_BOILER = new MachineModel("steam_boiler", new MIIdentifier("blocks/casings/steam/bricked_bronze/"))
                .withFrontOverlay(new MIIdentifier("blocks/generators/boiler/coal/overlay_front"), new MIIdentifier("blocks/generators/boiler/coal/overlay_front_active"));
        public static final MachineModel STEAM_FURNACE = new MachineModel("steam_furnace", new MIIdentifier("blocks/casings/steam/bricked_bronze/"))
                .withFrontOverlay(new MIIdentifier("blocks/generators/boiler/coal/overlay_front"), new MIIdentifier("blocks/generators/boiler/coal/overlay_front_active"));

    }
    public static final UnbakedModel FLUID_SLOT = new FluidSlotModel();

    public static Map<Identifier, UnbakedModel> getModelMap() {
        Map<Identifier, UnbakedModel> modelMap = new HashMap<>();
        Field[] declaredFields = BlockModels.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                try {
                    MachineModel model = (MachineModel)field.get(null);
                    modelMap.put(new MIIdentifier("block/" + model.model_name), model);
                    modelMap.put(new MIIdentifier("item/" + model.model_name), model);
                } catch (IllegalAccessException e) {
                    ModernIndustrialization.LOGGER.error("Exception:", e);
                }
            }
        }
        modelMap.put(new MIIdentifier("item/fluid_slot"), FLUID_SLOT);
        return modelMap;
    }
}
