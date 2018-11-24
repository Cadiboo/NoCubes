package cadiboo.nocubes.renderer;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

public class MarchingCubes {

	public static final int[]   EDGE_TABLE     = new int[] {
		0, 265, 515, 778, 1030, 1295, 1541, 1804, 2060, 2309, 2575, 2822, 3082, 3331, 3593, 3840, 400, 153, 915, 666, 1430, 1183, 1941, 1692, 2460, 2197, 2975, 2710, 3482, 3219, 3993, 3728, 560, 825, 51, 314, 1590, 1855, 1077, 1340, 2620, 2869, 2111, 2358, 3642, 3891, 3129, 3376, 928, 681, 419, 170, 1958, 1711, 1445, 1196, 2988, 2725, 2479, 2214, 4010, 3747, 3497, 3232, 1120, 1385, 1635, 1898, 102, 367, 613, 876, 3180, 3429, 3695, 3942, 2154, 2403, 2665, 2912, 1520, 1273, 2035, 1786, 502, 255,
		1013, 764, 3580, 3317, 4095, 3830, 2554, 2291, 3065, 2800, 1616, 1881, 1107, 1370, 598, 863, 85, 348, 3676, 3925, 3167, 3414, 2650, 2899, 2137, 2384, 1984, 1737, 1475, 1226, 966, 719, 453, 204, 4044, 3781, 3535, 3270, 3018, 2755, 2505, 2240, 2240, 2505, 2755, 3018, 3270, 3535, 3781, 4044, 204, 453, 719, 966, 1226, 1475, 1737, 1984, 2384, 2137, 2899, 2650, 3414, 3167, 3925, 3676, 348, 85, 863, 598, 1370, 1107, 1881, 1616, 2800, 3065, 2291, 2554, 3830, 4095, 3317, 3580, 764, 1013, 255,
		502, 1786, 2035, 1273, 1520, 2912, 2665, 2403, 2154, 3942, 3695, 3429, 3180, 876, 613, 367, 102, 1898, 1635, 1385, 1120, 3232, 3497, 3747, 4010, 2214, 2479, 2725, 2988, 1196, 1445, 1711, 1958, 170, 419, 681, 928, 3376, 3129, 3891, 3642, 2358, 2111, 2869, 2620, 1340, 1077, 1855, 1590, 314, 51, 825, 560, 3728, 3993, 3219, 3482, 2710, 2975, 2197, 2460, 1692, 1941, 1183, 1430, 666, 915, 153, 400, 3840, 3593, 3331, 3082, 2822, 2575, 2309, 2060, 1804, 1541, 1295, 1030, 778, 515, 265, 0
	};
	public static final int[][] TRIANGLE_TABLE = new int[][] {
		{ - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 1, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 8, 3, 9, 8, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 3, 1, 2, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 9, 2, 10, 0, 2, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 8, 3, 2, 10, 8, 10, 9, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 11, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 11, 2, 8, 11, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 9, 0, 2, 3, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 11, 2, 1, 9, 11, 9, 8, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 3, 10, 1, 11, 10, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 10, 1, 0, 8, 10, 8, 11, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 9, 0, 3, 11, 9, 11, 10, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 8, 10, 10, 8, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 7, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 3, 0, 7, 3, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 0, 1, 9, 8, 4, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 1, 9, 4, 7, 1, 7, 3, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 10, 8, 4, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 4, 7, 3, 0, 4, 1, 2, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 2, 10, 9, 0, 2, 8, 4, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, - 1, - 1, - 1, - 1 }, { 8, 4, 7, 3, 11, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 11, 4, 7, 11, 2, 4, 2, 0, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 0, 1, 8, 4, 7, 2, 3, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, - 1, - 1, - 1, - 1 }, { 3, 10, 1, 3, 11, 10, 7, 8, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, - 1, - 1, - 1, - 1 }, { 4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, - 1, - 1, - 1, - 1 }, { 4, 7, 11, 4, 11, 9, 9, 11, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 9, 5, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 5, 4, 0, 8, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 5, 4, 1, 5, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 5, 4, 8, 3, 5, 3, 1, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 10, 9, 5, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 0, 8, 1, 2, 10, 4, 9, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 5, 2, 10, 5, 4, 2, 4, 0, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, - 1, - 1, - 1, - 1 }, { 9, 5, 4, 2, 3, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 11, 2, 0, 8, 11, 4, 9, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 5, 4, 0, 1, 5, 2, 3, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, - 1, - 1, - 1, - 1 }, { 10, 3, 11, 10, 1, 3, 9, 5, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, - 1, - 1, - 1, - 1 }, { 5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, - 1, - 1, - 1, - 1 }, { 5, 4, 8, 5, 8, 10, 10, 8, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 7, 8, 5, 7, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 3, 0, 9, 5, 3, 5, 7, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 7, 8, 0, 1, 7, 1, 5, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 5, 3, 3, 5, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 9, 7, 8, 9, 5, 7, 10, 1, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, - 1, - 1, - 1, - 1 }, { 8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, - 1, - 1, - 1, - 1 }, { 2, 10, 5, 2, 5, 3, 3, 5, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 7, 9, 5, 7, 8, 9, 3, 11, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, - 1, - 1, - 1, - 1 }, { 2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, - 1, - 1, - 1, - 1 },
		{ 11, 2, 1, 11, 1, 7, 7, 1, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, - 1, - 1, - 1, - 1 }, { 5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, - 1 }, { 11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, - 1 }, { 11, 10, 5, 7, 11, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 6, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 3, 5, 10, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 9, 0, 1, 5, 10, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 8, 3, 1, 9, 8, 5, 10, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 6, 5, 2, 6, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 6, 5, 1, 2, 6, 3, 0, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 6, 5, 9, 0, 6, 0, 2, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, - 1, - 1, - 1, - 1 }, { 2, 3, 11, 10, 6, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 11, 0, 8, 11, 2, 0, 10, 6, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 1, 9, 2, 3, 11, 5, 10, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, - 1, - 1, - 1, - 1 }, { 6, 3, 11, 6, 5, 3, 5, 1, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, - 1, - 1, - 1, - 1 }, { 3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, - 1, - 1, - 1, - 1 }, { 6, 5, 9, 6, 9, 11, 11, 9, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 5, 10, 6, 4, 7, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 3, 0, 4, 7, 3, 6, 5, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 9, 0, 5, 10, 6, 8, 4, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, - 1, - 1, - 1, - 1 }, { 6, 1, 2, 6, 5, 1, 4, 7, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, - 1, - 1, - 1, - 1 }, { 8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, - 1, - 1, - 1, - 1 },
		{ 7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, - 1 }, { 3, 11, 2, 7, 8, 4, 10, 6, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, - 1, - 1, - 1, - 1 }, { 0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, - 1, - 1, - 1, - 1 }, { 9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, - 1 }, { 8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, - 1, - 1, - 1, - 1 }, { 5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, - 1 }, { 0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, - 1 },
		{ 6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, - 1, - 1, - 1, - 1 }, { 10, 4, 9, 6, 4, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 10, 6, 4, 9, 10, 0, 8, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 0, 1, 10, 6, 0, 6, 4, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, - 1, - 1, - 1, - 1 }, { 1, 4, 9, 1, 2, 4, 2, 6, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, - 1, - 1, - 1, - 1 },
		{ 0, 2, 4, 4, 2, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 3, 2, 8, 2, 4, 4, 2, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 4, 9, 10, 6, 4, 11, 2, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, - 1, - 1, - 1, - 1 }, { 3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, - 1, - 1, - 1, - 1 }, { 6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, - 1 }, { 9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, - 1, - 1, - 1, - 1 },
		{ 8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, - 1 }, { 3, 11, 6, 3, 6, 0, 0, 6, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 6, 4, 8, 11, 6, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 7, 10, 6, 7, 8, 10, 8, 9, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, - 1, - 1, - 1, - 1 }, { 10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, - 1, - 1, - 1, - 1 }, { 10, 6, 7, 10, 7, 1, 1, 7, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, - 1, - 1, - 1, - 1 }, { 2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, - 1 }, { 7, 8, 0, 7, 0, 6, 6, 0, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 7, 3, 2, 6, 7, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, - 1, - 1, - 1, - 1 }, { 2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, - 1 }, { 1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, - 1 }, { 11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, - 1, - 1, - 1, - 1 },
		{ 8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, - 1 }, { 0, 9, 1, 11, 6, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, - 1, - 1, - 1, - 1 }, { 7, 11, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 7, 6, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 0, 8, 11, 7, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 0, 1, 9, 11, 7, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 1, 9, 8, 3, 1, 11, 7, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 1, 2, 6, 11, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 10, 3, 0, 8, 6, 11, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 9, 0, 2, 10, 9, 6, 11, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, - 1, - 1, - 1, - 1 }, { 7, 2, 3, 6, 2, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 7, 0, 8, 7, 6, 0, 6, 2, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 7, 6, 2, 3, 7, 0, 1, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, - 1, - 1, - 1, - 1 }, { 10, 7, 6, 10, 1, 7, 1, 3, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, - 1, - 1, - 1, - 1 }, { 0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, - 1, - 1, - 1, - 1 }, { 7, 6, 10, 7, 10, 8, 8, 10, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 6, 8, 4, 11, 8, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 6, 11, 3, 0, 6, 0, 4, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 6, 11, 8, 4, 6, 9, 0, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, - 1, - 1, - 1, - 1 }, { 6, 8, 4, 6, 11, 8, 2, 10, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, - 1, - 1, - 1, - 1 }, { 4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, - 1, - 1, - 1, - 1 },
		{ 10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, - 1 }, { 8, 2, 3, 8, 4, 2, 4, 6, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 4, 2, 4, 6, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, - 1, - 1, - 1, - 1 }, { 1, 9, 4, 1, 4, 2, 2, 4, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, - 1, - 1, - 1, - 1 }, { 10, 1, 0, 10, 0, 6, 6, 0, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, - 1 }, { 10, 9, 4, 6, 10, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 9, 5, 7, 6, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 3, 4, 9, 5, 11, 7, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 5, 0, 1, 5, 4, 0, 7, 6, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, - 1, - 1, - 1, - 1 }, { 9, 5, 4, 10, 1, 2, 7, 6, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, - 1, - 1, - 1, - 1 }, { 7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, - 1, - 1, - 1, - 1 }, { 3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, - 1 }, { 7, 2, 3, 7, 6, 2, 5, 4, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, - 1, - 1, - 1, - 1 }, { 3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, - 1, - 1, - 1, - 1 }, { 6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, - 1 }, { 9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, - 1, - 1, - 1, - 1 },
		{ 1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, - 1 }, { 4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, - 1 }, { 7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, - 1, - 1, - 1, - 1 }, { 6, 9, 5, 6, 11, 9, 11, 8, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, - 1, - 1, - 1, - 1 }, { 0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, - 1, - 1, - 1, - 1 }, { 6, 11, 3, 6, 3, 5, 5, 3, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, - 1, - 1, - 1, - 1 }, { 0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, - 1 }, { 11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, - 1 }, { 6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, - 1, - 1, - 1, - 1 }, { 5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, - 1, - 1, - 1, - 1 }, { 9, 5, 6, 9, 6, 0, 0, 6, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, - 1 }, { 1, 5, 6, 2, 1, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, - 1 }, { 10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, - 1, - 1, - 1, - 1 }, { 0, 3, 8, 5, 6, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 10, 5, 6, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 11, 5, 10, 7, 5, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 11, 5, 10, 11, 7, 5, 8, 3, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 5, 11, 7, 5, 10, 11, 1, 9, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, - 1, - 1, - 1, - 1 }, { 11, 1, 2, 11, 7, 1, 7, 5, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, - 1, - 1, - 1, - 1 }, { 9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, - 1, - 1, - 1, - 1 }, { 7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, - 1 }, { 2, 5, 10, 2, 3, 5, 3, 7, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, - 1, - 1, - 1, - 1 },
		{ 9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, - 1, - 1, - 1, - 1 }, { 9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, - 1 }, { 1, 3, 5, 3, 7, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 7, 0, 7, 1, 1, 7, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 0, 3, 9, 3, 5, 5, 3, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 8, 7, 5, 9, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 5, 8, 4, 5, 10, 8, 10, 11, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, - 1, - 1, - 1, - 1 }, { 0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, - 1, - 1, - 1, - 1 }, { 10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, - 1 }, { 2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, - 1, - 1, - 1, - 1 }, { 0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, - 1 }, { 0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, - 1 }, { 9, 4, 5, 2, 11, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, - 1, - 1, - 1, - 1 }, { 5, 10, 2, 5, 2, 4, 4, 2, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, - 1 }, { 5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, - 1, - 1, - 1, - 1 }, { 8, 4, 5, 8, 5, 3, 3, 5, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 4, 5, 1, 0, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, - 1, - 1, - 1, - 1 },
		{ 9, 4, 5, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 11, 7, 4, 9, 11, 9, 10, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, - 1, - 1, - 1, - 1 }, { 1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, - 1, - 1, - 1, - 1 }, { 3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, - 1 }, { 4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, - 1, - 1, - 1, - 1 }, { 9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, - 1 },
		{ 11, 7, 4, 11, 4, 2, 2, 4, 0, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, - 1, - 1, - 1, - 1 }, { 2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, - 1, - 1, - 1, - 1 }, { 9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, - 1 }, { 3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, - 1 }, { 1, 10, 2, 8, 7, 4, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 9, 1, 4, 1, 7, 7, 1, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, - 1, - 1, - 1, - 1 }, { 4, 0, 3, 7, 4, 3, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 4, 8, 7, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 9, 10, 8, 10, 11, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 0, 9, 3, 9, 11, 11, 9, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 1, 10, 0, 10, 8, 8, 10, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 3, 1, 10, 11, 3, 10, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 2, 11, 1, 11, 9, 9, 11, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, - 1, - 1, - 1, - 1 }, { 0, 2, 11, 8, 0, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 3, 2, 11, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 3, 8, 2, 8, 10, 10, 8, 9, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ 9, 10, 2, 0, 9, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, - 1, - 1, - 1, - 1 }, { 1, 10, 2, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 1, 3, 8, 9, 1, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 9, 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }, { 0, 3, 8, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 },
		{ - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1, - 1 }
	};

