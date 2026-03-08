package me.vinceh121.n2ae.model;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Mesh {
	public static final int DEFAULT_DATA_START = 0x1C;
	private int countVertices, countIndices, countEdges, dataStart = DEFAULT_DATA_START, dataSize;
	private final Set<VertexType> types = EnumSet.noneOf(VertexType.class);
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

	public Set<VertexType> getTypes() {
		return types;
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
