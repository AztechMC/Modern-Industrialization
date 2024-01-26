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
package aztech.modern_industrialization;

import aztech.modern_industrialization.blocks.creativestorageunit.CreativeStorageUnitBlockEntity;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerRecipe;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.blocks.storage.barrel.CreativeBarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.creativetank.CreativeTankBlockEntity;
import aztech.modern_industrialization.compat.ae2.AECompatCondition;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.proxy.CommonProxy;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Misc registry objects.
 */
public class MIRegistries {
    // Block entities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MI.ID);

    public static final Supplier<BlockEntityType<CreativeBarrelBlockEntity>> CREATIVE_BARREL_BE = BLOCK_ENTITIES.register("creative_barrel", () -> {
        return BlockEntityType.Builder.of(CreativeBarrelBlockEntity::new, MIBlock.CREATIVE_BARREL.get()).build(null);
    });
    public static final Supplier<BlockEntityType<CreativeTankBlockEntity>> CREATIVE_TANK_BE = BLOCK_ENTITIES.register("creative_tank", () -> {
        return BlockEntityType.Builder.of(CreativeTankBlockEntity::new, MIBlock.CREATIVE_TANK.get()).build(null);
    });
    public static final Supplier<BlockEntityType<CreativeStorageUnitBlockEntity>> CREATIVE_STORAGE_UNIT_BE = BLOCK_ENTITIES
            .register("creative_storage_unit", () -> {
                return BlockEntityType.Builder.of(CreativeStorageUnitBlockEntity::new, MIBlock.CREATIVE_STORAGE_UNIT.get()).build(null);
            });

    // Conditions
    public static final DeferredRegister<Codec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS,
            MI.ID);

    public static final Supplier<Codec<AECompatCondition>> AE_COMPAT_CONDITION = CONDITIONS.register("ae_compat_loaded",
            () -> AECompatCondition.CODEC);

    // Menus
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MI.ID);

    public static final Supplier<MenuType<ForgeHammerScreenHandler>> FORGE_HAMMER_MENU = MENUS.register("forge_hammer", () -> {
        return new MenuType<>(ForgeHammerScreenHandler::new, FeatureFlags.VANILLA_SET);
    });
    public static final Supplier<MenuType<? extends MachineMenuCommon>> MACHINE_MENU = MENUS.register("machine", () -> {
        return IMenuTypeExtension.create(CommonProxy.INSTANCE::createClientMachineMenu);
    })::get;

    // POIs
    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MI.ID);

    public static final DeferredHolder<PoiType, PoiType> INDUSTRIALIST_POI = POIS.register("industrialist", () -> {
        return new PoiType(Set.copyOf(MIBlock.FORGE_HAMMER.asBlock().getStateDefinition().getPossibleStates()), 1, 1);
    });

    // Recipe serializers
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MI.ID);

    public static final Supplier<RecipeSerializer<ForgeHammerRecipe>> FORGE_HAMMER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("forge_hammer",
            ForgeHammerRecipe.Serializer::new);

    // Recipe types
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MI.ID);

    public static final Supplier<RecipeType<ForgeHammerRecipe>> FORGE_HAMMER_RECIPE_TYPE = RECIPE_TYPES.register("forge_hammer",
            () -> RecipeType.simple(MI.id("forge_hammer")));

    // Creative tabs
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MI.ID);

    private static final Supplier<CreativeModeTab> TAB = TABS.register("general", () -> CreativeModeTab.builder()
            .title(MIText.ModernIndustrialization.text())
            .icon(() -> MIBlock.FORGE_HAMMER.asItem().getDefaultInstance())
            .displayItems((params, output) -> {
                MIItem.ITEM_DEFINITIONS.values().stream()
                        .sorted(Comparator.comparing(e -> e.sortOrder))
                        .forEach(output::accept);
            })
            .build());

    // Villager professions
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MI.ID);

    public static final Supplier<VillagerProfession> INDUSTRIALIST = VILLAGER_PROFESSIONS.register("industrialist", () -> {
        return new VillagerProfession(
                INDUSTRIALIST_POI.getId().toString(),
                e -> e.is(INDUSTRIALIST_POI.getId()),
                e -> e.is(INDUSTRIALIST_POI.getId()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_TOOLSMITH);
    });

    static void init(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        CONDITIONS.register(modBus);
        MENUS.register(modBus);
        POIS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        RECIPE_TYPES.register(modBus);
        TABS.register(modBus);
        VILLAGER_PROFESSIONS.register(modBus);
    }
}
