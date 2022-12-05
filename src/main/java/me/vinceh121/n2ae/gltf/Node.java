package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class Node {
	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int mesh = -1;
	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int skin = -1;
	private String name;
	private float[] rotation, translation, scale;
	@JsonInclude(value = Include.NON_EMPTY)
	private final List<Integer> children = new ArrayList<>();

	public int getMesh() {
		return mesh;
	}

	public void setMesh(int mesh) {
		this.mesh = mesh;
	}

	public int getSkin() {
		return skin;
	}

	public void setSkin(int skin) {
		this.skin = skin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float[] getRotation() {
		return rotation;
	}

	public void setRotation(float[] rotation) {
		this.rotation = rotation;
	}

	public float[] getTranslation() {
		return translation;
	}

	public void setTranslation(float[] translation) {
		this.translation = translation;
	}

	public float[] getScale() {
		return scale;
	}

	public void setScale(float[] scale) {
		this.scale = scale;
	}

	public List<Integer> getChildren() {
		return children;
	}
}
