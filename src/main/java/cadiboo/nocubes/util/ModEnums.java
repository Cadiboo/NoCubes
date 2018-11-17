package cadiboo.nocubes.util;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * Holds all enums and enum-related stuff for this mod
 *
 * @author Cadiboo
 */
public final class ModEnums {

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

	public static enum RenderAlgorithm implements IEnumNameFormattable {

		SURFACE_NETS(

			(event) -> ModUtil.renderChunkSurfaceNets(event),

			(event) -> ModUtil.renderBlockSurfaceNets(event)

		),

		MARCHING_CUBES(

			(event) -> ModUtil.renderChunkMarchingCubes(event),

			(event) -> ModUtil.renderBlockMarchingCubes(event)

		),

		;

		private final Consumer<RebuildChunkPreEvent>   renderChunk;
		private final Consumer<RebuildChunkBlockEvent> renderBlock;

		private RenderAlgorithm(final Consumer<RebuildChunkPreEvent> renderChunk, final Consumer<RebuildChunkBlockEvent> renderBlock) {

			this.renderChunk = renderChunk;
			this.renderBlock = renderBlock;
		}

		public void renderChunk(final RebuildChunkPreEvent event) {

			this.renderChunk.accept(event);
		}

		public void renderBlock(final RebuildChunkBlockEvent event) {

			this.renderBlock.accept(event);
		}

	}

	public static enum RenderType implements IEnumNameFormattable {

		CHUNK,

		BLOCK,

		;

	}

}
