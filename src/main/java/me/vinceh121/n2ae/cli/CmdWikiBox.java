package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.json.JsonScriptGenerator;
import me.vinceh121.n2ae.script.json.WikiBoxes;
import me.vinceh121.n2ae.script.json.WikiBoxGenerator;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.tcl.TCLParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "wikibox", description = { "Generates a Mediawiki Portable Infobox from a script" })
public class CmdWikiBox implements Callable<Integer> {
	private final ObjectMapper mapper = new ObjectMapper();

	@Spec
	private CommandSpec spec;

	@Option(names = { "-i", "--input" })
	private File input;

	@Option(names = { "-m", "--model" })
	private File modelInput;

	@Option(names = { "-b", "--box" })
	private WikiBoxes box;

	@Option(names = { "-l", "--list-boxes" })
	private boolean list;

	@Option(names = { "-j", "--json" })
	private boolean json;

	@Override
	public Integer call() throws Exception {
		if (this.list) {
			for (final WikiBoxes box : WikiBoxes.values()) {
				System.out.println(box);
			}
			return 0;
		}

		if (this.input == null) {
			throw new ParameterException(this.spec.commandLine(), "Missing required argument -i, --input=<input>");
		}

		if (this.box == null && !this.json) {
			throw new ParameterException(this.spec.commandLine(), "Missing required argument -b, --box=<box>");
		}

		final IParser parser;
		if (this.input.getName().endsWith(".n")) {
			parser = new NOBParser();
		} else if (this.input.getName().endsWith(".tcl")) {
			parser = new TCLParser();
		} else {
			throw new IllegalArgumentException("Don't know how to read input file");
		}

		final Map<String, NOBClazz> model = this.mapper.readValue(this.modelInput,
				new TypeReference<Map<String, NOBClazz>>() {
				});

		parser.setClassModel(model);

		try (final InputStream in = new FileInputStream(this.input)) {
			parser.read(in);
			final JsonScriptGenerator json = new JsonScriptGenerator(this.mapper);
			final ObjectNode node = json.generateJson(parser.getCalls());
			if (this.json) {
				this.mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, node);
				return 0;
			}
			final WikiBoxGenerator gen = new WikiBoxGenerator();
			gen.write(System.out, this.box.getWikiBox(), node);
		}
		return 0;
	}
}
