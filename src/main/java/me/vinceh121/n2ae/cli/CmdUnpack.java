package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Callable;

import me.vinceh121.n2ae.pkg.NnpkFileExtractor;
import me.vinceh121.n2ae.pkg.NnpkFileReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "unpack", description = { "Unpacks an NPK archive" })
public class CmdUnpack implements Callable<Integer> {
	@Option(names = { "-o", "--output" }, defaultValue = "./")
	private File outputFolder;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-l", "--list" })
	private boolean list;

	@Option(names = { "-f", "--file" }, split = "/", description = { "file to unpack",
			"unpack entire archive if empty" })
	private List<String> path;

	@Override
	public Integer call() throws Exception {
		try (FileInputStream is = new FileInputStream(inputFile)) {
			NnpkFileReader r = new NnpkFileReader(is);
			r.readAll();

			if (list) {
				NnpkFileReader.printToc(r.getTableOfContents(), System.out);
				return 0;
			}

			NnpkFileExtractor ex = new NnpkFileExtractor(is);
			ex.setOutput(outputFolder);
			if (path == null) {
				ex.extractAllFiles(r.getTableOfContents());
			} else {
				ex.extractFile(r.getTableOfContents().get(path));
			}
		}
		return 0;
	}

}
