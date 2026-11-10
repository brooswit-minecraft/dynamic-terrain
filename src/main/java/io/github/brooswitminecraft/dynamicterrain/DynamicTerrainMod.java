package io.github.brooswitminecraft.dynamicterrain;

import io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_DIRT;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_GRASS;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_STONE;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_COBBLESTONE;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_SMOOTH_STONE;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_COBBLED_DEEPSLATE;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_COARSE_DIRT;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_PODZOL;
import static io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod.LAYERED_MYCELIUM;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Entry point and registry. The layered block family itself now lives in the Layered Slopes
 * mod (MINECRAFT-499); this class wires Dynamic Terrain's own erosion, grading tool and
 * driving-surface systems to that extracted block API via {@link LayeredMaterials}. Worldgen
 * smoothing itself moved to the standalone Smooth Slopes mod (MINECRAFT-500), which depends on
 * Layered Slopes directly and no longer needs this mod at all.
 */
@Mod(DynamicTerrainMod.MODID)
public class DynamicTerrainMod {
    public static final String MODID = "dynamicterrain";
    public static final Logger LOGGER = LogUtils.getLogger();

    static {
        LayeredMaterials.register(Blocks.DIRT, LAYERED_DIRT);
        LayeredMaterials.register(Blocks.SAND, LayeredSlopesMod.LAYERED_SAND);
        LayeredMaterials.register(Blocks.GRAVEL, LayeredSlopesMod.LAYERED_GRAVEL);
        LayeredMaterials.register(Blocks.STONE, LAYERED_STONE);
        LayeredMaterials.register(Blocks.COBBLESTONE, LAYERED_COBBLESTONE);
        LayeredMaterials.register(Blocks.SMOOTH_STONE, LAYERED_SMOOTH_STONE);
        LayeredMaterials.register(Blocks.DEEPSLATE, LayeredSlopesMod.LAYERED_DEEPSLATE);
        LayeredMaterials.register(Blocks.COBBLED_DEEPSLATE, LAYERED_COBBLED_DEEPSLATE);
        LayeredMaterials.register(Blocks.TERRACOTTA, LayeredSlopesMod.LAYERED_TERRACOTTA);
        LayeredMaterials.register(Blocks.RED_TERRACOTTA, LayeredSlopesMod.LAYERED_RED_TERRACOTTA);
        LayeredMaterials.register(Blocks.ORANGE_TERRACOTTA, LayeredSlopesMod.LAYERED_ORANGE_TERRACOTTA);
        LayeredMaterials.register(Blocks.YELLOW_TERRACOTTA, LayeredSlopesMod.LAYERED_YELLOW_TERRACOTTA);
        LayeredMaterials.register(Blocks.BROWN_TERRACOTTA, LayeredSlopesMod.LAYERED_BROWN_TERRACOTTA);
        LayeredMaterials.register(Blocks.WHITE_TERRACOTTA, LayeredSlopesMod.LAYERED_WHITE_TERRACOTTA);
        LayeredMaterials.register(Blocks.LIGHT_GRAY_TERRACOTTA, LayeredSlopesMod.LAYERED_LIGHT_GRAY_TERRACOTTA);
        LayeredMaterials.register(Blocks.MAGENTA_TERRACOTTA, LayeredSlopesMod.LAYERED_MAGENTA_TERRACOTTA);
        LayeredMaterials.register(Blocks.LIGHT_BLUE_TERRACOTTA, LayeredSlopesMod.LAYERED_LIGHT_BLUE_TERRACOTTA);
        LayeredMaterials.register(Blocks.LIME_TERRACOTTA, LayeredSlopesMod.LAYERED_LIME_TERRACOTTA);
        LayeredMaterials.register(Blocks.PINK_TERRACOTTA, LayeredSlopesMod.LAYERED_PINK_TERRACOTTA);
        LayeredMaterials.register(Blocks.GRAY_TERRACOTTA, LayeredSlopesMod.LAYERED_GRAY_TERRACOTTA);
        LayeredMaterials.register(Blocks.CYAN_TERRACOTTA, LayeredSlopesMod.LAYERED_CYAN_TERRACOTTA);
        LayeredMaterials.register(Blocks.PURPLE_TERRACOTTA, LayeredSlopesMod.LAYERED_PURPLE_TERRACOTTA);
        LayeredMaterials.register(Blocks.BLUE_TERRACOTTA, LayeredSlopesMod.LAYERED_BLUE_TERRACOTTA);
        LayeredMaterials.register(Blocks.GREEN_TERRACOTTA, LayeredSlopesMod.LAYERED_GREEN_TERRACOTTA);
        LayeredMaterials.register(Blocks.BLACK_TERRACOTTA, LayeredSlopesMod.LAYERED_BLACK_TERRACOTTA);
        LayeredMaterials.register(Blocks.RED_SAND, LayeredSlopesMod.LAYERED_RED_SAND);
        LayeredMaterials.register(Blocks.ICE, LayeredSlopesMod.LAYERED_ICE);
        LayeredMaterials.register(Blocks.PACKED_ICE, LayeredSlopesMod.LAYERED_PACKED_ICE);
        LayeredMaterials.register(Blocks.SNOW_BLOCK, LayeredSlopesMod.LAYERED_SNOW_BLOCK);
        LayeredMaterials.register(Blocks.COARSE_DIRT, LAYERED_COARSE_DIRT);
        LayeredMaterials.register(Blocks.PODZOL, LAYERED_PODZOL);
        LayeredMaterials.register(Blocks.MYCELIUM, LAYERED_MYCELIUM);
        LayeredMaterials.register(Blocks.MUD, LayeredSlopesMod.LAYERED_MUD);
        LayeredMaterials.register(Blocks.CLAY, LayeredSlopesMod.LAYERED_CLAY);
        LayeredMaterials.register(Blocks.SANDSTONE, LayeredSlopesMod.LAYERED_SANDSTONE);
        LayeredMaterials.register(Blocks.RED_SANDSTONE, LayeredSlopesMod.LAYERED_RED_SANDSTONE);
        LayeredMaterials.register(Blocks.TUFF, LayeredSlopesMod.LAYERED_TUFF);
        LayeredMaterials.register(Blocks.ANDESITE, LayeredSlopesMod.LAYERED_ANDESITE);
        LayeredMaterials.register(Blocks.GRANITE, LayeredSlopesMod.LAYERED_GRANITE);
        LayeredMaterials.register(Blocks.DIORITE, LayeredSlopesMod.LAYERED_DIORITE);
        LayeredMaterials.register(Blocks.NETHERRACK, LayeredSlopesMod.LAYERED_NETHERRACK);
        LayeredMaterials.register(Blocks.BLACKSTONE, LayeredSlopesMod.LAYERED_BLACKSTONE);

        LayeredMaterials.register(Blocks.GRASS_BLOCK, LAYERED_GRASS);
        // Soil blocks with a different top layer grade into plain layered dirt: the podzol or mycelium
        // top is lost with the first pull.
        for (Block soil : List.of(Blocks.ROOTED_DIRT)) {
            LayeredMaterials.register(soil, LAYERED_DIRT);
        }
        LayeredMaterials.registerSlab(Blocks.STONE_SLAB, LAYERED_STONE);
        LayeredMaterials.registerSlab(Blocks.COBBLESTONE_SLAB, LAYERED_COBBLESTONE);
        LayeredMaterials.registerSlab(Blocks.SMOOTH_STONE_SLAB, LAYERED_SMOOTH_STONE);
        LayeredMaterials.registerSlab(Blocks.COBBLED_DEEPSLATE_SLAB, LAYERED_COBBLED_DEEPSLATE);
        LayeredMaterials.joinGroup("soil", LAYERED_DIRT);
        LayeredMaterials.joinGroup("soil", LAYERED_GRASS);
        LayeredMaterials.joinGroup("soil", LAYERED_COARSE_DIRT);
        LayeredMaterials.joinGroup("soil", LAYERED_PODZOL);
        LayeredMaterials.joinGroup("soil", LAYERED_MYCELIUM);
        // Erosion disturbance settles grass as dirt instead of keeping a grass top
        // (see Smoothing's disturb flag); manual grading above is unaffected.
        LayeredMaterials.disturbsTo(LAYERED_GRASS, LAYERED_DIRT);
    }

