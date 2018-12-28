package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.client.render.BlockRenderData;
import io.github.cadiboo.nocubes.client.render.FluidInBlockRenderer;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

/**
 * Util that is only used on the Physical Client i.e. Rendering code
 *
 * @author Cadiboo
 * @author V0idW4lk3r
 */
@SuppressWarnings("WeakerAccess")
@SideOnly(Side.CLIENT)
public final class ClientUtil {

	/**
	 * A vertex definition for a simple 2-dimensional quad defined in counter-clockwise order with the top-left origin.
	 */
	public static final Vector4f[] SIMPLE_QUAD = {
			new Vector4f(1, 1, 0, 0),
			new Vector4f(1, 0, 0, 0),
			new Vector4f(0, 0, 0, 0),
			new Vector4f(0, 1, 0, 0)
	};
	// add or subtract from the sprites UV location to remove transparent lines in between textures
	private static final float UV_CORRECT = 1 / 10000F;
	/**
	 * A field reference to the rawIntBuffer of the BufferBuilder class. Need reflection since the field is private.
	 */
	// use the old (Class, String...) instead of the new (Class, String, String) for backwards compatibility
	//TODO: change back to (Class, String, String) soon
	private static final Field bufferBuilder_rawIntBuffer = ReflectionHelper.findField(BufferBuilder.class, "rawIntBuffer", "field_178999_b", "field_178999_b");

	/**
	 * Rotation algorithm Taken off Max_the_Technomancer from <a href= "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/2772267-tesr-getting-darker-and-lighter-as-it-rotates">here</a>
	 *
	 * @param face the {@link net.minecraft.util.EnumFacing face} to rotate for
	 */
	public static void rotateForFace(final EnumFacing face) {
		GlStateManager.rotate(face == DOWN ? 0 : face == EnumFacing.UP ? 180F : (face == EnumFacing.NORTH) || (face == EnumFacing.EAST) ? 90F : -90F, face.getAxis() == EnumFacing.Axis.Z ? 1 : 0, 0, face.getAxis() == EnumFacing.Axis.Z ? 0 : 1);
		GlStateManager.rotate(-90, 0, 0, 1);
	}

	/**
	 * Put a lot of effort into this, it gets the entities exact (really, really exact) position
	 *
	 * @param entity       The entity to calculate the position of
	 * @param partialTicks The multiplier used to predict where the entity is/will be
	 * @return The position of the entity as a Vec3d
	 */
	@Nonnull
	public static Vec3d getEntityRenderPos(@Nonnull final Entity entity, @Nonnull final double partialTicks) {
		double flyingMultiplier = 1.825;
		double yFlying = 1.02;
		double yAdd = 0.0784000015258789;

		if ((entity instanceof EntityPlayer) && ((EntityPlayer) entity).capabilities.isFlying) {
			flyingMultiplier = 1.1;
			yFlying = 1.67;
			yAdd = 0;
		}

		final double yGround = ((entity.motionY + yAdd) == 0) && (entity.prevPosY > entity.posY) ? entity.posY - entity.prevPosY : 0;
		double xFall = 1;
		if (flyingMultiplier == 1.825) {
			if (entity.motionX != 0) {
				if ((entity.motionY + yAdd) != 0) {
					xFall = 0.6;
				} else if (yGround != 0) {
					xFall = 0.6;
				}
			} else {
				xFall = 0.6;
			}
		}

		double zFall = 1;
		if (flyingMultiplier == 1.825) {
			if (entity.motionZ != 0) {
				if ((entity.motionY + yAdd) != 0) {
					zFall = 0.6;
				} else if (yGround != 0) {
					zFall = 0.6;
				}
			} else {
				zFall = 0.6;
			}
		}

		final double dX = entity.posX - ((entity.prevPosX - entity.posX) * partialTicks) - ((entity.motionX * xFall) * flyingMultiplier);
		final double dY = entity.posY - yGround - ((entity.prevPosY - entity.posY) * partialTicks) - ((entity.motionY + yAdd) * yFlying);
		final double dZ = entity.posZ - ((entity.prevPosZ - entity.posZ) * partialTicks) - ((entity.motionZ * zFall) * flyingMultiplier);

		return new Vec3d(dX, dY, dZ);
	}

