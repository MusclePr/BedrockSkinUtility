package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.interfaces.BedrockRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements BedrockRenderState {
    @Unique
    public AvatarRenderer<?> bedrockskinutility$bedrockRenderer;

    @Override
    public AvatarRenderer<?> bedrockskinutility$getBedrockRenderer() {
        return bedrockskinutility$bedrockRenderer;
    }

    @Override
    public void bedrockskinutility$setBedrockRenderer(AvatarRenderer<?> renderer) {
        this.bedrockskinutility$bedrockRenderer = renderer;
    }
}
