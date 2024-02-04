package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRender;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderOptiFine;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static io.github.cadiboo.nocubes.client.optifine.HD_U_G8.Reflect.BufferBuilder_setMidBlock;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G8.Reflect.Shaders_useMidBlockAttrib;
import static io.github.cadiboo.nocubes.client.optifine.Reflector.tryGetField;
import static io.github.cadiboo.nocubes.client.optifine.Reflector.tryGetMethod;

class HD_U_G8 extends HD_U_G7 {

	@Override
	public @Nullable String notUsableBecause() {
		var reason = super.notUsableBecause();
		if (reason != null)
			return reason;
		for (var field : Reflect.class.getDeclaredFields()) {
			try {
				if (field.get(null) == null)
					return "reflection was unable to find " + field.getName();
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access my own fields...?", e);
			}
		}
		return null;
	}

	@Override
	protected void prePushShaderEntity(INoCubesChunkSectionRender chunkRender, BufferBuilder buffer, BlockPos pos) {
		var chunkRenderOf = (INoCubesChunkSectionRenderOptiFine) chunkRender;
		if (Shaders_useMidBlockAttrib())
			BufferBuilder_setMidBlock(
				buffer,
				0.5F + (float) chunkRenderOf.noCubes$regionDX() + (float) (pos.getX() & 15),
				0.5F + (float) chunkRenderOf.noCubes$regionDY() + (float) (pos.getY() & 15),
				0.5F + (float) chunkRenderOf.noCubes$regionDZ() + (float) (pos.getZ() & 15)
			);
	}

	// All reflection stuff can be null but we check beforehand
	@SuppressWarnings("ConstantConditions")
	interface Reflect {

		Field useMidBlockAttrib = tryGetField("net.optifine.shaders.Shaders", "useMidBlockAttrib");
		MethodHandle setMidBlock = tryGetMethod(BufferBuilder.class.getName(), "setMidBlock", float.class, float.class, float.class);

		static boolean Shaders_useMidBlockAttrib() {
			try {
				return useMidBlockAttrib.getBoolean(null);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void BufferBuilder_setMidBlock(BufferBuilder buffer, float x, float y, float z) {
//			buffer.setMidBlock(x, y, z);
			try {
				setMidBlock.invokeExact(buffer, x, y, z);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

	}

}
