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
		if (outputFile == null) {
			outputFile = new File("./" + (inputFile.getName().endsWith(".nvx")
					? inputFile.getName().substring(0, inputFile.getName().length() - 4)
					: inputFile.getName()) + ".obj");
		}

		try (FileInputStream is = new FileInputStream(inputFile);
				FileOutputStream os = new FileOutputStream(outputFile)) {
			NvxFileReader reader = new NvxFileReader(is);
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
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}