	//	private static boolean renderGrid(final IBlockState state, final int x, final int y, final int z, final IBlockAccess cache, final Tessellator tesselator) {
	//		final Tessellator tessellator = Tessellator.getInstance();
	//		final Vec3dMutable[] pointList = new Vec3dMutable[] { new Vec3dMutable(0.0D, 0.0D, 1.0D), new Vec3dMutable(1.0D, 0.0D, 1.0D), new Vec3dMutable(1.0D, 0.0D, 0.0D), new Vec3dMutable(0.0D, 0.0D, 0.0D), new Vec3dMutable(0.0D, 1.0D, 1.0D), new Vec3dMutable(1.0D, 1.0D, 1.0D), new Vec3dMutable(1.0D, 1.0D, 0.0D), new Vec3dMutable(0.0D, 1.0D, 0.0D) };
	//		int fastx;
	//		for (fastx = 0; fastx < 8; ++fastx) {
	//			pointList[fastx].x += x;
	//			pointList[fastx].y += y;
	//			pointList[fastx].z += z;
	//		}
	//		fastx = x;
	//		int fasty = y;
	//		int fastz = z;
	//		boolean set = false;
	//		final float[] pointValue = new float[8];
	//		int i;
	//		for (i = 0; i < 8; ++i) {
	//			pointValue[i] = isPointCorner(pointList[i], cache);
	//			if (!set || !NoCubes.isBlockNatural(state)) {
	//				set = true;
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x;
	//					fasty = (int) pointList[i].y;
	//					fastz = (int) pointList[i].z;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x;
	//					fasty = (int) pointList[i].y - 1;
	//					fastz = (int) pointList[i].z;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x - 1;
	//					fasty = (int) pointList[i].y;
	//					fastz = (int) pointList[i].z;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x - 1;
	//					fasty = (int) pointList[i].y - 1;
	//					fastz = (int) pointList[i].z;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x;
	//					fasty = (int) pointList[i].y;
	//					fastz = (int) pointList[i].z - 1;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x;
	//					fasty = (int) pointList[i].y - 1;
	//					fastz = (int) pointList[i].z - 1;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x - 1;
	//					fasty = (int) pointList[i].y;
	//					fastz = (int) pointList[i].z - 1;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//				if (!NoCubes.isBlockNatural(state)) {
	//					fastx = (int) pointList[i].x - 1;
	//					fasty = (int) pointList[i].y - 1;
	//					fastz = (int) pointList[i].z - 1;
	//					state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
	//				}
	//			}
	//		}
	//		i = cache.getBlockMetadata(fastx, fasty, fastz);
	//		final int color = state.colorMultiplier(cache, fastx, fasty, fastz);
	//		final float colorRed = ((color >> 16) & 255) / 255.0F;
	//		final float colorGreen = ((color >> 8) & 255) / 255.0F;
	//		final float colorBlue = (color & 255) / 255.0F;
	//		final IIcon icon = renderblocks.getBlockIconFromSideAndMetadata(state, 1, i);
	//		final double minU = (double) icon.func_94214_a(0.0D);
	//		final double minV = (double) icon.func_94207_b(0.0D);
	//		final double maxU = (double) icon.func_94214_a(15.0D + (0.16666666666666666D * (double) MathHelper.clamp_int(x, 0, 6)));
	//		final double maxV = (double) icon.func_94207_b(15.0D + (0.16666666666666666D * (double) MathHelper.clamp_int(z, 0, 6)));
	//		int cubeIndex = 0;
	//		final float isolevel = 0.5F;
	//		if (pointValue[0] < isolevel) {
	//			cubeIndex |= 1;
	//		}
	//		if (pointValue[1] < isolevel) {
	//			cubeIndex |= 2;
	//		}
	//		if (pointValue[2] < isolevel) {
	//			cubeIndex |= 4;
	//		}
	//		if (pointValue[3] < isolevel) {
	//			cubeIndex |= 8;
	//		}
	//		if (pointValue[4] < isolevel) {
	//			cubeIndex |= 16;
	//		}
	//		if (pointValue[5] < isolevel) {
	//			cubeIndex |= 32;
	//		}
	//		if (pointValue[6] < isolevel) {
	//			cubeIndex |= 64;
	//		}
	//		if (pointValue[7] < isolevel) {
	//			cubeIndex |= 128;
	//		}
	//		if ((cubeIndex != 0) && (cubeIndex != 255)) {
	//			final Vec3dMutable[] vertexList = new Vec3dMutable[12];
	//			if ((EDGE_TABLE[cubeIndex] & 1) == 1) {
	//				vertexList[0] = vertexInterpolation(isolevel, pointList[0], pointList[1], pointValue[0], pointValue[1]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 2) == 2) {
	//				vertexList[1] = vertexInterpolation(isolevel, pointList[1], pointList[2], pointValue[1], pointValue[2]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 4) == 4) {
	//				vertexList[2] = vertexInterpolation(isolevel, pointList[2], pointList[3], pointValue[2], pointValue[3]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 8) == 8) {
	//				vertexList[3] = vertexInterpolation(isolevel, pointList[3], pointList[0], pointValue[3], pointValue[0]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 16) == 16) {
	//				vertexList[4] = vertexInterpolation(isolevel, pointList[4], pointList[5], pointValue[4], pointValue[5]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 32) == 32) {
	//				vertexList[5] = vertexInterpolation(isolevel, pointList[5], pointList[6], pointValue[5], pointValue[6]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 64) == 64) {
	//				vertexList[6] = vertexInterpolation(isolevel, pointList[6], pointList[7], pointValue[6], pointValue[7]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 128) == 128) {
	//				vertexList[7] = vertexInterpolation(isolevel, pointList[7], pointList[4], pointValue[7], pointValue[4]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 256) == 256) {
	//				vertexList[8] = vertexInterpolation(isolevel, pointList[0], pointList[4], pointValue[0], pointValue[4]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 512) == 512) {
	//				vertexList[9] = vertexInterpolation(isolevel, pointList[1], pointList[5], pointValue[1], pointValue[5]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 1024) == 1024) {
	//				vertexList[10] = vertexInterpolation(isolevel, pointList[2], pointList[6], pointValue[2], pointValue[6]);
	//			}
	//			if ((EDGE_TABLE[cubeIndex] & 2048) == 2048) {
	//				vertexList[11] = vertexInterpolation(isolevel, pointList[3], pointList[7], pointValue[3], pointValue[7]);
	//			}
	//			for (int i = 0; TRIANGLE_TABLE[cubeIndex][i] != -1; i += 3) {
	//				tessellator.setBrightness(state.getMixedBrightnessForBlock(cache, x, y + 1, z));
	//				tessellator.color(colorRed, colorGreen, colorBlue);
	//				final Vec3dMutable vertex0 = vertexList[TRIANGLE_TABLE[cubeIndex][i]];
	//				final Vec3dMutable vertex1 = vertexList[TRIANGLE_TABLE[cubeIndex][i + 1]];
	//				final Vec3dMutable vertex2 = vertexList[TRIANGLE_TABLE[cubeIndex][i + 2]];
	//				final Vec3dMutable vertex3 = vertexList[TRIANGLE_TABLE[cubeIndex][i + 2]];
	//				tessellator.addVertexWithUV(vertex0.x, vertex0.y, vertex0.z, maxU, maxV);
	//				tessellator.addVertexWithUV(vertex1.x, vertex1.y, vertex1.z, maxU, minV);
	//				tessellator.addVertexWithUV(vertex2.x, vertex2.y, vertex2.z, minU, minV);
	//				tessellator.addVertexWithUV(vertex3.x, vertex3.y, vertex3.z, minU, maxV);
	//			}
	//			return true;
	//		} else {
	//			return false;
	//		}
	//	}
	//
	private static float isPointCorner(final Vec3dMutable point, final IBlockAccess cache) {

		float result = 0.0F;

		for (int i = 0; i < 4; ++ i) {
			final int x = (int) point.x - (i & 1);
			final int y = (int) point.y;
			final int z = (int) point.z - ((i >> 1) & 1);
			final IBlockState block1 = cache.getBlockState(new BlockPos(x, y - 1, z));
			if (ModUtil.shouldSmooth(block1)) {
				result += 0.125F;
			}

			final IBlockState block2 = cache.getBlockState(new BlockPos(x, y, z));
			if (ModUtil.shouldSmooth(block2)) {
				result += 0.125F;
			}
		}

		return result;
	}
	//
	//	private static Vec3dMutable vertexInterpolation(final Vec3dMutable p1, final Vec3dMutable p2, final boolean valp1, final boolean valp2) {
	//		if (!valp1) {
	//			return p1;
	//		} else if (!valp2) {
	//			return p2;
	//		} else {
	//			final double x = (p1.x + p2.x) - p1.x;
	//			final double y = (p1.y + p2.y) - p1.y;
	//			final double z = (p1.z + p2.z) - p1.z;
	//			return new Vec3dMutable(x, y, z);
	//		}
	//	}

