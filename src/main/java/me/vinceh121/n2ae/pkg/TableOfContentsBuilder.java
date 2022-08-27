package me.vinceh121.n2ae.pkg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class TableOfContentsBuilder {
	private final TableOfContents toc = new TableOfContents();
	private int fileOffset;

	public void buildTableOfContents(File root) throws IOException {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("root isn't a directory");
		}
		this.toc.setName(root.getName());
		this.toc.setDirectory(true);

		File[] list = root.listFiles();
		Arrays.sort(list, (f1, f2) -> f1.getName().compareTo(f2.getName()));
		for (File child : list) {
			buildTableOfContents(child, this.toc);
		}
	}

	private void buildTableOfContents(File file, TableOfContents toc) throws IOException {
		if (file.isDirectory()) {
			TableOfContents dtoc = new TableOfContents();
			dtoc.setDirectory(true);
			dtoc.setName(file.getName());
			File[] list = file.listFiles();
			Arrays.sort(list, (f1, f2) -> f1.getName().compareTo(f2.getName()));
			for (File child : list) {
				buildTableOfContents(child, dtoc);
			}
			toc.put(dtoc.getName(), dtoc);
		} else if (file.isFile()) {
			TableOfContents ftoc = new TableOfContents();
			ftoc.setFile(true);
			ftoc.setName(file.getName());
			ftoc.setLength((int) Files.size(file.toPath()));
			ftoc.setOffset(this.fileOffset);
			this.fileOffset += ftoc.getLength();
			toc.put(file.getName(), ftoc);
		} else {
			System.err.println("wtf " + file);
		}
	}

	public int getOffset() {
		return fileOffset;
	}

	public void setOffset(int offset) {
		this.fileOffset = offset;
	}

	public TableOfContents getTableOfContents() {
		return this.toc;
	}
}
