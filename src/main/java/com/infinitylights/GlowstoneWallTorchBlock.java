package com.infinitylights;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class GlowstoneWallTorchBlock extends WallTorchBlock {
    private final Supplier<Item> dropItem;

    public GlowstoneWallTorchBlock(BlockBehaviour.Properties properties, Supplier<Item> dropItem) {
        super(ParticleTypes.FLAME, properties);
        this.dropItem = dropItem;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Intentionally empty: glowstone wall torch should not emit vanilla flame or smoke particles.
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
}
