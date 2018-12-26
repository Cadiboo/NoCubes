package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 * @see "https://mikolalysenko.github.io/Isosurface/js/surfacenets.js"
 */
public final class SurfaceNets {

	private static final int[] CUBE_EDGES = new int[24];
	private static final int[] EDGE_TABLE = new int[256];

	// because the tables are so big we compute them in a static {} instead of hardcoding them
	static {
		generateCubeEdgesTable();
		generateIntersectionTable();
	}

	/**
	 * Utility function to build a table of possible edges for a cube with each
	 * pair of points representing one edge i.e. [0,1,0,2,0,4,...] would be the
	 * edges from points 0 to 1, 0 to 2, and 0 to 4 respectively:
	 *
	 * <pre>
	 *  y         z
	 *  ^        /
	 *  |
	 *    6----7
	 *   /|   /|
	 *  4----5 |
	 *  | 2--|-3
	 *  |/   |/
	 *  0----1   --> x
	 * </pre>
	 */
	private static void generateCubeEdgesTable() {

		//Initialize the cube_edges table
		// This is just the vertex number (number of corners) of each cube
		int cubeEdgesIndex = 0;
		// 8 is the number of corners for a cube
		for (int cubeCornerIndex = 0; cubeCornerIndex < 8; ++cubeCornerIndex) {
			for (int em = 1; em <= 4; em <<= 1) {
				int j = cubeCornerIndex ^ em;
				if (cubeCornerIndex <= j) {
					SurfaceNets.CUBE_EDGES[cubeEdgesIndex++] = cubeCornerIndex;
					SurfaceNets.CUBE_EDGES[cubeEdgesIndex++] = j;
				}
			}
		}
	}

	/**
	 * Build an intersection table. This is a 2^(cube config) -> 2^(edge config) map
	 * There is only one entry for each possible cube configuration
	 * and the output is a 12-bit vector enumerating all edges
	 * crossing the 0-level
	 */
	private static void generateIntersectionTable() {

		// nope, I don't understand this either
		// yay, Lookup Tables...
		// Initialize the intersection table.
		// This is a 2^(cube configuration) ->  2^(edge configuration) map
		// There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
		for (int edgeTableIndex = 0; edgeTableIndex < 256; ++edgeTableIndex) {
			int em = 0;
			for (int cubeEdgesIndex = 0; cubeEdgesIndex < 24; cubeEdgesIndex += 2) {
				final boolean a = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex])) != 0;
				final boolean b = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex + 1])) != 0;
				em |= a != b ? 1 << (cubeEdgesIndex >> 1) : 0;
			}
			EDGE_TABLE[edgeTableIndex] = em;
		}

	}

	public static void renderPre(final RebuildChunkPreEvent event) {

//Internal buffer, this may get resized at run time
		var buffer = new Int32Array(4096);

		return function(data, dims) {

			var vertices = []
    , faces = []
    , n = 0
					, x = new Int32Array(3)
					, R = new Int32Array([1, (dims[0]+1), (dims[0]+1)*(dims[1]+1)])
    , grid = new Float32Array(8)
					, buf_no = 1;

			//Resize buffer if necessary
			if(R[2] * 2 > buffer.length) {
				buffer = new Int32Array(R[2] * 2);
			}

			//March over the voxel grid
			for(x[2]=0; x[2]<dims[2]-1; ++x[2], n+=dims[0], buf_no ^= 1, R[2]=-R[2]) {

				//m is the pointer into the buffer we are going to use.
				//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
				//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
				var m = 1 + (dims[0]+1) * (1 + buf_no * (dims[1]+1));

				for(x[1]=0; x[1]<dims[1]-1; ++x[1], ++n, m+=2)
					for(x[0]=0; x[0]<dims[0]-1; ++x[0], ++n, ++m) {

						//Read in 8 field values around this vertex and store them in an array
						//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
						var mask = 0, g = 0, idx = n;
						for(var k=0; k<2; ++k, idx += dims[0]*(dims[1]-2))
							for(var j=0; j<2; ++j, idx += dims[0]-2)
								for(var i=0; i<2; ++i, ++g, ++idx) {
									var p = data[idx];
									grid[g] = p;
									mask |= (p < 0) ? (1<<g) : 0;
								}

						//Check for early termination if cell does not intersect boundary
						if(mask === 0 || mask === 0xff) {
							continue;
						}

						//Sum up edge intersections
						var edge_mask = edge_table[mask]
        , v = [0.0,0.0,0.0]
        , e_count = 0;

						//For every edge of the cube...
						for(var i=0; i<12; ++i) {

							//Use edge mask to check if it is crossed
							if(!(edge_mask & (1<<i))) {
								continue;
							}

							//If it did, increment number of edge crossings
							++e_count;

							//Now find the point of intersection
							var e0 = cube_edges[ i<<1 ]       //Unpack vertices
									, e1 = cube_edges[(i<<1)+1]
									, g0 = grid[e0]                 //Unpack grid values
									, g1 = grid[e1]
									, t  = g0 - g1;                 //Compute point of intersection
							if(Math.abs(t) > 1e-6) {
								t = g0 / t;
							} else {
								continue;
							}

							//Interpolate vertices and add up intersections (this can be done without multiplying)
							for(var j=0, k=1; j<3; ++j, k<<=1) {
								var a = e0 & k
										, b = e1 & k;
								if(a !== b) {
									v[j] += a ? 1.0 - t : t;
								} else {
									v[j] += a ? 1.0 : 0;
								}
							}
						}

						//Now we just average the edge intersections and add them to coordinate
						var s = 1.0 / e_count;
						for(var i=0; i<3; ++i) {
							v[i] = x[i] + s * v[i];
						}

						//Add vertex to buffer, store pointer to vertex index in buffer
						buffer[m] = vertices.length;
						vertices.push(v);

						//Now we need to add faces together, to do this we just loop over 3 basis components
						for(var i=0; i<3; ++i) {
							//The first three entries of the edge_mask count the crossings along the edge
							if(!(edge_mask & (1<<i)) ) {
								continue;
							}

							// i = axes we are point along.  iu, iv = orthogonal axes
							var iu = (i+1)%3
									, iv = (i+2)%3;

							//If we are on a boundary, skip it
							if(x[iu] === 0 || x[iv] === 0) {
								continue;
							}

							//Otherwise, look up adjacent edges in buffer
							var du = R[iu]
									, dv = R[iv];

							//Remember to flip orientation depending on the sign of the corner.
							if(mask & 1) {
								faces.push([buffer[m], buffer[m-du], buffer[m-du-dv], buffer[m-dv]]);
							} else {
								faces.push([buffer[m], buffer[m-dv], buffer[m-du-dv], buffer[m-du]]);
							}
						}
					}
			}

			//All done!  Return the result
			return { vertices: vertices, faces: faces };

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

		ClientUtil.handleTransparentBlocksRenderType(event);

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		event.setCanceled(ModUtil.shouldSmooth(event.getBlockState()));

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

	@Nullable
	public static Vec3[] getPoints(final BlockPos blockPos, final World world) {
		// D:
		return null;
	}

}
