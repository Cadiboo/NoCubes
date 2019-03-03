package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.tempcompatibility.ReposeCompatibility;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"weakerAccess" // Hooks need to be public to be invoked
})
public class Hooks {

	private static final boolean REPOSE_INSTALLED = false;

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (!NoCubes.isEnabled() || !ModConfig.collisionsEnabled) {
			addCollisionBoxToListDefault(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			return;
		}

		if (!TERRAIN_SMOOTHABLE.isSmoothable(state)) {
			if (LEAVES_SMOOTHABLE.isSmoothable(state)) {
				return;
			} else {
				addCollisionBoxToListDefault(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
				return;
			}
		}

		final float boxRadius = 0.2F;

		try (final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlock(pos, worldIn, TERRAIN_SMOOTHABLE)) {
			for (Face face : faces) {
				// Ew, Yay, Java 8 variable try-with-resources support
				try {
					final Vec3 v0 = face.getVertex0();
					final Vec3 v1 = face.getVertex1();
					final Vec3 v2 = face.getVertex2();
					final Vec3 v3 = face.getVertex3();

					final int posX = pos.getX();
					final int posY = pos.getY();
					final int posZ = pos.getZ();

					collidingBoxes.add(createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v0, boxRadius));
					collidingBoxes.add(createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v1, boxRadius));
					collidingBoxes.add(createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v2, boxRadius));
					collidingBoxes.add(createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v3, boxRadius));

					v0.close();
					v1.close();
					v2.close();
					v3.close();

				} finally {
					face.close();
				}
			}
		}
	}

	private static AxisAlignedBB createAndOffsetAxisAlignedBBForVertex(final int posX, final int posY, final int posZ, final Vec3 vec3, final float boxRadius) {
		return new AxisAlignedBB(
				posX + (vec3.x - boxRadius),
				posY + (vec3.y - boxRadius),
				posZ + (vec3.z - boxRadius),
				posX + (vec3.x + boxRadius),
				posY + (vec3.y + boxRadius),
				posZ + (vec3.z + boxRadius)
		);
	}

	private static void addCollisionBoxToListDefault(final Block block, final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final Entity entityIn, final boolean isActualState) {
		if (REPOSE_INSTALLED) {
			ReposeCompatibility.addCollisionBoxToList(state, worldIn, entityBox, collidingBoxes, entityIn, isActualState);
		} else {
			block.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

}
