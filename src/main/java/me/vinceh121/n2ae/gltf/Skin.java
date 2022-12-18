package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Skin {
	private final List<Integer> joints = new ArrayList<>();
	private String name;
	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int skeleton = -1;
	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int inverseBindMatrices = -1;

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

	public int getInverseBindMatrices() {
		return inverseBindMatrices;
	}

	public void setInverseBindMatrices(int inverseBindMatrices) {
		this.inverseBindMatrices = inverseBindMatrices;
	}
}
