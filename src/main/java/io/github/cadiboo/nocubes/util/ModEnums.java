package io.github.cadiboo.nocubes.util;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.renderer.MarchingCubes;
import io.github.cadiboo.nocubes.renderer.OldNoCubes;
import io.github.cadiboo.nocubes.renderer.SurfaceNets;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * Holds all enums and enum-related stuff for this mod
 *
 * @author Cadiboo
 */
public final class ModEnums {

	public enum RenderAlgorithm implements IEnumNameFormattable {

		SURFACE_NETS(SurfaceNets::renderPre, SurfaceNets::renderLayer, SurfaceNets::renderType, SurfaceNets::renderBlock, SurfaceNets::renderPost),

		MARCHING_CUBES(MarchingCubes::renderPre, MarchingCubes::renderLayer, MarchingCubes::renderType, MarchingCubes::renderBlock, MarchingCubes::renderPost),

		OLD_NO_CUBES(OldNoCubes::renderPre, OldNoCubes::renderLayer, OldNoCubes::renderType, OldNoCubes::renderBlock, OldNoCubes::renderPost),

		;

		private final Consumer<RebuildChunkPreEvent> renderPre;
		private final Consumer<RebuildChunkBlockRenderInLayerEvent> renderLayer;
		private final Consumer<RebuildChunkBlockRenderInTypeEvent> renderType;
		private final Consumer<RebuildChunkBlockEvent> renderBlock;
		private final Consumer<RebuildChunkPostEvent> renderPost;

		RenderAlgorithm(final Consumer<RebuildChunkPreEvent> renderPre, final Consumer<RebuildChunkBlockRenderInLayerEvent> renderLayer, final Consumer<RebuildChunkBlockRenderInTypeEvent> renderType, final Consumer<RebuildChunkBlockEvent> renderBlock, final Consumer<RebuildChunkPostEvent> renderPost) {
			this.renderPre = renderPre;
			this.renderLayer = renderLayer;
			this.renderType = renderType;
			this.renderBlock = renderBlock;
			this.renderPost = renderPost;
		}

		public void renderPre(final RebuildChunkPreEvent event) {
			renderPre.accept(event);
		}

		public void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
			renderLayer.accept(event);
		}

		public void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
			renderType.accept(event);
		}

		public void renderBlock(final RebuildChunkBlockEvent event) {
			this.renderBlock.accept(event);
		}

		public void renderPost(final RebuildChunkPostEvent event) {
			renderPost.accept(event);
		}

	}

	/**
	 * provides some default methods for formatting enum names
	 *
	 * @author Cadiboo
	 */
	public interface IEnumNameFormattable {

		/**
		 * Converts the name to lowercase as per {@link java.lang.String#toLowerCase() String.toLowerCase}.
		 */
		default String getNameLowercase() {

			return this.name().toLowerCase();
		}

		/**
		 * Converts the name to uppercase as per {@link java.lang.String#toUpperCase() String.toUpperCase}.
		 */
		default String getNameUppercase() {

			return this.getNameLowercase().toUpperCase();
		}

		/**
		 * Capitalizes the name of the material as per {@link org.apache.commons.lang3.StringUtils#capitalize(String) StringUtils.capitalize}.
		 */
		default String getNameFormatted() {

			return StringUtils.capitalize(this.getNameLowercase());
		}

		// HACK
		String name(); /* not exactly hacky, but this method is provided by enum */

	}

}
