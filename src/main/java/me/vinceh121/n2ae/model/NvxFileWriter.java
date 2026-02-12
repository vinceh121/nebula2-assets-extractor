package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import me.vinceh121.n2ae.LEDataOutputStream;

public class NvxFileWriter {
	private final LEDataOutputStream out;

	public NvxFileWriter(final OutputStream out) {
		this.out = new LEDataOutputStream(out);
	}

	public void writeHeaders(final Mesh mesh) throws IOException {
		this.out.writeIntLE(NvxFileReader.MAGIC_NUMBER);

		this.out.writeIntLE(mesh.getCountVertices());
		this.out.writeIntLE(mesh.getCountIndices());
		this.out.writeIntLE(mesh.getCountEdges());
		this.out.writeIntLE(VertexType.getTypes(mesh.getTypes()));
		this.out.writeIntLE(mesh.getDataStart());
		this.out.writeIntLE(mesh.getDataSize());
	}

	public void writeData(final Mesh mesh) throws IOException {
		this.out.write(new byte[mesh.getDataStart() - 7 * 4]);

		for (int i = 0; i < mesh.getCountVertices(); i++) {
			final Vertex vertex = mesh.getVertices().get(i);

			if (mesh.getTypes().contains(VertexType.COORD)) {
				this.writeFloatArrayLE(vertex.getCoord());
			}

			if (mesh.getTypes().contains(VertexType.NORM)) {
				this.writeFloatArrayLE(vertex.getNormal());
			}

			if (mesh.getTypes().contains(VertexType.RGBA)) {
				this.out.writeIntLE(vertex.getColor());
			}

			if (mesh.getTypes().contains(VertexType.UV0)) {
				this.writeFloatArrayLE(vertex.getUv().get(0));
			}
			if (mesh.getTypes().contains(VertexType.UV1)) {
				this.writeFloatArrayLE(vertex.getUv().get(1));
			}
			if (mesh.getTypes().contains(VertexType.UV2)) {
				this.writeFloatArrayLE(vertex.getUv().get(2));
			}
			if (mesh.getTypes().contains(VertexType.UV3)) {
				this.writeFloatArrayLE(vertex.getUv().get(3));
			}

			if (mesh.getTypes().contains(VertexType.JOINTS_WEIGHTS)) {
				this.writeShortArrayLE(vertex.getJointIndices());
				this.writeFloatArrayLE(vertex.getWeights());
			}
		}

		for (int i = 0; i < mesh.getCountEdges(); i++) {
			this.writeShortArrayLE(mesh.getEdges().get(i));
		}

		for (int i = 0; i < mesh.getCountIndices() / 3; i++) {
			this.writeUnsignedShortArrayLE(mesh.getTriangles().get(i));
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


	public void close() throws IOException {
		this.out.close();
	}
}
