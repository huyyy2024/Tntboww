package com.firebow;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireBowClient implements ClientModInitializer {
    public static final String MOD_ID = "firebow";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[FireBow] Client loaded. Fire arrows → Rail + TNT Cart.");
    }
}
