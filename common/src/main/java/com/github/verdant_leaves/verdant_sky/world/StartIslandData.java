package com.github.verdant_leaves.verdant_sky.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 记录"初始岛屿是否已生成"的持久化数据。
 * 存档里对应 data/verdant_sky_start_island.dat
 */
public class StartIslandData extends SavedData {

    private static final String DATA_NAME = "verdant_sky_start_island";
    private static final String TAG_GENERATED = "generated";

    private boolean generated = false;

    public static StartIslandData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            StartIslandData::load,
            StartIslandData::new,
            DATA_NAME
        );
    }

    public boolean isGenerated() {
        return generated;
    }

    public void markGenerated() {
        if (!this.generated) {
            this.generated = true;
            setDirty();
        }
    }

    public static StartIslandData load(CompoundTag tag) {
        StartIslandData data = new StartIslandData();
        data.generated = tag.getBoolean(TAG_GENERATED);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean(TAG_GENERATED, generated);
        return tag;
    }
}