	/**
	 * Rotates around X axis based on Pitch input and around Y axis based on Yaw input
	 *
	 * @param pitch the pitch
	 * @param yaw   the yaw
	 */
	public static void rotateForPitchYaw(final double pitch, final double yaw) {
		GlStateManager.rotate((float) yaw, 0, 1, 0);
		GlStateManager.rotate((float) pitch, 1, 0, 0);
	}

	/**
	 * Gets the pitch rotation between two vectors
	 *
	 * @param source      the source vector
	 * @param destination the destination vector
	 * @return the pitch rotation
	 */
	public static double getPitch(@Nonnull final Vec3d source, @Nonnull final Vec3d destination) {
		double pitch = Math.atan2(destination.y, Math.sqrt((destination.x * destination.x) + (destination.z * destination.z)));
		pitch = pitch * (180 / Math.PI);
		pitch = pitch < 0 ? 360 - (-pitch) : pitch;
		return 90 - pitch;
	}

	/**
	 * Gets the yaw rotation between two vectors
	 *
	 * @param source      the source vector
	 * @param destination the destination vector
	 * @return the yaw rotation
	 */
	public static double getYaw(@Nonnull final Vec3d source, @Nonnull final Vec3d destination) {
		double yaw = Math.atan2(destination.x - source.x, destination.z - source.z);
		yaw = yaw * (180 / Math.PI);
		yaw = yaw < 0 ? 360 - (-yaw) : yaw;
		return yaw + 90;
	}

	/**
	 * @param red   the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param green the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param blue  the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @return the color in ARGB format
	 */
	public static int color(int red, int green, int blue) {

		red = MathHelper.clamp(red, 0x00, 0xFF);
		green = MathHelper.clamp(green, 0x00, 0xFF);
		blue = MathHelper.clamp(blue, 0x00, 0xFF);

		final int alpha = 0xFF;

		// 0x alpha red green blue
		// 0xaarrggbb

		// int colorRGBA = 0;
		// colorRGBA |= red << 16;
		// colorRGBA |= green << 8;
		// colorRGBA |= blue << 0;
		// colorRGBA |= alpha << 24;

		return blue | red << 16 | green << 8 | alpha << 24;

	}

	/**
	 * @param red   the red value of the color, 0F and 1F
	 * @param green the green value of the color, 0F and 1F
	 * @param blue  the blue value of the color, 0F and 1F
	 * @return the color in ARGB format
	 */
	public static int colorf(final float red, final float green, final float blue) {
		final int redInt = Math.max(0, Math.min(255, Math.round(red * 255)));
		final int greenInt = Math.max(0, Math.min(255, Math.round(green * 255)));
		final int blueInt = Math.max(0, Math.min(255, Math.round(blue * 255)));
		return color(redInt, greenInt, blueInt);
	}

	// Below are some helper methods to upload data to the buffer for use by FastTESRs

