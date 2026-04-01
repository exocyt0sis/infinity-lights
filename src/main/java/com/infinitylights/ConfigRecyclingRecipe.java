package com.infinitylights;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class ConfigRecyclingRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final String configKey;
    private final CraftingBookCategory category;

    public ConfigRecyclingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, String configKey, CraftingBookCategory category) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.configKey = configKey;
        this.category = category;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        // Gate recycling recipes behind config booleans without requiring datapack reloads.
        if (!InfinityLightsConfig.isEnabled(this.configKey)) {
            return false;
        }

        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
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
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return InfinityLightsMod.CONFIG_RECYCLING_RECIPE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
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

    public static class Serializer implements RecipeSerializer<ConfigRecyclingRecipe> {
        @Override
        public ConfigRecyclingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            JsonObject resultObject = GsonHelper.getAsJsonObject(json, "result");
            Item resultItem = readItem(GsonHelper.getAsString(resultObject, "item"));
            int count = GsonHelper.getAsInt(resultObject, "count", 1);
            String config = GsonHelper.getAsString(json, "config");
            String categoryName = GsonHelper.getAsString(json, "category", CraftingBookCategory.MISC.getSerializedName());
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(categoryName, CraftingBookCategory.MISC);
            return new ConfigRecyclingRecipe(recipeId, ingredient, new ItemStack(resultItem, count), config, category);
        }

        @Override
        public @Nullable ConfigRecyclingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            String config = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            return new ConfigRecyclingRecipe(recipeId, ingredient, result, config, category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ConfigRecyclingRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeUtf(recipe.configKey);
            buffer.writeEnum(recipe.category);
        }

        private static Item readItem(String id) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item == null) {
                throw new IllegalArgumentException("Unknown item id in config_recycling recipe: " + id);
            }
            return item;
        }
    }
}
