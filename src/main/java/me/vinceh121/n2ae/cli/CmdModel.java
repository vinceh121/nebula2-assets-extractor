package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

import me.vinceh121.n2ae.model.NvxFileReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "model", description = { "Convert an NVX file to a wavefront OBJ" })
public class CmdModel implements Callable<Integer> {
	@Option(names = { "-o", "--output" })
	private File outputFile;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

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
				FileOutputStream os = new FileOutputStream(this.outputFile)) {
			final NvxFileReader reader = new NvxFileReader(is);
			reader.readAll();

			if (this.info) {
				System.out.println("Vertices: " + reader.getCountVertices());
				System.out.println("Edges: " + reader.getCountEdges());
				System.out.println("Indices: " + reader.getCountIndices());
				System.out.println("Model size: " + reader.getDataSize());
				System.out.println("Model offset: 0x" + Integer.toHexString(reader.getDataStart()));
				return 0;
			}

			reader.writeObj(os);
		} catch (final Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}
