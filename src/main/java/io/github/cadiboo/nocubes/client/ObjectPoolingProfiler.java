package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.DensityCache;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Cadiboo
 */
public class ObjectPoolingProfiler {

	private static int counter = 20;

	private static String lightmapInfo = "";
	private static String packedLightCache = "";
	private static String densityCache = "";
	private static String face = "";
	private static String faceList = "";
	private static String smoothableCache = "";
	private static String stateCache = "";
	private static String vec3 = "";
	private static String vec3b = "";

	public static void onTick() {
		--counter;
		if (counter >= 0) {
			return;
		}
		counter = 20;
		if (FaceList.getInstances() == 0) {
			return;
		}
		lightmapInfo += "\t" + LightmapInfo.getInstances();
		packedLightCache += "\t" + PackedLightCache.getInstances();
		densityCache += "\t" + DensityCache.getInstances();
		face += "\t" + Face.getInstances();
		faceList += "\t" + FaceList.getInstances();
		smoothableCache += "\t" + SmoothableCache.getInstances();
		stateCache += "\t" + StateCache.getInstances();
		vec3 += "\t" + Vec3.getInstances();
		vec3b += "\t" + Vec3b.getInstances();

		Logger logger = LogManager.getLogger("debug pools");
		logger.info("EnablePools: " + ModConfig.enablePools);

//			logger.info("LightmapInfo " + LightmapInfo.getInstances());
//			logger.info("PackedLightCache " + PackedLightCache.getInstances());
//
//			logger.info("DensityCache " + DensityCache.getInstances());
//			logger.info("Face " + Face.getInstances() + " " + Face.getPoolSize());
//			logger.info("FaceList " + FaceList.getInstances() + " " + FaceList.getPoolSize());
//			logger.info("SmoothableCache " + SmoothableCache.getInstances());
//			logger.info("StateCache " + StateCache.getInstances());
//			logger.info("Vec3 " + Vec3.getInstances() + " " + Vec3.getPoolSize());
//			logger.info("Vec3b " + Vec3b.getInstances() + " " + Vec3b.getPoolSize());

		logger.info("LightmapInfo " + lightmapInfo);
		logger.info("PackedLightCache " + packedLightCache);

		logger.info("DensityCache " + densityCache);
		logger.info("Face " + face);
		logger.info("FaceList " + faceList);
		logger.info("SmoothableCache " + smoothableCache);
		logger.info("StateCache " + stateCache);
		logger.info("Vec3 " + vec3);
		logger.info("Vec3b " + vec3b);

	}

}
