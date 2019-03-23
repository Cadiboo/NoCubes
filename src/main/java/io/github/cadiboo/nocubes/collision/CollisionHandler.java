package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class CollisionHandler {

	public static final ConcurrentHashMap<BlockPos, CollisionsCache> CACHE = new ConcurrentHashMap<>();

	public static boolean isEntityInsideOpaqueBlock(final Entity entityIn) {
		return false;
	}

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		if (!TERRAIN_SMOOTHABLE.isSmoothable(state)) {
			if (LEAVES_SMOOTHABLE.isSmoothable(state)) {
				if (entityIn != null) {
					// TODO
					entityIn.motionX *= 0.99;
//					entityIn.motionY *= 0.99;
					entityIn.motionZ *= 0.99;
				}
			} else {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
			return;
		}

		//NOOOOOO
//		final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlock(pos, worldIn, TERRAIN_SMOOTHABLE);
//		if (faces != null) {
//			if (faces.isEmpty()) {
//				return;
//			}
//			faces.forEach(face -> {
//				face.getVertex0().close();
//				face.getVertex1().close();
//				face.getVertex2().close();
//				face.getVertex3().close();
//				face.close();
//			});
//			faces.close();
//		}

//		StolenReposeCode.addCollisionBoxToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);

		addMeshCollisionBoxesToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	private static void addMeshCollisionBoxesToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		final float boxRadius = 0.03125F;

		final boolean ignoreIntersects = false;

		worldIn.profiler.startSection("addMeshCollisionBoxesToList");
		try {

			final AxisAlignedBB originalBox = state.getCollisionBoundingBox(worldIn, pos);
			final AxisAlignedBB originalBoxOffset = originalBox == null ? null : originalBox.offset(pos);

			CollisionsCache cached = CACHE.get(pos);
			if (cached != null) {
				for (final Face face : cached.faces) {
					final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
					addFaceBoxesToList(boxes, face, worldIn, entityIn, originalBoxOffset, boxRadius);
					for (final AxisAlignedBB box : boxes) {
						addCollisionBoxToList(collidingBoxes, entityBox, box, ignoreIntersects);
					}
				}
				return;
			}

			if (entityIn == null) {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
				return;
			}

			try (final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlockMeshOffset(pos, worldIn, TERRAIN_SMOOTHABLE, ModConfig.terrainMeshGenerator)) {

				final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();

				for (Face face : faces) {
					// Ew, Yay, Java 8 variable try-with-resources support
//					try {
						addFaceBoxesToList(boxes, face, worldIn, entityIn, originalBoxOffset, boxRadius);
//					} finally {
//						face.close();
//					}
				}
				CACHE.put(pos.toImmutable(), new CollisionsCache(faces));

				for (final AxisAlignedBB box : boxes) {
					addCollisionBoxToList(collidingBoxes, entityBox, box, ignoreIntersects);
				}
			}
		} finally {
			worldIn.profiler.endSection();
		}
//		if(collidingBoxes.isEmpty()) {
//			collidingBoxes.add(new AxisAlignedBB(0, -100, 0, 0, -101, 0));
//		}

	}

	private static void addFaceBoxesToList(final List<AxisAlignedBB> outBoxes, final Face face, final World worldIn, final Entity entityIn, final AxisAlignedBB originalBoxOffset, final float boxRadius) {

		worldIn.profiler.startSection("interpolate");
		//0___3
		//_____
		//_____
		//_____
		//1___2
		final Vec3 v0 = face.getVertex0();
		final Vec3 v1 = face.getVertex1();
		final Vec3 v2 = face.getVertex2();
		final Vec3 v3 = face.getVertex3();

		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final Vec3 v0v1 = interp(v0, v1, 0.5F);
		final Vec3 v1v2 = interp(v1, v2, 0.5F);
		final Vec3 v2v3 = interp(v2, v3, 0.5F);
		final Vec3 v3v0 = interp(v3, v0, 0.5F);

		//0x*x3
		//x___x
		//*___*
		//x___x
		//1x*x2
		final Vec3 v0v1v0 = interp(v0v1, v0, 0.5F);
		final Vec3 v0v1v1 = interp(v0v1, v1, 0.5F);
		final Vec3 v1v2v1 = interp(v1v2, v1, 0.5F);
		final Vec3 v1v2v2 = interp(v1v2, v2, 0.5F);
		final Vec3 v2v3v2 = interp(v2v3, v2, 0.5F);
		final Vec3 v2v3v3 = interp(v2v3, v3, 0.5F);
		final Vec3 v3v0v3 = interp(v3v0, v3, 0.5F);
		final Vec3 v3v0v0 = interp(v3v0, v0, 0.5F);

		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final Vec3 v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
		final Vec3 v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
		final Vec3 v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
		final Vec3 v3v0v0v1 = interp(v3v0, v0v1, 0.5F);

		//0x*x3
		//xabax
		//*b_b*
		//xabax
		//1x*x2
		final Vec3 v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
		final Vec3 v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
		final Vec3 v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
		final Vec3 v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);

		//0x*x3
		//xabax
		//*bcb*
		//xabax
		//1x*x2
		final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
		final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);
		worldIn.profiler.endSection();

		try {
			worldIn.profiler.startSection("createBoxes");
			//0___3
			//_____
			//_____
			//_____
			//1___2
			final AxisAlignedBB v0box = createAxisAlignedBBForVertex(v0, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1box = createAxisAlignedBBForVertex(v1, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v2box = createAxisAlignedBBForVertex(v2, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v3box = createAxisAlignedBBForVertex(v3, entityIn, boxRadius, originalBoxOffset);

			//0_*_3
			//_____
			//*___*
			//_____
			//1_*_2
			final AxisAlignedBB v0v1box = createAxisAlignedBBForVertex(v0v1, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1v2box = createAxisAlignedBBForVertex(v1v2, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v2v3box = createAxisAlignedBBForVertex(v2v3, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v3v0box = createAxisAlignedBBForVertex(v3v0, entityIn, boxRadius, originalBoxOffset);

			//0x*x3
			//x___x
			//*___*
			//x___x
			//1x*x2
			final AxisAlignedBB v0v1v0box = createAxisAlignedBBForVertex(v0v1v0, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v0v1v1box = createAxisAlignedBBForVertex(v0v1v1, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1v2v1box = createAxisAlignedBBForVertex(v1v2v1, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1v2v2box = createAxisAlignedBBForVertex(v1v2v2, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v2v3v2box = createAxisAlignedBBForVertex(v2v3v2, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v2v3v3box = createAxisAlignedBBForVertex(v2v3v3, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v3v0v3box = createAxisAlignedBBForVertex(v3v0v3, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v3v0v0box = createAxisAlignedBBForVertex(v3v0v0, entityIn, boxRadius, originalBoxOffset);

			//0x*x3
			//xa_ax
			//*___*
			//xa_ax
			//1x*x2
			final AxisAlignedBB v0v1v1v2box = createAxisAlignedBBForVertex(v0v1v1v2, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1v2v2v3box = createAxisAlignedBBForVertex(v1v2v2v3, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v2v3v3v0box = createAxisAlignedBBForVertex(v2v3v3v0, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v3v0v0v1box = createAxisAlignedBBForVertex(v3v0v0v1, entityIn, boxRadius, originalBoxOffset);

			//0x*x3
			//xabax
			//*b_b*
			//xabax
			//1x*x2
			final AxisAlignedBB v0v1v1v2v1v2v2v3box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1v2v2v3v2v3v3v0box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v2v3v3v0v3v0v0v1, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v3v0v0v1v0v1v1v2, entityIn, boxRadius, originalBoxOffset);

			//0x*x3
			//xabax
			//*bcb*
			//xabax
			//1x*x2
			final AxisAlignedBB v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, entityIn, boxRadius, originalBoxOffset);
			final AxisAlignedBB v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, entityIn, boxRadius, originalBoxOffset);
			worldIn.profiler.endSection();

			worldIn.profiler.startSection("addBoxes");
			try {

				//0___3
				//_____
				//_____
				//_____
				//1___2
				outBoxes.add(v0box);
				outBoxes.add(v1box);
				outBoxes.add(v2box);
				outBoxes.add(v3box);

				//0_*_3
				//_____
				//*___*
				//_____
				//1_*_2
				outBoxes.add(v0v1box);
				outBoxes.add(v1v2box);
				outBoxes.add(v2v3box);
				outBoxes.add(v3v0box);

				//0x*x3
				//x___x
				//*___*
				//x___x
				//1x*x2
				outBoxes.add(v0v1v0box);
				outBoxes.add(v0v1v1box);
				outBoxes.add(v1v2v1box);
				outBoxes.add(v1v2v2box);
				outBoxes.add(v2v3v2box);
				outBoxes.add(v2v3v3box);
				outBoxes.add(v3v0v3box);
				outBoxes.add(v3v0v0box);

				//0x*x3
				//xa_ax
				//*___*
				//xa_ax
				//1x*x2
				outBoxes.add(v0v1v1v2box);
				outBoxes.add(v1v2v2v3box);
				outBoxes.add(v2v3v3v0box);
				outBoxes.add(v3v0v0v1box);

				//0x*x3
				//xabax
				//*b_b*
				//xabax
				//1x*x2
				outBoxes.add(v0v1v1v2v1v2v2v3box);
				outBoxes.add(v1v2v2v3v2v3v3v0box);
				outBoxes.add(v2v3v3v0v3v0v0v1box);
				outBoxes.add(v3v0v0v1v0v1v1v2box);

				//0x*x3
				//xabax
				//*bcb*
				//xabax
				//1x*x2
				outBoxes.add(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box);
				outBoxes.add(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box);

			} finally {
				worldIn.profiler.endSection();
			}
		} finally {

//			//0___3
//			//_____
//			//_____
//			//_____
//			//1___2
//			v0.close();
//			v1.close();
//			v2.close();
//			v3.close();
//
//			//0_*_3
//			//_____
//			//*___*
//			//_____
//			//1_*_2
//			v0v1.close();
//			v1v2.close();
//			v2v3.close();
//			v3v0.close();
//
//			//0x*x3
//			//x___x
//			//*___*
//			//x___x
//			//1x*x2
//			v0v1v0.close();
//			v0v1v1.close();
//			v1v2v1.close();
//			v1v2v2.close();
//			v2v3v2.close();
//			v2v3v3.close();
//			v3v0v3.close();
//			v3v0v0.close();
//
//			//0x*x3
//			//xa_ax
//			//*___*
//			//xa_ax
//			//1x*x2
//			v0v1v1v2.close();
//			v1v2v2v3.close();
//			v2v3v3v0.close();
//			v3v0v0v1.close();
//
//			//0x*x3
//			//xabax
//			//*b_b*
//			//xabax
//			//1x*x2
//			v0v1v1v2v1v2v2v3.close();
//			v1v2v2v3v2v3v3v0.close();
//			v2v3v3v0v3v0v0v1.close();
//			v3v0v0v1v0v1v1v2.close();
//
//			//0x*x3
//			//xabax
//			//*bcb*
//			//xabax
//			//1x*x2
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1.close();
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2.close();
		}

	}

	private static void addCollisionBoxToList(final List<AxisAlignedBB> collidingBoxes, final AxisAlignedBB entityBox, final AxisAlignedBB box, final boolean ignoreIntersects) {
		if (ignoreIntersects || entityBox.intersects(box)) {
			collidingBoxes.add(box);
		}
	}

	private static Vec3 interp(final Vec3 v0, final Vec3 v1, final float t) {
		return Vec3.retain(
				v0.x + t * (v1.x - v0.x),
				v0.y + t * (v1.y - v0.y),
				v0.z + t * (v1.z - v0.z)
		);

	}

	private static AxisAlignedBB createAxisAlignedBBForVertex(final Vec3 vec3, @Nullable final Entity entity, final float boxRadius, @Nullable final AxisAlignedBB originalBox) {
		if (entity == null) {
			return new AxisAlignedBB(
					vec3.x - boxRadius,
					vec3.y - boxRadius,
					vec3.z - boxRadius,
					vec3.x + boxRadius,
					vec3.y + boxRadius,
					vec3.z + boxRadius
			);
		}
		final boolean originalBoxMaxYGreaterThanVertex = originalBox != null && originalBox.maxY >= vec3.y;

		//min
		final double x1 = vec3.x - boxRadius;
		final double y1 = originalBoxMaxYGreaterThanVertex ? vec3.y - boxRadius - boxRadius : vec3.y - boxRadius;
		final double z1 = vec3.z - boxRadius;
		//max
		final double x2 = vec3.x + boxRadius;
		final double y2 = originalBoxMaxYGreaterThanVertex ? vec3.y : vec3.y + boxRadius;
		final double z2 = vec3.z + boxRadius;

		return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);

	}

	public static class CollisionsCache {

		public final FaceList faces;
		public int timeSinceLastUsed = 0;

		private CollisionsCache(final FaceList faces) {
			this.faces = faces;
		}

	}

}
