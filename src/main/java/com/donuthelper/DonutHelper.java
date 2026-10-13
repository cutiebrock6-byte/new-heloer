
package com.donuthelper;

import net.fabricmc.api.ClientModInitializer;

public class DonutHelper implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[DonutHelper] Loaded successfully!");
    }
}
