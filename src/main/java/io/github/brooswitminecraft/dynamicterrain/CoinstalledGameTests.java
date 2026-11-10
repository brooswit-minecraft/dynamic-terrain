package io.github.brooswitminecraft.dynamicterrain;

import io.github.brooswitminecraft.layeredslopes.LayeredBlock;
import io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MINECRAFT-499 acceptance evidence: coinstalled runtime proof, not just compile-time, that
 * Dynamic Terrain and Layered Slopes actually boot and interoperate together. {@code ci.yml}'s
 * paired build already proves this mod compiles against Layered Slopes' real classes; this
 * {@code runGameTestServer} run boots a real NeoForge server with BOTH mods installed (the run
 * succeeding at all is the coinstalled-load evidence) and exercises {@link LayeredMaterials} --
 * the actual boundary code this mod's smoothing/erosion/cave-ins/grading-tool systems use to
 * consume the extracted block API -- against the real, running {@link LayeredBlock} instances.
 */
@GameTestHolder(DynamicTerrainMod.MODID)
@PrefixGameTestTemplate(false)
public final class CoinstalledGameTests {
    private CoinstalledGameTests() {}

    /** LayeredMaterials' vanilla-block lookups must resolve to Layered Slopes' real registered blocks. */
    @GameTest(template = "flat_platform")
    public static void layeredMaterialsResolvesToTheRealLayeredSlopesBlocks(GameTestHelper helper) {
        helper.assertTrue(LayeredMaterials.layeredFor(Blocks.DIRT) == LayeredSlopesMod.LAYERED_DIRT.get(),
                "LayeredMaterials.layeredFor(DIRT) must resolve to Layered Slopes' own LAYERED_DIRT");
        helper.assertTrue(LayeredMaterials.layeredFor(Blocks.GRASS_BLOCK) == LayeredSlopesMod.LAYERED_GRASS.get(),
                "LayeredMaterials.layeredFor(GRASS_BLOCK) must resolve to Layered Slopes' own LAYERED_GRASS");
        helper.assertTrue(LayeredMaterials.layeredForSlab(Blocks.STONE_SLAB) == LayeredSlopesMod.LAYERED_STONE.get(),
                "LayeredMaterials.layeredForSlab(STONE_SLAB) must resolve to Layered Slopes' own LAYERED_STONE");
        helper.succeed();
    }

    /**
     * CaveIns' own {@code supported} tag (Dynamic Terrain-owned, untouched by the extraction)
     * must still list the real Layered Slopes block by its preserved {@code dynamicterrain}
     * registry name, and that block must actually be placeable and queryable in a live level with
     * both mods installed.
     */
    @GameTest(template = "flat_platform")
    public static void layeredSlopesBlocksArePlaceableAndTaggedForCaveIns(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, LayeredSlopesMod.LAYERED_DIRT.get().defaultBlockState()
                .setValue(LayeredBlock.LAYERS, 3));
        BlockState placed = helper.getBlockState(pos);
        helper.assertTrue(placed.getBlock() == LayeredSlopesMod.LAYERED_DIRT.get(),
                "the block placed in this coinstalled level must be Layered Slopes' real LAYERED_DIRT instance");
        helper.assertTrue(placed.is(CaveIns.SUPPORTED),
                "dynamicterrain:layered_dirt must still be in Dynamic Terrain's own cave-ins supported tag");
        helper.succeed();
    }
}
