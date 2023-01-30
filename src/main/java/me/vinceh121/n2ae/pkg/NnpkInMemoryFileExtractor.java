package me.vinceh121.n2ae.pkg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NnpkInMemoryFileExtractor {
	private final File output;

	public NnpkInMemoryFileExtractor(File output) {
		this.output = output;
	}
	
	public void write(TableOfContents toc) throws IOException {
		this.write(toc, output);
	}
	
	private void write(TableOfContents toc, File out) throws IOException {
		if (toc.isDirectory()) {
			out.mkdir();
			for (TableOfContents child : toc.getEntries().values()) {
				this.write(child, out.toPath().resolve(child.getName()).toFile());
			}
		} else if (toc.isFile()) {
			try (FileOutputStream os = new FileOutputStream(out)) {
				os.write(toc.getData());
			}
		}
	}
}
