package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class WoodenEmptyCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    private final String wood;

    public WoodenEmptyCauldronBlock(BlockBehaviour.Properties props, String wood) {
        super(props, VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR);
        this.wood = wood;
    }

    @Override public boolean isFull(BlockState state) { return false; }
    @Override public String wood() { return wood; }
    
    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
            return;
        }

        if (precipitation == Biome.Precipitation.RAIN) {
            if (level.getRandom().nextFloat() < 0.05F) {
                level.setBlockAndUpdate(pos, family.getWaterBlock().defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 1));
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            }
        } else if (precipitation == Biome.Precipitation.SNOW) {
            if (level.getRandom().nextFloat() < 0.1F) {
                level.setBlockAndUpdate(pos, family.getPowderSnowBlock().defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 1));
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            }
        }
    }
}