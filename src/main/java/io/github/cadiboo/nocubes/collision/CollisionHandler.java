package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import io.github.cadiboo.nocubes.hooks.GetCollisionBoundingBoxHook;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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

	public static AxisAlignedBB getCollisionBoundingBox(final Block block, final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
		if (ModConfig.terrainMeshGenerator == MeshGenerator.OldNoCubes) {
			if (block == Blocks.SNOW_LAYER) {
				return new AxisAlignedBB(0, 0, 0, 0, 0.2, 0);
			}
		}
		return GetCollisionBoundingBoxHook.getCollisionBoundingBoxDefault(state, worldIn, pos);
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

		if (entityIn == null) {
			if (ModConfig.collisionsForNullEntities) {
				addMeshCollisionBoxesToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			} else {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
			return;
		}

		if (shouldApplyCollisons(entityIn)) {
//		    StolenReposeCode.addCollisionBoxToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			addMeshCollisionBoxesToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		} else {
			AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

	private static boolean shouldApplyCollisons(final Entity entity) {
		return entity instanceof EntityPlayer || entity instanceof EntityItem;
	}

	private static void addMeshCollisionBoxesToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		worldIn.profiler.startSection("addMeshCollisionBoxesToList");

		final float boxRadius = 0.125F;

		final boolean ignoreIntersects = false;

		final ModProfiler profiler = NoCubes.getProfiler();

		final AxisAlignedBB originalBox = state.getCollisionBoundingBox(worldIn, pos);
		final AxisAlignedBB originalBoxOffset = originalBox == null ? null : originalBox.offset(pos);

		synchronized (CACHE) {
			CollisionsCache cached = CACHE.get(pos);
			if (cached != null) {
				cached.timeLastUsed = System.currentTimeMillis();
				final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
				for (final Face face : cached.faces) {
					addFaceBoxesToList(boxes, face, profiler, originalBoxOffset, boxRadius);
				}
				for (final AxisAlignedBB box : boxes) {
					addCollisionBoxToList(collidingBoxes, entityBox, box, ignoreIntersects);
				}
				worldIn.profiler.endSection();
				return;
			}
		}

		final FaceList faces;
		try (final ModProfiler ignored = profiler.start("generateBlockMeshOffset Collisions")) {
			faces = NoCubes.MESH_DISPATCHER.generateBlockMeshOffset(pos, worldIn, TERRAIN_SMOOTHABLE, ModConfig.terrainMeshGenerator);
		}

//		try
		{

			final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();

			try (final ModProfiler ignored = profiler.start("addFaceBoxesToList")) {
				// Ew, Yay, Java 8 variable try-with-resources support
				for (Face face : faces) {
//					try
					{
						addFaceBoxesToList(boxes, face, profiler, originalBoxOffset, boxRadius);
//					} finally {
//						face.close();
					}
				}
			}

			try (final ModProfiler ignored = profiler.start("addFacesToCache")) {
				synchronized (CACHE) {
					CACHE.put(pos.toImmutable(), new CollisionsCache(faces));
				}
			}

			try (final ModProfiler ignored = profiler.start("addCollisionBoxToList")) {
				for (final AxisAlignedBB box : boxes) {
					addCollisionBoxToList(collidingBoxes, entityBox, box, ignoreIntersects);
				}
			}
		}
//		finally {
//			faces.close();
//		}

		worldIn.profiler.endSection();

	}

	private static void addFaceBoxesToList(final List<AxisAlignedBB> outBoxes, final Face face, final ModProfiler profiler, final AxisAlignedBB originalBoxOffset, final float boxRadius) {

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final Vec3 v0;
		final Vec3 v1;
		final Vec3 v2;
		final Vec3 v3;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final Vec3 v0v1;
		final Vec3 v1v2;
		final Vec3 v2v3;
		final Vec3 v3v0;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final Vec3 v0v1v0;
//		final Vec3 v0v1v1;
//		final Vec3 v1v2v1;
//		final Vec3 v1v2v2;
//		final Vec3 v2v3v2;
//		final Vec3 v2v3v3;
//		final Vec3 v3v0v3;
//		final Vec3 v3v0v0;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final Vec3 v0v1v1v2;
		final Vec3 v1v2v2v3;
		final Vec3 v2v3v3v0;
		final Vec3 v3v0v0v1;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3;
//		final Vec3 v1v2v2v3v2v3v3v0;
//		final Vec3 v2v3v3v0v3v0v0v1;
//		final Vec3 v3v0v0v1v0v1v1v2;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1;
//		final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2;

		try (final ModProfiler ignored = profiler.start("interpolate")) {
			v0 = face.getVertex0();
			v1 = face.getVertex1();
			v2 = face.getVertex2();
			v3 = face.getVertex3();
			v0v1 = interp(v0, v1, 0.5F);
			v1v2 = interp(v1, v2, 0.5F);
			v2v3 = interp(v2, v3, 0.5F);
			v3v0 = interp(v3, v0, 0.5F);
//			v0v1v0 = interp(v0v1, v0, 0.5F);
//			v0v1v1 = interp(v0v1, v1, 0.5F);
//			v1v2v1 = interp(v1v2, v1, 0.5F);
//			v1v2v2 = interp(v1v2, v2, 0.5F);
//			v2v3v2 = interp(v2v3, v2, 0.5F);
//			v2v3v3 = interp(v2v3, v3, 0.5F);
//			v3v0v3 = interp(v3v0, v3, 0.5F);
//			v3v0v0 = interp(v3v0, v0, 0.5F);
			v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
			v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
			v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
			v3v0v0v1 = interp(v3v0, v0v1, 0.5F);
//			v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
//			v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
//			v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
//			v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);
		}

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final AxisAlignedBB v0box;
		final AxisAlignedBB v1box;
		final AxisAlignedBB v2box;
		final AxisAlignedBB v3box;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final AxisAlignedBB v0v1box;
		final AxisAlignedBB v1v2box;
		final AxisAlignedBB v2v3box;
		final AxisAlignedBB v3v0box;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final AxisAlignedBB v0v1v0box;
//		final AxisAlignedBB v0v1v1box;
//		final AxisAlignedBB v1v2v1box;
//		final AxisAlignedBB v1v2v2box;
//		final AxisAlignedBB v2v3v2box;
//		final AxisAlignedBB v2v3v3box;
//		final AxisAlignedBB v3v0v3box;
//		final AxisAlignedBB v3v0v0box;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final AxisAlignedBB v0v1v1v2box;
		final AxisAlignedBB v1v2v2v3box;
		final AxisAlignedBB v2v3v3v0box;
		final AxisAlignedBB v3v0v0v1box;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final AxisAlignedBB v0v1v1v2v1v2v2v3box;
//		final AxisAlignedBB v1v2v2v3v2v3v3v0box;
//		final AxisAlignedBB v2v3v3v0v3v0v0v1box;
//		final AxisAlignedBB v3v0v0v1v0v1v1v2box;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final AxisAlignedBB v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box;
//		final AxisAlignedBB v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box;

		try (final ModProfiler ignored = profiler.start("createBoxes")) {
			v0box = createAxisAlignedBBForVertex(v0, boxRadius, originalBoxOffset);
			v1box = createAxisAlignedBBForVertex(v1, boxRadius, originalBoxOffset);
			v2box = createAxisAlignedBBForVertex(v2, boxRadius, originalBoxOffset);
			v3box = createAxisAlignedBBForVertex(v3, boxRadius, originalBoxOffset);
			v0v1box = createAxisAlignedBBForVertex(v0v1, boxRadius, originalBoxOffset);
			v1v2box = createAxisAlignedBBForVertex(v1v2, boxRadius, originalBoxOffset);
			v2v3box = createAxisAlignedBBForVertex(v2v3, boxRadius, originalBoxOffset);
			v3v0box = createAxisAlignedBBForVertex(v3v0, boxRadius, originalBoxOffset);
//			v0v1v0box = createAxisAlignedBBForVertex(v0v1v0, boxRadius, originalBoxOffset);
//			v0v1v1box = createAxisAlignedBBForVertex(v0v1v1, boxRadius, originalBoxOffset);
//			v1v2v1box = createAxisAlignedBBForVertex(v1v2v1, boxRadius, originalBoxOffset);
//			v1v2v2box = createAxisAlignedBBForVertex(v1v2v2, boxRadius, originalBoxOffset);
//			v2v3v2box = createAxisAlignedBBForVertex(v2v3v2, boxRadius, originalBoxOffset);
//			v2v3v3box = createAxisAlignedBBForVertex(v2v3v3, boxRadius, originalBoxOffset);
//			v3v0v3box = createAxisAlignedBBForVertex(v3v0v3, boxRadius, originalBoxOffset);
//			v3v0v0box = createAxisAlignedBBForVertex(v3v0v0, boxRadius, originalBoxOffset);
			v0v1v1v2box = createAxisAlignedBBForVertex(v0v1v1v2, boxRadius, originalBoxOffset);
			v1v2v2v3box = createAxisAlignedBBForVertex(v1v2v2v3, boxRadius, originalBoxOffset);
			v2v3v3v0box = createAxisAlignedBBForVertex(v2v3v3v0, boxRadius, originalBoxOffset);
			v3v0v0v1box = createAxisAlignedBBForVertex(v3v0v0v1, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0, boxRadius, originalBoxOffset);
//			v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
		}

		try (final ModProfiler ignored = profiler.start("addBoxes")) {
			outBoxes.add(v0box);
			outBoxes.add(v1box);
			outBoxes.add(v2box);
			outBoxes.add(v3box);
			outBoxes.add(v0v1box);
			outBoxes.add(v1v2box);
			outBoxes.add(v2v3box);
			outBoxes.add(v3v0box);
//			outBoxes.add(v0v1v0box);
//			outBoxes.add(v0v1v1box);
//			outBoxes.add(v1v2v1box);
//			outBoxes.add(v1v2v2box);
//			outBoxes.add(v2v3v2box);
//			outBoxes.add(v2v3v3box);
//			outBoxes.add(v3v0v3box);
//			outBoxes.add(v3v0v0box);
			outBoxes.add(v0v1v1v2box);
			outBoxes.add(v1v2v2v3box);
			outBoxes.add(v2v3v3v0box);
			outBoxes.add(v3v0v0v1box);
//			outBoxes.add(v0v1v1v2v1v2v2v3box);
//			outBoxes.add(v1v2v2v3v2v3v3v0box);
//			outBoxes.add(v2v3v3v0v3v0v0v1box);
//			outBoxes.add(v3v0v0v1v0v1v1v2box);
//			outBoxes.add(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box);
//			outBoxes.add(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box);
		}

		//DO NOT CLOSE original face vectors
		{
//			v0.close();
//			v1.close();
//			v2.close();
//			v3.close();
		}
		v0v1.close();
		v1v2.close();
		v2v3.close();
		v3v0.close();
//		v0v1v0.close();
//		v0v1v1.close();
//		v1v2v1.close();
//		v1v2v2.close();
//		v2v3v2.close();
//		v2v3v3.close();
//		v3v0v3.close();
//		v3v0v0.close();
		v0v1v1v2.close();
		v1v2v2v3.close();
		v2v3v3v0.close();
		v3v0v0v1.close();
//		v0v1v1v2v1v2v2v3.close();
//		v1v2v2v3v2v3v3v0.close();
//		v2v3v3v0v3v0v0v1.close();
//		v3v0v0v1v0v1v1v2.close();
//		v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1.close();
//		v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2.close();

	}

	private static void addCollisionBoxToList(final List<AxisAlignedBB> collidingBoxes,
	                                          final AxisAlignedBB entityBox, final AxisAlignedBB box, final boolean ignoreIntersects) {
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

	private static AxisAlignedBB createAxisAlignedBBForVertex(final Vec3 vec3, final float boxRadius,
	                                                          @Nullable final AxisAlignedBB originalBox) {
//		if (entity == null) {
//			return new AxisAlignedBB(
//					vec3.x - boxRadius,
//					vec3.y - boxRadius,
//					vec3.z - boxRadius,
//					vec3.x + boxRadius,
//					vec3.y + boxRadius,
//					vec3.z + boxRadius
//			);
//		}
		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean originalBoxMaxYGreaterThanVertex = originalBox != null && originalBox.maxY >= vy;

		return new AxisAlignedBB(
				//min
				vx - boxRadius,
				originalBoxMaxYGreaterThanVertex ? vy - boxRadius - boxRadius : vy - boxRadius,
				vz - boxRadius,
				//max
				vx + boxRadius,
				originalBoxMaxYGreaterThanVertex ? vy : vy + boxRadius,
				vz + boxRadius
		);

	}

	public static class CollisionsCache {

		public final FaceList faces;
		public long timeLastUsed = System.currentTimeMillis();

		private CollisionsCache(final FaceList faces) {
			this.faces = faces;
		}

	}

}
