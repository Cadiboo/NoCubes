package cadiboo.nocubes.renderer;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
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

	public static boolean renderBlock(IBlockState state, BlockPos.MutableBlockPos blockPos, ChunkCache chunkCache, BufferBuilder bufferBuilder, BlockRendererDispatcher blockRendererDispatcher) {

		final int x = blockPos.getX();
		final int y = blockPos.getY();
		final int z= blockPos.getZ();

		final int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, chunkCache, blockPos, 0);
		final float colorRed = (color >> 16 & 0xFF) / 255.0f;
		final float colorGreen = (color >> 8 & 0xFF) / 255.0f;
		final float colorBlue = (color & 0xFF) / 255.0f;
		final float shadowBottom = 0.6f;
		final float shadowTop = 1.0f;
		final float shadowLeft = 0.9f;
		final float shadowRight = 0.8f;

		final TextureAtlasSprite sprite = ModUtil.getSprite(state, blockPos, blockRendererDispatcher);

		final double minU = sprite.getMinU();
		final double minV = sprite.getMinV();
		final double maxU = sprite.getMaxU();
		final double maxV = sprite.getMaxV();

		final int[][] points = {

			{0, 0, 0},

			{1, 0, 0},

			{1, 0, 1},

			{0, 0, 1},

			{0, 1, 0},

			{1, 1, 0},

			{1, 1, 1},

			{0, 1, 1},

		};

		for(int pointIndex = 0; pointIndex < 8; ++pointIndex)
		{
			final int[] point = points[pointIndex];
			point[0] += x;
			point[1] += y;
			point[2] += z;
			if(!doesPointIntersectWithManufactured(chunkCache, points[pointIndex]))
			{
				if(pointIndex < 4 && doesPointBottomIntersectWithAir(chunkCache, points[pointIndex]))
				{
					points[pointIndex][1] = y + 1;
				}
				else if(pointIndex >= 4 && doesPointTopIntersectWithAir(chunkCache, points[pointIndex]))
				{
					points[pointIndex][1] = y;
				}
				points[pointIndex] = givePointRoughness(points[pointIndex]);
			}
		}
		for(int side = 0; side < 6; ++side)
		{
			int facingX = x;
			int facingY = y;
			int facingZ = z;
			if(side == 0)
			{
				--facingY;
			}
			else if(side == 1)
			{
				++facingY;
			}
			else if(side == 2)
			{
				--facingZ;
			}
			else if(side == 3)
			{
				++facingX;
			}
			else if(side == 4)
			{
				++facingZ;
			}
			else if(side == 5)
			{
				--facingX;
			}
			if(/*renderer.renderAllFaces*/false || state.shouldSideBeRendered(chunkCache, new BlockPos(facingX, facingY, facingZ), EnumFacing.VALUES[side]))
			{
				float colorFactor = 1.0f;
				int[] vertex0 = null;
				int[] vertex2 = null;
				int[] vertex3 = null;
				int[] vertex4 = null;
				if(side == 0)
				{
					colorFactor = shadowBottom;
					vertex0 = points[0];
					vertex2 = points[1];
					vertex3 = points[2];
					vertex4 = points[3];
				}
				else if(side == 1)
				{
					colorFactor = shadowTop;
					vertex0 = points[7];
					vertex2 = points[6];
					vertex3 = points[5];
					vertex4 = points[4];
				}
				else if(side == 2)
				{
					colorFactor = shadowLeft;
					vertex0 = points[1];
					vertex2 = points[0];
					vertex3 = points[4];
					vertex4 = points[5];
				}
				else if(side == 3)
				{
					colorFactor = shadowRight;
					vertex0 = points[2];
					vertex2 = points[1];
					vertex3 = points[5];
					vertex4 = points[6];
				}
				else if(side == 4)
				{
					colorFactor = shadowLeft;
					vertex0 = points[3];
					vertex2 = points[2];
					vertex3 = points[6];
					vertex4 = points[7];
				}
				else if(side == 5)
				{
					colorFactor = shadowRight;
					vertex0 = points[0];
					vertex2 = points[3];
					vertex3 = points[7];
					vertex4 = points[4];
				}


//				tessellator.setBrightness(block.getMixedBrightnessForBlock(world, facingX, facingY, facingZ));
//				tessellator.setColorOpaque_F(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue);
//				tessellator.addVertexWithUV(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord, minU, maxV);
//				tessellator.addVertexWithUV(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, maxU, maxV);
//				tessellator.addVertexWithUV(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, maxU, minV);
//				tessellator.addVertexWithUV(vertex4.xCoord, vertex4.yCoord, vertex4.zCoord, minU, minV);



				final int lightmapSkyLight;
				final int lightmapBlockLight;
				if (ModConfig.shouldAproximateLighting) {
					final BlockPos brightnessPos = new BlockPos(facingX, facingY, facingZ);
					final int packedLightmapCoords = chunkCache.getBlockState(brightnessPos).getPackedLightmapCoords(chunkCache, brightnessPos);
					lightmapSkyLight = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
					lightmapBlockLight = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
				} else {
					lightmapSkyLight = 15 << 4;
					lightmapBlockLight = 15 << 4;
				}


				bufferBuilder.pos(vertex0[0], vertex0[1], vertex0[2]).color(colorRed, colorGreen, colorBlue, 1f).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex2[0], vertex2[1], vertex2[2]).color(colorRed, colorGreen, colorBlue, 1f).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex3[0], vertex3[1], vertex3[2]).color(colorRed, colorGreen, colorBlue, 1f).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex4[0], vertex4[1], vertex4[2]).color(colorRed, colorGreen, colorBlue, 1f).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();



