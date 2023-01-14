package me.vinceh121.n2ae.gui;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import me.vinceh121.n2ae.pkg.TableOfContents;

public class TableOfContentPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private final TableOfContents toc;

	public TableOfContentPopupMenu(TableOfContents toc) {
		this.toc = toc;

		JMenuItem itmRename = new JMenuItem("Rename");
		itmRename.addActionListener(e -> {
			final String newName = JOptionPane.showInputDialog(null, "Rename \"" + toc.getName() + "\"", toc.getName());
			toc.setName(newName);
		});
		this.add(itmRename);
	}
}
