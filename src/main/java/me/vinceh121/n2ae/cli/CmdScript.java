package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.tcl.TCLWriter;
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
		try (FileInputStream is = new FileInputStream(this.inputFile);
				FileOutputStream os = new FileOutputStream(this.outputFile)) {
			final IParser parser = new NOBParser();
			parser.read(is);

			if (this.clazzModel != null) {
				final ObjectMapper mapper = new ObjectMapper();
				final Map<String, NOBClazz> model = mapper.readValue(this.clazzModel,
						new TypeReference<Map<String, NOBClazz>>() {
						});
				parser.setClassModel(model);
			}

			final TCLWriter writer = new TCLWriter();
			writer.setHeader(parser.getHeader());
			writer.setCalls(parser.getCalls());
			writer.write(os);
		}
		return 0;
	}
}