    public DynamicTerrainMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, DynamicTerrainConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(GradingTool::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(EntityErosion::onServerTick);
        NeoForge.EVENT_BUS.addListener(HeatErosion::onServerTick);
        NeoForge.EVENT_BUS.addListener(CaveIns::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new TransitionLoader()));
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new SurfaceOverrideLoader()));
        // Canonical texture analysis is cached off the classpath, not the active resource pack (see
        // CanonicalTextures), but a reload still means "assets may have changed" (a dev hot-swap, a
        // mod/data version bump): drop the cache so the next lookup re-reads and re-analyzes.
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            CanonicalTextures.invalidateAll();
            BlockFaceTextures.invalidateAll();
        });
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

        // Debug entry point for terrain smoothness: /dtheightstats <x> <z> <radius> measures loaded surface steps.
        event.getDispatcher().register(Commands.literal("dtheightstats")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 300)).executes(context -> {
                                    String report = SurfaceStats.measure(context.getSource().getLevel(),
                                            IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "z"),
                                            IntegerArgumentType.getInteger(context, "radius"));
                                    context.getSource().sendSuccess(() -> Component.literal(report), false);
                                    return 1;
                                })))));

        // Debug entry point for the surface model: /dtsurface <pos> prints what a tire would feel there.
        event.getDispatcher().register(Commands.literal("dtsurface")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(context -> {
                    BlockPos target = BlockPosArgument.getLoadedBlockPos(context, "pos");
                    SurfaceProperties s = Surfaces.at(context.getSource().getLevel(), target);
                    context.getSource().sendSuccess(() -> Component.literal(String.format(
                            "grip=%.2f roughness=%.2f rollingResistance=%.3f deformability=%.2f", s.grip(), s.roughness(),
                            s.rollingResistance(), s.deformability())), false);
                    return 1;
                })));

        // Debug entry point for per-texel canonical texture evidence: /dttexture <pos> <face>.
        LiteralArgumentBuilder<CommandSourceStack> textureRoot = Commands.literal("dttexture")
                .requires(source -> source.hasPermission(2));
        RequiredArgumentBuilder<CommandSourceStack, Coordinates> texturePos = Commands.argument("pos", BlockPosArgument.blockPos());
        for (Direction direction : Direction.values()) {
            texturePos.then(Commands.literal(direction.getName()).executes(context -> {
                BlockPos target = BlockPosArgument.getLoadedBlockPos(context, "pos");
                var patch = Surfaces.contactPatch(context.getSource().getLevel(), target, direction,
                        0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
                context.getSource().sendSuccess(() -> Component.literal(patch
                        .map(p -> String.format("chroma=%.2f contrast=%.2f xVar=%.2f yVar=%.2f", p.average().chroma(),
                                p.average().luminanceContrast(), p.average().xVariation(), p.average().yVariation()))
                        .orElse("no canonical texture evidence for that block/face")), false);
                return patch.isPresent() ? 1 : 0;
            }));
        }
        event.getDispatcher().register(textureRoot.then(texturePos));

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
}
