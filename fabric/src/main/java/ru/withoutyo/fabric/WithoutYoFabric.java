package ru.withoutyo.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.withoutyo.WithoutYoConstants;

/**
 * Точка входа на Fabric (client-side). Реальной инициализации нет — модуль
 * Mixin'ов подгружается автоматически через {@code fabric.mod.json#mixins}.
 */
public final class WithoutYoFabric implements ClientModInitializer {

    public static final String MOD_ID = WithoutYoConstants.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger("withoutyo");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] {} v{} initialized (Fabric)",
                WithoutYoConstants.DISPLAY_NAME,
                WithoutYoConstants.MOD_ID,
                "1.0.0");
    }
}