	private static Vec3dMutable vertexInterpolation(final float isoLevel, final Vec3dMutable p1, final Vec3dMutable p2, final float valp1, final float valp2) {

		if (MathHelper.abs(isoLevel - valp1) < 1.0E-5F) {
			return p1;
		} else if (MathHelper.abs(isoLevel - valp2) < 1.0E-5F) {
			return p2;
		} else if (MathHelper.abs(valp1 - valp2) < 1.0E-5F) {
			return p1;
		} else {
			final double mu = (isoLevel - valp1) / (valp2 - valp1);
			final double x = p1.x + (mu * (p2.x - p1.x));
			final double y = p1.y + (mu * (p2.y - p1.y));
			final double z = p1.z + (mu * (p2.z - p1.z));
			return new Vec3dMutable(x, y, z);
		}
	}

	public static boolean renderBlock(IBlockState state, final BlockPos pos, final ChunkCache cache, final BufferBuilder bufferBuilder, final BlockRendererDispatcher blockRendererDispatcher) {

		try {

			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			final Vec3dMutable[] pointList = new Vec3dMutable[] { new Vec3dMutable(0.0D, 0.0D, 1.0D), new Vec3dMutable(1.0D, 0.0D, 1.0D), new Vec3dMutable(1.0D, 0.0D, 0.0D), new Vec3dMutable(0.0D, 0.0D, 0.0D), new Vec3dMutable(0.0D, 1.0D, 1.0D), new Vec3dMutable(1.0D, 1.0D, 1.0D), new Vec3dMutable(1.0D, 1.0D, 0.0D), new Vec3dMutable(0.0D, 1.0D, 0.0D) };

			int fastx;
			for (fastx = 0; fastx < 8; ++ fastx) {
				pointList[fastx].x += x;
				pointList[fastx].y += y;
				pointList[fastx].z += z;
			}

			fastx = x;
			int fasty = y;
			int fastz = z;
			boolean set = false;
			final float[] pointValue = new float[8];

			int i;
			for (i = 0; i < 8; ++ i) {
				pointValue[i] = isPointCorner(pointList[i], cache);
				if (! set || ! ModUtil.shouldSmooth(state)) {
					set = true;
					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x;
						fasty = (int) pointList[i].y;
						fastz = (int) pointList[i].z;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x;
						fasty = (int) pointList[i].y - 1;
						fastz = (int) pointList[i].z;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x - 1;
						fasty = (int) pointList[i].y;
						fastz = (int) pointList[i].z;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x - 1;
						fasty = (int) pointList[i].y - 1;
						fastz = (int) pointList[i].z;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x;
						fasty = (int) pointList[i].y;
						fastz = (int) pointList[i].z - 1;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x;
						fasty = (int) pointList[i].y - 1;
						fastz = (int) pointList[i].z - 1;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x - 1;
						fasty = (int) pointList[i].y;
						fastz = (int) pointList[i].z - 1;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}

					if (! ModUtil.shouldSmooth(state)) {
						fastx = (int) pointList[i].x - 1;
						fasty = (int) pointList[i].y - 1;
						fastz = (int) pointList[i].z - 1;
						state = cache.getBlockState(new BlockPos(fastx, fasty, fastz));
					}
				}
			}

			final TextureAtlasSprite sprite = ModUtil.getSprite(state, pos, blockRendererDispatcher);

			if (sprite == null) {
				return false;
			}

			final int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);

