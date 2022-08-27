package me.vinceh121.n2ae.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "pack", description = { "Packs an NPK archive" })
public class CmdPack implements Callable<Integer> {

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFolder;

	@Option(names = { "-o", "--output" }, required = true)
	private File outputFile;

	@Override
	public Integer call() throws Exception {
		try (final FileOutputStream os = new FileOutputStream(this.outputFile)) {
			final NnpkFileWriter writer = new NnpkFileWriter(os);
			writer.writeArchive(this.inputFolder);
			
			NnpkFileReader.printToc(writer.getTableOfContents(), System.out);
		}
		return 0;
	}
}
