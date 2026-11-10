package io.github.brooswitminecraft.dynamicterrain;

import io.github.brooswitminecraft.layeredslopegrading.LayeredMaterials;

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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Entry point. The layered block family itself lives in the Layered Slopes mod (MINECRAFT-499);
 * this class wires Dynamic Terrain's own erosion and driving-surface systems to that extracted
 * block API via {@link LayeredMaterials}. Worldgen smoothing moved to the standalone Smooth
 * Slopes mod (MINECRAFT-500); manual grading and the vanilla-block-to-layered-block
 * {@code LayeredMaterials} registration itself moved to the standalone Layered Slope Grading mod
 * (MINECRAFT-511, Jira comments 52878/52881) -- that mod now populates {@link LayeredMaterials}
 * at its own class-load time via the required {@code layeredslopegrading} dependency, so this
 * class no longer runs a duplicate copy of that registration.
 */
@Mod(DynamicTerrainMod.MODID)
public class DynamicTerrainMod {
    public static final String MODID = "dynamicterrain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DynamicTerrainMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, DynamicTerrainConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(WaterErosion::onServerTick);
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

    /** Debug and misc commands. The /dtsmooth command moved to the Layered Slope Grading mod (MINECRAFT-511). */
    private void registerCommands(RegisterCommandsEvent event) {
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
}
