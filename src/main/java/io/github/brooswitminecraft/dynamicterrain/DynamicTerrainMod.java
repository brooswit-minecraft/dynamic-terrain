package io.github.brooswitminecraft.dynamicterrain;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
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
 * Entry point and registry. Layered dirt is the first layered block; more
 * materials, smooth(), transitions and erode() follow in later MINECRAFT-58 tickets.
 */
@Mod(DynamicTerrainMod.MODID)
public class DynamicTerrainMod {
    public static final String MODID = "dynamicterrain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<LayeredBlock> LAYERED_DIRT = BLOCKS.registerBlock("layered_dirt",
            LayeredBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noOcclusion());
    public static final DeferredItem<BlockItem> LAYERED_DIRT_ITEM = ITEMS.registerSimpleBlockItem(LAYERED_DIRT);

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
            event.accept(LAYERED_DIRT_ITEM);
        }
    }
}
