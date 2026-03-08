package me.vinceh121.n2ae.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Scanner;

public class ObjFileReader extends AbstractMeshReader {
	public ObjFileReader(InputStream inputStream) {
		super(inputStream);
	}

	@Override
	public Mesh readMesh() throws IOException {
		final Mesh mesh = new Mesh();
		int indicesCount = 0;

		try (final BufferedReader br = new BufferedReader(new InputStreamReader(this.inputStream))) {
			int vertexIndex = 0;
			String lastType = null;
			String line;

			while ((line = br.readLine()) != null) {
				try (final Scanner scan = new Scanner(line)) {
					scan.useLocale(Locale.ENGLISH);
					final String type = scan.next();

					if (!type.equals(lastType)) {
						lastType = type;
						vertexIndex = 0;
					}

					if ("v".equals(type)) {
						mesh.getTypes().add(VertexType.COORD);
						final Vertex v = new Vertex();
						v.setCoord(new float[] { scan.nextFloat(), scan.nextFloat(), scan.nextFloat() });
						mesh.getVertices().add(v);
					} else if ("vt".equals(type)) {
						mesh.getTypes().add(VertexType.UV0);
						final Vertex v = mesh.getVertices().get(vertexIndex);
						v.getUv().add(new float[] { scan.nextFloat(), scan.nextFloat() });
					} else if ("vn".equals(type)) {
						mesh.getTypes().add(VertexType.NORM);
						final Vertex v = mesh.getVertices().get(vertexIndex);
						v.setNormal(new float[] { scan.nextFloat(), scan.nextFloat(), scan.nextFloat() });
					} else if ("jw4".equals(type)) {
						mesh.getTypes().add(VertexType.JOINTS_WEIGHTS);
						final Vertex v = mesh.getVertices().get(vertexIndex);
						final short[] jointIndices = new short[4];
						final float[] weights = new float[4];

						for (int i = 0; i < 4; i++) {
							jointIndices[i] = scan.nextShort();
							weights[i] = scan.nextFloat();
						}

						v.setJointIndices(jointIndices);
						v.setWeights(weights);
					} else if ("f".equals(type)) {
						final int[] indexes = scan.tokens().mapToInt(str -> {
							if (str.contains("/")) {
								// consider all indexes to be the same, as NVX doesn't support otherwise
								return Integer.parseInt(str.split("/")[0]);
							}

							return Integer.parseInt(str);
						}).toArray();

						mesh.getTriangles().add(indexes);
						indicesCount++;
					}

					vertexIndex++;
				}
			}
		}

		if (mesh.getTypes().contains(VertexType.COORD)) {
			mesh.setCountVertices(mesh.getVertices().size());
		}

		mesh.setCountIndices(indicesCount);

		return mesh;
	}
}
