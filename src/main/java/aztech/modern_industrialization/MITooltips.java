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

import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.definition.FluidLike;
import aztech.modern_industrialization.items.PortableStorageUnit;
import aztech.modern_industrialization.items.RedstoneControlModuleItem;
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
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                Optional<List<? extends Component>> maybeComponents = tooltip.tooltipLines.apply(stack, stack.getItem());
                if (!tooltip.requiresShift || CommonProxy.INSTANCE.hasShiftDown()) {
                    maybeComponents.ifPresent(lines::addAll);
                } else if (tooltip.requiresShift && !hasPrintRequiredShift) {
                    if (maybeComponents.isPresent()) {
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

            if (o instanceof Item) {
                return ITEM_PARSER.parse((Item) o);
            } else if (o instanceof ItemLike) {
                return ITEM_PARSER.parse(((ItemLike) o).asItem());
            }

            if (o instanceof Component c) {
                return c.copy().withStyle(style);
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

    public record NumberWithMax(Number number, Number max) {
    }

    public static final Parser<NumberWithMax> EU_MAXED_PARSER = new Parser<>() {
        @Override
        public Component parse(NumberWithMax numberWithMax) {
            MutableComponent component = TextHelper.getEuTextMaxed(numberWithMax.number, numberWithMax.max);
            return component.withStyle(NUMBER_TEXT);
        }
    };

    public static final Parser<Fluid> FLUID_PARSER = new Parser<>() {
        @Override
        public Component parse(Fluid fluid) {
            return fluid.getFluidType().getDescription();
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

    public static final Parser<Component> COMPONENT = state -> state;

    // Tooltips

    public static final TooltipAttachment BATTERIES = TooltipAttachment.of(
            (itemStack, item) -> {
                if (PortableStorageUnit.CAPACITY_PER_BATTERY.containsKey(item)) {
                    var capacity = PortableStorageUnit.CAPACITY_PER_BATTERY.get(itemStack.getItem());
                    return Optional.of(new Line(MIText.BatteryInStorageUnit).arg(capacity, EU_PARSER).build());
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment CABLES = TooltipAttachment.of(
            (itemStack, item) -> {
                if (item instanceof PipeItem pipe && MIPipes.ELECTRICITY_PIPE_TIER.containsKey(pipe.type)) {
                    var tier = MIPipes.ELECTRICITY_PIPE_TIER.get(pipe.type);
                    return Optional.of(new Line(MIText.EuCable).arg(tier.shortEnglishName()).arg(tier.getMaxTransfer(), EU_PER_TICK_PARSER).build());
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment COILS = TooltipAttachment.of(
            (itemStack, item) -> {
                if (item instanceof BlockItem blockItem
                        && ElectricBlastFurnaceBlockEntity.tiersByCoil.containsKey(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()))) {
                    long eu = ElectricBlastFurnaceBlockEntity.tiersByCoil
                            .get(BuiltInRegistries.BLOCK.getKey(((BlockItem) itemStack.getItem()).getBlock()))
                            .maxBaseEu();
                    return Optional.of(new Line(MIText.EbfMaxEu).arg(eu).build());
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment CREATIVE_FLIGHT = TooltipAttachment.of(
            (itemStack, item) -> {
                if (item == MIItem.QUANTUM_CHESTPLATE.asItem() || item == MIItem.GRAVICHESTPLATE.asItem()) {
                    return Optional.of(new Line(MIText.AllowCreativeFlight).build());
                } else {
                    return Optional.empty();
                }
            }).noShiftRequired();

    public static final TooltipAttachment ENERGY_STORED_ITEM = TooltipAttachment.of(
            (itemStack, item) -> {
                if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MI.ID)) {
                    var energyStorage = itemStack.getCapability(EnergyApi.ITEM);
                    if (energyStorage != null) {
                        long capacity = energyStorage.getCapacity();
                        if (capacity > 0) {
                            return Optional.of(new Line(MIText.EnergyStored)
                                    .arg(new NumberWithMax(energyStorage.getAmount(), capacity), EU_MAXED_PARSER).build());
                        }
                    }
                }
                return Optional.empty();
            }).noShiftRequired();

    public static final TooltipAttachment LUBRICANT_BUCKET = TooltipAttachment.of(MIFluids.LUBRICANT.getBucket(),
            new Line(MIText.LubricantTooltip).arg(LubricantHelper.mbPerTick));

    public static final TooltipAttachment GUNPOWDER = TooltipAttachment.of(Items.GUNPOWDER, MIText.GunpowderUpgrade);

    public static final TooltipAttachment MACHINE_TOOLTIPS = TooltipAttachment.ofMultilines(
            (itemStack, item) -> {
                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof MachineBlock machineBlock
                        && !machineBlock.getBlockEntityInstance().getTooltips().isEmpty()) {
                    return Optional.of((((MachineBlock) ((BlockItem) itemStack.getItem()).getBlock()).getBlockEntityInstance()).getTooltips());

                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment NUCLEAR = TooltipAttachment.ofMultilines(
            (itemStack, item) -> {
                if (item instanceof NuclearAbsorbable) {
                    List<Component> tooltips = new LinkedList<>();
                    long remAbs = ((NuclearAbsorbable) itemStack.getItem()).getRemainingDesintegrations(itemStack);
                    tooltips.add(new MITooltips.Line(MIText.RemAbsorption).arg(remAbs)
                            .arg(((NuclearAbsorbable) itemStack.getItem()).desintegrationMax).build());
                    if (itemStack.getItem() instanceof NuclearFuel fuel) {
                        long totalEu = (long) fuel.totalEUbyDesintegration * fuel.desintegrationMax;
                        tooltips.add(new MITooltips.Line(MIText.BaseEuTotalStored).arg(totalEu, MITooltips.EU_PARSER).build());
                    }
                    return Optional.of(tooltips);
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment ORES = TooltipAttachment.ofMultilines(
            (itemStack, item) -> {
                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof OreBlock) {
                    OreBlock oreBlock = (OreBlock) ((BlockItem) itemStack.getItem()).getBlock();
                    List<Component> lines = new LinkedList<>();
                    MIConfig config = MIConfig.getConfig();

                    if (config.enableDefaultOreGenTooltips) {
                        if (oreBlock.params.generate) {
                            lines.add(new Line(MIText.OreGenerationTooltipY).arg(-64).arg(oreBlock.params.maxYLevel).build());
                            lines.add(new Line(MIText.OreGenerationTooltipVeinFrequency).arg(oreBlock.params.veinsPerChunk).build());
                            lines.add(new Line(MIText.OreGenerationTooltipVeinSize).arg(oreBlock.params.veinSize).build());
                        } else {
                            lines.add(new Line(MIText.OreNotGenerated).build());
                        }

                        return Optional.of(lines);
                    }
                }

                return Optional.empty();
            });

    public static final TooltipAttachment REDSTONE_CONTROL_MODULE = TooltipAttachment.ofMultilines(
            (itemStack, item) -> {
                if (MIItem.REDSTONE_CONTROL_MODULE.is(itemStack)) {
                    var lines = new ArrayList<Component>();

                    var requiredSignal = RedstoneControlModuleItem.isRequiresLowSignal(itemStack) ? MIText.SignalLow : MIText.SignalHigh;

                    lines.add(line(MIText.RedstoneControlModuleHelp).build());
                    lines.add(line(MIText.RedstoneControlModuleMachineRequires)
                            .arg(requiredSignal.text().setStyle(NUMBER_TEXT), COMPONENT).build());
                    lines.add(line(MIText.UseItemToChange).build());

                    return Optional.of(lines);
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment SPEED_UPGRADES = TooltipAttachment.of(
            (itemStack, item) -> {
                var upgrade = itemStack.getItemHolder().getData(MIDataMaps.ITEM_PIPE_UPGRADES);
                if (upgrade != null) {
                    return Optional.of(new Line(MIText.TooltipSpeedUpgrade).arg(upgrade.maxExtractedItems()).build());
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment UPGRADES = TooltipAttachment.ofMultilines(
            (itemStack, item) -> {
                if (UpgradeComponent.getExtraEu(item) > 0) {

                    List<Component> lines = new LinkedList<>();
                    lines.add(new Line(MIText.MachineUpgrade).arg(UpgradeComponent.getExtraEu(itemStack.getItem()), EU_PER_TICK_PARSER).build());

                    if (itemStack.getCount() > 1) {
                        lines.add(new Line(MIText.MachineUpgradeStack)
                                .arg(itemStack.getCount() * UpgradeComponent.getExtraEu(itemStack.getItem()), EU_PER_TICK_PARSER).build());
                    }
                    return Optional.of(lines);
                } else {
                    return Optional.empty();
                }
            });

    public static final TooltipAttachment STEAM_DRILL = TooltipAttachment.ofMultilines(MIItem.STEAM_MINING_DRILL,
            MIText.SteamDrillWaterHelp,
            MIText.SteamDrillFuelHelp,
            MIText.SteamDrillProfit,
            MIText.SteamDrillToggle);

    public static final TooltipAttachment CONFIG_CARD_HELP = TooltipAttachment.ofMultilines(MIItem.CONFIG_CARD,
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

        MITooltips.TooltipAttachment.ofMultilines((itemStack, item) -> {
            if (attachTo.test(item)) {
                return Optional.of(Arrays.stream(translationKey).map(Component::translatable).collect(Collectors.toList()));
            } else {
                return Optional.empty();
            }
        });
    }

    private static void add(ItemLike itemLike, String... englishTooltipsLine) {
        add((item) -> itemLike.asItem() == item, BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath(), englishTooltipsLine);
    }

    private static void add(String itemId, String... englishTooltipsLine) {
        add(BuiltInRegistries.ITEM.get(new MIIdentifier(itemId)), englishTooltipsLine);
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

        public final BiFunction<ItemStack, Item, Optional<List<? extends Component>>> tooltipLines;
        public boolean requiresShift = true;
        public int priority = 0;

        public static TooltipAttachment of(ItemLike itemLike, MIText text) {
            return of(itemLike, new Line(text));
        }

        public static TooltipAttachment of(ItemLike itemLike, Line line) {

            return new TooltipAttachment(
                    (itemStack, item) -> itemStack.getItem() == itemLike.asItem() ? Optional.of(List.of(line.build())) : Optional.empty());
        }

        public static TooltipAttachment of(BiFunction<ItemStack, Item, Optional<? extends Component>> tooltipLines) {

            return new TooltipAttachment((itemStack, item) -> tooltipLines.apply(itemStack, item).map(List::of));
        }

        public static TooltipAttachment ofMultilines(BiFunction<ItemStack, Item, Optional<List<? extends Component>>> tooltipLines) {
            return new TooltipAttachment(tooltipLines);
        }

        public static TooltipAttachment ofMultilines(ItemLike itemLike, List<? extends Component> tooltipLines) {
            return new TooltipAttachment((itemStack, item) -> {
                if (itemStack.getItem() == itemLike.asItem()) {
                    return Optional.of(tooltipLines);
                } else {
                    return Optional.empty();
                }
            });
        }

        public static TooltipAttachment ofMultilines(ItemLike itemLike, MIText... tooltipLines) {
            return ofMultilines(itemLike, Arrays.stream(tooltipLines).map(MIText::text).collect(Collectors.toList()));
        }

        private TooltipAttachment(BiFunction<ItemStack, Item, Optional<List<? extends Component>>> tooltipLines) {
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
