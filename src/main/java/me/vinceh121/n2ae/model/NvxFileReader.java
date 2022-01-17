package me.vinceh121.n2ae.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import me.vinceh121.n2ae.LEDataInputStream;

public class NvxFileReader {
	public static final String MAGIC_STRING = "NVX1";
	public static final int MAGIC_NUMBER = 1314281521;

	private final LEDataInputStream in;

	private int countVertices, countIndices, countEdges, dataStart, dataSize;
	private List<VertexType> types;
	private List<Vertex> vertices = new Vector<>();
	private List<int[]> triangles = new Vector<>();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File file = new File(args[0]);
		try (FileInputStream in = new FileInputStream(file);
				FileOutputStream out = new FileOutputStream(new File(file.getPath() + ".obj"))) {
			NvxFileReader r = new NvxFileReader(in);
			r.readAll();
			System.out.println(r.getTypes());
			r.writeObj(out);
		}
	}

	public NvxFileReader(InputStream in) {
		this.in = new LEDataInputStream(in);
	}

	public void writeObj(OutputStream out) {
		this.writeObj(new PrintWriter(out, true));
	}

	public void writeObj(PrintWriter out) {
		for (Vertex v : this.getVertices()) {
			out.printf("v %.6f %.6f %.6f\n", v.getCoord()[0], v.getCoord()[1], v.getCoord()[2]);
		}
		for (Vertex v : this.getVertices()) {
			out.printf("vt %.6f %.6f\n", v.getUv().get(0)[0], v.getUv().get(0)[1]);
		}
		for (Vertex v : this.getVertices()) {
			out.printf("vn %.6f %.6f %.6f\n", v.getNormal()[0], v.getNormal()[1], v.getNormal()[2]);
		}

		for (int i = 0; i < this.getTriangles().size(); i++) {
			int[] t = this.getTriangles().get(i);
			out.printf("f %d/%d %d/%d %d/%d\n",
					t[0] + 1, t[0] + 1, //
					t[1] + 1, t[1] + 1, //
					t[2] + 1, t[2] + 1);
		}
	}

	public void readAll() throws IOException {
		this.readHeader();
		this.readData();
	}

	private void readHeader() throws IOException {
		int magic = in.readIntLE();
		if (magic != MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		this.countVertices = in.readIntLE();
		this.countIndices = in.readIntLE();
		this.countEdges = in.readIntLE();
		int vType = in.readIntLE();
		this.dataStart = in.readIntLE();
		this.dataSize = in.readIntLE();

		this.types = VertexType.getTypes(vType);
	}

	private void readData() throws IOException {
		this.in.skip(this.dataStart - (7 * 4 /* header size */));

		for (int i = 0; i < this.countVertices; i++) {
			Vertex vertex = new Vertex();

			if (this.types.contains(VertexType.COORD)) {
				float x = this.in.readFloatLE();
				float y = this.in.readFloatLE();
				float z = this.in.readFloatLE();
				vertex.setCoord(new float[] { x, y, z });
			}

			if (this.types.contains(VertexType.NORM)) {
				float x = this.in.readFloatLE();
				float y = this.in.readFloatLE();
				float z = this.in.readFloatLE();
				vertex.setNormal(new float[] { x, y, z });
			}

			if (this.types.contains(VertexType.RGBA)) {
				vertex.setColor(this.in.readInt());
			}

			if (this.types.contains(VertexType.UV0)) {
				float x = this.in.readFloatLE();
				float y = this.in.readFloatLE();
				vertex.getUv().set(0, new float[] { x, y });
			}
			if (this.types.contains(VertexType.UV1)) {
				float x = this.in.readFloatLE();
				float y = this.in.readFloatLE();
				vertex.getUv().set(1, new float[] { x, y });
			}
			if (this.types.contains(VertexType.UV2)) {
				float x = this.in.readFloatLE();
				float y = this.in.readFloatLE();
				vertex.getUv().set(2, new float[] { x, y });
			}
			if (this.types.contains(VertexType.UV3)) {
				float x = this.in.readFloatLE();
				float y = this.in.readFloatLE();
				vertex.getUv().set(3, new float[] { x, y });
			}

			if (this.types.contains(VertexType.JOINTS_WEIGHTS)) {
				short ji0 = this.in.readShort();
				short ji1 = this.in.readShort();
				short ji2 = this.in.readShort();
				short ji3 = this.in.readShort();

				vertex.setJointIndices(new int[] { ji0, ji1, ji2, ji3 });

				float w0 = this.in.readFloatLE();
				float w1 = this.in.readFloatLE();
				float w2 = this.in.readFloatLE();
				float w3 = this.in.readFloatLE();

				vertex.setWeights(new float[] { w0, w1, w2, w3 });
			}

			this.vertices.add(vertex);
		}

		for (int i = 0; i < countEdges; i++) {
			// skip edges
			/* short e0 = */this.in.readShort();
			/* short e1 = */this.in.readShort();
			/* short e2 = */this.in.readShort();
			/* short e3 = */this.in.readShort();
		}

		for (int i = 0; i < this.countIndices / 3; i++) {
			int i0 = this.in.readUnsignedShortLE();
			int i1 = this.in.readUnsignedShortLE();
			int i2 = this.in.readUnsignedShortLE();

			this.triangles.add(new int[] { i0, i1, i2 });
		}
	}

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

	public List<VertexType> getTypes() {
		return types;
	}

	public void setTypes(List<VertexType> types) {
		this.types = types;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public List<int[]> getTriangles() {
		return triangles;
	}

	public void setTriangles(List<int[]> triangles) {
		this.triangles = triangles;
	}
}
