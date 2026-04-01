package com.infinitylights;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.joml.Vector3f;
import org.slf4j.Logger;
import net.minecraftforge.event.server.ServerStartingEvent;

import java.util.HashMap;
import java.util.Map;

@Mod(InfinityLightsMod.MOD_ID)
public class InfinityLightsMod {
    public static final String MOD_ID = "infinitylights";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEPLETION_PUFF_TICKS = 20;
    private static final int NIGHT_CHECK_START = 18000;
    private static final int NIGHT_CHECK_END = 22000;

    private static final Map<ResourceKey<Level>, Long> LAST_DEPLETION_DAY = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long> LAST_SCHEDULED_DAY = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long> NIGHT_DEPLETION_TICK = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long2IntOpenHashMap> ACTIVE_PUFFS = new HashMap<>();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<Block> STATIC_TORCH = BLOCKS.register("static_torch",
            () -> new GlowstoneTorchBlock(Block.Properties.copy(Blocks.TORCH).lightLevel(state -> 15)));
    public static final RegistryObject<Block> STATIC_WALL_TORCH = BLOCKS.register("static_wall_torch",
            () -> new GlowstoneWallTorchBlock(Block.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 15)));
    public static final RegistryObject<Block> DEPLETED_TORCH = BLOCKS.register("depletedtorch",
            () -> new GlowstoneTorchBlock(Block.Properties.copy(Blocks.TORCH).lightLevel(state -> 0)));
    public static final RegistryObject<Block> DEPLETED_WALL_TORCH = BLOCKS.register("depleted_wall_torch",
            () -> new GlowstoneWallTorchBlock(Block.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 0)));
    public static final RegistryObject<Block> DEPLETED_LANTERN = BLOCKS.register("depletedlantern",
            () -> new GlowstoneLanternBlock(Block.Properties.copy(Blocks.LANTERN).lightLevel(state -> 0)));

    public static final RegistryObject<Item> DEPLETED_TORCH_ITEM = ITEMS.register("depletedtorch",
            () -> new StandingAndWallBlockItem(DEPLETED_TORCH.get(), DEPLETED_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<Item> DEPLETED_LANTERN_ITEM = ITEMS.register("depletedlantern",
            () -> new BlockItem(DEPLETED_LANTERN.get(), new Item.Properties()));
    public static final RegistryObject<RecipeSerializer<ConfigRecyclingRecipe>> CONFIG_RECYCLING_RECIPE =
            RECIPE_SERIALIZERS.register("config_recycling", ConfigRecyclingRecipe.Serializer::new);

    public InfinityLightsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, InfinityLightsConfig.SPEC, "infinitylights.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private static String getModVersionString() {
        return ModList.get().getModContainerById(MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
            LevelAccessor accessor = event.getLevel();
            if (!(accessor instanceof ServerLevel level)) {
                if (accessor instanceof Level clientLevel && clientLevel.isClientSide) {
                    applyClientReplacement(clientLevel, event.getPos(), event.getPlacedBlock());
                }
                return;
            }

            BlockPos pos = event.getPos();
            BlockState placedState = event.getPlacedBlock();

            // Replace vanilla torch placements with static glowstone torches (no flame particles).
            if (placedState.is(Blocks.TORCH)) {
                level.setBlock(pos, STATIC_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                ActiveLightRegistryData.get(level).track(pos);
                return;
            }

            if (placedState.is(Blocks.WALL_TORCH)) {
                Direction facing = placedState.getValue(WallTorchBlock.FACING);
                BlockState replacement = STATIC_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, facing);
                level.setBlock(pos, replacement, Block.UPDATE_ALL);
                ActiveLightRegistryData.get(level).track(pos);
                return;
            }

            if (placedState.is(Blocks.LANTERN)) {
                ActiveLightRegistryData.get(level).track(pos);
            }
        }

        @SubscribeEvent
        public static void onLevelTick(TickEvent.LevelTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
                return;
            }

            if (!(event.level instanceof ServerLevel level)) {
                return;
            }

            tickPuffs(level);

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
        double torchRisk = InfinityLightsConfig.TORCH_DEPLETION_RISK.get();
        double lanternRisk = InfinityLightsConfig.LANTERN_DEPLETION_RISK.get();

        // Only positions tracked as mod-created light sources are candidates for depletion.
        ActiveLightRegistryData tracker = ActiveLightRegistryData.get(level);
        LongArrayList trackedPositions = tracker.snapshot();
        int depletedTorches = 0;
        int depletedLanterns = 0;

        for (long posLong : trackedPositions) {
            BlockPos pos = BlockPos.of(posLong);
            BlockState state = level.getBlockState(pos);

            if (state.is(STATIC_TORCH.get())) {
                if (shouldDeplete(level, torchRisk)) {
                    level.setBlock(pos, DEPLETED_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedTorches++;
                }
                continue;
            }

            if (state.is(STATIC_WALL_TORCH.get())) {
                if (shouldDeplete(level, torchRisk)) {
                    Direction facing = state.getValue(WallTorchBlock.FACING);
                    BlockState replacement = DEPLETED_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, facing);
                    level.setBlock(pos, replacement, Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedTorches++;
                }
                continue;
            }

            if (state.is(Blocks.LANTERN)) {
                if (shouldDeplete(level, lanternRisk)) {
                    BlockState replacement = copyLanternState(state, DEPLETED_LANTERN.get().defaultBlockState());
                    level.setBlock(pos, replacement, Block.UPDATE_ALL);
                    tracker.untrack(pos);
                    startPuff(level, pos);
                    depletedLanterns++;
                }
                continue;
            }

            tracker.untrack(pos);
        }

        if (InfinityLightsConfig.CONSOLE_MESSAGES.get() && (depletedTorches > 0 || depletedLanterns > 0)) {
            LOGGER.info("Depleted {} glowstone torche(s) and {} glowstone lantern(s).", depletedTorches, depletedLanterns);
        }
    }

    private static boolean shouldDeplete(ServerLevel level, double risk) {
        return risk > 0.0D && level.getRandom().nextDouble() < risk;
    }

    private static void startPuff(ServerLevel level, BlockPos pos) {
        // Store a short per-block timer so depletion is visually noticeable but brief.
        ACTIVE_PUFFS.computeIfAbsent(level.dimension(), ignored -> new Long2IntOpenHashMap())
                .put(pos.asLong(), DEPLETION_PUFF_TICKS);
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
    }

    private static void spawnDepletionParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.6D;
        double z = pos.getZ() + 0.5D;

        level.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 0.6F),
                x, y, z, 1, 0.03D, 0.06D, 0.03D, 0.0004D);
        level.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.95F, 0.72F), 0.65F),
                x, y, z, 1, 0.03D, 0.06D, 0.03D, 0.0004D);
    }

    private static void applyClientReplacement(Level level, BlockPos pos, BlockState placedState) {
        if (placedState.is(Blocks.TORCH)) {
            level.setBlock(pos, STATIC_TORCH.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
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
}
