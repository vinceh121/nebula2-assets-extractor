package me.vinceh121.n2ae.gltf;

public class Accessor {
	public static final int BYTE = 5120, UNSIGNED_BYTE = 5121, SHORT = 5122, UNSIGNED_SHORT = 5123, UNSIGNED_INT = 5125,
			FLOAT = 5126;

	private int bufferView, componentType, count;
	private Type type;

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

	public enum Type {
		SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4;
	}
}
