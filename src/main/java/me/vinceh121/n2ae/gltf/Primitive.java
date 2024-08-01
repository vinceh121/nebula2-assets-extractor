package me.vinceh121.n2ae.gltf;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Primitive {
	private final Map<String, Integer> attributes = new HashMap<>();
	private int indices = -1;
	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int material = -1;

	public int getIndices() {
		return this.indices;
	}

	public void setIndices(final int indices) {
		this.indices = indices;
	}

	public Map<String, Integer> getAttributes() {
		return this.attributes;
	}

	public int getMaterial() {
		return this.material;
	}

	public void setMaterial(final int material) {
		this.material = material;
	}
}
