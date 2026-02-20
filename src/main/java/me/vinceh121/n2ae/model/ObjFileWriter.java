package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

public class ObjFileWriter extends AbstractMeshWriter {
	private final PrintWriter out;

	public ObjFileWriter(OutputStream outputStream) {
		super(outputStream);

		this.out = new PrintWriter(outputStream);
	}

	@Override
	public void writeMesh(Mesh mesh) throws IOException {
		final Locale l = Locale.ENGLISH;

		if (mesh.getTypes().contains(VertexType.COORD)) {
			for (final Vertex v : mesh.getVertices()) {
				out.printf(l, "v %.6f %.6f %.6f\n", v.getCoord()[0], v.getCoord()[1], v.getCoord()[2]);
			}
		}
		if (mesh.getTypes().contains(VertexType.UV0)) {
			for (final Vertex v : mesh.getVertices()) {
				out.printf(l, "vt %.6f %.6f\n", v.getUv().get(0)[0], v.getUv().get(0)[1]);
			}
		}
		if (mesh.getTypes().contains(VertexType.NORM)) {
			for (final Vertex v : mesh.getVertices()) {
				out.printf(l, "vn %.6f %.6f %.6f\n", v.getNormal()[0], v.getNormal()[1], v.getNormal()[2]);
			}
		}
		if (mesh.getTypes().contains(VertexType.JOINTS_WEIGHTS)) {
			for (final Vertex v : mesh.getVertices()) {
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

		for (int i = 0; i < mesh.getTriangles().size(); i++) {
			final int[] t = mesh.getTriangles().get(i);
			if (mesh.getTypes().contains(VertexType.UV0) && mesh.getTypes().contains(VertexType.NORM)) {
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
			} else if (mesh.getTypes().contains(VertexType.UV0)) {
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

	@Override
	public void close() throws IOException {
		this.out.close();
	}
}
