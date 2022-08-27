package me.vinceh121.n2ae.pkg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import me.vinceh121.n2ae.LEDataOutputStream;

public class NnpkFileWriter {
	private final LEDataOutputStream out;
	private int bufferSize = 1048576;
	private TableOfContents tableOfContents;

	public NnpkFileWriter(OutputStream out) {
		this(new LEDataOutputStream(out));
	}

	public NnpkFileWriter(LEDataOutputStream out) {
		this.out = out;
	}

	public void writeArchive(File root) throws IOException {
		TableOfContentsBuilder tocBuilder = new TableOfContentsBuilder();
		tocBuilder.buildTableOfContents(root);
		this.tableOfContents = tocBuilder.getTableOfContents();

		ByteArrayOutputStream tocBuffer = new ByteArrayOutputStream();
		NnpkFileWriter tocWriter = new NnpkFileWriter(tocBuffer);
		tocWriter.writeToc(tocBuilder.getTableOfContents());

		this.writeHeader(4, 4 * 3 + tocBuffer.size());
		this.out.write(tocBuffer.toByteArray());
		this.out.writeIntLE(NpkEntryType.DATA.getStartInt());
		this.out.writeIntLE(4); // blockLen

		this.writeArchiveFile(tocBuilder.getTableOfContents(), root.toPath().getParent());
	}

	private void writeArchiveFile(TableOfContents toc, Path folder) throws IOException {
		if (toc.isDirectory()) {
			for (TableOfContents c : toc.getEntries().values()) {
				this.writeArchiveFile(c, folder.resolve(toc.getName()));
			}
		} else if (toc.isFile()) {
			FileInputStream in = new FileInputStream(folder.resolve(toc.getName()).toFile());

			byte[] buf = new byte[this.bufferSize];
			int read;
			while ((read = in.read(buf)) != -1) {
				this.out.write(buf, 0, read);
			}
		}
	}

	public void writeHeader() throws IOException {
		this.writeHeader(4, 0);
	}

	public void writeHeader(int blockLength, int dataBlockStart) throws IOException {
		this.out.writeIntLE(NnpkFileReader.MAGIC_NUMBER);
		this.out.writeIntLE(blockLength);
		this.out.writeIntLE(dataBlockStart);
	}

	public void writeToc(TableOfContents toc) throws IOException {
		if (toc.isDirectory()) {
			this.out.writeIntLE(NpkEntryType.DIR.getStartInt());
			this.out.writeIntLE(4); // blockLen
			this.writeString(toc.getName());

			for (TableOfContents c : toc.getEntries().values()) {
				this.writeToc(c);
			}

			this.out.writeIntLE(NpkEntryType.DEND.getStartInt());
			this.out.writeIntLE(4); // blockLen
		} else if (toc.isFile()) {
			this.out.writeIntLE(NpkEntryType.FILE.getStartInt());
			this.out.writeIntLE(4); // blockLen

			this.out.writeIntLE(toc.getOffset());
			this.out.writeIntLE(toc.getLength());
			this.writeString(toc.getName());
		}
	}

	private void writeString(String s) throws IOException {
		out.writeShortLE((short) s.length());
		this.out.write(s.getBytes(Charset.forName("US-ASCII")));
	}

	public void close() throws IOException {
		this.out.close();
	}

	public TableOfContents getTableOfContents() {
		return tableOfContents;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
