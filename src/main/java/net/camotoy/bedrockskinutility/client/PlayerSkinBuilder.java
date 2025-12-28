package net.camotoy.bedrockskinutility.client;

import net.camotoy.bedrockskinutility.client.interfaces.BedrockPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

public final class PlayerSkinBuilder {
    public ClientAsset.Texture body;
    public ClientAsset.Texture cape;
    public ClientAsset.Texture elytra;
    public PlayerModelType model;
    public boolean secure;
    public boolean bedrockSkin;
    public boolean bedrockCape;

    public PlayerSkinBuilder(final PlayerSkin base) {
        this.body = base.body();
        this.cape = base.cape();
        this.elytra = base.elytra();
        this.model = base.model();
        this.secure = base.secure();
        this.bedrockSkin = ((BedrockPlayerSkin) (Object) base).bedrockskinutility$bedrockSkin();
        this.bedrockCape = ((BedrockPlayerSkin) (Object) base).bedrockskinutility$bedrockCape();
    }

    public PlayerSkin build() {
        final PlayerSkin playerSkin = new PlayerSkin(
                body,
                cape,
                elytra,
                model,
                secure
        );
        ((BedrockPlayerSkin) (Object) playerSkin).bedrockskinutility$bedrockSkin(bedrockSkin);
        ((BedrockPlayerSkin) (Object) playerSkin).bedrockskinutility$bedrockCape(bedrockCape);
        return playerSkin;
    }

    public static ClientAsset.Texture textureFromResource(Identifier id) {
        // In 1.21.9, ResourceTexture can derive a different texturePath from the id
        // (e.g. "textures/<path>.png"), which won't match dynamic TextureManager registrations.
        // Keep both id and texturePath identical so PlayerSkin resolves the registered texture.
        return new ClientAsset.ResourceTexture(id, id);
    }
}
