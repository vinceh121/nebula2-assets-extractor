package me.vinceh121.n2ae.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vertex {
	private float[] coord, normal;
	private List<float[]> uv = new ArrayList<>(4);
	private int color;
	/**
	 * grrrr no Vector4
	 */
	private int[] jointIndices;
	private float[] weights;

	public Vertex() {
		for (int i = 0; i < 4; i++) {
			this.uv.add(new float[2]);
		}
	}

	public float[] getCoord() {
		return coord;
	}

	public void setCoord(float[] coord) {
		this.coord = coord;
	}

	public float[] getNormal() {
		return normal;
	}

	public void setNormal(float[] normal) {
		this.normal = normal;
	}

	public List<float[]> getUv() {
		return uv;
	}

	public void setUv(List<float[]> uv) {
		this.uv = uv;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int[] getJointIndices() {
		return jointIndices;
	}

	public void setJointIndices(int[] jointIndices) {
		this.jointIndices = jointIndices;
	}

	public float[] getWeights() {
		return weights;
	}

	public void setWeights(float[] weights) {
		this.weights = weights;
	}

	@Override
	public String toString() {
		return "Vertex [coord=" + Arrays.toString(coord) + ", normal=" + Arrays.toString(normal) + ", uv=" + uv
				+ ", color=" + color + ", jointIndices=" + Arrays.toString(jointIndices) + ", weights="
				+ Arrays.toString(weights) + "]";
	}
}
