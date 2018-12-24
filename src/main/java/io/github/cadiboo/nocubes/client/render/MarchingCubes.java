package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;

import java.util.ArrayList;

/**
 * Implementation of the MarchingCubes algorithm in Minecraft
 *
 * @author Cadiboo
 */
public final class MarchingCubes {

	private static final int[] EDGE_TABLE = {
			0x0, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c,
			0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00,
			0x190, 0x99, 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c,
			0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90,
			0x230, 0x339, 0x33, 0x13a, 0x636, 0x73f, 0x435, 0x53c,
			0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30,
			0x3a0, 0x2a9, 0x1a3, 0xaa, 0x7a6, 0x6af, 0x5a5, 0x4ac,
			0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0,
			0x460, 0x569, 0x663, 0x76a, 0x66, 0x16f, 0x265, 0x36c,
			0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60,
			0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff, 0x3f5, 0x2fc,
			0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0,
			0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55, 0x15c,
			0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950,
			0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc,
			0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0,
			0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc,
			0xcc, 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0,
			0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c,
			0x15c, 0x55, 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650,
			0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc,
			0x2fc, 0x3f5, 0xff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
			0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c,
			0x36c, 0x265, 0x16f, 0x66, 0x76a, 0x663, 0x569, 0x460,
			0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac,
			0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa, 0x1a3, 0x2a9, 0x3a0,
			0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c,
			0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x33, 0x339, 0x230,
			0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c,
			0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99, 0x190,
			0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c,
			0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0
	};

