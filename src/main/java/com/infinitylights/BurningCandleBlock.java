package com.infinitylights;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.joml.Vector3f;

public class BurningCandleBlock extends CandleBlock {
    public static final BooleanProperty SMOLDERING = BooleanProperty.create("smoldering");

    private final Supplier<Item> dropItem;

    public BurningCandleBlock(BlockBehaviour.Properties properties, Supplier<Item> dropItem) {
        super(properties);
        this.dropItem = dropItem;
        this.registerDefaultState(this.defaultBlockState().setValue(SMOLDERING, Boolean.FALSE));
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (!state.getValue(LIT)) {
            return 0;
        }

        int dryLightLevel = 3 * state.getValue(CANDLES);
        return state.getValue(SMOLDERING) ? Math.max(1, dryLightLevel / 4) : dryLightLevel;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        if (!state.getValue(SMOLDERING)) {
            super.animateTick(state, level, pos, random);
            return;
        }

        if (random.nextInt(4) != 0) {
            return;
        }

        for (Vec3 offset : this.getParticleOffsets(state)) {
            double x = pos.getX() + offset.x;
            double y = pos.getY() + offset.y;
            double z = pos.getZ() + offset.z;
            level.addParticle(new DustParticleOptions(new Vector3f(0.15F, 0.15F, 0.15F), 0.45F), x, y, z, 0.0D, 0.01D, 0.0D);
            level.addParticle(new DustParticleOptions(new Vector3f(0.28F, 0.28F, 0.28F), 0.38F), x, y + 0.02D, z, 0.0D, 0.015D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.04D, z, 0.0D, 0.015D, 0.0D);
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.FLAME, x, y + 0.01D, z, 0.0D, 0.003D, 0.0D);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this.dropItem.get(), state.getValue(CANDLES));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(net.minecraft.stats.Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (level instanceof ServerLevel serverLevel) {
            popResource(serverLevel, pos, new ItemStack(this.dropItem.get(), state.getValue(CANDLES)));
            state.spawnAfterBreak(serverLevel, pos, tool, true);
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive()
            && context.getItemInHand().getItem() == this.dropItem.get()
            && state.getValue(CANDLES) < 4
            ? true
            : super.canBeReplaced(state, context);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.FIRESTARTER_LIGHT && canLightInternal(state)) {
            BlockState litState = state.setValue(LIT, Boolean.TRUE).setValue(SMOLDERING, Boolean.FALSE);
            if (!simulate && context.getLevel() instanceof ServerLevel serverLevel) {
                ActiveLightRegistryData.get(serverLevel).track(context.getClickedPos(), ActiveLightRegistryData.LightKind.VANILLA_CANDLE, serverLevel.getGameTime());
            }
            return litState;
        }

        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == this.dropItem.get() && state.getValue(CANDLES) < 4) {
            if (!level.isClientSide) {
                BlockState updatedState = state.cycle(CANDLES);
                level.setBlock(pos, updatedState, Block.UPDATE_ALL);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                level.playSound(null, pos, updatedState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        ItemInteractionResult result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (!level.isClientSide && stack.isEmpty() && state.getValue(LIT) && player.getAbilities().mayBuild) {
            ActiveLightRegistryData.get((ServerLevel) level).untrack(pos);
        }
        return result;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        boolean changed = super.placeLiquid(level, pos, state, fluidState);
        if (changed && fluidState.getType() == Fluids.WATER && level instanceof ServerLevel serverLevel) {
            ActiveLightRegistryData.get(serverLevel).untrack(pos);
        }
        return changed;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hitResult, Projectile projectile) {
        boolean shouldTrack = !level.isClientSide && projectile.isOnFire() && this.canBeLit(state);
        super.onProjectileHit(level, state, hitResult, projectile);
        if (shouldTrack && level instanceof ServerLevel serverLevel) {
            ActiveLightRegistryData.get(serverLevel).track(hitResult.getBlockPos(), ActiveLightRegistryData.LightKind.VANILLA_CANDLE, serverLevel.getGameTime());
        }
    }

    private static boolean canLightInternal(BlockState state) {
        if (!state.hasProperty(LIT) || state.getValue(LIT)) {
            return false;
        }

        if (!state.hasProperty(WATERLOGGED)) {
            return true;
        }

        return !state.getValue(WATERLOGGED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SMOLDERING);
    }
}