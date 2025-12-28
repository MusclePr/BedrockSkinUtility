package net.camotoy.bedrockskinutility.client.interfaces;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public interface BedrockPlayerInfo {
    AvatarRenderer<AbstractClientPlayer> bedrockskinutility$getModel();

    void bedrockskinutility$setModel(AvatarRenderer<AbstractClientPlayer> renderer);
}