//				tessellator.setBrightness(block.getMixedBrightnessForBlock(world, facingX, facingY, facingZ));
//				tessellator.setColorOpaque_F(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue);
//				tessellator.addVertexWithUV(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord, minU, maxV);
//				tessellator.addVertexWithUV(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, maxU, maxV);
//				tessellator.addVertexWithUV(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, maxU, minV);
//				tessellator.addVertexWithUV(vertex4.xCoord, vertex4.yCoord, vertex4.zCoord, minU, minV);


			}
		}
		return true;
	}

	private static int[] givePointRoughness(final int[] point)
	{
		long i = (long)(point[0] * 3129871.0) ^ (long)point[1] * 116129781L ^ (long)point[2];
		i = i * i * 42317861L + i * 11L;
		point[0] += ((i >> 16 & 0xFL) / 15.0f - 0.5f) * 0.5f;
		point[1] += ((i >> 20 & 0xFL) / 15.0f - 0.5f) * 0.5f;
		point[2] += ((i >> 24 & 0xFL) / 15.0f - 0.5f) * 0.5f;
		return point;
	}

	public static boolean isBlockAirOrPlant(final IBlockState state)
	{
		final Material material = state.getMaterial();
		return material == Material.AIR || material == Material.PLANTS || material == Material.VINE || ModUtil.isBlockLiquid(state);
	}

	public static boolean doesPointTopIntersectWithAir(final IBlockAccess world, final int[] point)
	{
		boolean intersects = false;
		for(int i = 0; i < 4; ++i)
		{
			final int x1 = (int)(point[0] - (i & 0x1));
			final int z1 = (int)(point[2] - (i >> 1 & 0x1));
			if(!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, point[1], z1))))
			{
				return false;
			}
			if(!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, point[1]-1, z1))))
			{
				intersects = true;
			}
		}
		return intersects;
	}

	public static boolean doesPointBottomIntersectWithAir(final IBlockAccess world, final int[] point)
	{
		boolean intersects = false;
		boolean notOnly = false;
		for(int i = 0; i < 4; ++i)
		{
			final int x1 = (int)(point[0] - (i & 0x1));
			final int z1 = (int)(point[2] - (i >> 1 & 0x1));
			if(!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, point[1]-1, z1))))
			{
				return false;
			}
			if(!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, point[1]+1, z1))))
			{
				notOnly = true;
			}
			if(!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, point[1], z1))))
			{
				intersects = true;
			}
		}
		return intersects && notOnly;
	}

	public static boolean doesPointIntersectWithManufactured(final IBlockAccess world, final int[] point)
	{
		for(int i = 0; i < 4; ++i)
		{
			final int x1 = (int)(point[0] - (i & 0x1));
			final int z1 = (int)(point[2] - (i >> 1 & 0x1));
			final IBlockState state1 = world.getBlockState(new BlockPos(x1, point[1], z1));
			if(!isBlockAirOrPlant(state1) && !ModUtil.shouldSmooth(state1))
			{
				return true;
			}
			final IBlockState state2 = world.getBlockState(new BlockPos(x1, point[1]-1, z1));
			if(!isBlockAirOrPlant(state2) && !ModUtil.shouldSmooth(state2))
			{
				return true;
			}
		}
		return false;
	}


	public static boolean renderLiquidBlock(IBlockState state, BlockPos.MutableBlockPos blockPos, ChunkCache chunkCache, BufferBuilder bufferBuilder, BlockRendererDispatcher blockRendererDispatcher) {
		final boolean rendered = blockRendererDispatcher.renderBlock(state, blockPos, chunkCache, bufferBuilder);
//		if(NoCubes.isBlockLiquid(world.getBlock(x, y + 1, z)))
//		{
//			return rendered;
//		}
//		final int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
//		if(NoCubes.isBlockSoft(world.getBlock(x + 1, y, z)))
//		{
//			this.renderGhostLiquid(block, x + 1, y, z, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z + 1)))
//		{
//			this.renderGhostLiquid(block, x, y, z + 1, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 1)))
//		{
//			this.renderGhostLiquid(block, x - 1, y, z, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z - 1)))
//		{
//			this.renderGhostLiquid(block, x, y, z - 1, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x + 1, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x + 2, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z + 2)))
//		{
//			this.renderGhostLiquid(block, x + 1, y, z + 1, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x + 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x + 2, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 2)))
//		{
//			this.renderGhostLiquid(block, x + 1, y, z - 1, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x - 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z)))
//		{
//			this.renderGhostLiquid(block, x - 1, y, z - 1, brightness, renderer, world);
//		}
//		if(NoCubes.isBlockSoft(world.getBlock(x - 1, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z + 2)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z + 2)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z + 2)))
//		{
//			this.renderGhostLiquid(block, x - 1, y, z + 1, brightness, renderer, world);
//		}
		return rendered;
	}

	public boolean doesPointIntersectWithLiquid(final int x, final int y, final int z, final IBlockAccess world)
	{
		return false;
//		return NoCubes.isBlockLiquid(world.getBlock(x, y, z)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z)) || NoCubes.isBlockLiquid(world.getBlock(x, y, z - 1)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 1)) || NoCubes.isBlockLiquid(world.getBlock(x, y + 1, z)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y + 1, z)) || NoCubes.isBlockLiquid(world.getBlock(x, y + 1, z - 1)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y + 1, z - 1));
	}

