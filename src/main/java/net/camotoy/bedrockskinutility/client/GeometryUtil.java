package net.camotoy.bedrockskinutility.client;

import com.google.common.collect.Maps;
import net.camotoy.bedrockskinutility.client.interfaces.BedrockModelPart;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.element.UVMap;
import org.joml.Vector3f;

import java.util.*;

public class GeometryUtil {
    private static final List<String> LEG_RELATED = List.of("leftleg", "rightleg");
    private static final List<String> PANTS_RELATED = List.of("leftpants", "rightpants");

    public static BedrockPlayerEntityModel<AbstractClientPlayer> bedrockGeoToJava(BedrockGeometryModel geometry) {
        // There are some times when the skin image file is larger than the geometry UV points.
        // In this case, we need to scale UV calls
        // https://github.com/Camotoy/BedrockSkinUtility/issues/9
        final float uvWidth = geometry.getTextureSize().getX();
        final float uvHeight = geometry.getTextureSize().getY();

        final Map<String, PartInfo> stringToPart = new HashMap<>();
        for (final Parent bone : geometry.getParents()) {
            final Map<String, ModelPart> children = Maps.newHashMap();
            final ModelPart part = new ModelPart(List.of(), children);
            // Arm and leg
            boolean neededOffset = switch (bone.getName().toLowerCase(Locale.ROOT)) {
                case "rightarm", "leftarm", "rightleg", "leftleg" -> true;
                default -> false;
            };

            ((BedrockModelPart)((Object)part)).bedrockskinutility$setBedrockModel();
            ((BedrockModelPart)((Object)part)).bedrockskinutility$setNeededOffset(neededOffset);
            ((BedrockModelPart)((Object)part)).bedrockskinutility$setAngles(new Vector3f(bone.getRotation().getX() , bone.getRotation().getY(), bone.getRotation().getZ()));

            boolean leg = LEG_RELATED.contains(bone.getName().toLowerCase(Locale.ROOT));
            boolean pants = PANTS_RELATED.contains(bone.getName().toLowerCase(Locale.ROOT));
            if (leg) {
                part.setPos(0, bone.getPivot().getY(), 0);
                part.setInitialPose(part.storePose());
            } else if (pants) {
                ((BedrockModelPart)((Object)part)).bedrockskinutility$setPivot(new Vector3f(bone.getPivot().getX(), -bone.getPivot().getY(), bone.getPivot().getZ()));
            } else {
                ((BedrockModelPart)((Object)part)).bedrockskinutility$setPivot(new Vector3f(bone.getPivot().getX(), -bone.getPivot().getY() + 24.016F, bone.getPivot().getZ()));
            }

            // Java don't allow individual cubes to have their own rotation therefore, we have to separate each cube into ModelPart to be able to rotate.
            for (final Cube cube : bone.getCubes().values()) {
                final Position3V pos = cube.getPosition();
                final float sizeY = cube.getSize().getY();

                // Use Java-equivalent position for vertical placement to avoid legs sinking into ground
                final float placeY = leg || pants ? pos.getY() : -(pos.getY() - 24.016F + sizeY);
                
                final ModelPart cubePart = new ModelPart(List.of(), Map.of());
                Renderer renderer = Renderer.get();
                if (renderer != null) {
                    MutableMesh mesh = renderer.mutableMesh();
                    addCubeToMesh(mesh, cube, placeY, uvWidth, uvHeight);
                    ((BedrockModelPart) (Object) cubePart).bedrockskinutility$setMesh(mesh.immutableCopy());
                }

                ((BedrockModelPart)((Object)cubePart)).bedrockskinutility$setPivot(new Vector3f(cube.getPivot().getX(), -cube.getPivot().getY() + 24.016F, cube.getPivot().getZ()));
                ((BedrockModelPart)((Object)cubePart)).bedrockskinutility$setAngles(new Vector3f(cube.getRotation().getX(), cube.getRotation().getY(), cube.getRotation().getZ()));
                ((BedrockModelPart)((Object)cubePart)).bedrockskinutility$setBedrockModel();
                ((BedrockModelPart)((Object)cubePart)).bedrockskinutility$setNeededOffset(neededOffset);
                children.put(cube.getParent() + cube.hashCode(), cubePart);
            }

            String parent = bone.getParent();
            String name = bone.getName();
            switch (name.toLowerCase(Locale.ROOT)) { // Also do this with the overlays? Those are final, though.
                case "head", "rightarm", "body", "leftarm", "leftleg", "rightleg" -> parent = "root";
            }

            stringToPart.put(adjustFormatting(name), new PartInfo(adjustFormatting(parent), part, children));
        }

        PartInfo root = stringToPart.get("root");
        if (root == null) {
            final Map<String, ModelPart> rootParts = Maps.newHashMap();
            stringToPart.put("root", root = new PartInfo("", new ModelPart(List.of(), rootParts), rootParts));
        }

        for (Map.Entry<String, PartInfo> entry : stringToPart.entrySet()) {
            if (entry.getValue().parent.isBlank() && entry.getValue().part() != root.part) {
                root.children.put(entry.getKey(), entry.getValue().part());
                continue;
            }

            PartInfo parentPart = stringToPart.get(entry.getValue().parent);
            if (parentPart != null) {
                parentPart.children.put(entry.getKey(), entry.getValue().part);
            }
        }

        return new BedrockPlayerEntityModel<>(root.part());
    }

