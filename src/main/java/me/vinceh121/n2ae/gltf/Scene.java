package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

public class Scene {
	private String name;
	private final List<Integer> nodes = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getNodes() {
		return nodes;
	}
}
