package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import me.vinceh121.n2ae.model.IMeshReader;
import me.vinceh121.n2ae.model.IMeshWriter;
import me.vinceh121.n2ae.model.Mesh;
import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.model.NvxFileWriter;
import me.vinceh121.n2ae.model.ObjFileReader;
import me.vinceh121.n2ae.model.ObjFileWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "model", description = { "Convert an NVX file to a wavefront OBJ and vice-versa" })
public class CmdModel implements Callable<Integer> {
	@Option(names = { "-o", "--output" })
	private File outputFile;

	@Option(names = { "-f", "--output-format" })
	private Format outputFormat;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-g", "--input-format" })
	private Format inputFormat;

	@Option(names = { "--info" }, description = { "list infos about the model, produces no output" })
	private boolean info;

	@Override
	public Integer call() throws Exception {
		if (this.outputFile == null) {
			this.outputFile = new File("./" + (this.inputFile.getName().endsWith(".nvx")
					? this.inputFile.getName().substring(0, this.inputFile.getName().length() - 4)
					: this.inputFile.getName()) + ".obj");
		}

		try (FileInputStream is = new FileInputStream(this.inputFile);
				FileOutputStream os = new FileOutputStream(this.outputFile);
				IMeshReader reader = this.buildReader(is);
				IMeshWriter writer = this.buildWriter(os)) {
			final Mesh mesh = reader.readMesh();

			if (this.info) {
				System.out.println("Vertices: " + mesh.getCountVertices());
				System.out.println("Edges: " + mesh.getCountEdges());
				System.out.println("Indices: " + mesh.getCountIndices());
				System.out.println("Model size: " + mesh.getDataSize());
				System.out.println("Model offset: 0x" + Integer.toHexString(mesh.getDataStart()));
				return 0;
			}

			writer.writeMesh(mesh);
		} catch (final Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	private IMeshWriter buildWriter(OutputStream out) {
		Format format = this.outputFormat;

		if (format == null) {
			if (outputFile.getName().endsWith(".nvx")) {
				format = Format.NVX;
			} else if (outputFile.getName().endsWith(".obj")) {
				format = Format.OBJ;
			} else {
				throw new RuntimeException("Couldn't guess format for file " + outputFile);
			}
		}

		switch (format) {
		case NVX:
			return new NvxFileWriter(out);
		case OBJ:
			return new ObjFileWriter(out);
		default:
			throw new IllegalStateException("Coudln't find suitable writer");
		}
	}
	
	private IMeshReader buildReader(InputStream out) {
		Format format = this.inputFormat;

		if (format == null) {
			if (inputFile.getName().endsWith(".nvx")) {
				format = Format.NVX;
			} else if (inputFile.getName().endsWith(".obj")) {
				format = Format.OBJ;
			} else {
				throw new RuntimeException("Couldn't guess format for file " + inputFile);
			}
		}

		switch (format) {
		case NVX:
			return new NvxFileReader(out);
		case OBJ:
			return new ObjFileReader(out);
		default:
			throw new IllegalStateException("Coudln't find suitable writer");
		}
	}

	private enum Format {
		NVX, OBJ;
	}
}
