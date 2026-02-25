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
						final Vertex v = new Vertex();
						v.setCoord(new float[] {scan.nextFloat(), scan.nextFloat(), scan.nextFloat()});
						mesh.getVertices().add(v);
					} else if ("vt".equals(type)) {
						final Vertex v = mesh.getVertices().get(vertexIndex);
						v.getUv().add(new float[] {scan.nextFloat(), scan.nextFloat()});
					} else if ("vn".equals(type)) {
						final Vertex v = mesh.getVertices().get(vertexIndex);
						v.setNormal(new float[] {scan.nextFloat(), scan.nextFloat(), scan.nextFloat()});
					} else if ("f".equals(type)) {
						final int[] indexes = scan.tokens().mapToInt(str -> {
							if (str.contains("/")) {
								// consider all indexes to be the same, as NVX doesn't support otherwise
								return Integer.parseInt(str.split("/")[0]);
							}
							
							return Integer.parseInt(str);
						}).toArray();

						mesh.getTriangles().add(indexes);
					}
					
					vertexIndex++;
				}
			}
		}

		return mesh;
	}
}
