package net.camotoy.bedrockskinutility.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

/**
 * All cached properties of a player, if their PlayerListPacket has not sent or is being reloaded
 */
public class BedrockCachedProperties {
    public Identifier cape;
    public AvatarRenderer<AbstractClientPlayer> model;
    public Identifier skin;
}
