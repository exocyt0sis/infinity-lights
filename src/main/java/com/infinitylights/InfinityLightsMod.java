package com.infinitylights;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mod(InfinityLightsMod.MOD_ID)
public class InfinityLightsMod {
    public static final String MOD_ID = "infinitylights";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEPLETION_PUFF_TICKS = 20;
    private static final int NIGHT_CHECK_START = 18000;
    private static final int NIGHT_CHECK_END = 22000;
    private static final int SMOLDER_SOUND_MIN_DELAY_TICKS = 20;
    private static final int SMOLDER_SOUND_MAX_DELAY_TICKS = 40;
    private static final List<String> SUPPORTED_CANDLE_IDS = List.of(
        "candle",
        "white_candle",
        "orange_candle",
        "magenta_candle",
        "light_blue_candle",
        "yellow_candle",
        "lime_candle",
        "pink_candle",
        "gray_candle",
        "light_gray_candle",
        "cyan_candle",
        "purple_candle",
        "blue_candle",
        "brown_candle",
        "green_candle",
        "red_candle",
        "black_candle"
    );
    private static final Map<String, Supplier<Block>> VANILLA_CANDLE_BLOCKS = Map.ofEntries(
        Map.entry("candle", () -> Blocks.CANDLE),
        Map.entry("white_candle", () -> Blocks.WHITE_CANDLE),
        Map.entry("orange_candle", () -> Blocks.ORANGE_CANDLE),
        Map.entry("magenta_candle", () -> Blocks.MAGENTA_CANDLE),
        Map.entry("light_blue_candle", () -> Blocks.LIGHT_BLUE_CANDLE),
        Map.entry("yellow_candle", () -> Blocks.YELLOW_CANDLE),
        Map.entry("lime_candle", () -> Blocks.LIME_CANDLE),
        Map.entry("pink_candle", () -> Blocks.PINK_CANDLE),
        Map.entry("gray_candle", () -> Blocks.GRAY_CANDLE),
        Map.entry("light_gray_candle", () -> Blocks.LIGHT_GRAY_CANDLE),
        Map.entry("cyan_candle", () -> Blocks.CYAN_CANDLE),
        Map.entry("purple_candle", () -> Blocks.PURPLE_CANDLE),
        Map.entry("blue_candle", () -> Blocks.BLUE_CANDLE),
        Map.entry("brown_candle", () -> Blocks.BROWN_CANDLE),
        Map.entry("green_candle", () -> Blocks.GREEN_CANDLE),
        Map.entry("red_candle", () -> Blocks.RED_CANDLE),
        Map.entry("black_candle", () -> Blocks.BLACK_CANDLE)
    );
    private static final Map<String, Supplier<Item>> VANILLA_CANDLE_ITEMS = Map.ofEntries(
        Map.entry("candle", () -> Items.CANDLE),
        Map.entry("white_candle", () -> Items.WHITE_CANDLE),
        Map.entry("orange_candle", () -> Items.ORANGE_CANDLE),
        Map.entry("magenta_candle", () -> Items.MAGENTA_CANDLE),
        Map.entry("light_blue_candle", () -> Items.LIGHT_BLUE_CANDLE),
        Map.entry("yellow_candle", () -> Items.YELLOW_CANDLE),
        Map.entry("lime_candle", () -> Items.LIME_CANDLE),
        Map.entry("pink_candle", () -> Items.PINK_CANDLE),
        Map.entry("gray_candle", () -> Items.GRAY_CANDLE),
        Map.entry("light_gray_candle", () -> Items.LIGHT_GRAY_CANDLE),
        Map.entry("cyan_candle", () -> Items.CYAN_CANDLE),
        Map.entry("purple_candle", () -> Items.PURPLE_CANDLE),
        Map.entry("blue_candle", () -> Items.BLUE_CANDLE),
        Map.entry("brown_candle", () -> Items.BROWN_CANDLE),
        Map.entry("green_candle", () -> Items.GREEN_CANDLE),
        Map.entry("red_candle", () -> Items.RED_CANDLE),
        Map.entry("black_candle", () -> Items.BLACK_CANDLE)
    );

    private static final Map<ResourceKey<Level>, Long> LAST_DEPLETION_DAY = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long> LAST_SCHEDULED_DAY = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long> NIGHT_DEPLETION_TICK = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long2IntOpenHashMap> ACTIVE_PUFFS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<BlockState>> PENDING_PUFF_REPLACEMENTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long2LongOpenHashMap> NEXT_SMOLDER_SOUND_TICKS = new HashMap<>();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    public static final DeferredHolder<Block, Block> GLOWSTONE_TORCH = BLOCKS.register("glowstone_torch",
        InfinityLightsMod::createGlowstoneTorch);
    public static final DeferredHolder<Block, Block> GLOWSTONE_WALL_TORCH = BLOCKS.register("glowstone_wall_torch",
        InfinityLightsMod::createGlowstoneWallTorch);
    public static final DeferredHolder<Block, Block> GLOWSTONE_LANTERN = BLOCKS.register("glowstone_lantern",
        InfinityLightsMod::createGlowstoneLantern);
    public static final DeferredHolder<Block, Block> GLOWSTONE_JACK_O_LANTERN = BLOCKS.register("glowstone_jack_o_lantern",
        InfinityLightsMod::createGlowstoneJackOLantern);
    public static final DeferredHolder<Block, Block> DEPLETED_GLOWSTONE_TORCH = BLOCKS.register("depleted_glowstone_torch",
        InfinityLightsMod::createDepletedGlowstoneTorch);
    public static final DeferredHolder<Block, Block> DEPLETED_GLOWSTONE_WALL_TORCH = BLOCKS.register("depleted_glowstone_wall_torch",
        InfinityLightsMod::createDepletedGlowstoneWallTorch);
    public static final DeferredHolder<Block, Block> BURNING_TORCH = BLOCKS.register("burning_torch",
        InfinityLightsMod::createBurningTorch);
    public static final DeferredHolder<Block, Block> BURNING_WALL_TORCH = BLOCKS.register("burning_wall_torch",
        InfinityLightsMod::createBurningWallTorch);
    public static final DeferredHolder<Block, Block> BURNING_LANTERN = BLOCKS.register("burning_lantern",
        InfinityLightsMod::createBurningLantern);
    public static final DeferredHolder<Block, Block> BURNING_CAMPFIRE = BLOCKS.register("burning_campfire",
        InfinityLightsMod::createBurningCampfire);
    public static final Map<String, DeferredHolder<Block, Block>> BURNING_CANDLES = registerBurningCandles();
    public static final DeferredHolder<Block, Block> DEPLETED_TORCH = BLOCKS.register("depleted_torch",
        InfinityLightsMod::createDepletedTorch);
    public static final DeferredHolder<Block, Block> DEPLETED_WALL_TORCH = BLOCKS.register("depleted_wall_torch",
        InfinityLightsMod::createDepletedWallTorch);
    public static final DeferredHolder<Block, Block> DEPLETED_LANTERN = BLOCKS.register("depleted_lantern",
        InfinityLightsMod::createDepletedLantern);

