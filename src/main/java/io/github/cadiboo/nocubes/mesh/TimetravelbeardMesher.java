package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.function.Predicate;

import static net.minecraft.core.BlockPos.MutableBlockPos;



//corner class for mesh generation
//optimize this into just a float or 8bit array with const char offsets
public final /* inline */ class Corner5 {

	public /* final */ float x;
	public /* final */ float y;
	public /* final */ float z;
    public /* final */ float unlocked;
    public float visible;
    //default constructor
	public Corner5() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.unlocked=1;
		this.visible=0;
	}

    //fill init
	public Corner5(float x, float y, float z,float u, float v) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.unlocked=u;
		this.visible=v;
	}
}
//to do: blank dummy for memcpy of chunk corner array



/**
 * @author Cadiboo
 * @author Click_Me
 */
public final class OldNoCubes extends SimpleMesher {

	// Points order
	public static final int X0Y0Z0 = 0;
	public static final int X1Y0Z0 = 1;
	public static final int X1Y0Z1 = 2;
	public static final int X0Y0Z1 = 3;
	public static final int X0Y1Z0 = 4;
	public static final int X1Y1Z0 = 5;
	public static final int X1Y1Z1 = 6;
	public static final int X0Y1Z1 = 7;

	private static void resetPoints(Vec[] points) {
		// The 8 points that make the block.
		// 1 point for each corner
		points[0].set(0, 0, 0);
		points[1].set(1, 0, 0);
		points[2].set(1, 0, 1);
		points[3].set(0, 0, 1);
		points[4].set(0, 1, 0);
		points[5].set(1, 1, 0);
		points[6].set(1, 1, 1);
		points[7].set(0, 1, 1);
	}

