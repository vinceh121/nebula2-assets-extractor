package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NvxFileReader {
	public static final String MAGIC_STRING = "NVX1";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NvxFileReader.MAGIC_STRING);

	private final LEDataInputStream in;

	private int countVertices, countIndices, countEdges, dataStart, dataSize;
	private List<VertexType> types;
	private List<Vertex> vertices = new Vector<>();
	private List<int[]> triangles = new Vector<>();

	public NvxFileReader(final InputStream in) {
		this.in = new LEDataInputStream(in);
	}

	public void writeObj(final OutputStream out) {
		this.writeObj(new PrintWriter(out, true));
	}

	public void writeObj(final PrintWriter out) {
		if (this.types.contains(VertexType.COORD)) {
			for (final Vertex v : this.getVertices()) {
				out.printf("v %.6f %.6f %.6f\n", v.getCoord()[0], v.getCoord()[1], v.getCoord()[2]);
			}
		}
		if (this.types.contains(VertexType.UV0)) {
			for (final Vertex v : this.getVertices()) {
				out.printf("vt %.6f %.6f\n", v.getUv().get(0)[0], v.getUv().get(0)[1]);
			}
		}
		if (this.types.contains(VertexType.NORM)) {
			for (final Vertex v : this.getVertices()) {
				out.printf("vn %.6f %.6f %.6f\n", v.getNormal()[0], v.getNormal()[1], v.getNormal()[2]);
			}
		}

		for (int i = 0; i < this.getTriangles().size(); i++) {
			final int[] t = this.getTriangles().get(i);
			if (this.types.contains(VertexType.UV0)) {
				out.printf("f %d/%d %d/%d %d/%d\n", t[0] + 1, t[0] + 1, //
						t[1] + 1, t[1] + 1, //
						t[2] + 1, t[2] + 1);
			} else {
				out.printf("f %d %d %d\n", t[0] + 1, t[1] + 1, t[2] + 1);
			}
		}
	}

	public void readAll() throws IOException {
		this.readHeader();
		this.readData();
	}

	private void readHeader() throws IOException {
		final int magic = this.in.readIntLE();
		if (magic != NvxFileReader.MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		this.countVertices = this.in.readIntLE();
		this.countIndices = this.in.readIntLE();
		this.countEdges = this.in.readIntLE();
		final int vType = this.in.readIntLE();
		this.dataStart = this.in.readIntLE();
		this.dataSize = this.in.readIntLE();

		this.types = VertexType.getTypes(vType);
	}

	private void readData() throws IOException {
		this.in.skip(this.dataStart - 7 * 4);

		for (int i = 0; i < this.countVertices; i++) {
			final Vertex vertex = new Vertex();

			if (this.types.contains(VertexType.COORD)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				final float z = this.in.readFloatLE();
				vertex.setCoord(new float[] { x, y, z });
			}

			if (this.types.contains(VertexType.NORM)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				final float z = this.in.readFloatLE();
				vertex.setNormal(new float[] { x, y, z });
			}

			if (this.types.contains(VertexType.RGBA)) {
				vertex.setColor(this.in.readInt());
			}

			if (this.types.contains(VertexType.UV0)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(0, new float[] { x, y });
			}
			if (this.types.contains(VertexType.UV1)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(1, new float[] { x, y });
			}
			if (this.types.contains(VertexType.UV2)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(2, new float[] { x, y });
			}
			if (this.types.contains(VertexType.UV3)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(3, new float[] { x, y });
			}

			if (this.types.contains(VertexType.JOINTS_WEIGHTS)) {
				final short ji0 = this.in.readShort();
				final short ji1 = this.in.readShort();
				final short ji2 = this.in.readShort();
				final short ji3 = this.in.readShort();

				vertex.setJointIndices(new int[] { ji0, ji1, ji2, ji3 });

				final float w0 = this.in.readFloatLE();
				final float w1 = this.in.readFloatLE();
				final float w2 = this.in.readFloatLE();
				final float w3 = this.in.readFloatLE();

				vertex.setWeights(new float[] { w0, w1, w2, w3 });
			}

			this.vertices.add(vertex);
		}

		for (int i = 0; i < this.countEdges; i++) {
			// skip edges
			/* short e0 = */this.in.readShort();
			/* short e1 = */this.in.readShort();
			/* short e2 = */this.in.readShort();
			/* short e3 = */this.in.readShort();
		}

		for (int i = 0; i < this.countIndices / 3; i++) {
			final int i0 = this.in.readUnsignedShortLE();
			final int i1 = this.in.readUnsignedShortLE();
			final int i2 = this.in.readUnsignedShortLE();

			this.triangles.add(new int[] { i0, i1, i2 });
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
}
