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
		return this.mesh;
	}

	public void setMesh(final int mesh) {
		this.mesh = mesh;
	}

	public int getSkin() {
		return this.skin;
	}

	public void setSkin(final int skin) {
		this.skin = skin;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public float[] getRotation() {
		return this.rotation;
	}

	public void setRotation(final float[] rotation) {
		this.rotation = rotation;
	}

	public float[] getTranslation() {
		return this.translation;
	}

	public void setTranslation(final float[] translation) {
		this.translation = translation;
	}

	public float[] getScale() {
		return this.scale;
	}

	public void setScale(final float[] scale) {
		this.scale = scale;
	}

	public List<Integer> getChildren() {
		return this.children;
	}
}
