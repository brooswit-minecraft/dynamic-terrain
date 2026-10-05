package io.github.brooswitminecraft.dynamicterrain;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

/** Layered dirt with a grass top. Like a grass block, it drops dirt, not itself. */
public class LayeredGrassBlock extends LayeredBlock {
    public static final MapCodec<LayeredGrassBlock> CODEC = simpleCodec(LayeredGrassBlock::new);

    public LayeredGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(DynamicTerrainMod.LAYERED_DIRT.get(), state.getValue(LAYERS)));
    }
}
