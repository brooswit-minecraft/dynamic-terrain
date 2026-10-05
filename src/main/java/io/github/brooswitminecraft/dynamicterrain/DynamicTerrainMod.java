package io.github.brooswitminecraft.dynamicterrain;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
        return BLOCKS.registerBlock(name, LayeredBlock::new, BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
    }

    public DynamicTerrainMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Dynamic Terrain scaffold loaded");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            LAYERED_ITEMS.forEach(event::accept);
        }
    }
}
