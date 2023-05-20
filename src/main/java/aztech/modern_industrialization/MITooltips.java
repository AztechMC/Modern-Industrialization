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
import aztech.modern_industrialization.definition.FluidLike;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.machines.components.LubricantHelper;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.nuclear.NuclearAbsorbable;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.base.Preconditions;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class MITooltips {

    public static final PriorityQueue<TooltipAttachment> TOOLTIPS = new PriorityQueue<>();

    public static final Style DEFAULT_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
    public static final Style NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);

    private static final Map<Class<?>, Style> DEFAULT_ARGUMENT_STYLE = new HashMap<>();

    public static int colorFromProgress(double progress, boolean zeroIsGreen) {
        // clip to [0, 1]
        progress = Math.max(0, Math.min(1, progress));
        if (!zeroIsGreen) {
            progress = 1 - progress;
        }

        double r = Math.min(2 * progress, 1);
        double g = Math.min(1, 2 - 2 * progress);
        return (int) (r * 255) << 16 | (int) (g * 255) << 8;
    }

    public static Style styleFromProgress(double progress, boolean zeroIsGreen) {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorFromProgress(progress, zeroIsGreen))).withItalic(false);
    }

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
                    if (!tooltip.requiresShift || CommonProxy.INSTANCE.hasShiftDown()) {
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
        public Component parse(Object o) {
            Style style = DEFAULT_STYLE;
            for (var entry : DEFAULT_ARGUMENT_STYLE.entrySet()) {
                if (o.getClass().isAssignableFrom(entry.getKey())) {
                    style = entry.getValue();
                    break;
                }
            }

            if (o instanceof Fluid f) {
                return FLUID_PARSER.parse(f);
            } else if (o instanceof FluidLike f) {
                return FLUID_PARSER.parse(f.asFluid());
            }

            return Component.literal(String.valueOf(o)).withStyle(style);
        }
    };

    public static final Parser<Number> EU_PER_TICK_PARSER = new Parser<>() {
        @Override
        public Component parse(Number number) {
            TextHelper.Amount amount = TextHelper.getAmountGeneric(number);
            return MIText.EuT.text(amount.digit(), amount.unit()).withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Number> EU_PARSER = new Parser<>() {
        @Override
        public Component parse(Number number) {
            TextHelper.Amount amount = TextHelper.getAmountGeneric(number);
            return MIText.Eu.text(amount.digit(), amount.unit()).withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Fluid> FLUID_PARSER = new Parser<>() {
        @Override
        public Component parse(Fluid fluid) {
            return FluidVariantAttributes.getName(FluidVariant.of(fluid));
        }
    };

    public static final Parser<Double> RATIO_PERCENTAGE_PARSER = new Parser<>() {
        @Override
        public Component parse(Double ratio) {
            String percentage = String.format("%.1f", ratio * 100);
            return Component.literal(percentage + "%").withStyle(styleFromProgress(ratio, false));
        }
    };

    public static final Parser<Double> INVERTED_RATIO_PERCENTAGE_PARSER = new Parser<>() {
        @Override
        public Component parse(Double ratio) {
            String percentage = String.format("%.1f", ratio * 100);
            return Component.literal(percentage + "%").withStyle(styleFromProgress(ratio, true));
        }
    };

    public static final Parser<BlockState> BLOCK_STATE_PARSER = state -> {
        return state.getBlock().getName().withStyle(NUMBER_TEXT);
    };

    public static final Parser<Item> ITEM_PARSER = state -> {
        return state.getDefaultInstance().getHoverName().copy().withStyle(NUMBER_TEXT);
    };

    // Tooltips

    public static final TooltipAttachment CABLES = TooltipAttachment
            .of((item) -> item instanceof PipeItem pipe && MIPipes.ELECTRICITY_PIPE_TIER.containsKey(pipe), itemStack -> {
                var tier = MIPipes.ELECTRICITY_PIPE_TIER.get((PipeItem) itemStack.getItem());
                return new Line(MIText.EuCable).arg(tier.englishName).arg(tier.getMaxTransfer(), EU_PER_TICK_PARSER).build();
            });

    public static final TooltipAttachment COILS = TooltipAttachment.of(
            (item) -> item instanceof BlockItem blockItem
                    && ElectricBlastFurnaceBlockEntity.tiersByCoil.containsKey(Registry.BLOCK.getKey(blockItem.getBlock())),
            (itemStack) -> {
                long eu = ElectricBlastFurnaceBlockEntity.tiersByCoil.get(Registry.BLOCK.getKey(((BlockItem) itemStack.getItem()).getBlock()))
                        .maxBaseEu();
                return new Line(MIText.EbfMaxEu).arg(eu).build();
            });

    public static final TooltipAttachment CREATIVE_FLIGHT = TooltipAttachment
            .of((item) -> item == MIItem.QUANTUM_CHESTPLATE.asItem() || item == MIItem.GRAVICHESTPLATE.asItem(),
                    (itemStack) -> new Line(MIText.AllowCreativeFligth).build())
            .noShiftRequired();

    public static final TooltipAttachment LUBRICANT_BUCKET = TooltipAttachment.of(MIFluids.LUBRICANT.getBucket(),
            new Line(MIText.LubricantTooltip).arg(LubricantHelper.mbPerTick));

    public static final TooltipAttachment GUNPOWDER = TooltipAttachment.of(Items.GUNPOWDER, MIText.GunpowderUpgrade);

    public static final TooltipAttachment MACHINE_TOOLTIPS = TooltipAttachment.ofMultiline(
            (item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof MachineBlock machineBlock
                    && !machineBlock.getBlockEntityInstance().getTooltips().isEmpty(),
            (itemStack) -> (((MachineBlock) ((BlockItem) itemStack.getItem()).getBlock()).getBlockEntityInstance()).getTooltips());

    public static final TooltipAttachment NUCLEAR = TooltipAttachment.ofMultiline(item -> item.asItem() instanceof NuclearAbsorbable, (itemStack) -> {
        List<Component> tooltips = new LinkedList<>();
        long remAbs = ((NuclearAbsorbable) itemStack.getItem()).getRemainingDesintegrations(itemStack);
        tooltips.add(new MITooltips.Line(MIText.RemAbsorption).arg(remAbs).arg(((NuclearAbsorbable) itemStack.getItem()).desintegrationMax).build());
        if (itemStack.getItem() instanceof NuclearFuel fuel) {
            long totalEu = (long) fuel.totalEUbyDesintegration * fuel.desintegrationMax;
            tooltips.add(new MITooltips.Line(MIText.BaseEuTotalStored).arg(totalEu, MITooltips.EU_PARSER).build());
        }
        return tooltips;
    });

    public static final TooltipAttachment ORES = TooltipAttachment
            .ofMultiline((item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof OreBlock, (itemStack) -> {
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

    public static final TooltipAttachment SPEED_UPGRADES = TooltipAttachment.of(SpeedUpgrade.UPGRADES::containsKey,
            (itemStack) -> new Line(MIText.TooltipSpeedUpgrade).arg(SpeedUpgrade.UPGRADES.get(itemStack.getItem())).build());

    public static final TooltipAttachment UPGRADES = TooltipAttachment.ofMultiline(item -> UpgradeComponent.getExtraEu(item) > 0, (itemStack) -> {
        List<Component> lines = new LinkedList<>();
        lines.add(new Line(MIText.MachineUpgrade).arg(UpgradeComponent.getExtraEu(itemStack.getItem()), EU_PER_TICK_PARSER).build());

        if (itemStack.getCount() > 1) {
            lines.add(new Line(MIText.MachineUpgradeStack)
                    .arg(itemStack.getCount() * UpgradeComponent.getExtraEu(itemStack.getItem()), EU_PER_TICK_PARSER).build());
        }
        return lines;
    });

    public static final TooltipAttachment STEAM_DRILL = TooltipAttachment.ofMultiline(MIItem.STEAM_MINING_DRILL,
            MIText.SteamDrillWaterHelp,
            MIText.SteamDrillFuelHelp,
            MIText.SteamDrillProfit,
            MIText.SteamDrillToggle);

    public static final TooltipAttachment CONFIG_CARD_HELP = TooltipAttachment.ofMultiline(MIItem.CONFIG_CARD,
            MIText.ConfigCardHelpCamouflage1,
            MIText.ConfigCardHelpCamouflage2,
            MIText.ConfigCardHelpCamouflage3,
            MIText.ConfigCardHelpCamouflage4,
            MIText.ConfigCardHelpCamouflage5,
            MIText.ConfigCardHelpCamouflage6,
            MIText.ConfigCardHelpCamouflage7,
            MIText.ConfigCardHelpCamouflage8,
            MIText.ConfigCardHelpItems1,
            MIText.ConfigCardHelpItems2,
            MIText.ConfigCardHelpItems3,
            MIText.ConfigCardHelpItems4,
            MIText.ConfigCardHelpItems5,
            MIText.ConfigCardHelpClear);

    // Long Tooltip with only text, no need of MIText

    public static final Map<String, String> TOOLTIPS_ENGLISH_TRANSLATION = new HashMap<>();

    private static void add(Predicate<ItemLike> attachTo, String translationId, String... englishTooltipsLine) {
        int lineCount = englishTooltipsLine.length;

        Preconditions.checkArgument(lineCount > 0);

        String[] translationKey = IntStream.range(0, lineCount).mapToObj(l -> "item_tooltip.modern_industrialization." + translationId + ".line_" + l)
                .toArray(String[]::new);

        for (int i = 0; i < lineCount; i++) {
            TOOLTIPS_ENGLISH_TRANSLATION.put(translationKey[i], englishTooltipsLine[i]);
        }

        MITooltips.TooltipAttachment.ofMultiline(attachTo::test, itemStack -> Arrays.stream(translationKey)
                .map(s -> Component.translatable(s).withStyle(MITooltips.DEFAULT_STYLE)).collect(Collectors.toList()));
    }

    private static void add(ItemLike itemLike, String... englishTooltipsLine) {
        add((item) -> itemLike.asItem() == item, Registry.ITEM.getKey(itemLike.asItem()).getPath(), englishTooltipsLine);
    }

    private static void add(String itemId, String... englishTooltipsLine) {
        add(Registry.ITEM.get(new MIIdentifier(itemId)), englishTooltipsLine);
    }

    static {
        add(MIBlock.FORGE_HAMMER, "Use it to increase the yield of your ore blocks early game!",
                "(Use the Steam Mining Drill for an easy to get Silk Touch.)");
        add("stainless_steel_dust", "Use Slot-Locking with REI to differentiate its recipe from the invar dust");
        add("steam_blast_furnace", "Needs at least one Steel or higher tier", "hatch for 3 and 4 EU/t recipes");
        add(MIBlock.TRASH_CAN, "Will delete any item or fluid sent into it.", "Can also be used to empty a fluid slot",
                "by Right-Clicking on it with a Trash Can");

        add(itemLike -> itemLike.asItem() instanceof PipeItem pipe && (pipe.isItemPipe() || pipe.isFluidPipe()), "pipe",
                "Can be instantly retrieved by", "Right-Clicking with any Wrench.", "Use Shift + Right-Click to connect ",
                "directly the pipe to the target block.");

        add(itemLike -> itemLike.asItem() instanceof PipeItem pipe && pipe.isCable(), "cable", " ", "Can power blocks from any mod, but can",
                "only extract energy from Modern", "Industrialization blocks and machines.", "They also are the only cables able",
                "to power Modern Industrialization machines.");

    }

    public static class TooltipAttachment implements Comparable<TooltipAttachment> {

        public final Predicate<Item> addTooltip;
        public final Function<ItemStack, List<? extends Component>> tooltipLines;
        public boolean requiresShift = true;
        public int priority = 0;

        public static TooltipAttachment ofMultiline(Predicate<Item> addTooltip, Function<ItemStack, List<? extends Component>> tooltipLines) {
            return new TooltipAttachment(addTooltip, tooltipLines);
        }

        public static TooltipAttachment ofMultiline(ItemLike itemLike, Function<ItemStack, List<? extends Component>> tooltips) {
            return new TooltipAttachment((item) -> item == itemLike.asItem(), tooltips);
        }

        public static TooltipAttachment ofMultiline(ItemLike itemLike, MIText... tooltipLines) {
            Preconditions.checkArgument(tooltipLines.length > 0);
            var tooltip = Stream.of(tooltipLines).map(t -> new Line(t).build()).toList();
            return new TooltipAttachment(item -> item == itemLike.asItem(), stack -> tooltip);
        }

        public static TooltipAttachment of(Predicate<Item> addTooltip, Function<ItemStack, ? extends Component> tooltips) {
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

        private TooltipAttachment(Predicate<Item> addTooltip, Function<ItemStack, List<? extends Component>> tooltipLines) {
            this.addTooltip = addTooltip;
            this.tooltipLines = tooltipLines;
            MITooltips.TOOLTIPS.add(this);
        }

        public TooltipAttachment noShiftRequired() {
            this.requiresShift = false;
            return this;
        }

        public TooltipAttachment setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public int compareTo(@NotNull MITooltips.TooltipAttachment o) {
            return -Integer.compare(priority, o.priority);
        }
    }

    public static Line line(MIText baseText) {
        return new Line(baseText);
    }

    public static Line line(MIText baseText, Style style) {
        return new Line(baseText, style);
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

        public MutableComponent build() {
            return baseText.text(args.toArray()).withStyle(baseStyle);
        }
    }

    @FunctionalInterface
    public interface Parser<T> {
        Component parse(T t);
    }

}
