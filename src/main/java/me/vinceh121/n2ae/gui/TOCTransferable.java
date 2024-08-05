package me.vinceh121.n2ae.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import me.vinceh121.n2ae.pkg.NnpkInMemoryFileExtractor;
import me.vinceh121.n2ae.pkg.TableOfContents;

public class TOCTransferable implements Transferable {
	public static final DataFlavor NPK_CHILD_FLAVOR = new DataFlavor(TableOfContents.class, "NPK0 child");
	private final TableOfContents toc;

	public TOCTransferable(final TableOfContents toc) {
		this.toc = toc;
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
			return this.toc;
		} else if (DataFlavor.javaFileListFlavor.equals(flavor)) {
			final File f = new File(System.getProperty("java.io.tmpdir") + "/" + this.toc.getName()); // FIXME cross
																										// platform file
																										// separator
			f.deleteOnExit();
			final NnpkInMemoryFileExtractor ext = new NnpkInMemoryFileExtractor(f);
			ext.write(this.toc);
			// because File#delete() doesn't recurse directories
			if (this.toc.isDirectory()) {
				Files.walk(f.toPath()).forEach(p -> p.toFile().deleteOnExit());
			}
			return List.of(f);
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public TableOfContents getToc() {
		return this.toc;
	}
}
