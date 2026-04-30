package com.infinitylights;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Vector3f;

public class BurningWallTorchBlock extends WallTorchBlock {
    private static final int SMOLDER_LIGHT_LEVEL = 4;
    public static final BooleanProperty SMOLDERING = BooleanProperty.create("smoldering");

    private final Supplier<Item> dropItem;
    private final BooleanSupplier rainSmolderEnabled;
    private final int dryLightLevel;

    public BurningWallTorchBlock(BlockBehaviour.Properties properties, Supplier<Item> dropItem, BooleanSupplier rainSmolderEnabled, int dryLightLevel) {
        super(ParticleTypes.FLAME, properties);
        this.dropItem = dropItem;
        this.rainSmolderEnabled = rainSmolderEnabled;
        this.dryLightLevel = dryLightLevel;
        this.registerDefaultState(this.defaultBlockState().setValue(SMOLDERING, Boolean.FALSE));
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(SMOLDERING) ? SMOLDER_LIGHT_LEVEL : this.dryLightLevel;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(SMOLDERING)) {
            super.animateTick(state, level, pos, random);
            return;
        }

        if (random.nextInt(4) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.68D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(new DustParticleOptions(new Vector3f(0.15F, 0.15F, 0.15F), 0.55F), x, y, z, 0.0D, 0.01D, 0.0D);
        level.addParticle(new DustParticleOptions(new Vector3f(0.28F, 0.28F, 0.28F), 0.5F), x, y + 0.02D, z, 0.0D, 0.015D, 0.0D);
        level.addParticle(ParticleTypes.SMOKE, x, y + 0.04D, z, 0.0D, 0.02D, 0.0D);
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.FLAME, x, y + 0.02D, z, 0.0D, 0.004D, 0.0D);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this.dropItem.get());
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(net.minecraft.stats.Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (level instanceof ServerLevel serverLevel) {
            popResource(serverLevel, pos, new ItemStack(this.dropItem.get()));
            state.spawnAfterBreak(serverLevel, pos, tool, true);
        }
    }

    private boolean isSmoldering(BlockGetter level, BlockPos pos) {
        return this.rainSmolderEnabled.getAsBoolean() && level instanceof Level world && world.isRainingAt(pos.above());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SMOLDERING);
    }
}