//	public boolean renderGhostLiquid(final Block block, final int x, final int y, final int z, final int brightness, final RenderBlocks renderer, final IBlockAccess world)
//	{
//		final Tessellator tessellator = Tessellator.instance;
//		final Material material = block.getMaterial();
//		double height0 = 0.7;
//		double height2 = 0.7;
//		double height3 = 0.7;
//		double height4 = 0.7;
//		if(this.doesPointIntersectWithLiquid(x, y, z, world))
//		{
//			height0 = renderer.getLiquidHeight(x, y, z, material);
//		}
//		if(this.doesPointIntersectWithLiquid(x, y, z + 1, world))
//		{
//			height2 = renderer.getLiquidHeight(x, y, z + 1, material);
//		}
//		if(this.doesPointIntersectWithLiquid(x + 1, y, z + 1, world))
//		{
//			height3 = renderer.getLiquidHeight(x + 1, y, z + 1, material);
//		}
//		if(this.doesPointIntersectWithLiquid(x + 1, y, z, world))
//		{
//			height4 = renderer.getLiquidHeight(x + 1, y, z, material);
//		}
//		height0 -= 0.0010000000474974513;
//		height2 -= 0.0010000000474974513;
//		height3 -= 0.0010000000474974513;
//		height4 -= 0.0010000000474974513;
//		final IIcon icon = renderer.getBlockIconFromSide(block, 1);
//		final double minU = icon.getInterpolatedU(0.0);
//		final double minV = icon.getInterpolatedV(0.0);
//		final double maxU = icon.getInterpolatedU(16.0);
//		final double maxV = icon.getInterpolatedV(16.0);
//		tessellator.setBrightness(brightness);
//		tessellator.setColorOpaque_I(block.colorMultiplier(world, x, y, z));
//		tessellator.addVertexWithUV((double)(x + 0), y + height0, (double)(z + 0), minU, minV);
//		tessellator.addVertexWithUV((double)(x + 0), y + height2, (double)(z + 1), minU, maxV);
//		tessellator.addVertexWithUV((double)(x + 1), y + height3, (double)(z + 1), maxU, maxV);
//		tessellator.addVertexWithUV((double)(x + 1), y + height4, (double)(z + 0), maxU, minV);
//		return true;
//	}
//
//	public static boolean shouldHookRenderer(final Block block)
//	{
//		return NoCubes.isNoCubesEnabled && (NoCubes.isBlockSoft(block) || NoCubes.isBlockLiquid(block));
//	}
//
//	public boolean directRenderHook(final Block block, final int x, final int y, final int z, final RenderBlocks renderer)
//	{
//		block.setBlockBoundsBasedOnState(renderer.blockAccess, x, y, z);
//		renderer.setRenderBoundsFromBlock(block);
//		final IBlockAccess world = renderer.blockAccess;
//		if(NoCubes.isBlockLiquid(block))
//		{
//			return this.renderLiquidBlock(block, x, y, z, renderer, world);
//		}
//		return this.renderSoftBlock(block, x, y, z, renderer, world);
//	}
//
//	public static void inject(final Block block, final World world, final int x, final int y, final int z, final AxisAlignedBB aabb, final List list, final Entity entity)
//	{
//		final float f = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x, y, z);
//		final float f2 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x, y, z + 1);
//		final float f3 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x + 1, y, z + 1);
//		final float f4 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x + 1, y, z);
//		addBBoundsToList(x, y, z, 0.0f, 0.0f, 0.0f, 0.5f, f, 0.5f, aabb, list);
//		addBBoundsToList(x, y, z, 0.0f, 0.0f, 0.5f, 0.5f, f2, 1.0f, aabb, list);
//		addBBoundsToList(x, y, z, 0.5f, 0.0f, 0.5f, 1.0f, f3, 1.0f, aabb, list);
//		addBBoundsToList(x, y, z, 0.5f, 0.0f, 0.0f, 1.0f, f4, 0.5f, aabb, list);
//	}
//
//	public static void addBBoundsToList(final int x, final int y, final int z, final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final AxisAlignedBB aabb, final List list)
//	{
//		final AxisAlignedBB aabb2 = AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
//		if(aabb2 != null && aabb.intersectsWith(aabb2))
//		{
//			list.add(aabb2);
//		}
//	}
//
//	public static class Vec3dMutable {
//
//		double x;
//		double y;
//		double z;
//
//		public Vec3dMutable(final double x, final double y, final double z) {
//
//			this.x = x;
//			this.y = y;
//			this.z = z;
//		}
//
//	}

}
