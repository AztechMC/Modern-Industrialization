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

import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.machines.components.LubricantHelper;
import aztech.modern_industrialization.machines.components.TooltipProvider;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.nuclear.NuclearAbsorbable;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import aztech.modern_industrialization.util.TextHelper;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("unused")
public class MITooltips {

    public static final List<TooltipAttachment> TOOLTIPS = new LinkedList<>();

    public static final Style DEFAULT_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
    public static final Style NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);

    private static final Map<Class<?>, Style> DEFAULT_ARGUMENT_STYLE = new HashMap<>();

    static {
        DEFAULT_ARGUMENT_STYLE.put(Integer.class, NUMBER_TEXT);
        DEFAULT_ARGUMENT_STYLE.put(Long.class, NUMBER_TEXT);
        DEFAULT_ARGUMENT_STYLE.put(Float.class, NUMBER_TEXT);
        DEFAULT_ARGUMENT_STYLE.put(Double.class, NUMBER_TEXT);
    }

    public static void attachTooltip(ItemStack stack, List<Component> lines) {
        Item item = stack.getItem();
        if (item != null) {
            boolean hasPrintRequiredShift = false;
            for (var tooltip : TOOLTIPS) {
                if (tooltip.addTooltip.test(item)) {
                    if (!tooltip.requiresShift || Screen.hasShiftDown()) {
                        lines.addAll(tooltip.tooltipLines.apply(stack));
                    } else if (tooltip.requiresShift && !hasPrintRequiredShift) {
                        lines.add(MIText.TooltipsShiftRequired.text().setStyle(DEFAULT_STYLE));
                        hasPrintRequiredShift = true;
                    }
                }
            }
        }
    }

    // Parser

    public static final Parser<Object> DEFAULT_PARSER = new Parser<>() {
        @Override
        Component parse(Object o) {
            Style style = DEFAULT_STYLE;
            for (var entry : DEFAULT_ARGUMENT_STYLE.entrySet()) {
                if (o.getClass().isAssignableFrom(entry.getKey())) {
                    style = entry.getValue();
                    break;
                }
            }

            if (o instanceof Fluid f) {
                return FLUID_PARSER.parse(f);
            }

            return new TextComponent(String.valueOf(o)).withStyle(style);
        }
    };

    public static final Parser<Number> EU_PER_TICK_PARSER = new Parser<>() {
        @Override
        Component parse(Number number) {
            TextHelper.Amount amount = TextHelper.getAmountGeneric(number);
            return MIText.EuT.text(amount.digit(), amount.unit()).withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Number> EU_PARSER = new Parser<>() {
        @Override
        Component parse(Number number) {
            TextHelper.Amount amount = TextHelper.getAmountGeneric(number);
            return MIText.Eu.text(amount.digit(), amount.unit()).withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Fluid> FLUID_PARSER = new Parser<>() {
        @Override
        Component parse(Fluid fluid) {
            return FluidVariantAttributes.getName(FluidVariant.of(fluid));
        }
    };

    // Tooltips

    public static final TooltipAttachment CABLES = TooltipAttachment.of(
            (item) -> item instanceof PipeItem pipe && MIPipes.electricityPipeTier.containsKey(pipe),
            itemStack -> {
                var tier = MIPipes.electricityPipeTier.get((PipeItem) itemStack.getItem());
                return new Line(MIText.EuCable).arg(tier.englishName).arg(tier.getMaxTransfer(), EU_PER_TICK_PARSER).build();
            });

    public static final TooltipAttachment COILS = TooltipAttachment.of(
            (item) -> item instanceof BlockItem blockItem && ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.containsKey(blockItem.getBlock()),
            (itemStack) -> {
                long eu = ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.get(((BlockItem) itemStack.getItem()).getBlock());
                return new Line(MIText.EbfMaxEu).arg(eu).build();
            });

    public static final TooltipAttachment CREATIVE_FLIGHT = TooltipAttachment.of(
            (item) -> item == MIItem.QUANTUM_CHESTPLATE.asItem() || item == MIItem.GRAVICHESTPLATE.asItem(),
            (itemStack) -> new Line(MIText.AllowCreativeFligth).build()).noShiftRequired();

    public static final TooltipAttachment LUBRICANT_BUCKET = TooltipAttachment.of(MIFluids.LUBRICANT.getBucket(),
            new Line(MIText.LubricantTooltip).arg(LubricantHelper.mbPerTick));

    public static final TooltipAttachment GUNPOWDER = TooltipAttachment.of(Items.GUNPOWDER, MIText.GunpowderUpgrade);

    public static final TooltipAttachment MACHINE_TOOLTIPS = TooltipAttachment.ofMultiline(
            (item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof MachineBlock machineBlock &&
                    machineBlock.BLOCK_ENTITY_INSTANCE instanceof TooltipProvider,
            (itemStack) -> ((TooltipProvider) ((MachineBlock) ((BlockItem) itemStack.getItem()).getBlock()).BLOCK_ENTITY_INSTANCE).getTooltips());

    public static final TooltipAttachment NUCLEAR = TooltipAttachment.ofMultiline(
            item -> item.asItem() instanceof NuclearAbsorbable,
            (itemStack) -> {
                List<Component> tooltips = new LinkedList<>();
                long remAbs = ((NuclearAbsorbable) itemStack.getItem()).getRemainingDesintegrations(itemStack);
                tooltips.add(new MITooltips.Line(MIText.RemAbsorption).arg(remAbs).arg(((NuclearAbsorbable) itemStack.getItem()).desintegrationMax)
                        .build());
                if (itemStack.getItem() instanceof NuclearFuel fuel) {
                    long totalEu = (long) fuel.totalEUbyDesintegration * fuel.desintegrationMax;
                    tooltips.add(new MITooltips.Line(MIText.BaseEuTotalStored).arg(totalEu, MITooltips.EU_PARSER).build());
                }
                return tooltips;
            });

    public static final TooltipAttachment ORES = TooltipAttachment.ofMultiline(
            (item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof OreBlock,
            (itemStack) -> {
                OreBlock oreBlock = (OreBlock) ((BlockItem) itemStack.getItem()).getBlock();
                List<Component> lines = new LinkedList<>();
                MIConfig config = MIConfig.getConfig();

                if (config.generateOres && !config.blacklistedOres.contains(oreBlock.materialName) && oreBlock.params.generate) {
                    lines.add(new Line(MIText.OreGenerationTooltipY).arg(-64).arg(oreBlock.params.maxYLevel).build());
                    lines.add(new Line(MIText.OreGenerationTooltipVeinFrequency).arg(oreBlock.params.veinsPerChunk).build());
                    lines.add(new Line(MIText.OreGenerationTooltipVeinSize).arg(oreBlock.params.veinSize).build());
                } else {
                    lines.add(new Line(MIText.OreNotGenerated).build());
                }
                return lines;
            });

    public static final TooltipAttachment SPEED_UPGRADES = TooltipAttachment.of(
            SpeedUpgrade.UPGRADES::containsKey,
            (itemStack) -> new Line(MIText.TooltipSpeedUpgrade).arg(SpeedUpgrade.UPGRADES.get(itemStack.getItem())).build());

    public static final TooltipAttachment UPGRADES = TooltipAttachment.of(
            UpgradeComponent.UPGRADES::containsKey,
            (itemStack) -> new Line(MIText.MachineUpgrade).arg(UpgradeComponent.UPGRADES.get(itemStack.getItem()), EU_PER_TICK_PARSER).build());

    public static class TooltipAttachment {

        public final Predicate<Item> addTooltip;
        public final Function<ItemStack, List<Component>> tooltipLines;
        public boolean requiresShift = true;

        public static TooltipAttachment ofMultiline(Predicate<Item> addTooltip, Function<ItemStack, List<Component>> tooltipLines) {
            return new TooltipAttachment(addTooltip, tooltipLines);
        }

        public static TooltipAttachment ofMultiline(ItemLike itemLike, Function<ItemStack, List<Component>> tooltips) {
            return new TooltipAttachment((item) -> item == itemLike.asItem(), tooltips);
        }

        public static TooltipAttachment of(Predicate<Item> addTooltip, Function<ItemStack, Component> tooltips) {
            return ofMultiline(addTooltip, (item -> List.of(tooltips.apply(item))));
        }

        public static TooltipAttachment of(ItemLike itemLike, Function<ItemStack, Component> tooltips) {
            return ofMultiline(itemLike, (item -> List.of(tooltips.apply(item))));
        }

        public static TooltipAttachment of(ItemLike itemLike, MIText text) {
            return of(itemLike, new Line(text));
        }

        public static TooltipAttachment of(ItemLike itemLike, Line line) {
            return of(itemLike, (item) -> line.build());
        }

        private TooltipAttachment(Predicate<Item> addTooltip, Function<ItemStack, List<Component>> tooltipLines) {
            this.addTooltip = addTooltip;
            this.tooltipLines = tooltipLines;
            MITooltips.TOOLTIPS.add(this);
        }

        public TooltipAttachment noShiftRequired() {
            this.requiresShift = false;
            return this;
        }

    }

    public static class Line {

        public final MIText baseText;
        public final Style baseStyle;

        public final List<Component> args = new LinkedList<>();

        public Line(MIText baseText, Style style) {
            this.baseText = baseText;
            this.baseStyle = style;
        }

        public Line(MIText baseText) {
            this(baseText, MITooltips.DEFAULT_STYLE);
        }

        public <T> Line arg(T arg, Parser<T> parser) {
            args.add(parser.parse(arg));
            return this;
        }

        public Line arg(Object arg) {
            arg(arg, MITooltips.DEFAULT_PARSER);
            return this;
        }

        public Component build() {
            return baseText.text(args.toArray()).withStyle(baseStyle);
        }

    }

    static abstract class Parser<T> {
        abstract Component parse(T t);
    }

}