	private static final int[][] TRIANGLE_TABLE = {
			{},
			{0, 8, 3},
			{0, 1, 9},
			{1, 8, 3, 9, 8, 1},
			{1, 2, 10},
			{0, 8, 3, 1, 2, 10},
			{9, 2, 10, 0, 2, 9},
			{2, 8, 3, 2, 10, 8, 10, 9, 8},
			{3, 11, 2},
			{0, 11, 2, 8, 11, 0},
			{1, 9, 0, 2, 3, 11},
			{1, 11, 2, 1, 9, 11, 9, 8, 11},
			{3, 10, 1, 11, 10, 3},
			{0, 10, 1, 0, 8, 10, 8, 11, 10},
			{3, 9, 0, 3, 11, 9, 11, 10, 9},
			{9, 8, 10, 10, 8, 11},
			{4, 7, 8},
			{4, 3, 0, 7, 3, 4},
			{0, 1, 9, 8, 4, 7},
			{4, 1, 9, 4, 7, 1, 7, 3, 1},
			{1, 2, 10, 8, 4, 7},
			{3, 4, 7, 3, 0, 4, 1, 2, 10},
			{9, 2, 10, 9, 0, 2, 8, 4, 7},
			{2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4},
			{8, 4, 7, 3, 11, 2},
			{11, 4, 7, 11, 2, 4, 2, 0, 4},
			{9, 0, 1, 8, 4, 7, 2, 3, 11},
			{4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1},
			{3, 10, 1, 3, 11, 10, 7, 8, 4},
			{1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4},
			{4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3},
			{4, 7, 11, 4, 11, 9, 9, 11, 10},
			{9, 5, 4},
			{9, 5, 4, 0, 8, 3},
			{0, 5, 4, 1, 5, 0},
			{8, 5, 4, 8, 3, 5, 3, 1, 5},
			{1, 2, 10, 9, 5, 4},
			{3, 0, 8, 1, 2, 10, 4, 9, 5},
			{5, 2, 10, 5, 4, 2, 4, 0, 2},
			{2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8},
			{9, 5, 4, 2, 3, 11},
			{0, 11, 2, 0, 8, 11, 4, 9, 5},
			{0, 5, 4, 0, 1, 5, 2, 3, 11},
			{2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5},
			{10, 3, 11, 10, 1, 3, 9, 5, 4},
			{4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10},
			{5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3},
			{5, 4, 8, 5, 8, 10, 10, 8, 11},
			{9, 7, 8, 5, 7, 9},
			{9, 3, 0, 9, 5, 3, 5, 7, 3},
			{0, 7, 8, 0, 1, 7, 1, 5, 7},
			{1, 5, 3, 3, 5, 7},
			{9, 7, 8, 9, 5, 7, 10, 1, 2},
			{10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3},
			{8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2},
			{2, 10, 5, 2, 5, 3, 3, 5, 7},
			{7, 9, 5, 7, 8, 9, 3, 11, 2},
			{9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11},
			{2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7},
			{11, 2, 1, 11, 1, 7, 7, 1, 5},
			{9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11},
			{5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0},
			{11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0},
			{11, 10, 5, 7, 11, 5},
			{10, 6, 5},
			{0, 8, 3, 5, 10, 6},
			{9, 0, 1, 5, 10, 6},
			{1, 8, 3, 1, 9, 8, 5, 10, 6},
			{1, 6, 5, 2, 6, 1},
			{1, 6, 5, 1, 2, 6, 3, 0, 8},
			{9, 6, 5, 9, 0, 6, 0, 2, 6},
			{5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8},
			{2, 3, 11, 10, 6, 5},
			{11, 0, 8, 11, 2, 0, 10, 6, 5},
			{0, 1, 9, 2, 3, 11, 5, 10, 6},
			{5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11},
			{6, 3, 11, 6, 5, 3, 5, 1, 3},
			{0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6},
			{3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9},
			{6, 5, 9, 6, 9, 11, 11, 9, 8},
			{5, 10, 6, 4, 7, 8},
			{4, 3, 0, 4, 7, 3, 6, 5, 10},
			{1, 9, 0, 5, 10, 6, 8, 4, 7},
			{10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4},
			{6, 1, 2, 6, 5, 1, 4, 7, 8},
			{1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7},
			{8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6},
			{7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9},
			{3, 11, 2, 7, 8, 4, 10, 6, 5},
			{5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11},
			{0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6},
			{9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6},
			{8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6},
			{5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11},
			{0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7},
			{6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9},
			{10, 4, 9, 6, 4, 10},
			{4, 10, 6, 4, 9, 10, 0, 8, 3},
			{10, 0, 1, 10, 6, 0, 6, 4, 0},
			{8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10},
			{1, 4, 9, 1, 2, 4, 2, 6, 4},
			{3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4},
			{0, 2, 4, 4, 2, 6},
			{8, 3, 2, 8, 2, 4, 4, 2, 6},
			{10, 4, 9, 10, 6, 4, 11, 2, 3},
			{0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6},
			{3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10},
			{6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1},
			{9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3},
			{8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1},
			{3, 11, 6, 3, 6, 0, 0, 6, 4},
			{6, 4, 8, 11, 6, 8},
			{7, 10, 6, 7, 8, 10, 8, 9, 10},
			{0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10},
			{10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0},
			{10, 6, 7, 10, 7, 1, 1, 7, 3},
			{1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7},
			{2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9},
			{7, 8, 0, 7, 0, 6, 6, 0, 2},
			{7, 3, 2, 6, 7, 2},
			{2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7},
			{2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7},
			{1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11},
			{11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1},
			{8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6},
			{0, 9, 1, 11, 6, 7},
			{7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0},
			{7, 11, 6},
			{7, 6, 11},
			{3, 0, 8, 11, 7, 6},
			{0, 1, 9, 11, 7, 6},
			{8, 1, 9, 8, 3, 1, 11, 7, 6},
			{10, 1, 2, 6, 11, 7},
			{1, 2, 10, 3, 0, 8, 6, 11, 7},
			{2, 9, 0, 2, 10, 9, 6, 11, 7},
			{6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8},
			{7, 2, 3, 6, 2, 7},
			{7, 0, 8, 7, 6, 0, 6, 2, 0},
			{2, 7, 6, 2, 3, 7, 0, 1, 9},
			{1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6},
			{10, 7, 6, 10, 1, 7, 1, 3, 7},
			{10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8},
			{0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7},
			{7, 6, 10, 7, 10, 8, 8, 10, 9},
			{6, 8, 4, 11, 8, 6},
			{3, 6, 11, 3, 0, 6, 0, 4, 6},
			{8, 6, 11, 8, 4, 6, 9, 0, 1},
			{9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6},
			{6, 8, 4, 6, 11, 8, 2, 10, 1},
			{1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6},
			{4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9},
			{10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3},
			{8, 2, 3, 8, 4, 2, 4, 6, 2},
			{0, 4, 2, 4, 6, 2},
			{1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8},
			{1, 9, 4, 1, 4, 2, 2, 4, 6},
			{8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1},
			{10, 1, 0, 10, 0, 6, 6, 0, 4},
			{4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3},
			{10, 9, 4, 6, 10, 4},
			{4, 9, 5, 7, 6, 11},
			{0, 8, 3, 4, 9, 5, 11, 7, 6},
			{5, 0, 1, 5, 4, 0, 7, 6, 11},
			{11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5},
			{9, 5, 4, 10, 1, 2, 7, 6, 11},
			{6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5},
			{7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2},
			{3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6},
			{7, 2, 3, 7, 6, 2, 5, 4, 9},
			{9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7},
			{3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0},
			{6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8},
			{9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7},
			{1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4},
			{4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10},
			{7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10},
			{6, 9, 5, 6, 11, 9, 11, 8, 9},
			{3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5},
			{0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11},
			{6, 11, 3, 6, 3, 5, 5, 3, 1},
			{1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6},
			{0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10},
			{11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5},
			{6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3},
			{5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2},
			{9, 5, 6, 9, 6, 0, 0, 6, 2},
			{1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8},
			{1, 5, 6, 2, 1, 6},
			{1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6},
			{10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0},
			{0, 3, 8, 5, 6, 10},
			{10, 5, 6},
			{11, 5, 10, 7, 5, 11},
			{11, 5, 10, 11, 7, 5, 8, 3, 0},
			{5, 11, 7, 5, 10, 11, 1, 9, 0},
			{10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1},
			{11, 1, 2, 11, 7, 1, 7, 5, 1},
			{0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11},
			{9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7},
			{7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2},
			{2, 5, 10, 2, 3, 5, 3, 7, 5},
			{8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5},
			{9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2},
			{9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2},
			{1, 3, 5, 3, 7, 5},
			{0, 8, 7, 0, 7, 1, 1, 7, 5},
			{9, 0, 3, 9, 3, 5, 5, 3, 7},
			{9, 8, 7, 5, 9, 7},
			{5, 8, 4, 5, 10, 8, 10, 11, 8},
			{5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0},
			{0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5},
			{10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4},
			{2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8},
			{0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11},
			{0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5},
			{9, 4, 5, 2, 11, 3},
			{2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4},
			{5, 10, 2, 5, 2, 4, 4, 2, 0},
			{3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9},
			{5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2},
			{8, 4, 5, 8, 5, 3, 3, 5, 1},
			{0, 4, 5, 1, 0, 5},
			{8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5},
			{9, 4, 5},
			{4, 11, 7, 4, 9, 11, 9, 10, 11},
			{0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11},
			{1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11},
			{3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4},
			{4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2},
			{9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3},
			{11, 7, 4, 11, 4, 2, 2, 4, 0},
			{11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4},
			{2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9},
			{9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7},
			{3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10},
			{1, 10, 2, 8, 7, 4},
			{4, 9, 1, 4, 1, 7, 7, 1, 3},
			{4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1},
			{4, 0, 3, 7, 4, 3},
			{4, 8, 7},
			{9, 10, 8, 10, 11, 8},
			{3, 0, 9, 3, 9, 11, 11, 9, 10},
			{0, 1, 10, 0, 10, 8, 8, 10, 11},
			{3, 1, 10, 11, 3, 10},
			{1, 2, 11, 1, 11, 9, 9, 11, 8},
			{3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9},
			{0, 2, 11, 8, 0, 11},
			{3, 2, 11},
			{2, 3, 8, 2, 8, 10, 10, 8, 9},
			{9, 10, 2, 0, 9, 2},
			{2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8},
			{1, 10, 2},
			{1, 3, 8, 9, 1, 8},
			{0, 9, 1},
			{0, 3, 8},
			{}
	};

