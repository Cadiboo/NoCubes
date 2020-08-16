package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static io.github.cadiboo.nocubes.client.render.OverlayRenderer.makeShape;

public final class TestBlocks {
	static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NoCubes.MOD_ID);

	public static final RegistryObject<Block> DEBUG = BLOCKS.register("debug", () -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.LIGHT_BLUE).hardnessAndResistance(0.3F)));
	public static final RegistryObject<Block> DEBUG2 = BLOCKS.register("debug2", () -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.LIGHT_BLUE).hardnessAndResistance(0.3F)) {
		@Override
		public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos blockPos) {
			if (!NoCubesConfig.Client.render)
				return super.getCollisionShape(state, reader, blockPos);
			final int x = blockPos.getX();
			final int y = blockPos.getY();
			final int z = blockPos.getZ();
			List<VoxelShape> shapes = new ArrayList<>();
			SurfaceNets.generate(
				x, y, z,
				1, 1, 1,
				reader, NoCubes.smoothableHandler::isSmoothable, null,
				(pos, face) -> {
					Vec v0 = face.v0;
					Vec v1 = face.v1;
					Vec v2 = face.v2;
					Vec v3 = face.v3;
					// Normals
					Vec n0 = Vec.normal(v3, v0, v1).multiply(-1);
					Vec n1 = Vec.normal(v0, v1, v2).multiply(-1);
					Vec n2 = Vec.normal(v1, v2, v3).multiply(-1);
					Vec n3 = Vec.normal(v2, v3, v0).multiply(-1);

					Vec centre = Vec.of(
						(v0.x + v1.x + v2.x + v3.x) / 4,
						(v0.y + v1.y + v2.y + v3.y) / 4,
						(v0.z + v1.z + v2.z + v3.z) / 4
					);

					final Vec nAverage = Vec.of(
						(n0.x + n2.x) / 2,
						(n0.y + n2.y) / 2,
						(n0.z + n2.z) / 2
					);
					nAverage.normalise().multiply(-0.125d);

					shapes.add(makeShape(x, y, z, centre, nAverage, v1));
					shapes.add(makeShape(x, y, z, centre, nAverage, v2));
					shapes.add(makeShape(x, y, z, centre, nAverage, v3));
					shapes.add(makeShape(x, y, z, centre, nAverage, centre));
					n0.close();
					n1.close();
					n2.close();
					n3.close();
					nAverage.close();
					centre.close();
					return true;
				}
			);
			return shapes
				.stream()
				.reduce((a, b) -> VoxelShapes.combine(a, b, IBooleanFunction.OR))
				.orElse(VoxelShapes.empty());
		}

		@Override
		public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
			if (!NoCubesConfig.Client.render)
				return super.getCollisionShape(state, reader, blockPos, context);
			final int x = blockPos.getX();
			final int y = blockPos.getY();
			final int z = blockPos.getZ();
			List<VoxelShape> shapes = new ArrayList<>();
			SurfaceNets.generate(
				x, y, z,
				1, 1, 1,
				reader, NoCubes.smoothableHandler::isSmoothable, null,
				(pos, face) -> {
					Vec v0 = face.v0;
					Vec v1 = face.v1;
					Vec v2 = face.v2;
					Vec v3 = face.v3;
					// Normals
					Vec n0 = Vec.normal(v3, v0, v1).multiply(-1);
					Vec n1 = Vec.normal(v0, v1, v2).multiply(-1);
					Vec n2 = Vec.normal(v1, v2, v3).multiply(-1);
					Vec n3 = Vec.normal(v2, v3, v0).multiply(-1);

					Vec centre = Vec.of(
						(v0.x + v1.x + v2.x + v3.x) / 4,
						(v0.y + v1.y + v2.y + v3.y) / 4,
						(v0.z + v1.z + v2.z + v3.z) / 4
					);

					final Vec nAverage = Vec.of(
						(n0.x + n2.x) / 2,
						(n0.y + n2.y) / 2,
						(n0.z + n2.z) / 2
					);
					nAverage.normalise().multiply(-0.125d);

					shapes.add(makeShape(0, 0, 0, centre, nAverage, v1));
					shapes.add(makeShape(0, 0, 0, centre, nAverage, v2));
					shapes.add(makeShape(0, 0, 0, centre, nAverage, v3));
					shapes.add(makeShape(0, 0, 0, centre, nAverage, centre));
					n0.close();
					n1.close();
					n2.close();
					n3.close();
					nAverage.close();
					centre.close();
					return true;
				}
			);
			return shapes
				.stream()
				.reduce((a, b) -> VoxelShapes.combine(a, b, IBooleanFunction.OR))
				.orElse(VoxelShapes.empty());
		}
	});

}