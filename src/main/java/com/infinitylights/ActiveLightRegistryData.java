package com.infinitylights;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class ActiveLightRegistryData extends SavedData {
    private static final String POSITIONS_TAG = "TrackedLights";
    private static final String POSITION_TAG = "Pos";
    private static final String KIND_TAG = "Kind";
    private static final String PLACED_AT_TAG = "PlacedAt";
    private static final String DATA_NAME = InfinityLightsMod.MOD_ID + "_active_lights";

    private final Long2ObjectOpenHashMap<TrackedLight> positions = new Long2ObjectOpenHashMap<>();

    public enum LightKind {
        GLOWSTONE_TORCH,
        GLOWSTONE_LANTERN,
        GLOWSTONE_JACKOLANTERN,
        VANILLA_TORCH,
        VANILLA_CANDLE,
        VANILLA_LANTERN,
        VANILLA_JACKOLANTERN,
        VANILLA_CAMPFIRE
    }

    public record TrackedLight(long posLong, LightKind kind, long placedAtGameTime) {
        public BlockPos pos() {
            return BlockPos.of(this.posLong);
        }
    }

    public ActiveLightRegistryData() {
    }

    private static ActiveLightRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        ActiveLightRegistryData data = new ActiveLightRegistryData();
        ListTag list = tag.getList(POSITIONS_TAG, Tag.TAG_COMPOUND);
        for (Tag rawTag : list) {
            CompoundTag entry = (CompoundTag) rawTag;
            long position = entry.getLong(POSITION_TAG);
            String kindName = entry.getString(KIND_TAG);
            long placedAt = entry.getLong(PLACED_AT_TAG);
            try {
                LightKind kind = LightKind.valueOf(kindName);
                data.positions.put(position, new TrackedLight(position, kind, placedAt));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }

    private static SavedData.Factory<ActiveLightRegistryData> factory() {
        return new SavedData.Factory<>(ActiveLightRegistryData::new, ActiveLightRegistryData::load);
    }

    public static ActiveLightRegistryData get(ServerLevel level) {
        // Persisted per-dimension set of active modded lights used by the daily depletion pass.
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public void track(BlockPos pos, LightKind kind, long placedAtGameTime) {
        long posLong = pos.asLong();
        TrackedLight current = this.positions.get(posLong);
        TrackedLight updated = new TrackedLight(posLong, kind, placedAtGameTime);
        if (!updated.equals(current)) {
            this.positions.put(posLong, updated);
            setDirty();
        }
    }

    public void untrack(BlockPos pos) {
        if (this.positions.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    public List<TrackedLight> snapshot() {
        return new ArrayList<>(this.positions.values());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (TrackedLight trackedLight : this.positions.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(POSITION_TAG, trackedLight.posLong());
            entry.putString(KIND_TAG, trackedLight.kind().name());
            entry.putLong(PLACED_AT_TAG, trackedLight.placedAtGameTime());
            list.add(entry);
        }
        tag.put(POSITIONS_TAG, list);
        return tag;
    }
}
