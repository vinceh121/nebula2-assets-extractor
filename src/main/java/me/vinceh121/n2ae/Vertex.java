package me.vinceh121.n2ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Vertex {
	private Vector3 coord, normal;
	private List<Vector2> uv = new ArrayList<>(4);
	private int color;
	/**
	 * grrrr no Vector4
	 */
	private int[] jointIndices;
	private float[] weights;

	public Vertex() {
		for (int i = 0; i < 4; i++) {
			this.uv.add(new Vector2());
		}
	}

	public Vector3 getCoord() {
		return coord;
	}

	public void setCoord(Vector3 coord) {
		this.coord = coord;
	}

	public Vector3 getNormal() {
		return normal;
	}

	public void setNormal(Vector3 normal) {
		this.normal = normal;
	}

	public List<Vector2> getUv() {
		return uv;
	}

	public void setUv(List<Vector2> uv) {
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
		return "Vertex [coord=" + coord + ", normal=" + normal + ", uv=" + uv + ", color=" + color + ", weights="
				+ Arrays.toString(weights) + "]";
	}
}