	private static final Vec3[] CUBE_VERTICES = {
			new Vec3(0, 0, 0),
			new Vec3(1, 0, 0),
			new Vec3(1, 1, 0),
			new Vec3(0, 1, 0),
			new Vec3(0, 0, 1),
			new Vec3(1, 0, 1),
			new Vec3(1, 1, 1),
			new Vec3(0, 1, 1)
	};

	public static void renderPre(final RebuildChunkPreEvent event) {

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		final float isosurfaceLevel = ModConfig.getIsosurfaceLevel();
		final MutableBlockPos pos = event.getBlockPos();
		final ChunkCache cache = event.getChunkCache();
		final IBlockState state = event.getBlockState();
		final BlockRendererDispatcher blockRendererDispatcher = event.getBlockRendererDispatcher();

		MutableBlockPos texturePos = pos;
		IBlockState textureState = state;

		final Vec3[] points = new Vec3[]{
				new Vec3(0.0D, 0.0D, 1.0D),
				new Vec3(1.0D, 0.0D, 1.0D),
				new Vec3(1.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 1.0D, 1.0D),
				new Vec3(1.0D, 1.0D, 1.0D),
				new Vec3(1.0D, 1.0D, 0.0D),
				new Vec3(0.0D, 1.0D, 0.0D)
		};

		final float[] neighbourDensities = new float[8];
		int neighbourIndex = 0;
		for (final MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos, pos.add(1, 1, 1))) {
			final float neighbourDensity = ModUtil.getBlockDensity(mutablePos, cache);
			neighbourDensities[neighbourIndex] = neighbourDensity;
			final boolean neighborIsInsideIsosurface = neighbourDensity > isosurfaceLevel;
			neighbourIndex++;
		}