	public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
	}

	public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return packedLightmapCoords & 0xFFFF; // get lower 4 bytes
	}

	/**
	 * Renders a simple 2 dimensional quad at a given position to a given buffer with the given transforms, color, texture and lightmap values.
	 *
	 * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
	 * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
	 * @param transform          the model matrix to use as the transform matrix.
	 * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
	 * @param texture            the TextureAtlasSprite object to gain the UV data from.
	 * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
	 * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
	 */
	public static void renderSimpleQuad(Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, int color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
		renderCustomQuad(SIMPLE_QUAD, baseOffset, buffer, transform, color, texture, lightmapSkyLight, lightmapBlockLight);
	}

	/**
	 * Renders a simple 2 dimensional quad at a given position to a given buffer with the given transforms, color, texture and lightmap values.
	 *
	 * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
	 * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
	 * @param transform          the model matrix to use as the transform matrix.
	 * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
	 * @param texture            the TextureAtlasSprite object to gain the UV data from.
	 * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
	 * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
	 */
	public static void renderCustomQuad(final Vector4f[] customQuad, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, int color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
		// A quad consists of 4 vertices so the loop is executed 4 times.
		for (int i = 0; i < 4; ++i) {
			// Getting the vertex position from a set of predefined positions for a basic quad.
			Vector4f quadPos = customQuad[i];

			// Transforming the position vector by the transform matrix.
			quadPos = Matrix4f.transform(transform, quadPos, new Vector4f());

			// Getting the RGBA values from the color. (The color is in ARGB format)
			// To put it another way - unpacking an int representation of a color to a 4-component float vector representation.
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = (color & 0xFF) / 255F;
			float a = ((color & 0xFF000000) >> 24) / 255F;

			// Getting the texture UV coordinates from an index. The quad looks like this
			/*0 3
			  1 2*/
			float u = i < 2 ? texture.getMaxU() - UV_CORRECT : texture.getMinU() + UV_CORRECT;
			float v = i == 1 || i == 2 ? texture.getMaxV() - UV_CORRECT : texture.getMinV() + UV_CORRECT;

			// Uploading the quad data to the buffer.
			buffer.pos(quadPos.x + baseOffset.x, quadPos.y + baseOffset.y, quadPos.z + baseOffset.z).color(r, g, b, a).tex(u, v).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
		}
	}

	/**
	 * Renders a collection of BakedQuads into the BufferBuilder given. This method allows you to render any model in game in the FastTESR, be it a block model or an item model.
	 * Alternatively a custom list of quads may be constructed at runtime to render things like text.
	 * Drawbacks: doesn't transform normals as they are not guaranteed to be present in the buffer. Not relevant for a FastTESR but may cause issues with Optifine's shaders.
	 *
	 * @param quads      the iterable of BakedQuads. This may be any iterable object.
	 * @param baseOffset the base position offset for the rendering. This position will not be transformed by the model matrix.
	 * @param pipeline   the vertex consumer object. It is a parameter for optimization reasons. It may simply be constructed as new VertexBufferConsumer(buffer) and may be reused indefinately in the scope of the render pass.
	 * @param buffer     the buffer to upload vertices to.
	 * @param transform  the model matrix that is used to transform quad vertices.
	 * @param brightness the brightness of the model. The packed lightmap coordinate system is pretty complex and a lot of parameters are not necessary here so only the dominant one is implemented.
	 * @param color      the color of the quad. This is a color multiplier in the ARGB format.
	 */
	public static void renderQuads(Iterable<BakedQuad> quads, Vector3f baseOffset, VertexBufferConsumer pipeline, BufferBuilder buffer, Matrix4f transform, float brightness, int color) {
		// Get the raw int buffer of the buffer builder object.
		IntBuffer intBuf = getIntBuffer(buffer);

		// Iterate the iterable
		for (BakedQuad quad : quads) {
			// Push the quad to the consumer so it can be uploaded onto the buffer.
			LightUtil.putBakedQuad(pipeline, quad);

			// After the quad has been uploaded the buffer contains enough info to apply the model matrix transformation.
			// Getting the vertex size for the given format.
			int vertexSize = buffer.getVertexFormat().getIntegerSize();

			// Getting the offset for the current quad.
			int quadOffset = (buffer.getVertexCount() - 4) * vertexSize;

			// Each quad is made out of 4 vertices, so looping 4 times.
			for (int k = 0; k < 4; ++k) {
				// Getting the offset for the current vertex.
				int vertexIndex = quadOffset + k * vertexSize;

				// Grabbing the position vector from the buffer.
				float vertX = Float.intBitsToFloat(intBuf.get(vertexIndex));
				float vertY = Float.intBitsToFloat(intBuf.get(vertexIndex + 1));
				float vertZ = Float.intBitsToFloat(intBuf.get(vertexIndex + 2));
				Vector4f vert = new Vector4f(vertX, vertY, vertZ, 1);

				// Transforming it by the model matrix.
				vert = Matrix4f.transform(transform, vert, new Vector4f());

				// Uploading the difference back to the buffer. Have to use the helper function since the provided putX methods upload the data for a quad, not a vertex and this data is vertex-dependent.
				putPositionForVertex(buffer, intBuf, vertexIndex, new Vector3f(vert.x - vertX, vert.y - vertY, vert.z - vertZ));
			}

			// Uploading the origin position to the buffer. This is an addition operation.
			buffer.putPosition(baseOffset.x, baseOffset.y, baseOffset.z);

			// Constructing the most basic packed lightmap data with a mask of 0x00FF0000.
			int bVal = ((byte) (brightness * 255)) << 16;

			// Uploading the brightness to the buffer.
			buffer.putBrightness4(bVal, bVal, bVal, bVal);

			// Uploading the color multiplier to the buffer
			buffer.putColor4(color);
		}
	}

	/**
	 * A helper method that grabs all BakedQuads of a given model of a given IBlockState and joins them onto a single iterable.
	 *
	 * @param state the block state object to get the quads from.
	 * @return the iterable of BakedQuads.
	 */
	public static Iterable<BakedQuad> iterateQuadsOfBlock(IBlockState state) {
		return Arrays.stream(EnumFacing.values()).map(q -> Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state).getQuads(state, q, 0L)).flatMap(Collection::stream).distinct().collect(Collectors.toList());
	}

	/**
	 * A setter for the vertex-based positions for a given BufferBuilder object.
	 *
	 * @param buffer the buffer to set the positions in.
	 * @param intBuf the raw int buffer.
	 * @param offset the offset for the int buffer, in ints.
	 * @param pos    the position to add to the buffer.
	 */
	public static void putPositionForVertex(BufferBuilder buffer, IntBuffer intBuf, int offset, Vector3f pos) {
		// Getting the old position data in the buffer currently.
		float ox = Float.intBitsToFloat(intBuf.get(offset));
		float oy = Float.intBitsToFloat(intBuf.get(offset + 1));
		float oz = Float.intBitsToFloat(intBuf.get(offset + 2));

		// Converting the new data to ints.
		int x = Float.floatToIntBits(pos.x + ox);
		int y = Float.floatToIntBits(pos.y + oy);
		int z = Float.floatToIntBits(pos.z + oz);

		// Putting the data into the buffer
		intBuf.put(offset, x);
		intBuf.put(offset + 1, y);
		intBuf.put(offset + 2, z);
	}

	/**
	 * A getter for the rawIntBuffer field value of the BufferBuilder.
	 *
	 * @param buffer the buffer builder to get the buffer from
	 * @return the rawIntbuffer component
	 */
	@Nonnull
	public static IntBuffer getIntBuffer(BufferBuilder buffer) {
		try {
			return (IntBuffer) bufferBuilder_rawIntBuffer.get(buffer);
		} catch (IllegalAccessException exception) {
			// Some other mod messed up and reset the access flag of the field.
			CrashReport crashReport = new CrashReport("An impossible error has occurred!", exception);
			crashReport.makeCategory("Reflectively Accessing BufferBuilder#rawIntBuffer");
			throw new ReportedException(crashReport);
		}
	}

	/**
	 * The order of {@link EnumFacing} and null used in {@link #getQuad(IBlockState, BlockPos, BlockRendererDispatcher)}
	 */
	public static final EnumFacing[] ENUMFACING_QUADS_ORDERED = {
			UP, null, DOWN, NORTH, EAST, SOUTH, WEST,
	};

	/**
	 * Gets The first quad of a model for a pos & state or null if the model has no quads
	 *
	 * @param state                   the state
	 * @param pos                     the position used in {@link MathHelper#getPositionRandom(Vec3i)}
	 * @param blockRendererDispatcher the {@link BlockRendererDispatcher} to get the model from
	 * @return The first quad or null if the model has no quads
	 */
	@Nullable
	public static BakedQuad getQuad(final IBlockState state, final BlockPos pos, final BlockRendererDispatcher blockRendererDispatcher) {
		final long posRand = MathHelper.getPositionRandom(pos);
		final IBakedModel model = blockRendererDispatcher.getModelForState(state);
		for (EnumFacing facing : ENUMFACING_QUADS_ORDERED) {
			final List<BakedQuad> quads = model.getQuads(state, facing, posRand);
			if (!quads.isEmpty()) {
				return quads.get(0);
			}
		}
		return null;
	}

	/**
	 * Gets the color of a quad through a block at a pos
	 *
	 * @param quad  the quad
	 * @param state the state
	 * @param cache the cache
	 * @param pos   the pos
	 * @return the color
	 */
	public static int getColor(final BakedQuad quad, final IBlockState state, final IBlockAccess cache, final BlockPos pos) {
		final int red;
		final int green;
		final int blue;

		if (quad.hasTintIndex()) {
			final int colorMultiplier = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
			red = (colorMultiplier >> 16) & 255;
			green = (colorMultiplier >> 8) & 255;
			blue = colorMultiplier & 255;
		} else {
			red = 0xFF;
			green = 0xFF;
			blue = 0xFF;
		}
		return color(red, green, blue);
	}

	/**
	 * Gets the fixed minimum U coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed minimum U coordinate to use when rendering the sprite
	 */
	public static float getMinU(final TextureAtlasSprite sprite) {
		return sprite.getMinU() + UV_CORRECT;
	}

	/**
	 * Gets the fixed maximum U coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed maximum U coordinate to use when rendering the sprite
	 */
	public static float getMaxU(final TextureAtlasSprite sprite) {
		return sprite.getMaxU() - UV_CORRECT;
	}

	/**
	 * Gets the fixed minimum V coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed minimum V coordinate to use when rendering the sprite
	 */
	public static float getMinV(final TextureAtlasSprite sprite) {
		return sprite.getMinV() + UV_CORRECT;
	}

	/**
	 * Gets the fixed maximum V coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed maximum V coordinate to use when rendering the sprite
	 */
	public static float getMaxV(final TextureAtlasSprite sprite) {
		return sprite.getMaxV() - UV_CORRECT;
	}

	public static LightmapInfo getLightmapInfo(BlockPos pos, IBlockAccess cache) {

		switch (Minecraft.getMinecraft().gameSettings.ambientOcclusion) {
			default:
			case 0: // Off
				return new LightmapInfo(240, 0);
			case 1: // Fast
				//the block above
				final BlockPos FAST_BrightnessPos = pos.up();
				final int FAST_PackedLightmapCoords = cache.getBlockState(FAST_BrightnessPos).getPackedLightmapCoords(cache, FAST_BrightnessPos);
				return new LightmapInfo(
						getLightmapSkyLightCoordsFromPackedLightmapCoords(FAST_PackedLightmapCoords),
						getLightmapBlockLightCoordsFromPackedLightmapCoords(FAST_PackedLightmapCoords)
				);
			case 2: // Fancy
				//credit to MineAndCraft12
				int averageSkyLight = 0;
				int totalBlocksCheckedForSkyLight = 0;

				int averageBlockLight = 0;
				int totalBlocksCheckedForBlockLight = 0;

				//every neighbour
				for (EnumFacing facing : EnumFacing.VALUES) {
					final BlockPos FANCYISH_BrightnessPos = pos.offset(facing);
					final int FANCYISH_PackedLightmapCoords = cache.getBlockState(FANCYISH_BrightnessPos).getPackedLightmapCoords(cache, FANCYISH_BrightnessPos);
					final int skyLight = getLightmapSkyLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
					if (skyLight > 0) {
						averageSkyLight += skyLight;
						totalBlocksCheckedForSkyLight++;
					}
					final int blockLight = getLightmapBlockLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
					if (blockLight > 0) {
						averageBlockLight += blockLight;
						totalBlocksCheckedForBlockLight++;
					}
				}

				//this block
				final int FANCYISH_PackedLightmapCoords = cache.getBlockState(pos).getPackedLightmapCoords(cache, pos);
				final int skyLight = getLightmapSkyLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
				if (skyLight > 0) {
					averageSkyLight += skyLight;
					totalBlocksCheckedForSkyLight++;
				}
				final int blockLight = getLightmapBlockLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
				if (blockLight > 0) {
					averageBlockLight += blockLight;
					totalBlocksCheckedForBlockLight++;
				}

				return new LightmapInfo(
						totalBlocksCheckedForSkyLight > 0 ? averageSkyLight / totalBlocksCheckedForSkyLight : averageSkyLight,
						totalBlocksCheckedForBlockLight > 0 ? averageBlockLight / totalBlocksCheckedForBlockLight : averageBlockLight
				);

		}
	}

