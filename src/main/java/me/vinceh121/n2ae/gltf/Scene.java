package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Scene {
	private String name;
	@JsonInclude(value = Include.NON_EMPTY)
	private final List<Integer> nodes = new ArrayList<>();

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<Integer> getNodes() {
		return this.nodes;
	}
}
