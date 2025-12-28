package net.camotoy.bedrockskinutility.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import net.camotoy.bedrockskinutility.client.interfaces.BedrockRenderState;

public class BedrockPlayerRenderer extends AvatarRenderer<AbstractClientPlayer> {
    private final Identifier texture;

    public BedrockPlayerRenderer(EntityRendererProvider.Context context, boolean bl, Identifier texture) {
        super(context, bl);
        this.texture = texture;
    }

    @Override
    public @NotNull Identifier getTextureLocation(AvatarRenderState avatarRenderState) {
        return this.texture;
    }

    @Override
    public void extractRenderState(AbstractClientPlayer entity, AvatarRenderState avatarRenderState, float partialTick) {
        super.extractRenderState(entity, avatarRenderState, partialTick);

        // 1.21.9 renderer pipeline note:
        // EntityRenderDispatcher.submit calls getRenderer(EntityRenderState).
        // We must ensure that the state carries a reference back to this renderer
        // so that the correct model is used during the submit phase.
        ((BedrockRenderState) avatarRenderState).bedrockskinutility$setBedrockRenderer(this);

        // 1.21.9 renderer pipeline note:
        // LivingEntityRenderer#submit submits the base model without calling Model#setupAnim.
        // setupAnim is only invoked when submitting render layers (and only if layers exist).
        // We rely on setupAnim to apply Bedrock geometry transforms and debug visibility toggles,
        // so invoke it here after the state is fully populated.
        try {
            this.model.setupAnim(avatarRenderState);
        } catch (Throwable ignored) {
        }
    }

    public void bedrockskinutility$setModel(PlayerModel model) {
        this.model = model;
    }
}