//	public static void extendLiquids(final RebuildChunkBlockEvent event) {
//
//		final IBlockState state = event.getBlockState();
//		if (!ModUtil.shouldSmooth(state)) {
//			return;
//		}
//		final ChunkCache cache = event.getChunkCache();
//		final BlockPos pos = event.getBlockPos();
//
//		MutableBlockPos liquidPos = null;
//		IBlockState liquidState = null;
//
//		for (final MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
//			final IBlockState tempState = cache.getBlockState(mutablePos);
//			if (tempState.getBlock() instanceof BlockLiquid) {
//				//usually we would make it immutable, but since it wont be changed anymore we can just reference it without worrying about that
//				liquidPos = mutablePos;
//				liquidState = tempState;
//				break;
//			}
//		}
//
//		// set at same time so can skip
//		if (liquidPos == null /*|| liquidState == null*/) {
//			return;
//		}
//
//		event.getBlockRendererDispatcher().renderBlock(liquidState, pos, cache, event.getBufferBuilder());
//
//	}

	public static void handleTransparentBlocksRenderType(final RebuildChunkBlockRenderInTypeEvent event) {
		final BlockPos pos = event.getBlockPos();
		for (BlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
			if (ModUtil.shouldSmooth(event.getChunkCache().getBlockState(mutablePos))) {
				event.setResult(Event.Result.ALLOW);
				event.setCanceled(true);
				break;
			}
		}
	}

	private static final MethodHandle compiledChunk_setLayerUsed;
	static {
		try {
			// newer forge versions
//			compiledChunk_setLayerUsed = MethodHandles.publicLookup().unreflect(ObfuscationReflectionHelper.findMethod(CompiledChunk.class, "func_178486_a", Void.class, BlockRenderLayer.class));
			compiledChunk_setLayerUsed = MethodHandles.publicLookup().unreflect(ReflectionHelper.findMethod(CompiledChunk.class, "setLayerUsed", "func_178486_a", BlockRenderLayer.class));
		} catch (IllegalAccessException e) {
			CrashReport crashReport = new CrashReport("Error getting method handle for CompiledChunk#setLayerUsed!", e);
			crashReport.makeCategory("Reflectively Accessing CompiledChunk#setLayerUsed");
			throw new ReportedException(crashReport);
		}
	}

	public static void compiledChunk_setLayerUsed(final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer) {
		try {
			compiledChunk_setLayerUsed.invokeExact(compiledChunk, blockRenderLayer);
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error invoking method handle for CompiledChunk#setLayerUsed!", throwable);
			crashReport.makeCategory("Reflectively Accessing CompiledChunk#setLayerUsed");
			throw new ReportedException(crashReport);
		}
	}

