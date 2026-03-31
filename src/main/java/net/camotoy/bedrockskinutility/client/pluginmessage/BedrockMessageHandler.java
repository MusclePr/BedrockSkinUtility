package net.camotoy.bedrockskinutility.client.pluginmessage;

import com.mojang.blaze3d.platform.NativeImage;
import net.camotoy.bedrockskinutility.client.BedrockCachedProperties;
import net.camotoy.bedrockskinutility.client.BedrockPlayerEntityModel;
import net.camotoy.bedrockskinutility.client.BedrockPlayerRenderer;
import net.camotoy.bedrockskinutility.client.GeometryUtil;
import net.camotoy.bedrockskinutility.client.PlayerSkinBuilder;
import net.camotoy.bedrockskinutility.client.SkinInfo;
import net.camotoy.bedrockskinutility.client.SkinManager;
import net.camotoy.bedrockskinutility.client.SkinUtils;
import net.camotoy.bedrockskinutility.client.interfaces.BedrockPlayerInfo;
import net.camotoy.bedrockskinutility.client.mixin.PlayerSkinFieldAccessor;
import net.camotoy.bedrockskinutility.client.mixin.accessor.EntityRenderDispatcherAccessor;
import net.camotoy.bedrockskinutility.client.pluginmessage.data.BaseSkinInfo;
import net.camotoy.bedrockskinutility.client.pluginmessage.data.CapeData;
import net.camotoy.bedrockskinutility.client.pluginmessage.data.SkinData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.apache.logging.log4j.Logger;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class BedrockMessageHandler {
    private final Logger logger;
    private final SkinManager skinManager;

    public BedrockMessageHandler(Logger logger, SkinManager skinManager) {
        this.logger = logger;
        this.skinManager = skinManager;
    }

    public void handle(CapeData payload, ClientPlayNetworking.Context context) {
        NativeImage capeImage = toNativeImage(payload.capeData(), payload.width(), payload.height());

        context.client().submit(() -> {
            // As of 1.17.1, identical identifiers do not result in multiple objects of the same type being registered
            context.client().getTextureManager().register(payload.identifier(), new DynamicTexture(() -> payload.identifier().toString() + capeImage.hashCode(), capeImage));
            applyCapeTexture(context.client().getConnection(), payload.playerUuid(), payload.identifier());
        });
    }

    /**
     * Should be run from the main thread
     */
    private void applyCapeTexture(ClientPacketListener handler, UUID playerUuid, Identifier identifier) {
        PlayerInfo entry = handler != null ? handler.getPlayerInfo(playerUuid) : null;
        if (entry == null) {
            // Save in the cache for later
            BedrockCachedProperties properties = skinManager.getCachedPlayers().getIfPresent(playerUuid);
            if (properties == null) {
                properties = new BedrockCachedProperties();
                skinManager.getCachedPlayers().put(playerUuid, properties);
            }
            properties.cape = identifier;
        } else {
            final PlayerSkinBuilder builder = new PlayerSkinBuilder(entry.getSkin());
            builder.cape = PlayerSkinBuilder.textureFromResource(identifier);
            builder.bedrockCape = true;
            final PlayerSkin playerSkin = builder.build();
            ((PlayerSkinFieldAccessor) entry).setPlayerSkin(() -> playerSkin);
        }
    }

    public void handle(BaseSkinInfo payload) {
        if (payload == null) {
            return;
        }

        skinManager.getSkinInfo().put(payload.playerUuid(), new SkinInfo(payload.skinWidth(), payload.skinHeight(), payload.jsonGeometry(),
                payload.chunkCount()));
    }

    public void handle(SkinData payload, ClientPlayNetworking.Context context) {
        SkinInfo info = skinManager.getSkinInfo().get(payload.playerUuid());
        if (info == null) {
            this.logger.error("Skin info was null!!!");
            return;
        }
        info.setData(payload.skinData(), payload.chunkPosition());
        this.logger.info("Skin chunk {} received for {}", payload.chunkPosition(), payload.playerUuid());

        if (info.isComplete()) {
            // All skin data has been received
            skinManager.getSkinInfo().remove(payload.playerUuid());
        } else {
            return;
        }

        NativeImage skinImage = toNativeImage(info.getData(), info.getWidth(), info.getHeight());

        if (skinImage == null) {
            logger.error("[BSU] Failed to decode skin image for {}", payload.playerUuid());
            return;
        }

        final boolean DEBUG_SKIN_SAVE = false; // Set to true to save received skin images for debugging purposes
        if (DEBUG_SKIN_SAVE) {
            BufferedImage bufferedImage = SkinUtils.toBufferedImage(info.getData(), info.getWidth(), info.getHeight());
            java.nio.file.Path debugDir = java.nio.file.Paths.get("bedrock_skins");
            try {
                java.nio.file.Files.createDirectories(debugDir);
                java.nio.file.Path outFile = debugDir.resolve(payload.playerUuid() + ".png");
                javax.imageio.ImageIO.write(bufferedImage, "png", outFile.toFile());
                logger.info("[BSU] Saved debug skin image: {}", outFile.toAbsolutePath());
            } catch (IOException e) {
                logger.error("[BSU] Failed to save debug skin image", e);
            }
        }

        final BedrockPlayerEntityModel<?> bedrockModel;
        boolean setModel = info.getGeometry() != null && !info.getGeometry().isEmpty();

        Identifier identifier = Identifier.fromNamespaceAndPath("geyserskinmanager", "textures/" + payload.playerUuid() + ".png");

        Minecraft client = context.client();

        if (setModel) {
            BedrockPlayerEntityModel<?> model = null;

            try {
                final List<BedrockGeometryModel> geometries = BedrockGeometryModel.fromJson(info.getGeometry());
                if (!geometries.isEmpty()) {
                    // Convert Bedrock JSON geometry into a class format that Java understands
                    model = GeometryUtil.bedrockGeoToJava(geometries.getFirst());
                }
            } catch (final Exception ignored) {
            }

            bedrockModel = model;
        } else {
            bedrockModel = null;
        }

        final NativeImage finalSkinImage = skinImage;

        client.submit(() -> {
            client.getTextureManager().register(identifier, new DynamicTexture(() -> identifier.toString() + finalSkinImage.hashCode(), finalSkinImage));

            final ClientPacketListener connection = client.getConnection();
            AvatarRenderer<AbstractClientPlayer> renderer = null;
            if (bedrockModel != null) {
                final PlayerInfo entry = connection != null ? connection.getPlayerInfo(payload.playerUuid()) : null;
                final boolean slim = entry != null && entry.getSkin() != null && entry.getSkin().model() == PlayerModelType.SLIM;

                final EntityRenderDispatcherAccessor dispatcherAccessor =
                        (EntityRenderDispatcherAccessor) client.getEntityRenderDispatcher();
                EntityRendererProvider.Context entityContext = new EntityRendererProvider.Context(
                        client.getEntityRenderDispatcher(),
                        dispatcherAccessor.bedrockskinutility$getBlockModelResolver(),
                        client.getItemModelResolver(),
                        client.getMapRenderer(),
                        client.getResourceManager(),
                        client.getEntityModels(),
                        dispatcherAccessor.bedrockskinutility$getEquipmentAssets(),
                        client.getAtlasManager(),
                        client.font,
                        client.playerSkinRenderCache()
                );
                final BedrockPlayerRenderer bedrockRenderer = new BedrockPlayerRenderer(entityContext, slim, identifier);
                bedrockRenderer.bedrockskinutility$setModel(bedrockModel);
                renderer = bedrockRenderer;
            }

            applySkinTexture(connection, payload.playerUuid(), identifier, renderer);
        });
    }

    /**
     * Should be run from the main thread
     */
    private void applySkinTexture(ClientPacketListener handler, UUID playerUuid, Identifier identifier, AvatarRenderer<AbstractClientPlayer> renderer) {
        PlayerInfo entry = handler != null ? handler.getPlayerInfo(playerUuid) : null;
        if (entry == null) {
            // Save in the cache for later
            BedrockCachedProperties properties = skinManager.getCachedPlayers().getIfPresent(playerUuid);
            if (properties == null) {
                properties = new BedrockCachedProperties();
                skinManager.getCachedPlayers().put(playerUuid, properties);
            }
            properties.model = renderer;
            properties.skin = identifier;
        } else {
            try {
                ((BedrockPlayerInfo) entry).bedrockskinutility$setModel(renderer);

                final PlayerSkinBuilder builder = new PlayerSkinBuilder(entry.getSkin());
                builder.body = PlayerSkinBuilder.textureFromResource(identifier);
                builder.bedrockSkin = true;
                final PlayerSkin playerSkin = builder.build();
                ((PlayerSkinFieldAccessor) entry).setPlayerSkin(() -> playerSkin);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private NativeImage toNativeImage(byte[] data, int width, int height) {
        BufferedImage bufferedImage = SkinUtils.toBufferedImage(data, width, height);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", outputStream);
            return NativeImage.read(outputStream.toByteArray());
        } catch (IOException ignored) {
            return null;
        }
    }
}
