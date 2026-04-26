package net.camotoy.bedrockskinutility.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BedrockSkinUtilityConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "bedrockskinutility.json";

    private BedrockSkinUtilityConfigLoader() {
    }

    public static BedrockSkinUtilityConfig load(Logger logger) {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        BedrockSkinUtilityConfig defaultConfig = new BedrockSkinUtilityConfig();

        if (Files.notExists(configPath)) {
            save(configPath, defaultConfig, logger);
            return defaultConfig;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            BedrockSkinUtilityConfig config = GSON.fromJson(reader, BedrockSkinUtilityConfig.class);
            if (config == null) {
                logger.warn("[BSU] Config file was empty. Recreating with defaults at {}", configPath.toAbsolutePath());
                save(configPath, defaultConfig, logger);
                return defaultConfig;
            }
            return config;
        } catch (IOException | JsonParseException e) {
            logger.warn("[BSU] Failed to load config from {}. Falling back to defaults.", configPath.toAbsolutePath(), e);
            save(configPath, defaultConfig, logger);
            return defaultConfig;
        }
    }

    private static void save(Path configPath, BedrockSkinUtilityConfig config, Logger logger) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            logger.warn("[BSU] Failed to write config to {}", configPath.toAbsolutePath(), e);
        }
    }
}
