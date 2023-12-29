package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.animation.Curve;
import me.vinceh121.n2ae.animation.NaxFileReader;
import me.vinceh121.n2ae.gltf.GLTFGenerator;
import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.tcl.TCLParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "gltf", description = { "Extracts a mesh, rig, and animation file into a single glTF 2.0 file" })
public class CmdGltf implements Callable<Integer> {
	@Option(names = { "-n", "--script" }, description = { "Script (NOB0 or TCL) that contains joint list" })
	private File nobScript;

	@Option(names = { "--model" }, description = { "Script class model JSON" })
	private File model;

	@Option(names = { "-m", "--mesh" }, description = { "NVX1 file" })
	private File mesh;

	@Option(names = { "-a", "--animation" }, description = { "NAX0 file" })
	private File animation;

	@Option(names = { "-o", "--output" }, description = { "A .gltf output file." }, required = true)
	private File output;

	@Option(names = { "-b", "--buffer" })
	private File buffer;

	@Override
	public Integer call() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();

		if (this.buffer == null) {
			this.buffer = this.output.getAbsoluteFile()
				.toPath()
				.resolveSibling(this.output.getName().replace(".gltf", ".bin"))
				.toFile();
		}

		final FileOutputStream bufferOut = new FileOutputStream(this.buffer);
		final GLTFGenerator gen = new GLTFGenerator(bufferOut);

		if (this.nobScript != null) {
			IParser parser;

			if (this.nobScript.getName().endsWith(".n")) {
				parser = new NOBParser();
			} else if (this.nobScript.getName().endsWith(".tcl")) {
				parser = new TCLParser();
			} else {
				throw new IllegalArgumentException("Can't determine script type from extension");
			}

			parser.setClassModel(mapper.readValue(this.model, new TypeReference<Map<String, NOBClazz>>() {
			}));

			try (FileInputStream scriptIn = new FileInputStream(this.nobScript)) {
				parser.read(scriptIn);
			}

			gen.addBones(parser.getCalls());
		}

		gen.buildBasicScene("scene", this.nobScript != null ? gen.getGltf().getNodes().size() : -1);

		if (this.mesh != null) {
			try (FileInputStream meshIn = new FileInputStream(this.mesh)) {
				final NvxFileReader meshReader = new NvxFileReader(meshIn);
				meshReader.readAll();
				gen.addMesh("skin",
						meshReader.getTypes(),
						meshReader.getVertices(),
						meshReader.getTriangles(),
						this.nobScript != null ? 0 : -1);
			}
		}

		if (this.animation != null) {
			try (FileInputStream animIn = new FileInputStream(this.animation)) {
				final NaxFileReader animReader = new NaxFileReader(animIn);
				final List<Curve> curves = animReader.readAll();
				gen.addCurves(curves);
			}
		}

		gen.buildBuffer(this.buffer.getName());

		bufferOut.flush();
		bufferOut.close();

		mapper.writerWithDefaultPrettyPrinter().writeValue(this.output, gen.getGltf());

		return 0;
	}

}