	@Override
	public BlockPos getPositiveAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public BlockPos getNegativeAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
		var vertexNormals = new Face();
		var faceNormal = new Vec();
		var centre = new Vec();
		generateInternal(
			area, isSmoothable,
			(x, y, z) -> ShapeConsumer.acceptFullCube(x, y, z, action),
			(pos, face) -> {
				face.assignAverageTo(centre);
				face.assignNormalTo(vertexNormals);
				vertexNormals.assignAverageTo(faceNormal);

				// Keeps flat surfaces collidable but also allows super rough terrain
				faceNormal.multiply(0.00001F);

				return CollisionHandler.generateShapes(centre, faceNormal, action, face);
			}
		);
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		generateInternal(area, isSmoothable, FullBlockAction.IGNORE, action);
	}

	private interface FullBlockAction {
		FullBlockAction IGNORE = (x, y, z) -> true;

		boolean apply(int x, int y, int z);
	}

	private void generateInternal(Area area, Predicate<BlockState> isSmoothable, FullBlockAction fullBlockAction, FaceAction faceAction) {
		//dummies
		var face = new Face();
		var pos = new MutableBlockPos();
		// The 8 points that make the block.
		// 1 point for each corner
		var points = new Vec[]{new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec()};
		
		var directions = ModUtil.DIRECTIONS;
		var directionsLength = directions.length;
		var neighboursSmoothness = new float[directionsLength];
		
		var roughness = NoCubesConfig.Server.oldNoCubesRoughness;
		
		
		//to do: cache block data to reduce calls if applicable
		//get dimensions 
		int chunk_w,chunk_h,chunk_d;//in blocks
		int chunk_w1,chunk_h1,chunk_d1;//+1 in each for vertexes not cubes
		int block_count=chunk_w1*chunk_w1*chunk_w1;
		//make chunk 
		//
		//var temp_blocks= new [block_count];//mem
		//get data
		//
		
		//generate vertex grid
		int vertex_count=chunk_w1*chunk_w1*chunk_w1;
		//make temp grid, should default values, optimize later with memcpy from blank helper
		var temp_net= new Vev[vertex_count];//mem
		//x is east, z is south, y is up
		//set cube offsets
	//	int layer_offset=chunk_h*chunk_w;
		int layer_offset1=chunk_h1*chunk_w1;
		
		//todo: make these class scope
		//orientation is northbottomwest to southtopeast
		float cube_corner_offsets[8]{0,1,chunk_w1,chunk_w1+1,layer_offset1,layer_offset1+1,layer_offset1+chunk_w1,
		layer_offset1+chunk_w1+1};
        float max_smoothing_amount=.3;
		float smoothing_amount =max_smoothing_amount *.25;//per 4 corners
		//for readability and to fit convention, respecting offset order
	public static final int x0y0z0 = 0;
	public static final int x1y0z0 = 1;
	public static final int x0y1z0 = 2;
	public static final int x1y1z0 = 3;
	public static final int x0y0z1 = 4;
	public static final int x1y0z1 = 5;
	public static final int x0y1z1 = 6;
	public static final int x1y1z1 = 7;
		
		//offset points
		for(int z=0;z<chunk_d;z++)
		{
	  	for(int y=0;y<chunk_h;y++)
	   	{
			for(int x=0;x<chunk_w;x++)
	    	{//per block
	    	
	    	//get point offset for block, not block offset
	    	int o=z*layer_offset1+y*chunk_w1+x;//o++ optimize?
	    	//deoending on minecraft.core.directions: maybe optimize
	    			for (var i = 0; i < directionsLength; ++i) {
	    			//per direction
				var direction = directions[i];
				switch (directions[i]) {
					
				  case DOWN:
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];//cut
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				//smooth
				if(density<1)//neighbor smoothness is smoothable OR AIR / WATER
				{
					//smooth corners 
					//x is east, z is south, y is up
			  	temp_net[o+cube_corner_offsets[x0y0z0]].y+=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x1y0z0]].y+=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x0y0z1]].y+=smoothing_amount;
					temp_net[o+cube_corner_offsets[x1y0z1]].y+=smoothing_amount;
				}else{
					//lock corners
					for(int co=0;co<8;co++)//**THIS SHOULD ONLY DO THE RELEVABT CORNERS LISTED ABOVE INSTEAD OF ALL 8
					{//forveach corner
					temp_net[o+cube_corner_offsets[co]].unlocked=0;
					}
				}
				break;
				
				case UP:
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];//cut
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				//smooth
				if(density<1)//neighbor smoothness is smoothable OR AIR / WATER
				{
					//smooth corners 
					//x is east, z is south, y is up
			  	temp_net[o+cube_corner_offsets[x0y1z0]].y-=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x1y1z0]].y-=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x0y1z1]].y-=smoothing_amount;
					temp_net[o+cube_corner_offsets[x1y1z1]].y-=smoothing_amount;
				}else{
					//lock corners
					for(int co=0;co<8;co++)
					{//forveach corner
					temp_net[o+cube_corner_offsets[co]].unlocked=0;
					}
				}
				break;
				
				case NORTH:
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];//cut
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				//smooth
				if(density<1)//neighbor smoothness is smoothable OR AIR / WATER
				{
					//smooth corners 
					//x is east, z is south, y is up
			  	temp_net[o+cube_corner_offsets[x0y0z0]].z+=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x1y0z0]].z+=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x0y1z0]].z+=smoothing_amount;
					temp_net[o+cube_corner_offsets[x1y1z0]].z+=smoothing_amount;
				}else{
					//lock corners
					for(int co=0;co<8;co++)
					{//forveach corner
					temp_net[o+cube_corner_offsets[co]].unlocked=0;
					}
				}
				break;
				
				case SOUTH:
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];//cut
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				//smooth
				if(density<1)//neighbor smoothness is smoothable OR AIR / WATER
				{
					//smooth corners 
					//x is east, z is south, y is up
			  	temp_net[o+cube_corner_offsets[x0y0z1]].z-=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x1y0z1]].z-=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x0y1z1]].z-=smoothing_amount;
					temp_net[o+cube_corner_offsets[x1y1z1]].z-=smoothing_amount;
				}else{
					//lock corners
					for(int co=0;co<8;co++)
					{//forveach corner
					temp_net[o+cube_corner_offsets[co]].unlocked=0;
					}
				}
				break;
				
				case WEST:
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];//cut
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				//smooth 
				if(density<1)//neighbor smoothness is smoothable OR AIR / WATER
				{
					//smooth corners 
					//x is east, z is south, y is up
			  	temp_net[o+cube_corner_offsets[x0y0z0]].x+=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x0y1z0]].x+=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x0y0z1]].x+=smoothing_amount;
					temp_net[o+cube_corner_offsets[x0y1z1]].x+=smoothing_amount;
				}else{
					//lock corners
					for(int co=0;co<8;co++)
					{//forveach corner
					temp_net[o+cube_corner_offsets[co]].unlocked=0;
					}
				}
				break;
				
				case EAST:
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];//cut
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				//smooth
				if(density<1)//neighbor smoothness is smoothable OR AIR / WATER
				{
					//smooth corners 
					//x is east, z is south, y is up
			  	temp_net[o+cube_corner_offsets[x1y0z0]].x-=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x1y1z0]].x-=smoothing_amount;
			  	temp_net[o+cube_corner_offsets[x1y0z1]].x-=smoothing_amount;
					temp_net[o+cube_corner_offsets[x1y1z1]].x-=smoothing_amount;
				}else{
					//lock corners
					for(int co=0;co<8;co++)
					{//forveach corner
					temp_net[o+cube_corner_offsets[co]].unlocked=0;
					}
				}
				break;
				
	    			}//switch direction
	    	}//x
	   	}//y
		}//z
		
		int o=-1;
		//lock locked corners, set smoothing offsets into chunk space
		for(int z=0;z<chunk_d1;z++)
		{
	  	for(int y=0;y<chunk_h1;y++)
	   	{
			for(int x=0;x<chunk_w1;x++)
	    	{//per block
	    	//get offset
	    	  o++;
	    	  //lock vertexes
	    	  temp_net[o].x=x+temp_net[o].x*temp_net[o].unlocked;
	    	  temp_net[o].y=y+temp_net[o].y*temp_net[o].unlocked;
	    	  temp_net[o].z=z+temp_net[o].z*temp_net[o].unlocked;
	    	}}}//xyz
		
		
		
		
		
		generate(area, isSmoothable, (x, y, z, index) -> {//loop through each block
			
			
			//get point offset for block, not block offset
	    	int o=z*layer_offset1+y*chunk_w1+x;//o++ optimize?
	    	
	    	
	    	//cut this to optimize
			var blocks = area.getAndCacheBlocks();
			var state = blocks[index];

			var combinedNeighboursSmoothness = 0F;
			for (var i = 0; i < directionsLength; ++i) {
				var direction = directions[i];
				pos.set(x, y, z).move(direction);
				var neighbour = blocks[area.index(pos)];
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				combinedNeighboursSmoothness += density;
				neighboursSmoothness[i] = density;
			}

			var amountInsideIsosurface = (combinedNeighboursSmoothness / directionsLength) / 2 + 0.5F;
			if (amountInsideIsosurface == 1 && !fullBlockAction.apply(x, y, z))
				return false;
			if (amountInsideIsosurface == 0 || ModUtil.isSnowLayer(state))
				return true;




			resetPoints(points);
			// Loop through all the points:
			// Here everything will be 'smoothed'.
			for (byte pointIndex = 0; pointIndex < 8; ++pointIndex) {
				var point = points[pointIndex];

				// Give the point the block's coordinates.
				point.x += x;
				point.y += y;
				point.z += z;

				if (!doesPointIntersectWithManufactured(area, point, isSmoothable, pos)) {
					if (NoCubesConfig.Server.oldNoCubesSlopes) {
						if (pointIndex < 4 && doesPointBottomIntersectWithAir(area, point, pos))
							point.y = y + 1.0F - 0.0001F; // - 0.0001F to prevent z-fighting
						else if (pointIndex >= 4 && doesPointTopIntersectWithAir(area, point, pos))
							point.y = y + 0.0F + 0.0001F; // + 0.0001F to prevent z-fighting
					}
					givePointRoughness(roughness, area, point);
				}
			}

			for (int i = 0; i < directionsLength; ++i) {
				if (neighboursSmoothness[i] == ModUtil.FULLY_SMOOTHABLE)
					continue;
				//0-3
				//1-2
				//0,0-1,0
				//0,1-1,1
				int block_corner_offset;
				if (!faceAction.apply(pos.set(x, y, z), switch (directions[i]) {
					case DOWN -> face.set(
					temp_net[o+cube_corner_offsets[x1y0z1]],
						//points[X1Y0Z1],
						temp_net[o+cube_corner_offsets[x0y0z1]],
						//points[X0Y0Z1],
						temp_net[o+cube_corner_offsets[x0y0z0]],
						//points[X0Y0Z0],
						temp_net[o+cube_corner_offsets[x1y0z0]],
					//points[X1Y0Z0]
					);
					case UP -> face.set(
					temp_net[o+cube_corner_offsets[x1y1z1]],
						//points[X1Y1Z1],
						temp_net[o+cube_corner_offsets[x1y1z0]],
						temp_net[o+cube_corner_offsets[x1y1z0]],
						//points[X1Y1Z0],
						temp_net[o+cube_corner_offsets[x0y1z0]],
					//	points[X0Y1Z0],
					temp_net[o+cube_corner_offsets[x1y0z1]],
						//points[X1Y0Z1]
						);
					case NORTH -> face.set(
						points[X1Y1Z0],
						points[X1Y0Z0],
						points[X0Y0Z0],
						points[X0Y1Z0]);
					case SOUTH -> face.set(
						points[X1Y1Z1],
						points[X0Y1Z1],
						points[X0Y0Z1],
						points[X1Y0Z1]);
					case WEST -> face.set(
						points[X0Y1Z1],
						points[X0Y1Z0],
						points[X0Y0Z0],
						points[X0Y0Z1]);
					case EAST -> face.set(
						points[X1Y1Z1],
						points[X1Y0Z1],
						points[X1Y0Z0],
						points[X1Y1Z0]);
				}))//direction switch, apply, if apply
					return false;
			}//per direction
			return true;
		});//generate, loop through blocks
	}

	private static float max(float a, float b, float c, float d, float e, float f, float g, float h) {
		float max = a;
		if (b > max)
			max = b;
		if (c > max)
			max = c;
		if (d > max)
			max = d;
		if (e > max)
			max = e;
		if (f > max)
			max = f;
		if (g > max)
			max = g;
		if (h > max)
			max = h;
		return max;
	}

	private static float min(float a, float b, float c, float d, float e, float f, float g, float h) {
		float min = a;
		if (b < min)
			min = b;
		if (c < min)
			min = c;
		if (d < min)
			min = d;
		if (e < min)
			min = e;
		if (f < min)
			min = f;
		if (g < min)
			min = g;
		if (h < min)
			min = h;
		return min;
	}

	public static void givePointRoughness(float roughness, Area area, Vec point) {
		double worldX = area.start.getX() + point.x;
		double worldY = area.start.getY() + point.y;
		double worldZ = area.start.getZ() + point.z;
		long i = (long) (worldX * 3129871d) ^ (long) worldY * 116129781L ^ (long) worldZ;

		i = i * i * 42317861L + i * 11L;
		point.x += ((float) (i >> 16 & 0xF) / 15.0F - 0.5F) * roughness;
		point.y += ((float) (i >> 20 & 0xF) / 15.0F - 0.5F) * roughness;
		point.z += ((float) (i >> 24 & 0xF) / 15.0F - 0.5F) * roughness;
	}

	public static boolean isBlockAirPlantOrSnowLayer(BlockState state) {
		return state.getMaterial() == Material.AIR || ModUtil.isPlant(state) || ModUtil.isSnowLayer(state);
	}

	public static boolean doesPointTopIntersectWithAir(Area area, Vec point, MutableBlockPos pos) {
		boolean intersects = false;
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			if (!isBlockAirPlantOrSnowLayer(area.getBlockState(pos.set(x, y, z))))
				return false;
			if (isBlockAirPlantOrSnowLayer(area.getBlockState(pos.set(x, y - 1, z))))
				intersects = true;
		}
		return intersects;
	}

	public static boolean doesPointBottomIntersectWithAir(Area area, Vec point, MutableBlockPos pos) {
		boolean intersects = false;
		boolean notOnly = false;
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			if (!isBlockAirPlantOrSnowLayer(area.getBlockState(pos.set(x, y - 1, z))))
				return false;
			if (!isBlockAirPlantOrSnowLayer(area.getBlockState(pos.set(x, y + 1, z))))
				notOnly = true;
			if (isBlockAirPlantOrSnowLayer(area.getBlockState(pos.set(x, y, z))))
				intersects = true;
		}
		return intersects && notOnly;
	}

	public static boolean doesPointIntersectWithManufactured(Area area, Vec point, Predicate<BlockState> isSmoothable, MutableBlockPos pos) {
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			BlockState state0 = area.getBlockState(pos.set(x, y, z));
			if (!isBlockAirPlantOrSnowLayer(state0) && !isSmoothable.test(state0))
				return true;
			BlockState state1 = area.getBlockState(pos.set(x, y - 1, z));
			if (!isBlockAirPlantOrSnowLayer(state1) && !isSmoothable.test(state1))
				return true;
		}
		return false;
	}

}
