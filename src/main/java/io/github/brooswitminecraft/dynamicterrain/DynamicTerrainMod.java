package io.github.brooswitminecraft.dynamicterrain;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entry point and registry. Layered dirt, sand and gravel are the first layered blocks; more
 * materials, smooth(), transitions and erode() follow in later MINECRAFT-58 tickets.
 */
@Mod(DynamicTerrainMod.MODID)
public class DynamicTerrainMod {
    public static final String MODID = "dynamicterrain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<LayeredBlock> LAYERED_DIRT = registerLayered("layered_dirt", Blocks.DIRT);
    public static final DeferredBlock<LayeredBlock> LAYERED_SAND = registerLayered("layered_sand", Blocks.SAND);
    public static final DeferredBlock<LayeredBlock> LAYERED_GRAVEL = registerLayered("layered_gravel", Blocks.GRAVEL);

    private static final List<DeferredItem<BlockItem>> LAYERED_ITEMS = List.of(
            ITEMS.registerSimpleBlockItem(LAYERED_DIRT),
            ITEMS.registerSimpleBlockItem(LAYERED_SAND),
            ITEMS.registerSimpleBlockItem(LAYERED_GRAVEL));

    private static DeferredBlock<LayeredBlock> registerLayered(String name, Block base) {
        DeferredBlock<LayeredBlock> layered = BLOCKS.registerBlock(name, LayeredBlock::new,
                BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
        LayeredMaterials.register(base, layered);
        return layered;
    }

    public DynamicTerrainMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, DynamicTerrainConfig.SPEC);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(GradingTool::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(WaterErosion::onServerTick);
        NeoForge.EVENT_BUS.addListener(EntityErosion::onServerTick);
        NeoForge.EVENT_BUS.addListener(HeatErosion::onServerTick);
        NeoForge.EVENT_BUS.addListener(CaveIns::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new TransitionLoader()));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Dynamic Terrain scaffold loaded");
    }

    /** Debug entry point for smooth(): /dtsmooth <pos> <direction>. Op only; the grading tool comes later. */
    private void registerCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("dtsmooth")
                .requires(source -> source.hasPermission(2));
        RequiredArgumentBuilder<CommandSourceStack, Coordinates> pos = Commands.argument("pos", BlockPosArgument.blockPos());
        for (Direction direction : Direction.values()) {
            pos.then(Commands.literal(direction.getName()).executes(context -> {
                BlockPos target = BlockPosArgument.getLoadedBlockPos(context, "pos");
                boolean moved = Smoothing.smooth(context.getSource().getLevel(), target, direction);
                context.getSource().sendSuccess(() -> Component.literal(
                        moved ? "Moved one layer " + direction.getName() : "Rejected: nothing moved"), true);
                return moved ? 1 : 0;
            }));
        }
        event.getDispatcher().register(root.then(pos));

        // Debug entry point for erode(): /dterode <pos> <amount>. Respects the erosionEnabled config.
        event.getDispatcher().register(Commands.literal("dterode")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0)).executes(context -> {
                            BlockPos target = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            double amount = DoubleArgumentType.getDouble(context, "amount");
                            Erosion.Result result = Erosion.erode(context.getSource().getLevel(), target, amount);
                            context.getSource().sendSuccess(() -> Component.literal("erode: " + result), true);
                            return result == Erosion.Result.MOVED || result == Erosion.Result.DEGRADED ? 1 : 0;
                        }))));

        // Debug entry point for the tire-slip input: /dtslip <pos> <slipSpeed> <loadKg> reports once.
        event.getDispatcher().register(Commands.literal("dtslip")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("slip", DoubleArgumentType.doubleArg(0))
                                .then(Commands.argument("loadKg", DoubleArgumentType.doubleArg(0)).executes(context -> {
                                    BlockPos target = BlockPosArgument.getLoadedBlockPos(context, "pos");
                                    Erosion.Result result = TireSlip.report(context.getSource().getLevel(), target,
                                            DoubleArgumentType.getDouble(context, "slip"),
                                            DoubleArgumentType.getDouble(context, "loadKg"));
                                    context.getSource().sendSuccess(() -> Component.literal("tire slip: " + result), true);
                                    return result == Erosion.Result.MOVED || result == Erosion.Result.DEGRADED ? 1 : 0;
                                })))));

        // Debug entry point for water erosion: /dtwater <pos> <samples> samples positions around pos once.
        event.getDispatcher().register(Commands.literal("dtwater")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("samples", IntegerArgumentType.integer(1, 100000)).executes(context -> {
                            BlockPos center = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            int samples = IntegerArgumentType.getInteger(context, "samples");
                            int changed = WaterErosion.sampleAround(context.getSource().getLevel(), center, samples,
                                    context.getSource().getLevel().getRandom());
                            context.getSource().sendSuccess(() -> Component.literal("water erosion changed " + changed + " blocks"), true);
                            return changed;
                        }))));

        // Debug entry point for heat erosion: /dtheat <pos> <samples> samples positions around pos once.
        event.getDispatcher().register(Commands.literal("dtheat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("samples", IntegerArgumentType.integer(1, 100000)).executes(context -> {
                            BlockPos center = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            int changed = HeatErosion.sampleAround(context.getSource().getLevel(), center,
                                    IntegerArgumentType.getInteger(context, "samples"), context.getSource().getLevel().getRandom());
                            context.getSource().sendSuccess(() -> Component.literal("heat erosion changed " + changed + " blocks"), true);
                            return changed;
                        }))));

        // Debug entry point for the transition registry: /dttransition <pos> <name>.
        event.getDispatcher().register(Commands.literal("dttransition")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("name", StringArgumentType.word()).executes(context -> {
                            BlockPos target = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            String name = StringArgumentType.getString(context, "name");
                            boolean changed = Transitions.applyTransition(context.getSource().getLevel(), target, name);
                            context.getSource().sendSuccess(() -> Component.literal(
                                    changed ? "Applied " + name : "No-op: no '" + name + "' transition for that block"), true);
                            return changed ? 1 : 0;
                        }))));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            LAYERED_ITEMS.forEach(event::accept);
        }
    }
}
