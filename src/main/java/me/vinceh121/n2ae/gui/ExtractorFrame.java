package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatDarculaLaf;

import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileWriter;
import me.vinceh121.n2ae.pkg.NnpkInMemoryFileReader;
import me.vinceh121.n2ae.pkg.TableOfContents;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.texture.NtxFileReader;

public class ExtractorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final DataFlavor FLAVOR_FILE = new DataFlavor("text/uri-list;class=java.lang.String", "file list");
	private static final FileFilter NPK_FILTER = new FileFilter() {
		@Override
		public String getDescription() {
			return "NPK0 archive file";
		}

		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".npk") || f.isDirectory();
		}
	};
	private final JTree tree;
	private final JTabbedPane tabbed;
	private final ExtractorClipboardOwner clipboardOwner = new ExtractorClipboardOwner();
	private final AboutDialog aboutDialog = new AboutDialog();
	private GuiSettings settings;
	private File openedNpk;
	private TableOfContents toc;
	private Map<String, NOBClazz> classModel;

	public static void main(String[] args) {
		FlatDarculaLaf.setup();

		ExtractorFrame frame = new ExtractorFrame();
		frame.setVisible(true);
	}

	public ExtractorFrame() {
		try { // XXX ugly mess
			try {
				this.settings = GuiSettings.load();
			} catch (FileNotFoundException e) {
				this.settings = new GuiSettings();
			}
			this.loadClassModel();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Failed to load settings. " + e1);
			e1.printStackTrace();
			this.settings = new GuiSettings();
			this.classModel = new HashMap<>();
		}

		this.setTitle("Nebula 2 Assets Extractor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setIconImage(Icons.getImage("bricks"));
		this.setSize(800, 700);
		this.setExtendedState(MAXIMIZED_BOTH);
		this.setLayout(new BorderLayout());

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setResizeWeight(0.33);
		this.add(split, BorderLayout.CENTER);

		this.tree = new JTree(new DefaultMutableTreeNode());
		this.tree.setEnabled(false);
		this.tree.setCellRenderer(new NpkTreeCellRenderer());
		this.tree.setActionMap(null);
		this.tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					if (node == null || !(node.getUserObject() instanceof TableOfContents)) {
						return;
					}

					TableOfContents sel = (TableOfContents) node.getUserObject();
					if (sel.isDirectory()) {
						return;
					}
					final String ext = sel.getName().substring(sel.getName().lastIndexOf('.') + 1);
					switch (ext) {
					case "ntx":
						openTexture(sel);
						break;
					case "n":
						openScript(sel);
						break;
					default:
						JOptionPane.showMessageDialog(null, "Cannot open file " + sel.getName());
						break;
					}
				}
			}

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

					TableOfContentPopupMenu pop =
							new TableOfContentPopupMenu((DefaultTreeModel) tree.getModel(), tree.getSelectionPath());
					pop.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		split.setLeftComponent(new JScrollPane(this.tree));

		tabbed = new JTabbedPane();
		split.setRightComponent(tabbed);

		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);

		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('f');
		bar.add(mnFile);

		JMenuItem mntOpen = new JMenuItem("Open NPK0");
		mntOpen.setMnemonic('o');
		mntOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mntOpen.addActionListener(e -> this.openNPK());
		mnFile.add(mntOpen);

		JMenuItem mntSave = new JMenuItem("Save NPK0");
		mntSave.setMnemonic('s');
		mntSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mntSave.addActionListener(e -> this.saveNPK());
		mnFile.add(mntSave);

		JMenuItem mntSaveAs = new JMenuItem("Save NPK0 as");
		mntSaveAs.setMnemonic('a');
		mntSaveAs.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		mntSaveAs.addActionListener(e -> this.saveAsNPK());
		mnFile.add(mntSaveAs);

		JMenuItem mntQuit = new JMenuItem("Quit");
		mntQuit.setMnemonic('q');
		mntQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		mntQuit.addActionListener(e -> System.exit(0));
		mnFile.add(mntQuit);

		JMenu mnEdit = new JMenu("Edit");
		mnEdit.setMnemonic('e');
		bar.add(mnEdit);

		JMenuItem mntCopy = new JMenuItem("Copy");
		mntCopy.setMnemonic('c');
		mntCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		mntCopy.addActionListener(e -> this.copy());
		mnEdit.add(mntCopy);

		JMenuItem mntCut = new JMenuItem("Cut");
		mntCut.setMnemonic('u');
		mntCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		mntCut.addActionListener(e -> this.cut());
		mnEdit.add(mntCut);

		JMenuItem mntPaste = new JMenuItem("Paste");
		mntCut.setMnemonic('p');
		mntPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		mntPaste.addActionListener(e -> this.paste());
		mnEdit.add(mntPaste);

		JMenu mnHelp = new JMenu("Help");
		bar.add(mnHelp);

		JMenuItem mntWiki = new JMenuItem("Wiki");
		mntWiki.addActionListener(e -> {
			final String uri = "https://github.com/vinceh121/nebula2-assets-extractor/wiki";
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to open browser, the wiki is at " + uri);
			}
		});
		mnHelp.add(mntWiki);

		JMenuItem mntAbout = new JMenuItem("About");
		mntAbout.addActionListener(e -> this.aboutDialog.setVisible(true));
		mnHelp.add(mntAbout);
	}

	public void openScript(TableOfContents toc) {
		this.tabbed.addTab(toc.getName(), new ScriptPanel(classModel, toc));
		this.ensureTabsCloseable();
		this.selectLastTab();
	}

	public void openTexture(TableOfContents toc) {
		NtxFileReader read = new NtxFileReader(new ByteArrayInputStream(toc.getData()));
		try {
			read.readHeader();
			read.readAllTextures();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.tabbed.addTab(toc.getName(), Icons.get("image"), new TexturePanel(read.getBlocks(), read.getTextures()));
		this.ensureTabsCloseable();
		this.selectLastTab();
	}

	private void paste() {
		try {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (clip.isDataFlavorAvailable(TOCTransferable.NPK_CHILD_FLAVOR)) {
				this.pasteInternal((TOCTransferable) clip.getData(TOCTransferable.NPK_CHILD_FLAVOR));
			} else if (clip.isDataFlavorAvailable(FLAVOR_FILE)) {
				// On Linux DataFlavor.javaFileListFlavor is broken as it reaches an unexpected
				// \0 in the last URL, so we have to reinvent the wheel
				final String[] files = ((String) clip.getData(FLAVOR_FILE)).split("\n");
				for (String f : files) {
					File file = new File(new URI(f.strip().replace("\0", "")));
					this.pasteFile(file);
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Don't know how to handle mimetype " + Arrays.toString(clip.getAvailableDataFlavors()));
			}
		} catch (URISyntaxException | UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to paste: " + e);
		}
	}

	private void pasteFile(File f) throws IOException {
		DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		TableOfContents selToc = (TableOfContents) selNode.getUserObject();

		TableOfContents newToc = new TableOfContents();
		newToc.setDirectory(f.isDirectory());
		newToc.setFile(f.isFile());
		newToc.setName(f.getName());
		newToc.setData(Files.readAllBytes(f.toPath()));
		newToc.setLength(newToc.getData().length);

		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newToc);

		if (selToc.isDirectory()) { // selected path is a dir, insert inside
			this.getTreeModel()
				.insertNodeInto(newNode, selNode, this.getTreeModel().getIndexOfChild(selNode.getParent(), selNode));
			selToc.getEntries().put(newToc.getName(), newToc);
		} else if (selToc.isFile()) { // selected path is file, insert as sibling
			DefaultMutableTreeNode selParent = (DefaultMutableTreeNode) selNode.getParent();
			TableOfContents selTocParent = ((TableOfContents) selParent.getUserObject());
			this.getTreeModel()
				.insertNodeInto(newNode, selParent, this.getTreeModel().getIndexOfChild(selNode.getParent(), selNode));
			selTocParent.getEntries().put(newToc.getName(), newToc);
		}
	}

	private void pasteInternal(TOCTransferable trans) {
		DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		TableOfContents selToc = (TableOfContents) selNode.getUserObject();

		if (selToc.isDirectory()) { // selected path is a dir, insert inside
			this.getTreeModel()
				.insertNodeInto(trans.getNode(),
						selNode,
						this.getTreeModel().getIndexOfChild(selNode.getParent(), selNode));
			selToc.getEntries().put(trans.getToc().getName(), trans.getToc());
		} else if (selToc.isFile()) { // selected path is file, insert as sibling
			DefaultMutableTreeNode selParent = (DefaultMutableTreeNode) selNode.getParent();
			TableOfContents selTocParent = ((TableOfContents) selParent.getUserObject());
			this.getTreeModel()
				.insertNodeInto(trans.getNode(),
						selParent,
						this.getTreeModel().getIndexOfChild(selNode.getParent(), selNode));
			selTocParent.getEntries().put(trans.getToc().getName(), trans.getToc());
		}
	}

	private void cut() {
		this.copy();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		TableOfContents parentToc = ((TableOfContents) ((DefaultMutableTreeNode) node.getParent()).getUserObject());
		parentToc.getEntries().remove(((TableOfContents) node.getUserObject()).getName());
		this.getTreeModel().removeNodeFromParent(node);
	}

	private void copy() {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		TOCTransferable trans = this.makeTocTransferable();
		clip.setContents(trans, this.clipboardOwner);
	}

	private TOCTransferable makeTocTransferable() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null || !(node.getUserObject() instanceof TableOfContents)) {
			throw new IllegalStateException();
		}

		return new TOCTransferable((TableOfContents) node.getUserObject(), node);
	}

	public void saveAsNPK() {
		this.openedNpk = null;
		this.saveNPK();
	}

	public void saveNPK() {
		if (this.openedNpk == null) {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(NPK_FILTER);
			fc.setFileFilter(NPK_FILTER);
			int status = fc.showSaveDialog(null);
			if (status != JFileChooser.APPROVE_OPTION) {
				return;
			}

			this.openedNpk = fc.getSelectedFile();
		}

		this.setEnabled(false);
		this.tree.setEnabled(false);
		CompletableFuture.runAsync(this::writeNPK).exceptionally(t -> {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to write NPK0 file: " + t);
			return null;
		}).thenRunAsync(() -> {
			this.setEnabled(true);
			this.tree.setEnabled(true);
		});
	}

	private void writeNPK() throws RuntimeException {
		try (FileOutputStream out = new FileOutputStream(openedNpk)) {
			NnpkFileWriter.updateTableOfContentsOffsets(this.toc);
			NnpkFileWriter writer = new NnpkFileWriter(out);
			writer.setTableOfContents(this.toc);
			writer.writeFromMemory();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void openNPK() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(NPK_FILTER);
		fc.setFileFilter(NPK_FILTER);
		int status = fc.showOpenDialog(null);
		if (status != JFileChooser.APPROVE_OPTION) {
			return;
		}

		this.openedNpk = fc.getSelectedFile();

		CompletableFuture.runAsync(this::readNPK).exceptionally((t) -> {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to read NPK0 file: " + t);
			return null;
		}).thenRunAsync(() -> {
			this.updateTreeModel();
			this.tree.setEnabled(true);
		});
	}

	private void readNPK() throws RuntimeException {
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
			NnpkInMemoryFileReader ex = new NnpkInMemoryFileReader(rand);
			ex.setDataOffset(dataOffset);
			ex.readTableOfContents(toc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	private void ensureTabsCloseable() {
		for (int i = 0; i < this.tabbed.getTabCount(); i++) {
			if (!(this.tabbed.getTabComponentAt(i) instanceof TabCloseButton)) {
				this.tabbed.setTabComponentAt(i, new TabCloseButton(tabbed, this.tabbed.getComponentAt(i)));
			}
		}
	}

	private void selectLastTab() {
		this.tabbed.setSelectedIndex(this.tabbed.getTabCount() - 1);
	}

	private void loadClassModel() throws StreamReadException, DatabindException, IOException {
		if (this.settings.getClassModelUrl() == null) {
			ClassModelSelectionDialog select = new ClassModelSelectionDialog();
			select.setVisible(true);
			this.settings.setClassModelUrl(select.getSelectedUrl());
		}

		this.classModel =
				MAPPER.readValue(new URL(this.settings.getClassModelUrl()), new TypeReference<Map<String, NOBClazz>>() {
				});
	}

	private DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) this.tree.getModel();
	}

	private static class ExtractorClipboardOwner implements ClipboardOwner {
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
		}
	}

	private static class TabCloseButton extends JPanel {
		private static final long serialVersionUID = 1L;

		public TabCloseButton(JTabbedPane tabs, Component tabComp) {
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.setOpaque(false);

			JLabel lblTitle = new JLabel(tabs.getTitleAt(tabs.indexOfComponent(tabComp)));
			this.add(lblTitle);

			JButton btnClose = new JButton();
			btnClose.setIcon(Icons.get("cross"));
			btnClose.addActionListener(e -> tabs.removeTabAt(tabs.indexOfComponent(tabComp)));
			this.add(btnClose);
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