		byte cubeIndex = 0b00000000;

		if (neighbourDensities[0] < isosurfaceLevel) cubeIndex |= 0b00000001;
		if (neighbourDensities[1] < isosurfaceLevel) cubeIndex |= 0b00000010;
		if (neighbourDensities[2] < isosurfaceLevel) cubeIndex |= 0b00000100;
		if (neighbourDensities[3] < isosurfaceLevel) cubeIndex |= 0b00001000;
		if (neighbourDensities[4] < isosurfaceLevel) cubeIndex |= 0b00010000;
		if (neighbourDensities[5] < isosurfaceLevel) cubeIndex |= 0b00100000;
		if (neighbourDensities[6] < isosurfaceLevel) cubeIndex |= 0b01000000;
		if (neighbourDensities[7] < isosurfaceLevel) cubeIndex |= 0b10000000;

		if ((cubeIndex == 0) || (cubeIndex == 255)) {
			return;
		}

		final Vec3[] vertices = new Vec3[12];
		final int edgeMask = EDGE_TABLE[cubeIndex];

		if ((edgeMask & 1) == 1)
			vertices[0] = vertexInterpolation(isosurfaceLevel, points[0], points[1], neighbourDensities[0], neighbourDensities[1]);
		if ((edgeMask & 2) == 2)
			vertices[1] = vertexInterpolation(isosurfaceLevel, points[1], points[2], neighbourDensities[1], neighbourDensities[2]);
		if ((edgeMask & 4) == 4)
			vertices[2] = vertexInterpolation(isosurfaceLevel, points[2], points[3], neighbourDensities[2], neighbourDensities[3]);
		if ((edgeMask & 8) == 8)
			vertices[3] = vertexInterpolation(isosurfaceLevel, points[3], points[0], neighbourDensities[3], neighbourDensities[0]);
		if ((edgeMask & 16) == 16)
			vertices[4] = vertexInterpolation(isosurfaceLevel, points[4], points[5], neighbourDensities[4], neighbourDensities[5]);
		if ((edgeMask & 32) == 32)
			vertices[5] = vertexInterpolation(isosurfaceLevel, points[5], points[6], neighbourDensities[5], neighbourDensities[6]);
		if ((edgeMask & 64) == 64)
			vertices[6] = vertexInterpolation(isosurfaceLevel, points[6], points[7], neighbourDensities[6], neighbourDensities[7]);
		if ((edgeMask & 128) == 128)
			vertices[7] = vertexInterpolation(isosurfaceLevel, points[7], points[4], neighbourDensities[7], neighbourDensities[4]);
		if ((edgeMask & 256) == 256)
			vertices[8] = vertexInterpolation(isosurfaceLevel, points[0], points[4], neighbourDensities[0], neighbourDensities[4]);
		if ((edgeMask & 512) == 512)
			vertices[9] = vertexInterpolation(isosurfaceLevel, points[1], points[5], neighbourDensities[1], neighbourDensities[5]);
		if ((edgeMask & 1024) == 1024)
			vertices[10] = vertexInterpolation(isosurfaceLevel, points[2], points[6], neighbourDensities[2], neighbourDensities[6]);
		if ((edgeMask & 2048) == 2048)
			vertices[11] = vertexInterpolation(isosurfaceLevel, points[3], points[7], neighbourDensities[3], neighbourDensities[7]);

