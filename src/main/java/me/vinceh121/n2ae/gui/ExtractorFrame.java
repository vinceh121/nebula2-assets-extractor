package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CompletableFuture;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.pkg.NnpkInMemoryFileExtractor;
import me.vinceh121.n2ae.pkg.TableOfContents;

public class ExtractorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final JTree tree;
	private File openedNpk;
	private TableOfContents toc;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("Failed to set LAF");
			e.printStackTrace();
		}

		ExtractorFrame frame = new ExtractorFrame();
		frame.setVisible(true);
	}

	public ExtractorFrame() {
		this.setTitle("Nebula 2 Assets Extractor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setIconImage(Icons.getImage("bricks"));
		this.setSize(500, 400);
		this.setLayout(new BorderLayout());

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		this.add(new JScrollPane(split), BorderLayout.CENTER);

		this.tree = new JTree(new DefaultMutableTreeNode());
		this.tree.setEnabled(false);
		this.tree.setCellRenderer(new NpkTreeCellRenderer());
		this.tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				this.popup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				this.popup(e);
			}

			public void popup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					if (node == null || !(node.getUserObject() instanceof TableOfContents)) {
						return;
					}

					TableOfContentPopupMenu pop = new TableOfContentPopupMenu((DefaultTreeModel) tree.getModel(),
							tree.getSelectionPath());
					pop.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		split.setLeftComponent(this.tree);

		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);

		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('f');
		bar.add(mnFile);

		JMenuItem mntOpen = new JMenuItem("Open NPK0");
		mntOpen.setMnemonic('o');
		mntOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mntOpen.addActionListener(e -> this.openFile());
		mnFile.add(mntOpen);

		JMenuItem mntQuit = new JMenuItem("Quit");
		mntQuit.setMnemonic('q');
		mntQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		mntQuit.addActionListener(e -> System.exit(0));
		mnFile.add(mntQuit);
	}

	private void openFile() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "NPK0 archive file";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".npk") || f.isDirectory();
			}
		});
		int status = fc.showOpenDialog(null);
		if (status != JFileChooser.APPROVE_OPTION) {
			return;
		}

		this.openedNpk = fc.getSelectedFile();

		CompletableFuture.runAsync(this::readNpk).thenRunAsync(() -> {
			this.updateTreeModel();
			this.tree.setEnabled(true);
		}).exceptionally((t) -> {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to read NPK0 file: " + t);
			return null;
		});
	}

	private void readNpk() throws RuntimeException {
		final int dataOffset;
		try (FileInputStream in = new FileInputStream(this.openedNpk)) {
			NnpkFileReader read = new NnpkFileReader(in);
			read.readAll();
			dataOffset = read.getDataOffset();
			this.toc = read.getTableOfContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (RandomAccessFile rand = new RandomAccessFile(this.openedNpk, "r")) {
			NnpkInMemoryFileExtractor ex = new NnpkInMemoryFileExtractor(rand);
			ex.setDataOffset(dataOffset);
			ex.readTableOfContents(toc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void repaintTree() {
		this.tree.invalidate();
	}

	private void updateTreeModel() {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.toc);
		this.buildTreeNodes(toc, root);
		final DefaultTreeModel mdl = new DefaultTreeModel(root);
		this.tree.setModel(mdl);
	}

	private void buildTreeNodes(TableOfContents toc, DefaultMutableTreeNode node) {
		if (toc.isDirectory()) {
			for (TableOfContents child : toc.getEntries().values()) {
				final DefaultMutableTreeNode cNode = new DefaultMutableTreeNode(child);
				this.buildTreeNodes(child, cNode);
				node.add(cNode);
			}
		} else if (toc.isFile()) {
			node.setUserObject(toc);
		}
	}

	private static class NpkTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (!(node.getUserObject() instanceof TableOfContents)) {
				return this;
			}
			final TableOfContents toc = (TableOfContents) node.getUserObject();

			this.setText(toc.getName());

			if (toc.isDirectory()) {
				this.setIcon(Icons.get("folder"));
			} else if (toc.isFile()) {
				final String ext = toc.getName().substring(toc.getName().lastIndexOf('.') + 1);
				switch (ext) {
				case "ntx":
					this.setIcon(Icons.get("image"));
					break;
				case "n":
					this.setIcon(Icons.get("script"));
					break;
				case "nvx":
					this.setIcon(Icons.get("database"));
					break;
				case "nax":
					this.setIcon(Icons.get("chart_line"));
					break;
				case "txt":
					this.setIcon(Icons.get("page_white_text"));
					break;
				default:
					this.setIcon(Icons.get("page_white_error"));
					break;
				}
			}

			return this;
		}
	}
}
