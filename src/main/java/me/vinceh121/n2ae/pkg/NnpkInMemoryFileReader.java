package me.vinceh121.n2ae.pkg;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NnpkInMemoryFileReader {
	private final RandomAccessFile file;
	private int dataOffset;

	public NnpkInMemoryFileReader(RandomAccessFile file) {
		this.file = file;
	}

	public void readTableOfContents(TableOfContents toc) throws IOException {
		if (toc.isDirectory()) {
			for (TableOfContents child : toc.getEntries().values()) {
				this.readTableOfContents(child);
			}
		} else if (toc.isFile()) {
			this.file.seek(toc.getOffset() + this.dataOffset);
			byte[] data = new byte[toc.getLength()];
			this.file.read(data);
			toc.setData(data);
		}
	}

	public int getDataOffset() {
		return dataOffset;
	}

	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}
}
