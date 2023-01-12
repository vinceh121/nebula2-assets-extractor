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
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<Integer> getJoints() {
		return this.joints;
	}

	public int getSkeleton() {
		return this.skeleton;
	}

	public void setSkeleton(final int skeleton) {
		this.skeleton = skeleton;
	}

	public int getInverseBindMatrices() {
		return this.inverseBindMatrices;
	}

	public void setInverseBindMatrices(final int inverseBindMatrices) {
		this.inverseBindMatrices = inverseBindMatrices;
	}
}
