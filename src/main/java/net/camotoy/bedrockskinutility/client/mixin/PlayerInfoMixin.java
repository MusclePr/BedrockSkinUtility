package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.interfaces.BedrockPlayerInfo;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin implements BedrockPlayerInfo {
    private AvatarRenderer<AbstractClientPlayer> bedrockModel;

    @Override
    public AvatarRenderer<AbstractClientPlayer> bedrockskinutility$getModel() {
        return this.bedrockModel;
    }

    @Override
    public void bedrockskinutility$setModel(AvatarRenderer<AbstractClientPlayer> renderer) {
        this.bedrockModel = renderer;
    }
}
