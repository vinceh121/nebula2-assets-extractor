package me.vinceh121.n2ae.model;

import java.util.List;
import java.util.Vector;

public class Mesh {
	private int countVertices, countIndices, countEdges, dataStart, dataSize;
	private List<VertexType> types;
	private final List<Vertex> vertices = new Vector<>();
	private final List<int[]> triangles = new Vector<>();
	private final List<short[]> edges = new Vector<>();

	public int getCountVertices() {
		return countVertices;
	}

	public void setCountVertices(int countVertices) {
		this.countVertices = countVertices;
	}

	public int getCountIndices() {
		return countIndices;
	}

	public void setCountIndices(int countIndices) {
		this.countIndices = countIndices;
	}

	public int getCountEdges() {
		return countEdges;
	}

	public void setCountEdges(int countEdges) {
		this.countEdges = countEdges;
	}

	public int getDataStart() {
		return dataStart;
	}

	public void setDataStart(int dataStart) {
		this.dataStart = dataStart;
	}

	public int getDataSize() {
		return dataSize;
	}

	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	public List<VertexType> getTypes() {
		return types;
	}

	public void setTypes(List<VertexType> types) {
		this.types = types;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public List<int[]> getTriangles() {
		return triangles;
	}

	public List<short[]> getEdges() {
		return edges;
	}
}
