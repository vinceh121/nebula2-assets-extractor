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
	private short[] jointIndices;
	private float[] weights;

	public Vertex() {
		for (int i = 0; i < 4; i++) {
			this.uv.add(new float[2]);
		}
	}

	public float[] getCoord() {
		return this.coord;
	}

	public void setCoord(final float[] coord) {
		this.coord = coord;
	}

	public float[] getNormal() {
		return this.normal;
	}

	public void setNormal(final float[] normal) {
		this.normal = normal;
	}

	public List<float[]> getUv() {
		return this.uv;
	}

	public void setUv(final List<float[]> uv) {
		this.uv = uv;
	}

	public int getColor() {
		return this.color;
	}

	public void setColor(final int color) {
		this.color = color;
	}

	public short[] getJointIndices() {
		return this.jointIndices;
	}

	public void setJointIndices(final short[] jointIndices) {
		this.jointIndices = jointIndices;
	}

	public float[] getWeights() {
		return this.weights;
	}

	public void setWeights(final float[] weights) {
		this.weights = weights;
	}

	@Override
	public String toString() {
		return "Vertex [coord=" + Arrays.toString(this.coord) + ", normal=" + Arrays.toString(this.normal) + ", uv="
				+ this.uv + ", color=" + this.color + ", jointIndices=" + Arrays.toString(this.jointIndices)
				+ ", weights=" + Arrays.toString(this.weights) + "]";
	}
}
