package io.github.cadiboo.nocubes.util;

/**
 * Some basic functions that differ depending on the physical side
 *
 * @author Cadiboo
 */
public interface IProxy {

	void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately);

	void replaceFluidRendererCauseImBored();

	void setupDecentGraphicsSettings();

}