//	static final ThreadLocal<HashMap<BlockPos, Object[]>> RENDER_LIQUID_POSITIONS = new ThreadLocal<HashMap<BlockPos, Object[]>>() {
//		@Override
//		protected HashMap<BlockPos, Object[]> initialValue() {
//			return new HashMap<>();
//		}
//	};

	public static void extendLiquids(final RebuildChunkPreEvent event) {
		final BlockPos renderChunkPosition = event.getRenderChunkPosition();
		final ChunkCache cache = event.getChunkCache();

		// 18 * 18 * 18 add 1 block on each side of chunk
		final boolean[] isLiquid = new boolean[5832];

		for (MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(renderChunkPosition.add(-1, -1, -1), renderChunkPosition.add(16, 16, 16))) {
			final BlockPos sub = mutableBlockPos.subtract(renderChunkPosition);
			final int x = sub.getX() + 1;
			final int y = sub.getY() + 1;
			final int z = sub.getZ() + 1;
			// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
			isLiquid[x + 18 * (y + 18 * z)] = cache.getBlockState(mutableBlockPos).getBlock() instanceof BlockLiquid && !(cache.getBlockState(mutableBlockPos.up()).getBlock() instanceof BlockLiquid);
		}

		for (MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(renderChunkPosition, renderChunkPosition.add(15, 15, 15))) {
			IF:
			if (ModUtil.shouldSmooth(cache.getBlockState(mutableBlockPos))) {
				final BlockPos sub = mutableBlockPos.subtract(renderChunkPosition);
				final int x = sub.getX() + 1;
				final int y = sub.getY() + 1;
				final int z = sub.getZ() + 1;
				for (int xOff = -1; xOff <= 1; xOff++) {
					for (int zOff = -1; zOff <= 1; zOff++) {
						if (isLiquid[(x + xOff) + 18 * (y + 18 * (z + zOff))]) {

							final BlockPos potentialLiquidPos = mutableBlockPos.add(xOff, 0, zOff);
							final IBlockState liquidState = cache.getBlockState(potentialLiquidPos);

							// if source block
							if (liquidState.getValue(BlockLiquid.LEVEL) == 0)
								renderLiquidInPre(event, potentialLiquidPos, mutableBlockPos, cache, liquidState);
//								RENDER_LIQUID_POSITIONS.get().put(mutableBlockPos.toImmutable(), new Object[]{potentialLiquidPos.toImmutable(), liquidState});

							break IF;
						}
					}
				}
			}
		}

	}

