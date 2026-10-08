package ru.withoutyo.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.withoutyo.WithoutYoConstants;

/**
 * Точка входа на Forge (1.20.1). Реальной инициализации нет — модуль Mixin'ов
 * подгружается автоматически через {@code META-INF/mods.toml} и Mixin-loader.
 * Конструктор {@code @Mod}-аннотации регистрирует этот класс как entrypoint.
 */
@Mod(WithoutYoConstants.MOD_ID)
public final class WithoutYoForge {

    public static final Logger LOGGER = LoggerFactory.getLogger("withoutyo");

    public WithoutYoForge() {
        LOGGER.info("[{}] {} v{} initialized (Forge)",
                WithoutYoConstants.DISPLAY_NAME,
                WithoutYoConstants.MOD_ID,
                "1.0.0");

        // Хук для client-side инициализации (используется только для логирования)
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Заглушка: реальной работы нет, Mixin-loader сам применяет Mixin'ы из common/.
        LOGGER.info("[{}] client setup complete", WithoutYoConstants.MOD_ID);
    }
}