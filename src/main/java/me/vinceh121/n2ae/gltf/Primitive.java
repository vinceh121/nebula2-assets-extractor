package me.vinceh121.n2ae.gltf;

import java.util.HashMap;
import java.util.Map;

public class Primitive {
	private final Map<String, Integer> attributes = new HashMap<>();
	private int indices = -1;

	public int getIndices() {
		return this.indices;
	}

	public void setIndices(final int indices) {
		this.indices = indices;
	}

	public Map<String, Integer> getAttributes() {
		return this.attributes;
	}
}
