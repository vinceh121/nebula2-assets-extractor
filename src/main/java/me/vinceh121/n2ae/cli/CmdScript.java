package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.IWriter;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.nob.NOBWriter;
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

	@Option(names = { "-f", "--from" }, description = "Input file type ('nob0' or 'tcl')")
	private String from;

	@Option(names = { "-t", "--to" }, description = "Output file type ('nob0' or 'tcl')")
	private String to;

	@Override
	public Integer call() throws Exception {
		if (this.from == null) {
			if (this.inputFile.getName().endsWith(".n")) {
				this.from = "nob0";
			} else if (this.inputFile.getName().endsWith(".tcl")) {
				this.from = "tcl";
			} else {
				System.err.println("Don't know how to guess input file format");
				return -1;
			}
		} else {
			this.from = this.from.toLowerCase();
		}

		if (this.to == null) {
			if (this.outputFile != null) {
				if (this.outputFile.getName().endsWith(".n")) {
					this.to = "nob0";
				} else if (this.outputFile.getName().endsWith(".tcl")) {
					this.to = "tcl";
				} else {
					System.err.println("Don't know how to guess output file format");
					return -1;
				}
			} else {
				if ("nob0".equals(this.from)) {
					this.to = "tcl";
				} else if ("tcl".equals(this.from)) {
					this.to = "nob0";
				}
			}
		} else {
			this.to = this.to.toLowerCase();
		}

		if (this.outputFile == null) {
			final String outExt;
			if ("nob0".equals(this.to)) {
				outExt = ".n";
			} else if ("tcl".equals(this.to)) {
				outExt = ".tcl";
			} else {
				outExt = "";
			}
			this.outputFile = new File(
					"./" + this.inputFile.getName().substring(0, this.inputFile.getName().lastIndexOf(".")) + outExt);
		}

		try (FileInputStream is = new FileInputStream(this.inputFile)) {
			final IParser parser;
			if ("nob0".equals(this.from)) {
				parser = new NOBParser();
			} else if ("tcl".equals(this.from)) {
				throw new UnsupportedOperationException("TCL Parser not yet implemented");
			} else {
				throw new RuntimeException("Unknown from " + this.from);
			}

			if (this.clazzModel != null) {
				final ObjectMapper mapper = new ObjectMapper();
				final Map<String, NOBClazz> model = mapper.readValue(this.clazzModel,
						new TypeReference<Map<String, NOBClazz>>() {
						});
				parser.setClassModel(model);
			}
			parser.read(is);

			try (FileOutputStream os = new FileOutputStream(this.outputFile)) {
				final IWriter writer;
				if ("nob0".equals(this.to)) {
					writer = new NOBWriter();
				} else if ("tcl".equals(this.to)) {
					writer = new TCLWriter();
				} else {
					throw new RuntimeException("Unknown to " + this.to);
				}
				writer.setHeader(parser.getHeader());
				writer.setCalls(parser.getCalls());
				writer.write(os);
			}
		}
		return 0;
	}
}
