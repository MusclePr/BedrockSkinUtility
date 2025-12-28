package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.interfaces.BedrockPlayerInfo;
import net.camotoy.bedrockskinutility.client.interfaces.BedrockRenderState;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererDispatcherMixin {

    @SuppressWarnings("unchecked")
    @Inject(
            method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    public <T extends Entity> void getRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<? super T, ?>> cir) {
        if (!(entity instanceof BedrockAbstractClientPlayerEntity bedrockPlayer)) {
            return;
        }

        PlayerInfo playerListEntry = bedrockPlayer.bedrockskinutility$getPlayerListEntry();
        if (playerListEntry != null) {
            EntityRenderer<? super T, ?> renderer = (EntityRenderer<? super T, ?>) ((BedrockPlayerInfo) playerListEntry).bedrockskinutility$getModel();
            if (renderer != null) {
                cir.setReturnValue(renderer);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(
            method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    public <S extends EntityRenderState> void getRendererFromState(S state, CallbackInfoReturnable<EntityRenderer<?, ? super S>> cir) {
        if (state instanceof AvatarRenderState avatarState) {
            EntityRenderer<?, ?> renderer = ((BedrockRenderState) avatarState).bedrockskinutility$getBedrockRenderer();
            if (renderer != null) {
                cir.setReturnValue((EntityRenderer<?, ? super S>) renderer);
            }
        }
    }
}
