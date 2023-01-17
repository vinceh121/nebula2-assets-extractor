package me.vinceh121.n2ae.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import me.vinceh121.n2ae.pkg.TableOfContents;

public class TOCTransferable implements Transferable {
	public static final DataFlavor NPK_CHILD_FLAVOR = new DataFlavor("application/nebula.npk.child", "NPK0 child");
	private final TableOfContents toc;
	private final DefaultMutableTreeNode node;

	public TOCTransferable(TableOfContents toc, DefaultMutableTreeNode node) {
		this.toc = toc;
		this.node = node;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { NPK_CHILD_FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return NPK_CHILD_FLAVOR.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return this;
	}

	public TableOfContents getToc() {
		return toc;
	}

	public DefaultMutableTreeNode getNode() {
		return node;
	}
}
