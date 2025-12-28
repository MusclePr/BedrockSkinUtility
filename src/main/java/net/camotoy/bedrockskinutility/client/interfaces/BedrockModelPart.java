package net.camotoy.bedrockskinutility.client.interfaces;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import org.joml.Vector3f;

public interface BedrockModelPart {
    void bedrockskinutility$setBedrockModel();
    void bedrockskinutility$setNeededOffset(boolean needed);
    void bedrockskinutility$setPivot(Vector3f vec3);
    void bedrockskinutility$setAngles(Vector3f vec3);
    void bedrockskinutility$setMesh(Mesh mesh);
}