    public static final DeferredHolder<Item, Item> GLOWSTONE_TORCH_ITEM = ITEMS.register("glowstone_torch",
        () -> new StandingAndWallBlockItem(GLOWSTONE_TORCH.get(), GLOWSTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, Item> GLOWSTONE_LANTERN_ITEM = ITEMS.register("glowstone_lantern",
        () -> new BlockItem(GLOWSTONE_LANTERN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> GLOWSTONE_JACK_O_LANTERN_ITEM = ITEMS.register("glowstone_jack_o_lantern",
        () -> new BlockItem(GLOWSTONE_JACK_O_LANTERN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> DEPLETED_GLOWSTONE_TORCH_ITEM = ITEMS.register("depleted_glowstone_torch",
        () -> new StandingAndWallBlockItem(DEPLETED_GLOWSTONE_TORCH.get(), DEPLETED_GLOWSTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, Item> DEPLETED_TORCH_ITEM = ITEMS.register("depleted_torch",
        () -> new StandingAndWallBlockItem(DEPLETED_TORCH.get(), DEPLETED_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredHolder<Item, Item> DEPLETED_LANTERN_ITEM = ITEMS.register("depleted_lantern",
        () -> new BlockItem(DEPLETED_LANTERN.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfinityCampfireBlockEntity>> INFINITY_CAMPFIRE_BLOCK_ENTITY =
        BLOCK_ENTITY_TYPES.register("campfire", () -> BlockEntityType.Builder.of(
            InfinityCampfireBlockEntity::new,
            BURNING_CAMPFIRE.get()
        ).build(null));
    public static final DeferredHolder<SoundEvent, SoundEvent> RAIN_HISS =
        SOUND_EVENTS.register("rain_hiss", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain_hiss")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ConfigRecyclingRecipe>> CONFIG_RECYCLING_RECIPE =
        RECIPE_SERIALIZERS.register("config_recycling", () -> ConfigRecyclingRecipe.SERIALIZER);

    public InfinityLightsMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTabContents);

        modContainer.registerConfig(ModConfig.Type.COMMON, InfinityLightsConfig.SPEC, "infinitylights.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(new ItemStack(GLOWSTONE_TORCH_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(GLOWSTONE_LANTERN_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(GLOWSTONE_JACK_O_LANTERN_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(DEPLETED_GLOWSTONE_TORCH_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(DEPLETED_TORCH_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(DEPLETED_LANTERN_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            return;
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(new ItemStack(GLOWSTONE_TORCH_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static GlowstoneTorchBlock createGlowstoneTorch() {
        return new GlowstoneTorchBlock(glowstoneTorchProperties(15), () -> GLOWSTONE_TORCH_ITEM.get());
    }

    private static GlowstoneWallTorchBlock createGlowstoneWallTorch() {
        return new GlowstoneWallTorchBlock(glowstoneWallTorchProperties(15, GLOWSTONE_TORCH), () -> GLOWSTONE_TORCH_ITEM.get());
    }

    private static GlowstoneLanternBlock createGlowstoneLantern() {
        return new GlowstoneLanternBlock(glowstoneLanternProperties(15), () -> GLOWSTONE_LANTERN_ITEM.get());
    }

    private static Block createGlowstoneJackOLantern() {
        return new CarvedPumpkinBlock(jackOLanternProperties(15));
    }

    private static GlowstoneTorchBlock createDepletedGlowstoneTorch() {
        return new GlowstoneTorchBlock(glowstoneTorchProperties(0), () -> DEPLETED_GLOWSTONE_TORCH_ITEM.get());
    }

    private static GlowstoneWallTorchBlock createDepletedGlowstoneWallTorch() {
        return new GlowstoneWallTorchBlock(glowstoneWallTorchProperties(0, DEPLETED_GLOWSTONE_TORCH), () -> DEPLETED_GLOWSTONE_TORCH_ITEM.get());
    }

    private static BurningTorchBlock createBurningTorch() {
        return new BurningTorchBlock(vanillaTorchProperties(14), () -> Items.TORCH, InfinityLightsConfig.TORCH_SMOLDERED_BY_RAIN::get, 14);
    }

    private static BurningWallTorchBlock createBurningWallTorch() {
        return new BurningWallTorchBlock(vanillaWallTorchProperties(14, BURNING_TORCH), () -> Items.TORCH, InfinityLightsConfig.TORCH_SMOLDERED_BY_RAIN::get, 14);
    }

    private static GlowstoneLanternBlock createBurningLantern() {
        return new GlowstoneLanternBlock(vanillaLanternProperties(15), () -> Items.LANTERN);
    }

    private static InfinityCampfireBlock createBurningCampfire() {
        return new InfinityCampfireBlock(true, 1, burningCampfireProperties(), () -> Items.CAMPFIRE, 15, InfinityLightsConfig.CAMPFIRE_SMOLDERED_BY_RAIN::get, true, true);
    }

    private static Map<String, DeferredHolder<Block, Block>> registerBurningCandles() {
        LinkedHashMap<String, DeferredHolder<Block, Block>> burningCandles = new LinkedHashMap<>();
        for (String candleId : SUPPORTED_CANDLE_IDS) {
            Supplier<Block> vanillaBlock = VANILLA_CANDLE_BLOCKS.get(candleId);
            Supplier<Item> candleItem = VANILLA_CANDLE_ITEMS.get(candleId);
            burningCandles.put(candleId, BLOCKS.register("burning_" + candleId, () -> createBurningCandle(vanillaBlock, candleItem)));
        }
        return Map.copyOf(burningCandles);
    }

    private static BurningCandleBlock createBurningCandle(Supplier<Block> vanillaBlock, Supplier<Item> candleItem) {
        return new BurningCandleBlock(BlockBehaviour.Properties.ofFullCopy(vanillaBlock.get()).lootFrom(vanillaBlock), candleItem);
    }

    private static GlowstoneTorchBlock createDepletedTorch() {
        return new GlowstoneTorchBlock(glowstoneTorchProperties(0), () -> DEPLETED_TORCH_ITEM.get());
    }

    private static GlowstoneWallTorchBlock createDepletedWallTorch() {
        return new GlowstoneWallTorchBlock(glowstoneWallTorchProperties(0, DEPLETED_TORCH), () -> DEPLETED_TORCH_ITEM.get());
    }

    private static GlowstoneLanternBlock createDepletedLantern() {
        return new GlowstoneLanternBlock(glowstoneLanternProperties(0), () -> DEPLETED_LANTERN_ITEM.get());
    }

    private static BlockBehaviour.Properties glowstoneTorchProperties(int lightLevel) {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> lightLevel)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties glowstoneWallTorchProperties(int lightLevel, DeferredHolder<Block, Block> lootBlock) {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> lightLevel)
                .sound(SoundType.WOOD)
                .lootFrom(lootBlock)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties glowstoneLanternProperties(int lightLevel) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .forceSolidOn()
                .strength(3.5F)
                .sound(SoundType.LANTERN)
                .lightLevel(state -> lightLevel)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties vanillaTorchProperties(int lightLevel) {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> lightLevel)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties vanillaWallTorchProperties(int lightLevel, DeferredHolder<Block, Block> lootBlock) {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> lightLevel)
                .sound(SoundType.WOOD)
                .lootFrom(lootBlock)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties vanillaLanternProperties(int lightLevel) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .forceSolidOn()
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .sound(SoundType.LANTERN)
                .lightLevel(state -> lightLevel)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties jackOLanternProperties(int lightLevel) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.JACK_O_LANTERN)
                .lightLevel(state -> lightLevel);
    }

    private static BlockBehaviour.Properties burningCampfireProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)
                .lightLevel(state -> state.getValue(CampfireBlock.LIT) ? 15 : 0)
            .noOcclusion();
    }

    private static String getModVersionString() {
        return ModList.get().getModContainerById(MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
            LevelAccessor accessor = event.getLevel();
            if (!(accessor instanceof ServerLevel level)) {
                if (accessor instanceof Level clientLevel && clientLevel.isClientSide) {
                    applyClientReplacement(clientLevel, event.getPos(), event.getPlacedBlock(), event.getBlockSnapshot().getState());
                }
                return;
            }

            BlockPos pos = event.getPos();
            BlockState placedState = event.getPlacedBlock();
            BlockState replacedState = event.getBlockSnapshot().getState();

            if (placedState.is(Blocks.TORCH)) {
                level.setBlock(pos, BURNING_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_TORCH);
                return;
            }

            if (placedState.is(GLOWSTONE_TORCH.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.GLOWSTONE_TORCH);
                return;
            }

            if (placedState.is(BURNING_TORCH.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_TORCH);
                return;
            }

            if (placedState.is(Blocks.WALL_TORCH)) {
                Direction facing = placedState.getValue(WallTorchBlock.FACING);
                BlockState replacement = BURNING_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, facing);
                level.setBlock(pos, replacement, Block.UPDATE_ALL);
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_TORCH);
                return;
            }

            if (placedState.is(GLOWSTONE_WALL_TORCH.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.GLOWSTONE_TORCH);
                return;
            }

            if (placedState.is(BURNING_WALL_TORCH.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_TORCH);
                return;
            }

            if (placedState.is(Blocks.LANTERN)) {
                BlockState replacement = copyLanternState(placedState, BURNING_LANTERN.get().defaultBlockState());
                level.setBlock(pos, replacement, Block.UPDATE_ALL);
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_LANTERN);
                return;
            }

            if (placedState.is(GLOWSTONE_LANTERN.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.GLOWSTONE_LANTERN);
                return;
            }

            if (placedState.is(Blocks.JACK_O_LANTERN)) {
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_JACKOLANTERN);
                return;
            }

            if (placedState.is(GLOWSTONE_JACK_O_LANTERN.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.GLOWSTONE_JACKOLANTERN);
                return;
            }

            if (placedState.is(BURNING_LANTERN.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_LANTERN);
                return;
            }

            if (isSupportedVanillaCandle(placedState)) {
                BlockState replacement = mergePlacedCandleState(replacedState, placedState, toBurningCandleState(placedState));
                if (replacement != null) {
                    level.setBlock(pos, replacement, Block.UPDATE_ALL);
                    if (replacement.getValue(AbstractCandleBlock.LIT)) {
                        track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_CANDLE);
                    } else {
                        ActiveLightRegistryData.get(level).untrack(pos);
                    }
                }
                return;
            }

            if (isBurningCandle(placedState)) {
                if (placedState.getValue(AbstractCandleBlock.LIT)) {
                    track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_CANDLE);
                } else {
                    ActiveLightRegistryData.get(level).untrack(pos);
                }
                return;
            }

            if (placedState.is(Blocks.CAMPFIRE)) {
                level.setBlock(pos, copyCampfireState(placedState, BURNING_CAMPFIRE.get().defaultBlockState(), true), Block.UPDATE_ALL);
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_CAMPFIRE);
                return;
            }

            if (placedState.is(BURNING_CAMPFIRE.get())) {
                track(level, pos, ActiveLightRegistryData.LightKind.VANILLA_CAMPFIRE);
            }
        }

        @SubscribeEvent
        public static void onLevelTick(LevelTickEvent.Post event) {
            Level currentLevel = event.getLevel();
            if (currentLevel.isClientSide()) {
                return;
            }

            if (!(currentLevel instanceof ServerLevel level)) {
                return;
            }

            tickPuffs(level);
            updateSmolderingStates(level);
            if (level.getGameTime() % 20L == 0L) {
                runTimedBurnout(level);
            }

            long dayTime = level.getDayTime();
            long day = dayTime / 24000L;
            ResourceKey<Level> dimension = level.dimension();

            Long scheduledDay = LAST_SCHEDULED_DAY.get(dimension);
            if (scheduledDay == null || scheduledDay != day) {
                long randomNightOffset = NIGHT_CHECK_START + level.getRandom().nextInt(NIGHT_CHECK_END - NIGHT_CHECK_START + 1);
                long triggerTick = (day * 24000L) + randomNightOffset;
                LAST_SCHEDULED_DAY.put(dimension, day);
                NIGHT_DEPLETION_TICK.put(dimension, triggerTick);
                LOGGER.debug("Scheduled depletion for {} at tick {} (day {}).", dimension.location(), triggerTick, day);
            }

            Long lastProcessed = LAST_DEPLETION_DAY.get(dimension);
            if (lastProcessed != null && lastProcessed == day) {
                return;
            }

            Long triggerTick = NIGHT_DEPLETION_TICK.get(dimension);
            if (triggerTick != null && dayTime >= triggerTick) {
                LAST_DEPLETION_DAY.put(dimension, day);
                runDailyDepletion(level);
            }
        }

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            if (!InfinityLightsConfig.CONSOLE_MESSAGES.get()) {
                return;
            }
            LOGGER.info("Starting Infinity Lights version {}.", getModVersionString());
        }

    }

    private static void runDailyDepletion(ServerLevel level) {
        double torchRisk = InfinityLightsConfig.GLOWSTONE_TORCH_DEPLETION_RISK.get();
        double lanternRisk = InfinityLightsConfig.GLOWSTONE_LANTERN_DEPLETION_RISK.get();
        double jackOLanternRisk = InfinityLightsConfig.GLOWSTONE_JACKOLANTERN_DEPLETION_RISK.get();
        ActiveLightRegistryData tracker = ActiveLightRegistryData.get(level);
        List<ActiveLightRegistryData.TrackedLight> trackedPositions = tracker.snapshot();
        int depletedTorches = 0;
        int depletedLanterns = 0;
        int depletedJackOLanterns = 0;

        for (ActiveLightRegistryData.TrackedLight trackedLight : trackedPositions) {
            if (trackedLight.kind() == ActiveLightRegistryData.LightKind.VANILLA_TORCH
                    || trackedLight.kind() == ActiveLightRegistryData.LightKind.VANILLA_CANDLE
                    || trackedLight.kind() == ActiveLightRegistryData.LightKind.VANILLA_LANTERN
                    || trackedLight.kind() == ActiveLightRegistryData.LightKind.VANILLA_JACKOLANTERN
                    || trackedLight.kind() == ActiveLightRegistryData.LightKind.VANILLA_CAMPFIRE) {
                continue;
            }

            BlockPos pos = trackedLight.pos();
            BlockState state = level.getBlockState(pos);

            if (state.is(GLOWSTONE_TORCH.get())) {
                if (shouldDeplete(level, torchRisk)) {
                    level.setBlock(pos, DEPLETED_GLOWSTONE_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedTorches++;
                }
                continue;
            }

            if (state.is(GLOWSTONE_WALL_TORCH.get())) {
                if (shouldDeplete(level, torchRisk)) {
                    Direction facing = state.getValue(WallTorchBlock.FACING);
                    BlockState replacement = DEPLETED_GLOWSTONE_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, facing);
                    level.setBlock(pos, replacement, Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedTorches++;
                }
                continue;
            }

            if (state.is(GLOWSTONE_LANTERN.get())) {
                if (shouldDeplete(level, lanternRisk)) {
                    // Glowstone and vanilla lanterns now share the same depleted block and recycling path.
                    BlockState replacement = copyLanternState(state, DEPLETED_LANTERN.get().defaultBlockState());
                    level.setBlock(pos, replacement, Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedLanterns++;
                }
                continue;
            }

            if (state.is(GLOWSTONE_JACK_O_LANTERN.get())) {
                if (shouldDeplete(level, jackOLanternRisk)) {
                    level.setBlock(pos, copyJackOLanternState(state, Blocks.CARVED_PUMPKIN.defaultBlockState()), Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedJackOLanterns++;
                }
                continue;
            }

            tracker.untrack(pos);
        }

        if (InfinityLightsConfig.CONSOLE_MESSAGES.get() && (depletedTorches > 0 || depletedLanterns > 0 || depletedJackOLanterns > 0)) {
            LOGGER.info("Depleted {} glowstone torche(s), {} glowstone lantern(s), and {} glowstone Jack o'Lantern(s).", depletedTorches, depletedLanterns, depletedJackOLanterns);
        }
    }

    private static void runTimedBurnout(ServerLevel level) {
        ActiveLightRegistryData tracker = ActiveLightRegistryData.get(level);
        List<ActiveLightRegistryData.TrackedLight> trackedLights = tracker.snapshot();
        long torchTicks = ticksFromDays(InfinityLightsConfig.TORCH_BURNOUT_TIME.get());
        long candleTicks = ticksFromDays(InfinityLightsConfig.CANDLE_BURNOUT_TIME.get());
        long lanternTicks = ticksFromDays(InfinityLightsConfig.LANTERN_BURNOUT_TIME.get());
        long jackOLanternTicks = ticksFromDays(InfinityLightsConfig.JACKOLANTERN_BURNOUT_TIME.get());
        long campfireTicks = ticksFromDays(InfinityLightsConfig.CAMPFIRE_BURNOUT_TIME.get());
        int depletedTorches = 0;
        int depletedCandles = 0;
        int depletedLanterns = 0;
        int depletedJackOLanterns = 0;
        int depletedCampfires = 0;

        for (ActiveLightRegistryData.TrackedLight trackedLight : trackedLights) {
            BlockPos pos = trackedLight.pos();
            BlockState state = level.getBlockState(pos);
            long elapsed = Math.max(0L, level.getGameTime() - trackedLight.placedAtGameTime());

            switch (trackedLight.kind()) {
                case VANILLA_TORCH -> {
                    if (!state.is(BURNING_TORCH.get()) && !state.is(BURNING_WALL_TORCH.get())) {
                        tracker.untrack(pos);
                        continue;
                    }
                    if (torchTicks == Long.MAX_VALUE || elapsed < torchTicks) {
                        continue;
                    }
                    if (state.is(BURNING_WALL_TORCH.get())) {
                        Direction facing = state.getValue(WallTorchBlock.FACING);
                        level.setBlock(pos, DEPLETED_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, facing), Block.UPDATE_ALL);
                    } else {
                        level.setBlock(pos, DEPLETED_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedTorches++;
                }
                case VANILLA_CANDLE -> {
                    if (!isBurningCandle(state)) {
                        tracker.untrack(pos);
                        clearSmolderSound(level, pos);
                        continue;
                    }
                    if (!state.getValue(AbstractCandleBlock.LIT)) {
                        BlockState replacement = toVanillaCandleState(state, false);
                        if (replacement != null) {
                            level.setBlock(pos, replacement, Block.UPDATE_ALL);
                        }
                        tracker.untrack(pos);
                        clearSmolderSound(level, pos);
                        continue;
                    }
                    if (candleTicks == Long.MAX_VALUE || elapsed < candleTicks) {
                        continue;
                    }
                    BlockState replacement = toVanillaCandleState(state, false);
                    if (replacement != null) {
                        level.setBlock(pos, replacement, Block.UPDATE_ALL);
                    }
                    tracker.untrack(pos);
                    clearSmolderSound(level, pos);
                    startPuff(level, pos);
                    depletedCandles++;
                }
                case VANILLA_LANTERN -> {
                    if (!state.is(BURNING_LANTERN.get())) {
                        tracker.untrack(pos);
                        continue;
                    }
                    if (lanternTicks == Long.MAX_VALUE || elapsed < lanternTicks) {
                        continue;
                    }
                    level.setBlock(pos, copyLanternState(state, DEPLETED_LANTERN.get().defaultBlockState()), Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedLanterns++;
                }
                case VANILLA_JACKOLANTERN -> {
                    if (!state.is(Blocks.JACK_O_LANTERN)) {
                        tracker.untrack(pos);
                        continue;
                    }
                    if (jackOLanternTicks == Long.MAX_VALUE || elapsed < jackOLanternTicks) {
                        continue;
                    }
                    level.setBlock(pos, copyJackOLanternState(state, Blocks.CARVED_PUMPKIN.defaultBlockState()), Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedJackOLanterns++;
                }
                case VANILLA_CAMPFIRE -> {
                    if (!state.is(BURNING_CAMPFIRE.get())) {
                        tracker.untrack(pos);
                        continue;
                    }
                    if (!state.getValue(CampfireBlock.LIT)) {
                        continue;
                    }
                    if (campfireTicks == Long.MAX_VALUE || elapsed < campfireTicks) {
                        continue;
                    }
                    startCampfireBurnout(level, pos);
                    tracker.untrack(pos);
                    depletedCampfires++;
                }
                default -> {
                }
            }
        }

        if (InfinityLightsConfig.CONSOLE_MESSAGES.get() && (depletedTorches > 0 || depletedCandles > 0 || depletedLanterns > 0 || depletedJackOLanterns > 0 || depletedCampfires > 0)) {
            LOGGER.info("Depleted {} vanilla torche(s), {} vanilla candle(s), {} vanilla lantern(s), {} vanilla Jack o'Lantern(s), and {} campfire(s).", depletedTorches, depletedCandles, depletedLanterns, depletedJackOLanterns, depletedCampfires);
        }
    }

    private static boolean shouldDeplete(ServerLevel level, double risk) {
        return risk > 0.0D && level.getRandom().nextDouble() < risk;
    }

    private static long ticksFromDays(double days) {
        if (days <= 0.0D) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, Math.round(days * 24000.0D));
    }

    private static void track(ServerLevel level, BlockPos pos, ActiveLightRegistryData.LightKind kind) {
        ActiveLightRegistryData.get(level).track(pos, kind, level.getGameTime());
    }

    private static void startPuff(ServerLevel level, BlockPos pos) {
        // Store a short per-block timer so depletion is visually noticeable but brief.
        ACTIVE_PUFFS.computeIfAbsent(level.dimension(), ignored -> new Long2IntOpenHashMap())
                .put(pos.asLong(), DEPLETION_PUFF_TICKS);
    }

    private static void startCampfireBurnout(ServerLevel level, BlockPos pos) {
        BlockState replacement = getCampfireBurnoutReplacement();
        if (replacement != null) {
            PENDING_PUFF_REPLACEMENTS.computeIfAbsent(level.dimension(), ignored -> new Long2ObjectOpenHashMap<>())
                    .put(pos.asLong(), replacement);
        }

        level.removeBlock(pos, false);
        startPuff(level, pos);
    }

    private static BlockState getCampfireBurnoutReplacement() {
        if (!ModList.get().isLoaded("supplementaries")) {
            return null;
        }

        ResourceLocation ashId = ResourceLocation.tryParse("supplementaries:ash");
        if (ashId == null) {
            return null;
        }

        return BuiltInRegistries.BLOCK.getOptional(ashId)
                .filter(block -> block != Blocks.AIR)
                .map(Block::defaultBlockState)
                .orElse(null);
    }

    private static void updateSmolderingStates(ServerLevel level) {
        boolean torchRain = InfinityLightsConfig.TORCH_SMOLDERED_BY_RAIN.get();
        boolean candleRain = InfinityLightsConfig.CANDLE_SMOLDERED_BY_RAIN.get();
        boolean campfireRain = InfinityLightsConfig.CAMPFIRE_SMOLDERED_BY_RAIN.get();
        if (!torchRain && !candleRain && !campfireRain) {
            return;
        }

        for (ActiveLightRegistryData.TrackedLight trackedLight : ActiveLightRegistryData.get(level).snapshot()) {
            BlockPos pos = trackedLight.pos();
            BlockState state = level.getBlockState(pos);

            switch (trackedLight.kind()) {
                case VANILLA_TORCH -> updateTorchSmoldering(level, pos, state, torchRain);
                case VANILLA_CANDLE -> updateCandleSmoldering(level, pos, state, candleRain);
                case VANILLA_CAMPFIRE -> updateCampfireSmoldering(level, pos, state, campfireRain);
                default -> {
                }
            }
        }

        pruneSmolderSoundSchedule(level);
    }

    private static void updateTorchSmoldering(ServerLevel level, BlockPos pos, BlockState state, boolean enabled) {
        boolean shouldSmolder = enabled && level.isRainingAt(pos.above());

        if (state.is(BURNING_TORCH.get()) && state.hasProperty(BurningTorchBlock.SMOLDERING) && state.getValue(BurningTorchBlock.SMOLDERING) != shouldSmolder) {
            level.setBlock(pos, state.setValue(BurningTorchBlock.SMOLDERING, shouldSmolder), Block.UPDATE_ALL);
            if (shouldSmolder) {
                scheduleSmolderSound(level, pos);
            } else {
                clearSmolderSound(level, pos);
            }
            return;
        }

        if (state.is(BURNING_WALL_TORCH.get()) && state.hasProperty(BurningWallTorchBlock.SMOLDERING) && state.getValue(BurningWallTorchBlock.SMOLDERING) != shouldSmolder) {
            level.setBlock(pos, state.setValue(BurningWallTorchBlock.SMOLDERING, shouldSmolder), Block.UPDATE_ALL);
            if (shouldSmolder) {
                scheduleSmolderSound(level, pos);
            } else {
                clearSmolderSound(level, pos);
            }
            return;
        }

        if (shouldSmolder && isTorchSmoldering(state)) {
            maybePlaySmolderSound(level, pos);
        } else {
            clearSmolderSound(level, pos);
        }
    }

    private static void updateCampfireSmoldering(ServerLevel level, BlockPos pos, BlockState state, boolean enabled) {
        if (!state.is(BURNING_CAMPFIRE.get()) || !state.hasProperty(InfinityCampfireBlock.SMOLDERING) || !state.getValue(CampfireBlock.LIT)) {
            clearSmolderSound(level, pos);
            return;
        }

        boolean shouldSmolder = enabled && level.isRainingAt(pos.above());
        if (state.getValue(InfinityCampfireBlock.SMOLDERING) != shouldSmolder) {
            level.setBlock(pos, state.setValue(InfinityCampfireBlock.SMOLDERING, shouldSmolder), Block.UPDATE_ALL);
            if (shouldSmolder) {
                scheduleSmolderSound(level, pos);
            } else {
                clearSmolderSound(level, pos);
            }
            return;
        }

        if (shouldSmolder && state.getValue(InfinityCampfireBlock.SMOLDERING)) {
            maybePlaySmolderSound(level, pos);
        } else {
            clearSmolderSound(level, pos);
        }
    }

    private static void updateCandleSmoldering(ServerLevel level, BlockPos pos, BlockState state, boolean enabled) {
        if (!isBurningCandle(state) || !state.getValue(AbstractCandleBlock.LIT)) {
            clearSmolderSound(level, pos);
            return;
        }

        boolean shouldSmolder = enabled && level.isRainingAt(pos.above());
        if (state.getValue(BurningCandleBlock.SMOLDERING) != shouldSmolder) {
            level.setBlock(pos, state.setValue(BurningCandleBlock.SMOLDERING, shouldSmolder), Block.UPDATE_ALL);
            if (shouldSmolder) {
                scheduleSmolderSound(level, pos);
            } else {
                clearSmolderSound(level, pos);
            }
            return;
        }

        if (shouldSmolder && state.getValue(BurningCandleBlock.SMOLDERING)) {
            maybePlaySmolderSound(level, pos);
        } else {
            clearSmolderSound(level, pos);
        }
    }

    private static boolean isTorchSmoldering(BlockState state) {
        if (state.is(BURNING_TORCH.get()) && state.hasProperty(BurningTorchBlock.SMOLDERING)) {
            return state.getValue(BurningTorchBlock.SMOLDERING);
        }

        return state.is(BURNING_WALL_TORCH.get())
            && state.hasProperty(BurningWallTorchBlock.SMOLDERING)
            && state.getValue(BurningWallTorchBlock.SMOLDERING);
    }

    private static void maybePlaySmolderSound(ServerLevel level, BlockPos pos) {
        Long2LongOpenHashMap schedule = NEXT_SMOLDER_SOUND_TICKS.computeIfAbsent(level.dimension(), ignored -> new Long2LongOpenHashMap());
        long posLong = pos.asLong();
        long gameTime = level.getGameTime();
        long nextTick = schedule.getOrDefault(posLong, Long.MIN_VALUE);
        if (nextTick > gameTime) {
            return;
        }

        level.playSound(null, pos, RAIN_HISS.get(), SoundSource.BLOCKS, 0.3F + level.getRandom().nextFloat() * 0.15F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        scheduleSmolderSound(level, pos);
    }

    private static void scheduleSmolderSound(ServerLevel level, BlockPos pos) {
        NEXT_SMOLDER_SOUND_TICKS.computeIfAbsent(level.dimension(), ignored -> new Long2LongOpenHashMap())
            .put(pos.asLong(), level.getGameTime() + SMOLDER_SOUND_MIN_DELAY_TICKS + level.getRandom().nextInt(SMOLDER_SOUND_MAX_DELAY_TICKS - SMOLDER_SOUND_MIN_DELAY_TICKS + 1));
    }

    private static void clearSmolderSound(ServerLevel level, BlockPos pos) {
        Long2LongOpenHashMap schedule = NEXT_SMOLDER_SOUND_TICKS.get(level.dimension());
        if (schedule == null) {
            return;
        }

        schedule.remove(pos.asLong());
        if (schedule.isEmpty()) {
            NEXT_SMOLDER_SOUND_TICKS.remove(level.dimension());
        }
    }

    private static void pruneSmolderSoundSchedule(ServerLevel level) {
        Long2LongOpenHashMap schedule = NEXT_SMOLDER_SOUND_TICKS.get(level.dimension());
        if (schedule == null || schedule.isEmpty()) {
            return;
        }

        var iterator = schedule.long2LongEntrySet().iterator();
        while (iterator.hasNext()) {
            Long2LongMap.Entry entry = iterator.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            BlockState state = level.getBlockState(pos);
            boolean stillSmoldering = isTorchSmoldering(state)
                || isCandleSmoldering(state)
                || (state.is(BURNING_CAMPFIRE.get())
                    && state.hasProperty(InfinityCampfireBlock.SMOLDERING)
                    && state.getValue(InfinityCampfireBlock.SMOLDERING)
                    && state.getValue(CampfireBlock.LIT));
            if (!stillSmoldering) {
                iterator.remove();
            }
        }

        if (schedule.isEmpty()) {
            NEXT_SMOLDER_SOUND_TICKS.remove(level.dimension());
        }
    }

    private static void tickPuffs(ServerLevel level) {
        Long2IntOpenHashMap puffs = ACTIVE_PUFFS.get(level.dimension());
        if (puffs == null || puffs.isEmpty()) {
            return;
        }

        var iterator = puffs.long2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry entry = iterator.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (entry.getIntValue() % 3 == 0) {
                spawnDepletionParticles(level, pos);
            }

            int remaining = entry.getIntValue() - 1;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }

        if (puffs.isEmpty()) {
            ACTIVE_PUFFS.remove(level.dimension());
        }

        Long2ObjectOpenHashMap<BlockState> replacements = PENDING_PUFF_REPLACEMENTS.get(level.dimension());
        if (replacements == null || replacements.isEmpty()) {
            return;
        }

        var replacementIterator = replacements.long2ObjectEntrySet().iterator();
        while (replacementIterator.hasNext()) {
            var entry = replacementIterator.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (puffs.containsKey(entry.getLongKey())) {
                continue;
            }
            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, entry.getValue(), Block.UPDATE_ALL);
            }
            replacementIterator.remove();
        }

        if (replacements.isEmpty()) {
            PENDING_PUFF_REPLACEMENTS.remove(level.dimension());
        }
    }

    private static void spawnDepletionParticles(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + depletionParticleYOffset(state);
        double z = pos.getZ() + 0.5D;

        level.sendParticles(new DustParticleOptions(new Vector3f(0.1F, 0.1F, 0.1F), 0.55F),
            x, y, z, 1, 0.025D, 0.05D, 0.025D, 0.0003D);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.22F, 0.22F, 0.22F), 0.45F),
            x, y + 0.02D, z, 1, 0.025D, 0.05D, 0.025D, 0.0003D);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.65F, 0.65F, 0.65F), 0.35F),
            x, y + 0.04D, z, 1, 0.025D, 0.05D, 0.025D, 0.0002D);
        level.sendParticles(ParticleTypes.SMOKE,
            x, y + 0.03D, z, 1, 0.02D, 0.04D, 0.02D, 0.004D);
    }

