package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

public class Mesh {
	private String name;
	private final List<Primitive> primitives = new ArrayList<>();

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<Primitive> getPrimitives() {
		return this.primitives;
	}
}
