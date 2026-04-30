package com.infinitylights;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ConfigRecyclingRecipe implements CraftingRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    private final Ingredient ingredient;
    private final ItemStack result;
    private final String configKey;
    private final CraftingBookCategory category;

    public ConfigRecyclingRecipe(CraftingBookCategory category, Ingredient ingredient, ItemStack result, String configKey) {
        this.ingredient = ingredient;
        this.result = result;
        this.configKey = configKey;
        this.category = category;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // Gate recycling recipes behind config booleans without requiring datapack reloads.
        if (!InfinityLightsConfig.isEnabled(this.configKey)) {
            return false;
        }

        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return false;
            }
            found = stack;
        }

        return !found.isEmpty() && this.ingredient.test(found);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<ConfigRecyclingRecipe> getSerializer() {
        return InfinityLightsMod.CONFIG_RECYCLING_RECIPE.get();
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.ingredient);
        return list;
    }

    private Ingredient ingredient() {
        return this.ingredient;
    }

    private ItemStack result() {
        return this.result;
    }

    private String configKey() {
        return this.configKey;
    }

    public static class Serializer implements RecipeSerializer<ConfigRecyclingRecipe> {
        private static final MapCodec<ConfigRecyclingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ConfigRecyclingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ConfigRecyclingRecipe::ingredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ConfigRecyclingRecipe::result),
                Codec.STRING.fieldOf("config").forGetter(ConfigRecyclingRecipe::configKey)
        ).apply(instance, ConfigRecyclingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ConfigRecyclingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<ConfigRecyclingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ConfigRecyclingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ConfigRecyclingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            String configKey = buffer.readUtf();
            return new ConfigRecyclingRecipe(category, ingredient, result, configKey);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ConfigRecyclingRecipe recipe) {
            buffer.writeEnum(recipe.category);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeUtf(recipe.configKey);
        }
    }
}
