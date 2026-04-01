package com.infinitylights;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ActiveLightRegistryData extends SavedData {
    private static final String DATA_NAME = "infinitylights_active_lights";
    private static final String POSITIONS_TAG = "TrackedPositions";

    private final LongOpenHashSet positions = new LongOpenHashSet();

    public static ActiveLightRegistryData get(ServerLevel level) {
        // Persisted per-dimension set of active modded lights used by the daily depletion pass.
        return level.getDataStorage().computeIfAbsent(ActiveLightRegistryData::load, ActiveLightRegistryData::new, DATA_NAME);
    }

    public static ActiveLightRegistryData load(CompoundTag tag) {
        ActiveLightRegistryData data = new ActiveLightRegistryData();
        for (long pos : tag.getLongArray(POSITIONS_TAG)) {
            data.positions.add(pos);
        }
        return data;
    }

    public void track(BlockPos pos) {
        if (this.positions.add(pos.asLong())) {
            setDirty();
        }
    }

    public void untrack(BlockPos pos) {
        if (this.positions.remove(pos.asLong())) {
            setDirty();
        }
    }

    public LongArrayList snapshot() {
        return new LongArrayList(this.positions);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray(POSITIONS_TAG, this.positions.toLongArray());
        return tag;
    }
}
