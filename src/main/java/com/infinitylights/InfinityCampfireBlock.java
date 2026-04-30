package com.infinitylights;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

import java.util.Optional;

public class InfinityCampfireBlock extends CampfireBlock {
    public static final BooleanProperty SMOLDERING = BooleanProperty.create("smoldering");

    private final Supplier<Item> dropItem;
    private final int dryLightLevel;
    private final BooleanSupplier rainSmolderEnabled;
    private final boolean emitVanillaParticles;

    public InfinityCampfireBlock(
            boolean spawnParticles,
            int fireDamage,
            BlockBehaviour.Properties properties,
            Supplier<Item> dropItem,
            int dryLightLevel,
            BooleanSupplier rainSmolderEnabled,
            boolean emitVanillaParticles,
            boolean defaultLit) {
        super(spawnParticles, fireDamage, properties);
        this.dropItem = dropItem;
        this.dryLightLevel = dryLightLevel;
        this.rainSmolderEnabled = rainSmolderEnabled;
        this.emitVanillaParticles = emitVanillaParticles;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(defaultLit)).setValue(SMOLDERING, Boolean.FALSE));
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
        return state.getValue(SMOLDERING) ? 4 : this.dryLightLevel;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        if (state.getValue(SMOLDERING)) {
            if (random.nextInt(3) != 0) {
                return;
            }

            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            double y = pos.getY() + 0.6D + random.nextDouble() * 0.12D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            level.addParticle(new DustParticleOptions(new Vector3f(0.12F, 0.12F, 0.12F), 0.7F), x, y, z, 0.0D, 0.01D, 0.0D);
            level.addParticle(new DustParticleOptions(new Vector3f(0.3F, 0.3F, 0.3F), 0.65F), x, y + 0.03D, z, 0.0D, 0.02D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.05D, z, 0.0D, 0.03D, 0.0D);
            return;
        }

        if (this.emitVanillaParticles) {
            super.animateTick(state, level, pos, random);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this.dropItem.get());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof InfinityCampfireBlockEntity campfireBlockEntity) {
            ItemStack held = player.getItemInHand(hand);
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe = campfireBlockEntity.getCookableRecipe(held);
            if (recipe.isPresent()) {
                if (!level.isClientSide && campfireBlockEntity.placeFood(player, held, recipe.get().value().getCookingTime())) {
                    player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return ItemInteractionResult.SUCCESS;
                }

                return ItemInteractionResult.CONSUME;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfinityCampfireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return state.getValue(LIT)
                ? createTickerHelper(blockEntityType, InfinityLightsMod.INFINITY_CAMPFIRE_BLOCK_ENTITY.get(), InfinityCampfireBlockEntity::particleTick)
                : null;
        }

        return state.getValue(LIT)
            ? createTickerHelper(blockEntityType, InfinityLightsMod.INFINITY_CAMPFIRE_BLOCK_ENTITY.get(), InfinityCampfireBlockEntity::cookTick)
            : createTickerHelper(blockEntityType, InfinityLightsMod.INFINITY_CAMPFIRE_BLOCK_ENTITY.get(), InfinityCampfireBlockEntity::cooldownTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof InfinityCampfireBlockEntity campfireBlockEntity) {
                for (ItemStack stack : campfireBlockEntity.getItems()) {
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SMOLDERING);
    }
}