    private static double depletionParticleYOffset(BlockState state) {
        if (state.is(Blocks.JACK_O_LANTERN) || state.is(GLOWSTONE_JACK_O_LANTERN.get()) || state.is(Blocks.CARVED_PUMPKIN)) {
            return 0.9D;
        }
        return 0.6D;
    }

    private static void applyClientReplacement(Level level, BlockPos pos, BlockState placedState, BlockState replacedState) {
        if (placedState.is(Blocks.TORCH)) {
            level.setBlock(pos, BURNING_TORCH.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            return;
        }

        if (placedState.is(Blocks.WALL_TORCH)) {
            level.setBlock(pos, BURNING_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, placedState.getValue(WallTorchBlock.FACING)), Block.UPDATE_CLIENTS);
            return;
        }

        if (placedState.is(Blocks.LANTERN)) {
            level.setBlock(pos, copyLanternState(placedState, BURNING_LANTERN.get().defaultBlockState()), Block.UPDATE_CLIENTS);
            return;
        }

        if (placedState.is(Blocks.JACK_O_LANTERN)) {
            return;
        }

        if (isSupportedVanillaCandle(placedState)) {
            BlockState replacement = mergePlacedCandleState(replacedState, placedState, toBurningCandleState(placedState));
            if (replacement != null) {
                level.setBlock(pos, replacement, Block.UPDATE_CLIENTS);
            }
            return;
        }

        if (placedState.is(Blocks.CAMPFIRE)) {
            level.setBlock(pos, copyCampfireState(placedState, BURNING_CAMPFIRE.get().defaultBlockState(), true), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean isSupportedVanillaCandle(BlockState state) {
        String candleId = getSupportedCandleId(state);
        return candleId != null && !isBurningCandle(state) && state.is(BlockTags.CANDLES) && state.hasProperty(AbstractCandleBlock.LIT);
    }

    private static boolean isBurningCandle(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (!path.startsWith("burning_")) {
            return false;
        }

        String candleId = path.substring("burning_".length());
        return BURNING_CANDLES.containsKey(candleId) && state.hasProperty(BurningCandleBlock.SMOLDERING);
    }

    private static boolean isCandleSmoldering(BlockState state) {
        return isBurningCandle(state)
            && state.hasProperty(BurningCandleBlock.SMOLDERING)
            && state.hasProperty(AbstractCandleBlock.LIT)
            && state.getValue(AbstractCandleBlock.LIT)
            && state.getValue(BurningCandleBlock.SMOLDERING);
    }

    private static String getSupportedCandleId(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (path.startsWith("burning_")) {
            path = path.substring("burning_".length());
        }
        return BURNING_CANDLES.containsKey(path) ? path : null;
    }

    private static BlockState toBurningCandleState(BlockState source) {
        String candleId = getSupportedCandleId(source);
        if (candleId == null) {
            return null;
        }

        DeferredHolder<Block, Block> holder = BURNING_CANDLES.get(candleId);
        if (holder == null) {
            return null;
        }

        return copyCandleState(source, holder.get().defaultBlockState(), true, false);
    }

    private static BlockState mergePlacedCandleState(BlockState replacedState, BlockState placedState, BlockState replacement) {
        if (replacement == null || !replacement.hasProperty(CandleBlock.CANDLES)) {
            return replacement;
        }

        String placedCandleId = getSupportedCandleId(placedState);
        String replacedCandleId = getSupportedCandleId(replacedState);
        if (placedCandleId == null || replacedCandleId == null || !placedCandleId.equals(replacedCandleId)) {
            return replacement;
        }

        if (!replacedState.hasProperty(CandleBlock.CANDLES)) {
            return replacement;
        }

        int mergedCandles = Math.min(4, replacedState.getValue(CandleBlock.CANDLES) + 1);
        return replacement.setValue(CandleBlock.CANDLES, mergedCandles);
    }

    private static BlockState toVanillaCandleState(BlockState source, boolean lit) {
        String candleId = getSupportedCandleId(source);
        Supplier<Block> vanillaBlock = candleId == null ? null : VANILLA_CANDLE_BLOCKS.get(candleId);
        if (vanillaBlock == null) {
            return null;
        }

        BlockState target = copyCandleState(source, vanillaBlock.get().defaultBlockState(), true, false);
        return target.setValue(AbstractCandleBlock.LIT, lit);
    }

    private static BlockState copyCandleState(BlockState source, BlockState target, boolean copyLit, boolean copySmoldering) {
        if (source.hasProperty(CandleBlock.CANDLES) && target.hasProperty(CandleBlock.CANDLES)) {
            target = target.setValue(CandleBlock.CANDLES, source.getValue(CandleBlock.CANDLES));
        }
        if (copyLit && source.hasProperty(AbstractCandleBlock.LIT) && target.hasProperty(AbstractCandleBlock.LIT)) {
            target = target.setValue(AbstractCandleBlock.LIT, source.getValue(AbstractCandleBlock.LIT));
        }
        if (source.hasProperty(BlockStateProperties.WATERLOGGED) && target.hasProperty(BlockStateProperties.WATERLOGGED)) {
            target = target.setValue(BlockStateProperties.WATERLOGGED, source.getValue(BlockStateProperties.WATERLOGGED));
        }
        if (copySmoldering && source.hasProperty(BurningCandleBlock.SMOLDERING) && target.hasProperty(BurningCandleBlock.SMOLDERING)) {
            target = target.setValue(BurningCandleBlock.SMOLDERING, source.getValue(BurningCandleBlock.SMOLDERING));
        }
        if (target.hasProperty(BurningCandleBlock.SMOLDERING) && !copySmoldering) {
            target = target.setValue(BurningCandleBlock.SMOLDERING, Boolean.FALSE);
        }
        return target;
    }

    private static BlockState copyLanternState(BlockState source, BlockState target) {
        if (source.hasProperty(LanternBlock.HANGING) && target.hasProperty(LanternBlock.HANGING)) {
            target = target.setValue(LanternBlock.HANGING, source.getValue(LanternBlock.HANGING));
        }
        if (source.hasProperty(BlockStateProperties.WATERLOGGED) && target.hasProperty(BlockStateProperties.WATERLOGGED)) {
            target = target.setValue(BlockStateProperties.WATERLOGGED, source.getValue(BlockStateProperties.WATERLOGGED));
        }
        return target;
    }

    private static BlockState copyJackOLanternState(BlockState source, BlockState target) {
        if (source.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && target.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            target = target.setValue(BlockStateProperties.HORIZONTAL_FACING, source.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return target;
    }

    private static BlockState copyCampfireState(BlockState source, BlockState target, boolean copyLit) {
        if (source.hasProperty(CampfireBlock.FACING) && target.hasProperty(CampfireBlock.FACING)) {
            target = target.setValue(CampfireBlock.FACING, source.getValue(CampfireBlock.FACING));
        }
        if (source.hasProperty(CampfireBlock.SIGNAL_FIRE) && target.hasProperty(CampfireBlock.SIGNAL_FIRE)) {
            target = target.setValue(CampfireBlock.SIGNAL_FIRE, source.getValue(CampfireBlock.SIGNAL_FIRE));
        }
        if (source.hasProperty(CampfireBlock.WATERLOGGED) && target.hasProperty(CampfireBlock.WATERLOGGED)) {
            target = target.setValue(CampfireBlock.WATERLOGGED, source.getValue(CampfireBlock.WATERLOGGED));
        }
        if (copyLit && source.hasProperty(CampfireBlock.LIT) && target.hasProperty(CampfireBlock.LIT)) {
            target = target.setValue(CampfireBlock.LIT, source.getValue(CampfireBlock.LIT));
        }
        return target;
    }
}
