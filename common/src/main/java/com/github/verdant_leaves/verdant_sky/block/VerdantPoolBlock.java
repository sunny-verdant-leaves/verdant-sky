package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.state.BotaniaStateProperties.OptionalDyeColor;
import vazkii.botania.common.block.BotaniaWaterloggedBlock;
import vazkii.botania.common.block.decor.BotaniaMushroomBlock;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.material.MysticalPetalItem;

import com.github.verdant_leaves.verdant_sky.block.entity.VerdantPoolBlockEntity;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

import java.util.Optional;

import static vazkii.botania.api.state.BotaniaStateProperties.OPTIONAL_DYE_COLOR;

public class VerdantPoolBlock extends BotaniaWaterloggedBlock implements EntityBlock {

    private static final VoxelShape SHAPE;
    private static final VoxelShape SHAPE_INTERACT;

    static {
        SHAPE_INTERACT = box(0, 0, 0, 16, 6, 16);
        VoxelShape cutout = box(1, 1, 1, 15, 6, 15);
        SHAPE = Shapes.join(SHAPE_INTERACT, cutout, BooleanOp.ONLY_FIRST);
    }

    public VerdantPoolBlock(Properties builder) {
        super(builder);
        registerDefaultState(defaultBlockState().setValue(OPTIONAL_DYE_COLOR, OptionalDyeColor.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPTIONAL_DYE_COLOR);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext econtext
                && econtext.getEntity() instanceof ManaBurstEntity) {
            return SHAPE_INTERACT;
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @NotNull
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE_INTERACT;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, Level world, @NotNull BlockPos pos,
                                  Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        BlockEntity be = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);
        Optional<DyeColor> itemColor = Optional.empty();
        if (stack.getItem() instanceof MysticalPetalItem petalItem) {
            itemColor = Optional.of(petalItem.color);
        }
        if (Block.byItem(stack.getItem()) instanceof BotaniaMushroomBlock mushroomBlock) {
            itemColor = Optional.of(mushroomBlock.color);
        }
        if (itemColor.isPresent() && be instanceof VerdantPoolBlockEntity pool) {
            if (!itemColor.equals(pool.getColor())) {
                pool.setColor(itemColor);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(world.isClientSide());
            }
        }
        if (stack.is(Items.CLAY_BALL) && be instanceof VerdantPoolBlockEntity pool && pool.getColor().isPresent()) {
            pool.setColor(Optional.empty());
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(world.isClientSide());
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new VerdantPoolBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.VERDANT_POOL.get(),
                level.isClientSide ? VerdantPoolBlockEntity::clientTick : VerdantPoolBlockEntity::serverTick);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity item && world.getBlockEntity(pos) instanceof VerdantPoolBlockEntity pool) {
            pool.collideEntityItem(item);
        }
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof VerdantPoolBlockEntity pool
                ? VerdantPoolBlockEntity.calculateComparatorLevel(pool.getCurrentMana(), pool.getMaxMana())
                : 0;
    }
}