package net.camotoy.bedrockskinutility.client.pluginmessage.data;

import net.camotoy.bedrockskinutility.client.pluginmessage.BedrockMessageHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record CapeData(UUID playerUuid, int width, int height, Identifier identifier, byte[] capeData) implements BedrockData {
    public static final StreamDecoder<FriendlyByteBuf, CapeData> STREAM_DECODER = buf -> {
        int version = buf.readInt();
        if (version != 1) {
            throw new RuntimeException("Could not load cape data! Is the mod and plugin updated?");
        }

        UUID playerUuid = new UUID(buf.readLong(), buf.readLong());
        int width = buf.readInt();
        int height = buf.readInt();

        String capeId = BedrockData.readString(buf);
        Identifier identifier = Identifier.fromNamespaceAndPath("geyserskinmanager", capeId);

        byte[] capeData = new byte[buf.readInt()];
        buf.readBytes(capeData);
        return new CapeData(playerUuid, width, height, identifier, capeData);
    };

    @Override
    public void handle(ClientPlayNetworking.Context context, BedrockMessageHandler handler) {
        handler.handle(this, context);
    }
}
