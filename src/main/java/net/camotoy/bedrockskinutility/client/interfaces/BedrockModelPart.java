package net.camotoy.bedrockskinutility.client.interfaces;

import org.joml.Vector3f;

public interface BedrockModelPart {
    void bedrockskinutility$setBedrockModel();
    void bedrockskinutility$setNeededOffset(boolean needed);
    void bedrockskinutility$setPivot(Vector3f vec3);
    void bedrockskinutility$setAngles(Vector3f vec3);

    boolean bedrockskinutility$isBedrockModel();
    boolean bedrockskinutility$isNeededOffset();
    Vector3f bedrockskinutility$getPivot();
    Vector3f bedrockskinutility$getRotation();
}