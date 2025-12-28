package net.camotoy.bedrockskinutility.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.camotoy.bedrockskinutility.client.interfaces.BedrockModelPart;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Mixin(ModelPart.class)
public class ModelPartMixin implements BedrockModelPart {
    @Unique
    private static final float DEGREES_TO_RADIANS = 0.017453292519943295f;

    @Shadow public float x;
    @Shadow public float y;
    @Shadow public float z;

    @Shadow @Final
    private Map<String, ModelPart> children;

    @Unique private boolean isBedrockModel;
    @Unique private boolean neededOffset;

    @Unique
    private Vector3f pivot = new Vector3f();

    @Unique
    private Vector3f rotation = new Vector3f();

    @Inject(method = "translateAndRotate", at = @At("HEAD"))
    private void bedrockskinutility$translateAndRotateHead(PoseStack poseStack, CallbackInfo ci) {
        if (!this.isBedrockModel) {
            return;
        }
        poseStack.translate(this.pivot.x / 16.0F, this.pivot.y / 16.0F, this.pivot.z / 16.0F);
        poseStack.mulPose((new Quaternionf()).rotationXYZ(this.rotation.x * DEGREES_TO_RADIANS, this.rotation.y * DEGREES_TO_RADIANS, this.rotation.z * DEGREES_TO_RADIANS));
        poseStack.translate(-this.pivot.x / 16.0F, -this.pivot.y / 16.0F, -this.pivot.z / 16.0F);
    }

    @Inject(method = "translateAndRotate", at = @At("TAIL"))
    private void bedrockskinutility$translateAndRotateTail(PoseStack poseStack, CallbackInfo ci) {
        if (!this.isBedrockModel || !this.neededOffset) {
            return;
        }

        // Have to do this because of how java pivot point and bedrock pivot point system works for certain model part.
        poseStack.translate(-this.x / 16.0F, -this.y / 16.0F, -this.z / 16.0F);
    }

    @Inject(method = "getChild", at = @At("HEAD"), cancellable = true)
    private void getChild(String name, CallbackInfoReturnable<ModelPart> cir) {
        if (this.isBedrockModel) {
            ModelPart child = this.children.get(name);
            if (child == null) {
                final ModelPart placeholder = new ModelPart(List.of(), new HashMap<>());
                ((BedrockModelPart) (Object) placeholder).bedrockskinutility$setBedrockModel();
                try {
                    this.children.put(name, placeholder);
                } catch (UnsupportedOperationException ignored) {
                    // Some parts may have immutable children maps (e.g., Map.of()).
                    // In that case we still return the placeholder, but can't cache it.
                }
                child = placeholder;
            }
            cir.setReturnValue(child);
        }
    }

    @Override
    public void bedrockskinutility$setNeededOffset(boolean needed) {
        this.neededOffset = needed;
    }

    @Override
    public void bedrockskinutility$setBedrockModel() {
        this.isBedrockModel = true;
    }

    @Override
    public void bedrockskinutility$setPivot(Vector3f vec3) {
        this.pivot = vec3;
    }

    @Override
    public void bedrockskinutility$setAngles(Vector3f vec3) {
        this.rotation = new Vector3f(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public boolean bedrockskinutility$isBedrockModel() {
        return this.isBedrockModel;
    }

    @Override
    public boolean bedrockskinutility$isNeededOffset() {
        return this.neededOffset;
    }

    @Override
    public Vector3f bedrockskinutility$getPivot() {
        return this.pivot;
    }

    @Override
    public Vector3f bedrockskinutility$getRotation() {
        return this.rotation;
    }
}
