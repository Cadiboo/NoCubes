package io.github.cadiboo.nocubes.client.render.struct;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;

public final /* inline record */ class Texture {
	public float u0;
	public float v0;
	public float u1;
	public float v1;
	public float u2;
	public float v2;
	public float u3;
	public float v3;

	public void unpackFromQuad(BakedQuad quad) {
		int formatSize = getFormatSize(quad);
		int[] vertexData = quad.getVertices();
		// Quads are packed xyz|argb|u|v|ts
		u0 = Float.intBitsToFloat(vertexData[4]);
		v0 = Float.intBitsToFloat(vertexData[5]);
		u1 = Float.intBitsToFloat(vertexData[formatSize + 4]);
		v1 = Float.intBitsToFloat(vertexData[formatSize + 5]);
		u2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
		v2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
		u3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
		v3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
	}

	private static int getFormatSize(BakedQuad quad) {
		return DefaultVertexFormats.BLOCK.getIntegerSize();
	}

	public void rearrangeForDirection(Direction direction) {
		switch (direction) {
			case NORTH:
			case EAST:
				break;
			case DOWN:
			case SOUTH:
			case WEST: {
				float u0 = this.u0;
				float v0 = this.v0;
				float u1 = this.u1;
				float v1 = this.v1;
				float u2 = this.u2;
				float v2 = this.v2;
				float u3 = this.u3;
				float v3 = this.v3;

				this.u0 = u3;
				this.v0 = v3;
				this.u1 = u0;
				this.v1 = v0;
				this.u2 = u1;
				this.v2 = v1;
				this.u3 = u2;
				this.v3 = v2;
				break;
			}
			case UP: {
				float u0 = this.u0;
				float v0 = this.v0;
				float u1 = this.u1;
				float v1 = this.v1;
				float u2 = this.u2;
				float v2 = this.v2;
				float u3 = this.u3;
				float v3 = this.v3;

				this.u0 = u2;
				this.v0 = v2;
				this.u1 = u3;
				this.v1 = v3;
				this.u2 = u0;
				this.v2 = v0;
				this.u3 = u1;
				this.v3 = v1;
				break;
			}
			default:
				throw new IllegalStateException("Unexpected value: " + direction);
		}
	}

}
