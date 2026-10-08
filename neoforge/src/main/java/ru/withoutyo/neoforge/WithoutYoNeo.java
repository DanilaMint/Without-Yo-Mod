package ru.withoutyo.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.withoutyo.WithoutYoConstants;

/**
 * Точка входа на NeoForge (1.21.x). Реальной инициализации нет — модуль Mixin'ов
 * подгружается автоматически через {@code META-INF/neoforged.mods.toml} и Mixin-loader.
 */
@Mod(WithoutYoConstants.MOD_ID)
public final class WithoutYoNeo {

    public static final Logger LOGGER = LoggerFactory.getLogger("withoutyo");

    public WithoutYoNeo(IEventBus modBus, ModContainer container) {
        LOGGER.info("[{}] {} v{} initialized (NeoForge)",
                WithoutYoConstants.DISPLAY_NAME,
                WithoutYoConstants.MOD_ID,
                "1.0.0");

        // Регистрируем хук на client setup для логирования
        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[{}] client setup complete", WithoutYoConstants.MOD_ID);
    }
}