package cadiboo.nocubes.core;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class TransformRenderChunk implements IClassTransformer {

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
		if(!transformedName.equals("net.minecraft.client.renderer.chunk.RenderChunk")) {
			return basicClass;
		}
		return basicClass;

		ReflectionHelper

	}

}
