package me.vinceh121.n2ae.cli;

import java.io.File;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.classmodel.DecompiledCommandIdsExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract-classes", description = { "Extracts a class model to decompile NOB scripts" })
public class CmdExtractClasses implements Callable<Integer> {
	@Option(names = { "-i",
			"--input" }, required = true, description = "File or folder of decompiled sources to generate a model")
	private File input;

	@Option(names = { "-o", "--output" }, required = true)
	private File output;

	@Option(names = { "--no-pretty-print" })
	private boolean noPrettyPrint;

	@Override
	public Integer call() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final DecompiledCommandIdsExtractor ex = new DecompiledCommandIdsExtractor();
		ex.readRecurse(this.input);
		if (this.noPrettyPrint) {
			mapper.writeValue(this.output, ex.getClazzes());
		} else {
			mapper.writerWithDefaultPrettyPrinter().writeValue(this.output, ex.getClazzes());
		}
		return 0;
	}
}
