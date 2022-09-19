package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import me.vinceh121.n2ae.LEDataOutputStream;

public class NvxFileWriter {
	private final LEDataOutputStream out;

	private int countVertices, countIndices, countEdges, dataStart, dataSize;
	private List<VertexType> types;
	private List<Vertex> vertices = new Vector<>();
	private List<int[]> triangles = new Vector<>();
	private List<short[]> edges = new Vector<>();

	public NvxFileWriter(final OutputStream out) {
		this.out = new LEDataOutputStream(out);
	}

	public void writeHeaders() throws IOException {
		this.out.writeIntLE(NvxFileReader.MAGIC_NUMBER);

		this.out.writeIntLE(this.countVertices);
		this.out.writeIntLE(this.countIndices);
		this.out.writeIntLE(this.countEdges);
		this.out.writeIntLE(VertexType.getTypes(this.types));
		this.out.writeIntLE(this.dataStart);
		this.out.writeIntLE(this.dataSize);
	}

	public void writeData() throws IOException {
		this.out.write(new byte[this.dataStart - 7 * 4]);

		for (int i = 0; i < this.countVertices; i++) {
			final Vertex vertex = this.vertices.get(i);

			if (this.types.contains(VertexType.COORD)) {
				this.writeFloatArrayLE(vertex.getCoord());
			}

			if (this.types.contains(VertexType.NORM)) {
				this.writeFloatArrayLE(vertex.getNormal());
			}

			if (this.types.contains(VertexType.RGBA)) {
				this.out.writeIntLE(vertex.getColor());
			}

			if (this.types.contains(VertexType.UV0)) {
				this.writeFloatArrayLE(vertex.getUv().get(0));
			}
			if (this.types.contains(VertexType.UV1)) {
				this.writeFloatArrayLE(vertex.getUv().get(1));
			}
			if (this.types.contains(VertexType.UV2)) {
				this.writeFloatArrayLE(vertex.getUv().get(2));
			}
			if (this.types.contains(VertexType.UV3)) {
				this.writeFloatArrayLE(vertex.getUv().get(3));
			}

			if (this.types.contains(VertexType.JOINTS_WEIGHTS)) {
				this.writeShortArrayLE(vertex.getJointIndices());
				this.writeFloatArrayLE(vertex.getWeights());
			}
		}

		for (int i = 0; i < this.countEdges; i++) {
			this.writeShortArrayLE(this.edges.get(i));
		}

		for (int i = 0; i < this.countIndices / 3; i++) {
			this.writeUnsignedShortArrayLE(this.triangles.get(i));
		}
	}

	private void writeUnsignedShortArrayLE(final int[] a) throws IOException {
		for (final int i : a) {
			this.out.writeUnsignedShortLE(i);
		}
	}

	private void writeShortArrayLE(final short[] a) throws IOException {
		for (final short s : a) {
			this.out.writeShortLE(s);
		}
	}

	private void writeFloatArrayLE(final float[] a) throws IOException {
		for (final float f : a) {
			this.out.writeFloatLE(f);
		}
	}

	public int getCountVertices() {
		return this.countVertices;
	}

	public void setCountVertices(final int countVertices) {
		this.countVertices = countVertices;
	}

	public int getCountIndices() {
		return this.countIndices;
	}

	public void setCountIndices(final int countIndices) {
		this.countIndices = countIndices;
	}

	public int getCountEdges() {
		return this.countEdges;
	}

	public void setCountEdges(final int countEdges) {
		this.countEdges = countEdges;
	}

	public int getDataStart() {
		return this.dataStart;
	}

	public void setDataStart(final int dataStart) {
		this.dataStart = dataStart;
	}

	public int getDataSize() {
		return this.dataSize;
	}

	public void setDataSize(final int dataSize) {
		this.dataSize = dataSize;
	}

	public List<VertexType> getTypes() {
		return this.types;
	}

	public void setTypes(final List<VertexType> types) {
		this.types = types;
	}

	public List<Vertex> getVertices() {
		return this.vertices;
	}

	public void setVertices(final List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public List<int[]> getTriangles() {
		return this.triangles;
	}

	public void setTriangles(final List<int[]> triangles) {
		this.triangles = triangles;
	}

	public List<short[]> getEdges() {
		return this.edges;
	}

	public void setEdges(final List<short[]> edges) {
		this.edges = edges;
	}

	public void close() throws IOException {
		this.out.close();
	}
}
