package io.github.cadiboo.nocubes.util;

import javax.annotation.Nonnull;

public class Face {

	@Nonnull
	private Vec3 vertex0;
	@Nonnull
	private Vec3 vertex1;
	@Nonnull
	private Vec3 vertex2;
	@Nonnull
	private Vec3 vertex3;

	private Face(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2, @Nonnull final Vec3 vertex3) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
	}

	@Nonnull
	public Vec3 getVertex0() {
		return vertex0;
	}

	@Nonnull
	public Vec3 getVertex1() {
		return vertex1;
	}

	@Nonnull
	public Vec3 getVertex2() {
		return vertex2;
	}

	@Nonnull
	public Vec3 getVertex3() {
		return vertex3;
	}

	@Nonnull
	public static Face retain(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2, @Nonnull final Vec3 vertex3) {
		return new Face(vertex0, vertex1, vertex2, vertex3);
	}

	@Nonnull
	public static Face retain(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2) {
		return retain(vertex0.clone(), vertex0, vertex1, vertex2);
	}

	public void release() {

	}

}
