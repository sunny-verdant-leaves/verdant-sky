package com.github.verdant_leaves.verdant_sky.fabric.client;

import net.fabricmc.api.ClientModInitializer;

import com.github.verdant_leaves.verdant_sky.client.VerdantSkyClient;

public final class VerdantSkyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VerdantSkyClient.init();
    }
}
