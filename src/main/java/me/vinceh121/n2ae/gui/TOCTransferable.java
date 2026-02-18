package me.vinceh121.n2ae.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import me.vinceh121.n2ae.pkg.NnpkInMemoryFileExtractor;
import me.vinceh121.n2ae.pkg.TableOfContents;

public class TOCTransferable implements Transferable {
	public static final DataFlavor NPK_CHILD_FLAVOR = new DataFlavor(TableOfContents.class, "NPK0 child");
	private final List<TableOfContents> tocs;

	public TOCTransferable(final List<TableOfContents> tocs) {
		this.tocs = tocs;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { TOCTransferable.NPK_CHILD_FLAVOR, DataFlavor.javaFileListFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		return TOCTransferable.NPK_CHILD_FLAVOR.equals(flavor);
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (TOCTransferable.NPK_CHILD_FLAVOR.equals(flavor)) {
			return this.tocs;
		} else if (DataFlavor.javaFileListFlavor.equals(flavor)) {
			final List<File> files = new ArrayList<>(this.tocs.size());

			for (TableOfContents toc : this.tocs) {
				files.add(this.writeTmp(toc));
			}

			return files;
		}
		throw new UnsupportedFlavorException(flavor);
	}
	
	private File writeTmp(TableOfContents toc) throws IOException {
		final File f = Path.of(System.getProperty("java.io.tmpdir"), toc.getName()).toFile();
		f.deleteOnExit();
		final NnpkInMemoryFileExtractor ext = new NnpkInMemoryFileExtractor(f);
		ext.write(toc);
		// because File#delete() doesn't recurse directories
		if (toc.isDirectory()) {
			Files.walk(f.toPath()).forEach(p -> p.toFile().deleteOnExit());
		}
		
		return f;
	}

	public List<TableOfContents> getTocs() {
		return this.tocs;
	}
}