//	public static void renderLiquidInBlock(final RebuildChunkBlockEvent event) {
//
//		final Object[] data = RENDER_LIQUID_POSITIONS.get().get(event.getBlockPos());
//		if (data == null) return;
//
//		final BlockPos liquidPos = (BlockPos) data[0];
//		final IBlockState liquidState = (IBlockState) data[1];
//
//		final BlockPos pos = event.getBlockPos();
//		final ChunkCache cache = event.getChunkCache();
//	    final RenderChunk renderChunk = event.getRenderChunk();
//
//
//
//		final BlockRenderLayer blockRenderLayer = BlockRenderLayer.TRANSLUCENT;//liquidState.getBlock().getRenderLayer();
//		final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
//
//		if(true) {
//			event.getBlockRendererDispatcher().renderBlock(liquidState, pos, cache, bufferBuilder);
//			return;
//		}
//
//		//		final BufferBuilder bufferBuilder = event.getBufferBuilder();
////		final CompiledChunk compiledChunk = event.getCompiledChunk();
////		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
////
////		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
////			compiledChunk.setLayerStarted(blockRenderLayer);
////			compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
////            ClientUtil.renderChunk_preRenderBlocks(renderChunk, bufferBuilder, pos);
////		}
//
//		final TextureAtlasSprite textureAtlasSprite = liquidState.getMaterial() == Material.LAVA ? Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/lava_still") : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/water_still");
//
//		float minU = getMinU(textureAtlasSprite);
//		float minV = getMinV(textureAtlasSprite);
//		float maxV = getMaxV(textureAtlasSprite);
//		float maxU = getMaxU(textureAtlasSprite);
//
//		int packedLightmapCoords = liquidState.getPackedLightmapCoords(cache, liquidPos);
//		int skyLight = packedLightmapCoords >> 16 & 65535;
//		int blockLight = packedLightmapCoords & 65535;
//
//		int x = pos.getX();
//		//14/16
//		float y = pos.getY() + 0.888F;
//		int z = pos.getZ();
//
//		bufferBuilder.pos(x + 0, y, z + 0).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
//		bufferBuilder.pos(x + 0, y, z + 1).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) minU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
//		bufferBuilder.pos(x + 1, y, z + 1).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) maxU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
//		bufferBuilder.pos(x + 1, y, z + 0).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) maxU, (double) minV).lightmap(skyLight, blockLight).endVertex();
//
////		if (blockliquid.shouldRenderSides(blockAccess, blockPosIn.up())) {
//		if (((BlockLiquid) liquidState.getBlock()).shouldRenderSides(cache, liquidPos.up())) {
//			bufferBuilder.pos(x + 0, y, z + 0).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
//			bufferBuilder.pos(x + 1, y, z + 0).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) maxU, (double) minV).lightmap(skyLight, blockLight).endVertex();
//			bufferBuilder.pos(x + 1, y, z + 1).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) maxU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
//			bufferBuilder.pos(x + 0, y, z + 1).color(0xFF, 0xFF, 0xFF, 0xFF).tex((double) minU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
//		}
////		}
//
//	}

