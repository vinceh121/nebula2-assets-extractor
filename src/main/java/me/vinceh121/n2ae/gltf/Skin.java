package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Skin {
	private final List<Integer> joints = new ArrayList<>();
	private String name;
	@JsonInclude(valueFilter = NotMinusOne.class)
	private int skeleton;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getJoints() {
		return joints;
	}

	public int getSkeleton() {
		return skeleton;
	}

	public void setSkeleton(int skeleton) {
		this.skeleton = skeleton;
	}
}
