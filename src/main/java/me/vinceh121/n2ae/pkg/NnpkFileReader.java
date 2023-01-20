package me.vinceh121.n2ae.pkg;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NnpkFileReader {
	public static final int MAX_PATH_LEN = 512;
	public static final String NO_NAME = "<noname>";
	public static final String MAGIC_STRING = "NPK0";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NnpkFileReader.MAGIC_STRING);

	private final LEDataInputStream in;

	private int dataOffset, dataLength;

	private TableOfContents toc;

	public static void printToc(final TableOfContents toc, final PrintStream out) {
		NnpkFileReader.printToc(toc, 0, out);
	}

	private static void printToc(final TableOfContents toc, final int depth, final PrintStream out) {
		for (int i = 0; i < depth; i++) {
			if (depth != 1 && i == depth - 1) {
				out.print(" ");
			} else {
				out.print("│");
			}
		}
		if (toc.isDirectory()) {
			out.println("└" + toc.getName());
			for (final TableOfContents t : toc.getEntries().values()) {
				NnpkFileReader.printToc(t, depth + 1, out);
			}
		} else if (toc.isFile()) {
			out.println("├" + toc.getName() + "  " + toc.getOffset() + ":" + toc.getLength());
		} else {
			out.println("├ TOC entry is neither file nor dir: " + toc);
		}
	}

	public NnpkFileReader(final InputStream in) {
		this(new LEDataInputStream(in));
	}

	public NnpkFileReader(final LEDataInputStream in) {
		this.in = in;
	}

	public void readAll() throws IOException {
		this.readHeader();
		this.readTableOfContents();
	}

	private void readHeader() throws IOException {
		final int magic = this.in.readIntLE();
		if (magic != NnpkFileReader.MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		/* int blockLen = */this.in.readIntLE();
		final int dataBlockStart = this.in.readIntLE();
		this.dataOffset = dataBlockStart + 8;
	}

	private void readTableOfContents() throws IOException {
		boolean insideToc = true;
		final ArrayDeque<String> path = new ArrayDeque<>(6);

		while (insideToc) {
			final int type = this.in.readIntLE();
			int blockLen = this.in.readIntLE();

			if (type == NpkEntryType.DIR.getStartInt()) {
				final short nameLength = this.in.readShortLE();
				final String name = new String(this.in.readNBytes(nameLength));

				if (NnpkFileReader.NO_NAME.equals(name)) {
					// XXX replace name with file name
				}

				path.add(name);
				final TableOfContents e = new TableOfContents();
				e.setBlockLen(blockLen);
				e.setDirectory(true);
				e.setName(name);
				if (this.toc == null) {
					this.toc = e;
					path.pop();
				} else {
					this.toc.put(path, e);
				}
			} else if (type == NpkEntryType.DEND.getStartInt()) {
				path.pollLast();
			} else if (type == NpkEntryType.FILE.getStartInt()) {
				final int fileOffset = this.in.readIntLE();
				final int fileLength = this.in.readIntLE();
				final int nameLength = this.in.readShortLE();
				final String name = new String(this.in.readNBytes(nameLength));

				path.add(name);
				final TableOfContents e = new TableOfContents();
				e.setBlockLen(blockLen);
				e.setFile(true);
				e.setLength(fileLength);
				e.setOffset(fileOffset);
				e.setName(name);
				this.toc.put(path, e);
				path.pollLast();
			} else {
				insideToc = false;
			}
		}
	}

	public TableOfContents getTableOfContents() {
		return this.toc;
	}

	public int getDataOffset() {
		return this.dataOffset;
	}

	public int getDataLength() {
		return this.dataLength;
	}
}
