package me.vinceh121.n2ae.pkg;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;

import me.vinceh121.n2ae.LEDataInputStream;

public class NnpkFileReader {
	public static final int MAX_PATH_LEN = 512;
	public static final String NO_NAME = "<noname>";
	public static final String MAGIC_STRING = "NPK0";
	public static final int MAGIC_NUMBER = 1313884976;

	private final LEDataInputStream in;

	private int dataOffset;

	private TableOfContents toc;

	public static void main(String[] args) throws IOException {
		NnpkFileReader r = new NnpkFileReader(new FileInputStream(args[0]));
		r.readAll();

		printToc(r.getTableOfContents(), System.out);
	}

	public static void printToc(TableOfContents toc, PrintStream out) {
		printToc(toc, 0, out);
	}
	
	private static void printToc(TableOfContents toc, int depth, PrintStream out) {
		for (int i = 0; i < depth; i++) {
			if (depth != 1 && i == depth - 1) {
				out.print(" ");
			} else {
				out.print("│");
			}
		}
		if (toc.isDirectory()) {
			System.out.println("└" + toc.getName());
			for (TableOfContents t : toc.getEntries().values()) {
				printToc(t, depth + 1, out);
			}
		} else if (toc.isFile()) {
			System.out.println("├" + toc.getName() + "  " + toc.getOffset() + ":" + toc.getLength());
		} else {
			System.out.println("├ TOC entry is neither file nor dir: " + toc);
		}
	}

	public NnpkFileReader(InputStream in) {
		this(new LEDataInputStream(in));
	}

	public NnpkFileReader(LEDataInputStream in) {
		this.in = in;
	}

	public void readAll() throws IOException {
		this.readHeader();
		this.readTableOfContents();
	}

	private void readHeader() throws IOException {
		int magic = in.readIntLE();
		if (magic != MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		/* int blockLen = */in.readIntLE();
		int dataBlockStart = in.readIntLE();
		dataOffset = dataBlockStart + 8;
	}

	private void readTableOfContents() throws IOException {
		boolean insideToc = true;
		ArrayDeque<String> path = new ArrayDeque<>(6);

		while (insideToc) {
			int type = this.in.readIntLE();
			/* int blockLen = */in.readIntLE();

			if (type == NpkEntryType.DIR.getStartInt()) {
				short nameLength = this.in.readShortLE();
				String name = new String(this.in.readNBytes(nameLength));

				if (NO_NAME.equals(name)) {
					// XXX replace name with file name
				}

				path.add(name);
				TableOfContents e = new TableOfContents();
				e.setDirectory(true);
				e.setName(name);
				if (toc == null) {
					toc = e;
					path.pop();
				} else {
					toc.put(path, e);
				}
			} else if (type == NpkEntryType.DEND.getStartInt()) {
				path.pollLast();
			} else if (type == NpkEntryType.FILE.getStartInt()) {
				int fileOffset = in.readIntLE();
				int fileLength = in.readIntLE();
				int nameLength = in.readShortLE();
				String name = new String(in.readNBytes(nameLength));

				path.add(name);
				TableOfContents e = new TableOfContents();
				e.setFile(true);
				e.setLength(fileLength);
				e.setOffset(fileOffset);
				e.setName(name);
				toc.put(path, e);
				path.pollLast();
			} else {
				insideToc = false;
			}
		}
	}

	public TableOfContents getTableOfContents() {
		return toc;
	}

	public int getDataOffset() {
		return dataOffset;
	}
}