		// get texture
		for (final MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
			if (ModUtil.shouldSmooth(state)) {
				break;
			} else {
				textureState = cache.getBlockState(mutablePos);
				texturePos = mutablePos;
			}
		}

		final BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
		if (quad == null) {
			return;
		}
		final TextureAtlasSprite sprite = quad.getSprite();
		final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
		final int red = (color >> 16) & 0xFF ;
		final int green = (color >> 8) & 0xFF;
		final int blue = color & 0xFF;
		final int alpha = 0xFF;

		final double minU = ClientUtil.getMinU(sprite);
		final double minV = ClientUtil.getMinV(sprite);
		final double maxU = ClientUtil.getMaxU(sprite);
		final double maxV = ClientUtil.getMaxV(sprite);

		// real pos not texturePos
		final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);
		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		final BufferBuilder bufferBuilder = event.getBufferBuilder();

		final ArrayList<Vec3[]> faces = new ArrayList<>();

		//shit I don't understand (lookup table)
		for (int triangleIndex = 0; TRIANGLE_TABLE[cubeIndex][triangleIndex] != -1; triangleIndex += 3) {
			final Vec3 vertex0 = vertices[TRIANGLE_TABLE[cubeIndex][triangleIndex]];
			final Vec3 vertex1 = vertices[TRIANGLE_TABLE[cubeIndex][triangleIndex + 1]];
			final Vec3 vertex2 = vertices[TRIANGLE_TABLE[cubeIndex][triangleIndex + 2]];
			faces.add(new Vec3[]{
					vertex0, vertex1, vertex2
			});
		}

		// triangles -> quads
		for (int i = 0; i < faces.size(); i += 2) {
			final Vec3[] vertexList0 = faces.get(i);
			final Vec3[] vertexList1 = faces.get(i + 1);

			//1st face
			{
				final Vec3 vertex0 = vertexList0[0];
				final Vec3 vertex1 = vertexList0[1];
				final Vec3 vertex2 = vertexList0[2];
				bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			}

			//2nd face
			{
				final Vec3 vertex0 = vertexList1[0];
				final Vec3 vertex1 = vertexList1[1];
				final Vec3 vertex2 = vertexList1[2];
				bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			}
		}

	}

	private static Vec3 vertexInterpolation(final float isoLevel, final Vec3 p1, final Vec3 p2, final float valp1, final float valp2) {

		if (MathHelper.abs(isoLevel - valp1) < 1.0E-5F) {
			return p1;
		} else if (MathHelper.abs(isoLevel - valp2) < 1.0E-5F) {
			return p2;
		} else if (MathHelper.abs(valp1 - valp2) < 1.0E-5F) {
			return p1;
		} else {
			final double mu = (isoLevel - valp1) / (valp2 - valp1);
			final double x = p1.xCoord + (mu * (p2.xCoord - p1.xCoord));
			final double y = p1.yCoord + (mu * (p2.yCoord - p1.yCoord));
			final double z = p1.zCoord + (mu * (p2.zCoord - p1.zCoord));
			return new Vec3(x, y, z);
		}
	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

}
