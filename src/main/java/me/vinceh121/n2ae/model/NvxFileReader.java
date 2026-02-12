package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NvxFileReader {
	public static final String MAGIC_STRING = "NVX1";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NvxFileReader.MAGIC_STRING);

	private final LEDataInputStream in;
	private Mesh mesh;

	public NvxFileReader(final InputStream in) {
		this.in = new LEDataInputStream(in);
	}

	public void writeObj(final OutputStream out) {
		this.writeObj(new PrintWriter(out, true));
	}

	public void writeObj(final PrintWriter out) {
		final Locale l = Locale.ENGLISH;

		if (this.mesh.getTypes().contains(VertexType.COORD)) {
			for (final Vertex v : this.mesh.getVertices()) {
				out.printf(l, "v %.6f %.6f %.6f\n", v.getCoord()[0], v.getCoord()[1], v.getCoord()[2]);
			}
		}
		if (this.mesh.getTypes().contains(VertexType.UV0)) {
			for (final Vertex v : this.mesh.getVertices()) {
				out.printf(l, "vt %.6f %.6f\n", v.getUv().get(0)[0], v.getUv().get(0)[1]);
			}
		}
		if (this.mesh.getTypes().contains(VertexType.NORM)) {
			for (final Vertex v : this.mesh.getVertices()) {
				out.printf(l, "vn %.6f %.6f %.6f\n", v.getNormal()[0], v.getNormal()[1], v.getNormal()[2]);
			}
		}
		if (this.mesh.getTypes().contains(VertexType.JOINTS_WEIGHTS)) {
			for (final Vertex v : this.mesh.getVertices()) {
				out.printf(l,
						"jw4 %d %.6f %d %.6f %d %.6f %d %.6f\n",
						v.getJointIndices()[0],
						v.getWeights()[0],
						v.getJointIndices()[1],
						v.getWeights()[1],
						v.getJointIndices()[2],
						v.getWeights()[2],
						v.getJointIndices()[3],
						v.getWeights()[3]);
			}
		}

		for (int i = 0; i < this.mesh.getTriangles().size(); i++) {
			final int[] t = this.mesh.getTriangles().get(i);
			if (this.mesh.getTypes().contains(VertexType.UV0) && this.mesh.getTypes().contains(VertexType.NORM)) {
				out.printf(l,
						"f %d/%d/%d %d/%d/%d %d/%d/%d\n",
						t[0] + 1,
						t[0] + 1,
						t[0] + 1, //
						t[1] + 1,
						t[1] + 1,
						t[1] + 1, //
						t[2] + 1,
						t[2] + 1,
						t[2] + 1);
			} else if (this.mesh.getTypes().contains(VertexType.UV0)) {
				out.printf(l,
						"f %d/%d %d/%d %d/%d\n",
						t[0] + 1,
						t[0] + 1, //
						t[1] + 1,
						t[1] + 1, //
						t[2] + 1,
						t[2] + 1);
			} else {
				out.printf(l, "f %d %d %d\n", t[0] + 1, t[1] + 1, t[2] + 1);
			}
		}
	}

	public void readAll() throws IOException {
		this.mesh = new Mesh();
		
		this.readHeader();
		this.readData();
	}

	private void readHeader() throws IOException {
		final int magic = this.in.readIntLE();
		if (magic != NvxFileReader.MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		this.mesh.setCountVertices(this.in.readIntLE());
		this.mesh.setCountIndices(this.in.readIntLE());
		this.mesh.setCountEdges(this.in.readIntLE());
		final int vType = this.in.readIntLE();
		this.mesh.setDataStart(this.in.readIntLE());
		this.mesh.setDataSize(this.in.readIntLE());

		this.mesh.setTypes(VertexType.getTypes(vType));
	}

	private void readData() throws IOException {
		this.in.skip(this.mesh.getDataStart() - 7 * 4);

		for (int i = 0; i < this.mesh.getCountVertices(); i++) {
			final Vertex vertex = new Vertex();

			if (this.mesh.getTypes().contains(VertexType.COORD)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				final float z = this.in.readFloatLE();
				vertex.setCoord(new float[] { x, y, z });
			}

			if (this.mesh.getTypes().contains(VertexType.NORM)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				final float z = this.in.readFloatLE();
				vertex.setNormal(new float[] { x, y, z });
			}

			if (this.mesh.getTypes().contains(VertexType.RGBA)) {
				vertex.setColor(this.in.readInt());
			}

			if (this.mesh.getTypes().contains(VertexType.UV0)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(0, new float[] { x, y });
			}
			if (this.mesh.getTypes().contains(VertexType.UV1)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(1, new float[] { x, y });
			}
			if (this.mesh.getTypes().contains(VertexType.UV2)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(2, new float[] { x, y });
			}
			if (this.mesh.getTypes().contains(VertexType.UV3)) {
				final float x = this.in.readFloatLE();
				final float y = this.in.readFloatLE();
				vertex.getUv().set(3, new float[] { x, y });
			}

			if (this.mesh.getTypes().contains(VertexType.JOINTS_WEIGHTS)) {
				final short ji0 = this.in.readShortLE();
				final short ji1 = this.in.readShortLE();
				final short ji2 = this.in.readShortLE();
				final short ji3 = this.in.readShortLE();

				vertex.setJointIndices(new short[] { ji0, ji1, ji2, ji3 });

				final float w0 = this.in.readFloatLE();
				final float w1 = this.in.readFloatLE();
				final float w2 = this.in.readFloatLE();
				final float w3 = this.in.readFloatLE();

				vertex.setWeights(new float[] { w0, w1, w2, w3 });
			}

			this.mesh.getVertices().add(vertex);
		}

		for (int i = 0; i < this.mesh.getCountEdges(); i++) {
			final short e0 = this.in.readShortLE();
			final short e1 = this.in.readShortLE();
			final short e2 = this.in.readShortLE();
			final short e3 = this.in.readShortLE();

			this.mesh.getEdges().add(new short[] { e0, e1, e2, e3 });
		}

		for (int i = 0; i < this.mesh.getCountIndices() / 3; i++) {
			final int i0 = this.in.readUnsignedShortLE();
			final int i1 = this.in.readUnsignedShortLE();
			final int i2 = this.in.readUnsignedShortLE();

			this.mesh.getTriangles().add(new int[] { i0, i1, i2 });
		}
	}
	
	public Mesh getMesh() {
		return this.mesh;
	}


	public void close() throws IOException {
		this.in.close();
	}
}
