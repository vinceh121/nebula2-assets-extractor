package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.InputStream;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NvxFileReader extends AbstractMeshReader {
	public static final String MAGIC_STRING = "NVX1";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NvxFileReader.MAGIC_STRING);

	private final LEDataInputStream in;
	private Mesh mesh;

	public NvxFileReader(final InputStream in) {
		super(in);
		this.in = new LEDataInputStream(in);
	}

	@Override
	public Mesh readMesh() throws IOException {
		this.readAll();

		return this.mesh;
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

		this.mesh.getTypes().addAll(VertexType.getTypes(vType));
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
