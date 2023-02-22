package me.vinceh121.n2ae.gltf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class Accessor {
	public static final int BYTE = 5120, UNSIGNED_BYTE = 5121, SHORT = 5122, UNSIGNED_SHORT = 5123, UNSIGNED_INT = 5125,
			FLOAT = 5126;

	private int bufferView, componentType, count;
	private Type type;
	private float[] min;
	private float[] max;
	private boolean normalized;

	public int getBufferView() {
		return this.bufferView;
	}

	public void setBufferView(final int bufferView) {
		this.bufferView = bufferView;
	}

	public int getComponentType() {
		return this.componentType;
	}

	public void setComponentType(final int componentType) {
		this.componentType = componentType;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(final int count) {
		this.count = count;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public float[] getMin() {
		return this.min;
	}

	public void setMin(final float[] min) {
		this.min = min;
	}

	public float[] getMax() {
		return this.max;
	}

	public void setMax(final float[] max) {
		this.max = max;
	}

	public boolean isNormalized() {
		return this.normalized;
	}

	public void setNormalized(final boolean normalized) {
		this.normalized = normalized;
	}

	public static int getComponentTypeLength(int compType) {
		switch (compType) {
		case Accessor.UNSIGNED_INT:
		case Accessor.FLOAT:
			return 4;
		case Accessor.UNSIGNED_SHORT:
		case Accessor.SHORT:
			return 2;
		case Accessor.UNSIGNED_BYTE:
		case Accessor.BYTE:
			return 1;
		default:
			throw new IllegalArgumentException("Invalid accessor componentType " + compType);
		}
	}

	public enum Type {
		SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4;
	}
}
