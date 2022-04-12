package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.LEDataInputStream;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.NOBScriptReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "script", description = { "Decompiles a NOB (.n) script" })
public class CmdScript implements Callable<Integer> {
	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-o", "--output" })
	private File outputFile;

	@Option(names = { "-m",
			"--model" }, description = "Json file containing a class model generated using `extract-classes`")
	private File clazzModel;

	@Override
	public Integer call() throws Exception {
		try (OutputStream out = this.outputFile == null ? System.out : new FileOutputStream(outputFile);
				LEDataInputStream in = new LEDataInputStream(new FileInputStream(inputFile))) {

			NOBScriptReader reader = new NOBScriptReader(in);

			if (clazzModel != null) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, NOBClazz> model = mapper.readValue(clazzModel, new TypeReference<Map<String, NOBClazz>>() {
				});
				reader.setClazzes(model);
			}

			out.write(reader.readHeader().getBytes());
			while (in.available() > 0) {
				out.write(reader.readBlock().getBytes());
			}
		}
		return 0;
	}
}
