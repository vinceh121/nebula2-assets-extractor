package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

public class Skin {
	private final List<Integer> joints = new ArrayList<>();
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getJoints() {
		return joints;
	}
}
