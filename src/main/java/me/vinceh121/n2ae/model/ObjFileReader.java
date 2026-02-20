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
			String line;
			
			while ((line = br.readLine()) != null) {
				try (final Scanner scan = new Scanner(line)) {
					scan.useLocale(Locale.ENGLISH);
					final String type = scan.next();

					if ("v".equals(type)) {
						final Vertex v = new Vertex();
						v.setCoord(new float[] {scan.nextFloat(), scan.nextFloat(), scan.nextFloat()});
						mesh.getVertices().add(v);
					}
				}
			}
		}

		return mesh;
	}
}
