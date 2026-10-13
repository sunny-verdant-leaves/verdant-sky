package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public class WoodenCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    private final WoodVariant variant;

    public WoodenCauldronBlock(BlockBehaviour.Properties props, WoodVariant variant) {
        super(props, VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR);
        this.variant = variant;
    }

    @Override public boolean isFull(BlockState state) { return false; }
    @Override public WoodVariant variant() { return variant; }

    @Override
    public Item asItem() {
        Item item = ModBlocks.CAULDRON_ITEMS_BY_WOOD.get(wood()).get();
        return item;
    }

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

    
    /**
     * 是否可以接受指定流体的滴落，该方法由滴水石锥使用。
     * <p>接受熔岩需要防火木材。</p>
     */
    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return true;
        }
        if (fluid == Fluids.LAVA) {
            return this.fireResistant();
        }
        return false;
    }

    /** 滴水石锥实际滴入流体时的处理。 */
    @Override
    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
        if (fluid == Fluids.WATER) {
            BlockState newState = getWaterBlock().defaultBlockState();
            level.setBlockAndUpdate(pos, newState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
            level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (fluid == Fluids.LAVA) {
            BlockState newState = getLavaBlock().defaultBlockState();
            level.setBlockAndUpdate(pos, newState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
            level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}