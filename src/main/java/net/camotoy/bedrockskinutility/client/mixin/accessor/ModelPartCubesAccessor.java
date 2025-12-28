package net.camotoy.bedrockskinutility.client.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ModelPart.class)
public interface ModelPartCubesAccessor {
    @Accessor("cubes")
    List<ModelPart.Cube> bedrockskinutility$getCubes();
}