    private static String adjustFormatting(String name) {
        if (name == null) {
            return null;
        }

        return switch (name.toLowerCase(Locale.ROOT)) {
            case "leftarm" -> "left_arm";
            case "rightarm" -> "right_arm";
            case "leftleg" -> "left_leg";
            case "rightleg" -> "right_leg";
            case "leftpants" -> "left_pants";
            case "rightpants" -> "right_pants";
            default -> name.toLowerCase(Locale.ROOT);
        };
    }

    private record PartInfo(String parent, ModelPart part, Map<String, ModelPart> children) {
    }

    private static void addCubeToMesh(MutableMesh mesh, Cube cube, float placeY, float uvWidth, float uvHeight) {
        QuadEmitter emitter = mesh.emitter();
        float x = cube.getPosition().getX();
        float y = placeY;
        float z = cube.getPosition().getZ();
        float sizeX = cube.getSize().getX();
        float sizeY = cube.getSize().getY();
        float sizeZ = cube.getSize().getZ();
        float inflate = cube.getInflate();
        boolean mirror = cube.isMirror();

        float minX = x - inflate;
        float minY = y - inflate;
        float minZ = z - inflate;
        float maxX = x + sizeX + inflate;
        float maxY = y + sizeY + inflate;
        float maxZ = z + sizeZ + inflate;

        if (!mirror) {
            float temp = maxX;
            maxX = minX;
            minX = temp;
        }

        Vector3f v1 = new Vector3f(minX, minY, minZ);
        Vector3f v2 = new Vector3f(maxX, minY, minZ);
        Vector3f v3 = new Vector3f(maxX, maxY, minZ);
        Vector3f v4 = new Vector3f(minX, maxY, minZ);
        Vector3f v5 = new Vector3f(minX, minY, maxZ);
        Vector3f v6 = new Vector3f(maxX, minY, maxZ);
        Vector3f v7 = new Vector3f(maxX, maxY, maxZ);
        Vector3f v8 = new Vector3f(minX, maxY, maxZ);

        UVMap uvMap = cube.getUvMap();

        // DOWN
        emitQuad(emitter, Direction.DOWN, new Vector3f[]{v7, v8, v4, v3}, uvMap.getUvMap().get(org.cube.converter.util.element.Direction.DOWN), uvWidth, uvHeight, mirror);
        // UP
        emitQuad(emitter, Direction.UP, new Vector3f[]{v6, v2, v1, v5}, uvMap.getUvMap().get(org.cube.converter.util.element.Direction.UP), uvWidth, uvHeight, mirror);
        // WEST
        emitQuad(emitter, Direction.WEST, new Vector3f[]{v5, v1, v4, v8}, uvMap.getUvMap().get(org.cube.converter.util.element.Direction.WEST), uvWidth, uvHeight, mirror);
        // NORTH
        emitQuad(emitter, Direction.NORTH, new Vector3f[]{v1, v2, v3, v4}, uvMap.getUvMap().get(org.cube.converter.util.element.Direction.NORTH), uvWidth, uvHeight, mirror);
        // EAST
        emitQuad(emitter, Direction.EAST, new Vector3f[]{v2, v6, v7, v3}, uvMap.getUvMap().get(org.cube.converter.util.element.Direction.EAST), uvWidth, uvHeight, mirror);
        // SOUTH
        emitQuad(emitter, Direction.SOUTH, new Vector3f[]{v6, v5, v8, v7}, uvMap.getUvMap().get(org.cube.converter.util.element.Direction.SOUTH), uvWidth, uvHeight, mirror);
    }

    private static void emitQuad(QuadEmitter emitter, Direction direction, Vector3f[] vertices, Float[] uv, float uvWidth, float uvHeight, boolean mirror) {
        if (uv == null) return;

        if (mirror) {
            Vector3f temp = vertices[0];
            vertices[0] = vertices[3];
            vertices[3] = temp;
            temp = vertices[1];
            vertices[1] = vertices[2];
            vertices[2] = temp;
        }

        for (int i = 0; i < 4; i++) {
            emitter.pos(i, vertices[i].x / 16.0f, vertices[i].y / 16.0f, vertices[i].z / 16.0f);
        }

        float u1 = uv[0];
        float v1 = uv[1];
        float u2 = uv[2];
        float v2 = uv[3];

        for (int i = 0; i < 4; i++) {
            float u = (i == 0 || i == 3) ? u2 : u1;
            float v = (i < 2) ? v1 : v2;
            emitter.uv(i, u / uvWidth, v / uvHeight);
        }

        emitter.nominalFace(direction);
        emitter.emit();
    }
}