			final float colorRed = ((color >> 16) & 255) / 255.0F;
			final float colorGreen = ((color >> 8) & 255) / 255.0F;
			final float colorBlue = (color & 255) / 255.0F;

			final float alpha = 1f;

			final double minU = sprite.getInterpolatedU(0.0D);
			final double minV = sprite.getInterpolatedV(0.0D);
			final double maxU = sprite.getInterpolatedU(15.0D + (0.16666666666666666D * MathHelper.clamp(x, 0, 6)));
			final double maxV = sprite.getInterpolatedV(15.0D + (0.16666666666666666D * MathHelper.clamp(z, 0, 6)));

			//			final double minU = 0;
			//			final double minV = 0;
			//			final double maxU = 0.1;
			//			final double maxV = 0.1;

			final int lightmapSkyLight;
			final int lightmapBlockLight;
			if (ModConfig.shouldAproximateLighting) {
				final BlockPos brightnessPos = pos.up();
				final int packedLightmapCoords = cache.getBlockState(brightnessPos).getPackedLightmapCoords(cache, brightnessPos);
				lightmapSkyLight = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
				lightmapBlockLight = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
			} else {
				lightmapSkyLight = 15 << 4;
				lightmapBlockLight = 15 << 4;
			}

			//			int meta = cache.getBlockMetadata(fastx, fasty, fastz);
			//			final int color = state.colorMultiplier(cache, fastx, fasty, fastz);
			//			final float colorRed = ((color >> 16) & 255) / 255.0F;
			//			final float colorGreen = ((color >> 8) & 255) / 255.0F;
			//			final float colorBlue = (color & 255) / 255.0F;
			//			final IIcon icon = renderblocks.getBlockIconFromSideAndMetadata(state, 1, meta);
			//			final double minU = (double) icon.func_94214_a(0.0D);
			//			final double minV = (double) icon.func_94207_b(0.0D);
			//			final double maxU = (double) icon.func_94214_a(15.0D + (0.16666666666666666D * (double) MathHelper.clamp_int(x, 0, 6)));
			//			final double maxV = (double) icon.func_94207_b(15.0D + (0.16666666666666666D * (double) MathHelper.clamp_int(z, 0, 6)));

