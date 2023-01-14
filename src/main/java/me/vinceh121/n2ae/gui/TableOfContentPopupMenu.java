package me.vinceh121.n2ae.gui;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import me.vinceh121.n2ae.pkg.TableOfContents;

public class TableOfContentPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private final TableOfContents toc;

	public TableOfContentPopupMenu(DefaultTreeModel model, TreePath treePath) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

		TableOfContents[] path = new TableOfContents[treePath.getPathCount()];
		for (int i = 0; i < treePath.getPathCount(); i++) {
			path[i] = (TableOfContents) ((DefaultMutableTreeNode) treePath.getPathComponent(i)).getUserObject();
		}

		this.toc = (TableOfContents) node.getUserObject();

		JMenuItem itmRename = new JMenuItem("Rename");
		itmRename.addActionListener(e -> {
			final String newName = JOptionPane.showInputDialog(null, "Rename \"" + toc.getName() + "\"", toc.getName());
			toc.setName(newName);
			model.nodeChanged(node);
		});
		this.add(itmRename);

		JMenuItem itmDelete = new JMenuItem("Delete");
		itmDelete.addActionListener(e -> {
			TableOfContents parent = path[path.length - 2];
			parent.getEntries().remove(toc.getName());
			model.removeNodeFromParent(node);
		});
		this.add(itmDelete);
	}
}
