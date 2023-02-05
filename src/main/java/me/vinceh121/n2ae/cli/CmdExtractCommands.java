package me.vinceh121.n2ae.cli;

import java.io.File;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.classmodel.CommandIdsExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract-commands", description = { "Extract Nebula class commands from source files" })
public class CmdExtractCommands implements Callable<Integer> {
	@Option(names = { "-i", "--input" }, required = true)
	private File input;

	@Override
	public Integer call() throws Exception {
		CommandIdsExtractor extractor = new CommandIdsExtractor();
		extractor.readRecurse(input);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, extractor.getClazzes());
		return 0;
	}
}