//	public static void extendLiquidInPost(final RebuildChunkPostEvent event) {
//		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
//
//		for (MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(renderChunkPos, renderChunkPos.add(15, 15, 15)))
//			RENDER_LIQUID_POSITIONS.get().remove(mutablePos);
//
//	}

	//TODO: I _could_ use thread local & render the liquids in the block event

	//	@Deprecated
	private static void renderLiquidInPre(final RebuildChunkPreEvent event, final BlockPos liquidPos, final BlockPos pos, final IBlockAccess world, final IBlockState liquidState) {

//		final BlockRenderLayer blockRenderLayer = liquidState.getBlock().getRenderLayer();
		final BlockRenderLayer blockRenderLayer = BlockRenderLayer.TRANSLUCENT;
		final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
		final CompiledChunk compiledChunk = event.getCompiledChunk();
		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final RenderChunk renderChunk = event.getRenderChunk();

		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
			compiledChunk.setLayerStarted(blockRenderLayer);
			compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
			ClientUtil.renderChunk_preRenderBlocks(renderChunk, bufferBuilder, pos);
		}

		OptifineCompatibility.pushShaderThing(liquidState, liquidPos, world, bufferBuilder);

		FluidInBlockRenderer.renderLiquidInBlock(liquidState, liquidPos, pos, world, bufferBuilder);

		OptifineCompatibility.popShaderThing(bufferBuilder);

	}

	public static BlockRenderData getBlockRenderData(final BlockPos pos, final ChunkCache cache) {

		final IBlockState state = cache.getBlockState(pos);
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		BlockPos texturePos = pos;
		IBlockState textureState = state;

		IF:
		if (ModConfig.shouldBeautifyTextures) {

			for (final BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
				final IBlockState tempState = cache.getBlockState(mutablePos);
				if (tempState.getBlock() == Blocks.SNOW_LAYER) {
					textureState = tempState;
					texturePos = mutablePos;
					break IF;
				} else if (ModUtil.shouldSmooth(tempState)) {
					textureState = tempState;
					texturePos = mutablePos;
				}
			}

			for (final BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
				final IBlockState tempState = cache.getBlockState(mutablePos);
				if (tempState.getBlock() == Blocks.GRASS) {
					textureState = tempState;
					texturePos = mutablePos;
					break IF;
				} else if (ModUtil.shouldSmooth(tempState)) {
					textureState = tempState;
					texturePos = mutablePos;
				}
			}

		} else {
			// get texture
			for (final BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
				if (ModUtil.shouldSmooth(textureState)) {
					break;
				} else {
					textureState = cache.getBlockState(mutablePos);
					texturePos = mutablePos;
				}
			}
		}

		BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
		if (quad == null) {
			quad = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, null, 0L).get(0);
		}
		final TextureAtlasSprite sprite = quad.getSprite();
		final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
		final int red = (color >> 16) & 255;
		final int green = (color >> 8) & 255;
		final int blue = color & 255;
		final int alpha = 0xFF;

		final float minU = ClientUtil.getMinU(sprite);
		final float minV = ClientUtil.getMinV(sprite);
		final float maxU = ClientUtil.getMaxU(sprite);
		final float maxV = ClientUtil.getMaxV(sprite);

		//real pos not texture pos
		final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);
		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		final BlockRenderLayer blockRenderLayer = state.getBlock().getRenderLayer();

		return new BlockRenderData(blockRenderLayer, red, green, blue, alpha, minU, maxU, minV, maxV, lightmapSkyLight, lightmapBlockLight);

	}

	private static final MethodHandle renderChunk_preRenderBlocks;
	static {
		try {
			// newer forge versions
//			renderChunk_preRenderBlocks = MethodHandles.publicLookup().unreflect(ObfuscationReflectionHelper.findMethod(RenderChunk.class, "func_178573_a", Void.class, BlockRenderLayer.class));
			renderChunk_preRenderBlocks = MethodHandles.publicLookup().unreflect(ReflectionHelper.findMethod(RenderChunk.class, "preRenderBlocks", "func_178573_a", BufferBuilder.class, BlockPos.class));
		} catch (IllegalAccessException e) {
			CrashReport crashReport = new CrashReport("Error getting method handle for RenderChunk#preRenderBlocks!", e);
			crashReport.makeCategory("Reflectively Accessing RenderChunk#preRenderBlocks");
			throw new ReportedException(crashReport);
		}
	}

	public static void renderChunk_preRenderBlocks(final RenderChunk renderChunk, final BufferBuilder bufferBuilder, final BlockPos pos) {
		try {
			renderChunk_preRenderBlocks.invokeExact(renderChunk, bufferBuilder, pos);
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error invoking method handle for RenderChunk#preRenderBlocks!", throwable);
			crashReport.makeCategory("Reflectively Accessing RenderChunk#preRenderBlocks");
			throw new ReportedException(crashReport);
		}
	}

}
