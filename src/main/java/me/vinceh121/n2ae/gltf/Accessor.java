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

	public int getBufferView() {
		return bufferView;
	}

	public void setBufferView(int bufferView) {
		this.bufferView = bufferView;
	}

	public int getComponentType() {
		return componentType;
	}

	public void setComponentType(int componentType) {
		this.componentType = componentType;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public float[] getMin() {
		return min;
	}

	public void setMin(float[] min) {
		this.min = min;
	}

	public float[] getMax() {
		return max;
	}

	public void setMax(float[] max) {
		this.max = max;
	}

	public enum Type {
		SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4;
	}
}
