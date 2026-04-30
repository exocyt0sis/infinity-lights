package com.infinitylights;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;

public class InfinityCampfireBlockEntity extends BlockEntity implements Clearable {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];
    private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

    public InfinityCampfireBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(InfinityLightsMod.INFINITY_CAMPFIRE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void cookTick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, InfinityCampfireBlockEntity campfire) {
        boolean changed = false;

        for (int i = 0; i < campfire.items.size(); i++) {
            ItemStack itemStack = campfire.items.get(i);
            if (!itemStack.isEmpty()) {
                changed = true;
                campfire.cookingProgress[i]++;
                if (campfire.cookingProgress[i] >= campfire.cookingTime[i]) {
                    SingleRecipeInput input = new SingleRecipeInput(itemStack);
                    ItemStack result = campfire.quickCheck
                        .getRecipeFor(input, level)
                        .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                        .orElse(itemStack);
                    if (result.isItemEnabled(level.enabledFeatures())) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), result);
                        campfire.items.set(i, ItemStack.EMPTY);
                        level.sendBlockUpdated(pos, state, state, 3);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                    }
                }
            }
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    public static void cooldownTick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, InfinityCampfireBlockEntity campfire) {
        boolean changed = false;

        for (int i = 0; i < campfire.items.size(); i++) {
            if (campfire.cookingProgress[i] > 0) {
                changed = true;
                campfire.cookingProgress[i] = Mth.clamp(campfire.cookingProgress[i] - 2, 0, campfire.cookingTime[i]);
            }
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    public static void particleTick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, InfinityCampfireBlockEntity campfire) {
        RandomSource random = level.random;
        if (random.nextFloat() < 0.11F) {
            for (int i = 0; i < random.nextInt(2) + 2; i++) {
                CampfireBlock.makeParticles(level, pos, state.getValue(CampfireBlock.SIGNAL_FIRE), false);
            }
        }

        int rotation = state.getValue(CampfireBlock.FACING).get2DDataValue();

        for (int i = 0; i < campfire.items.size(); i++) {
            if (!campfire.items.get(i).isEmpty() && random.nextFloat() < 0.2F) {
                Direction direction = Direction.from2DDataValue(Math.floorMod(i + rotation, 4));
                double x = pos.getX() + 0.5 - direction.getStepX() * 0.3125D + direction.getClockWise().getStepX() * 0.3125D;
                double y = pos.getY() + 0.5D;
                double z = pos.getZ() + 0.5 - direction.getStepZ() * 0.3125D + direction.getClockWise().getStepZ() * 0.3125D;

                for (int particle = 0; particle < 4; particle++) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 5.0E-4D, 0.0D);
                }
            }
        }
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        if (tag.contains("CookingTimes", 11)) {
            int[] values = tag.getIntArray("CookingTimes");
            System.arraycopy(values, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, values.length));
        }

        if (tag.contains("CookingTotalTimes", 11)) {
            int[] values = tag.getIntArray("CookingTotalTimes");
            System.arraycopy(values, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, values.length));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        tag.putIntArray("CookingTimes", this.cookingProgress);
        tag.putIntArray("CookingTotalTimes", this.cookingTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        return tag;
    }

    public Optional<RecipeHolder<CampfireCookingRecipe>> getCookableRecipe(ItemStack stack) {
        return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SingleRecipeInput(stack), this.level);
    }

    public boolean placeFood(@Nullable LivingEntity entity, ItemStack stack, int cookingTime) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                this.cookingTime[i] = cookingTime;
                this.cookingProgress[i] = 0;
                this.items.set(i, stack.consumeAndReturn(1, entity));
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
                this.markUpdated();
                return true;
            }
        }

        return false;
    }

    private void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public void dowse() {
        if (this.level != null) {
            this.markUpdated();
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getItems());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("Items");
    }
}