			int cubeIndex = 0;
			final float isolevel = 0.5F;
			//			final float isolevel = 1F; // gives interesting results
			if (pointValue[0] < isolevel) {
				cubeIndex |= 1;
			}

			if (pointValue[1] < isolevel) {
				cubeIndex |= 2;
			}

			if (pointValue[2] < isolevel) {
				cubeIndex |= 4;
			}

			if (pointValue[3] < isolevel) {
				cubeIndex |= 8;
			}

			if (pointValue[4] < isolevel) {
				cubeIndex |= 16;
			}

			if (pointValue[5] < isolevel) {
				cubeIndex |= 32;
			}

			if (pointValue[6] < isolevel) {
				cubeIndex |= 64;
			}

			if (pointValue[7] < isolevel) {
				cubeIndex |= 128;
			}

			if ((cubeIndex != 0) && (cubeIndex != 255)) {
				final Vec3dMutable[] vertexList = new Vec3dMutable[12];
				if ((EDGE_TABLE[cubeIndex] & 1) == 1) {
					vertexList[0] = vertexInterpolation(isolevel, pointList[0], pointList[1], pointValue[0], pointValue[1]);
				}

				if ((EDGE_TABLE[cubeIndex] & 2) == 2) {
					vertexList[1] = vertexInterpolation(isolevel, pointList[1], pointList[2], pointValue[1], pointValue[2]);
				}

				if ((EDGE_TABLE[cubeIndex] & 4) == 4) {
					vertexList[2] = vertexInterpolation(isolevel, pointList[2], pointList[3], pointValue[2], pointValue[3]);
				}

				if ((EDGE_TABLE[cubeIndex] & 8) == 8) {
					vertexList[3] = vertexInterpolation(isolevel, pointList[3], pointList[0], pointValue[3], pointValue[0]);
				}

				if ((EDGE_TABLE[cubeIndex] & 16) == 16) {
					vertexList[4] = vertexInterpolation(isolevel, pointList[4], pointList[5], pointValue[4], pointValue[5]);
				}

				if ((EDGE_TABLE[cubeIndex] & 32) == 32) {
					vertexList[5] = vertexInterpolation(isolevel, pointList[5], pointList[6], pointValue[5], pointValue[6]);
				}

				if ((EDGE_TABLE[cubeIndex] & 64) == 64) {
					vertexList[6] = vertexInterpolation(isolevel, pointList[6], pointList[7], pointValue[6], pointValue[7]);
				}

				if ((EDGE_TABLE[cubeIndex] & 128) == 128) {
					vertexList[7] = vertexInterpolation(isolevel, pointList[7], pointList[4], pointValue[7], pointValue[4]);
				}

				if ((EDGE_TABLE[cubeIndex] & 256) == 256) {
					vertexList[8] = vertexInterpolation(isolevel, pointList[0], pointList[4], pointValue[0], pointValue[4]);
				}

				if ((EDGE_TABLE[cubeIndex] & 512) == 512) {
					vertexList[9] = vertexInterpolation(isolevel, pointList[1], pointList[5], pointValue[1], pointValue[5]);
				}

				if ((EDGE_TABLE[cubeIndex] & 1024) == 1024) {
					vertexList[10] = vertexInterpolation(isolevel, pointList[2], pointList[6], pointValue[2], pointValue[6]);
				}

				if ((EDGE_TABLE[cubeIndex] & 2048) == 2048) {
					vertexList[11] = vertexInterpolation(isolevel, pointList[3], pointList[7], pointValue[3], pointValue[7]);
				}

				for (int triangleIndex = 0; TRIANGLE_TABLE[cubeIndex][triangleIndex] != - 1; triangleIndex += 3) {

					//					tessellator.setBrightness(state.getMixedBrightnessForBlock(cache, x, y + 1, z));
					//					tessellator.color(colorRed, colorGreen, colorBlue);
					//					final Vec3dMutable vertex0 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex]];
					//					final Vec3dMutable vertex1 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 1]];
					//					final Vec3dMutable vertex2 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 2]];
					//					final Vec3dMutable vertex3 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 2]];
					//					tessellator.addVertexWithUV(vertex0.x, vertex0.y, vertex0.z, maxU, maxV);
					//					tessellator.addVertexWithUV(vertex1.x, vertex1.y, vertex1.z, maxU, minV);
					//					tessellator.addVertexWithUV(vertex2.x, vertex2.y, vertex2.z, minU, minV);
					//					tessellator.addVertexWithUV(vertex3.x, vertex3.y, vertex3.z, minU, maxV);

					//					bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

					final Vec3dMutable vertex0 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 0]];
					final Vec3dMutable vertex1 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 1]];
					final Vec3dMutable vertex2 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 2]];
					final Vec3dMutable vertex3 = vertexList[TRIANGLE_TABLE[cubeIndex][triangleIndex + 2]];

					bufferBuilder.pos(vertex0.x, vertex0.y, vertex0.z).color(colorRed, colorGreen, colorBlue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(vertex1.x, vertex1.y, vertex1.z).color(colorRed, colorGreen, colorBlue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(vertex2.x, vertex2.y, vertex2.z).color(colorRed, colorGreen, colorBlue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(vertex3.x, vertex3.y, vertex3.z).color(colorRed, colorGreen, colorBlue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

				}
				return true;
			}
		} catch (final Exception e) {
			return false;
		}
		return false;
	}

	public static class Vec3dMutable {

		double x;
		double y;
		double z;

		public Vec3dMutable(final double x, final double y, final double z) {

			this.x = x;
			this.y = y;
			this.z = z;
		}

	}

}
