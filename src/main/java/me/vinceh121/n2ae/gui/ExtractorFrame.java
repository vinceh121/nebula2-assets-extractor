package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.swing.Icon;
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
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rtextarea.SearchContext;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileWriter;
import me.vinceh121.n2ae.pkg.NnpkInMemoryFileReader;
import me.vinceh121.n2ae.pkg.TableOfContents;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.ParseException;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.tcl.TCLWriter;
import me.vinceh121.n2ae.texture.NtxFileReader;
import me.vinceh121.wanderer.launcher.pntheme.PnDarkLaf;

public class ExtractorFrame extends JFrame implements SearchListener {
	private static final long serialVersionUID = 1L;
	public static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<TableOfContents, File> TEMP_EXPORT_CACHE = new HashMap<>();
	private static final DataFlavor FLAVOR_FILE = new DataFlavor("text/uri-list;class=java.lang.String", "file list");
	private static final FileFilter NPK_FILTER = new FileFilter() {
		@Override
		public String getDescription() {
			return "NPK0 archive file";
		}

		@Override
		public boolean accept(final File f) {
			return f.getName().endsWith(".npk") || f.isDirectory();
		}
	};
	private final JTree tree;
	private final JTabbedPane tabbed;
	private final ExtractorClipboardOwner clipboardOwner = new ExtractorClipboardOwner();
	private final AboutDialog aboutDialog = new AboutDialog();
	private final List<TabListener> listeners = new LinkedList<>();
	private final FindDialog searchAllDialog = new FindDialog(this, this);
	private GuiSettings settings;
	private File openedNpk;
	private TableOfContents toc;
	private Map<String, NOBClazz> classModel;

	public static void main(final String[] args) {
		PnDarkLaf.setup();

		final ExtractorFrame frame = new ExtractorFrame();
		frame.setVisible(true);
	}

	public ExtractorFrame() {
		try { // XXX ugly mess
			try {
				this.settings = GuiSettings.load();
			} catch (final FileNotFoundException e) {
				this.settings = new GuiSettings();
			}
			this.loadClassModel();
		} catch (final IOException e1) {
			JOptionPane.showMessageDialog(null, "Failed to load settings. " + e1);
			e1.printStackTrace();
			this.settings = new GuiSettings();
			this.classModel = new HashMap<>();
		}

		this.setTitle("Nebula 2 Assets Extractor");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setIconImage(Icons.getImage("bricks"));
		this.setSize(800, 700);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setLayout(new BorderLayout());

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setResizeWeight(0.33);
		this.add(split, BorderLayout.CENTER);

		this.tree = new JTree(new DefaultMutableTreeNode()) {
			private static final long serialVersionUID = 1L;

			@Override
			public String convertValueToText(final Object value, final boolean selected, final boolean expanded,
					final boolean leaf, final int row, final boolean hasFocus) {
				if (value instanceof DefaultMutableTreeNode node
						&& node.getUserObject() instanceof TableOfContents toc) {
					return toc.getName();
				} else {
					return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
				}
			}
		};
		this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.tree.setExpandsSelectedPaths(true);
		this.tree.setEnabled(false);
		this.tree.setCellRenderer(new NpkTreeCellRenderer());
		this.tree.setActionMap(null);
		this.tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					final DefaultMutableTreeNode node =
							(DefaultMutableTreeNode) ExtractorFrame.this.tree.getLastSelectedPathComponent();
					if (node == null || !(node.getUserObject() instanceof TableOfContents)) {
						return;
					}

					final TableOfContents sel = (TableOfContents) node.getUserObject();
					if (sel.isDirectory()) {
						return;
					}
					final String ext = sel.getName().substring(sel.getName().lastIndexOf('.') + 1);
					switch (ext) {
					case "ntx":
						ExtractorFrame.this.openTexture(sel);
						break;
					case "n":
						ExtractorFrame.this.openScript(sel);
						break;
					case "nvx":
						ExtractorFrame.this.openModel(sel);
						break;
					case "txt":
						ExtractorFrame.this.openText(sel);
						break;
					default:
						JOptionPane.showMessageDialog(null, "Cannot open file " + sel.getName());
						break;
					}
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				this.popup(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				this.popup(e);
			}

			public void popup(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					final DefaultMutableTreeNode node =
							(DefaultMutableTreeNode) ExtractorFrame.this.tree.getLastSelectedPathComponent();

					if (node == null || !(node.getUserObject() instanceof TableOfContents)) {
						return;
					}

					final TableOfContentPopupMenu pop =
							new TableOfContentPopupMenu((DefaultTreeModel) ExtractorFrame.this.tree.getModel(),
									ExtractorFrame.this.tree.getSelectionPath());
					pop.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		split.setLeftComponent(new JScrollPane(this.tree));

		this.tabbed = new JTabbedPane();
		split.setRightComponent(this.tabbed);

		final JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);

		final JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('f');
		bar.add(mnFile);

		final JMenuItem mntOpen = new JMenuItem("Open NPK0");
		mntOpen.setMnemonic('o');
		mntOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mntOpen.addActionListener(e -> this.openNPK());
		mnFile.add(mntOpen);

		final JMenuItem mntSave = new JMenuItem("Save NPK0");
		mntSave.setMnemonic('s');
		mntSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mntSave.addActionListener(e -> this.saveNPK());
		mnFile.add(mntSave);

		final JMenuItem mntSaveAs = new JMenuItem("Save NPK0 as");
		mntSaveAs.setMnemonic('a');
		mntSaveAs.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		mntSaveAs.addActionListener(e -> this.saveAsNPK());
		mnFile.add(mntSaveAs);

		final JMenuItem mntQuit = new JMenuItem("Quit");
		mntQuit.setMnemonic('q');
		mntQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		mntQuit.addActionListener(e -> System.exit(0));
		mnFile.add(mntQuit);

		final JMenu mnEdit = new JMenu("Edit");
		mnEdit.setMnemonic('e');
		bar.add(mnEdit);

		final JMenuItem mntCopy = new JMenuItem("Copy");
		mntCopy.setMnemonic('c');
		mntCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		mntCopy.addActionListener(e -> this.copy());
		mnEdit.add(mntCopy);

		final JMenuItem mntCut = new JMenuItem("Cut");
		mntCut.setMnemonic('u');
		mntCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		mntCut.addActionListener(e -> this.cut());
		mnEdit.add(mntCut);

		final JMenuItem mntPaste = new JMenuItem("Paste");
		mntPaste.setMnemonic('p');
		mntPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		mntPaste.addActionListener(e -> this.paste());
		mnEdit.add(mntPaste);

		mnEdit.addSeparator();

		final JMenuItem mntSearchAll = new JMenuItem("Search all");
		mntSearchAll.setMnemonic('a');
		mntSearchAll.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
		mntSearchAll.addActionListener(e -> this.searchAll());
		mnEdit.add(mntSearchAll);

		final JMenu mnHelp = new JMenu("Help");
		bar.add(mnHelp);

		final JMenuItem mntWiki = new JMenuItem("Wiki");
		mntWiki.addActionListener(e -> {
			final String uri = "https://github.com/vinceh121/nebula2-assets-extractor/wiki";

			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (final Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to open browser, the wiki is at " + uri);
			}
		});
		mnHelp.add(mntWiki);

		final JMenuItem mntAbout = new JMenuItem("About");
		mntAbout.addActionListener(e -> this.aboutDialog.setVisible(true));
		mnHelp.add(mntAbout);
	}

	public void openScript(final TableOfContents toc) {
		this.addTab(toc.getName(), new ScriptPanel(this.classModel, toc));
		this.ensureTabsCloseable();
		this.selectLastTab();
	}

	public void openText(final TableOfContents toc) {
		this.addTab(toc.getName(), new TextPanel(toc));
		this.ensureTabsCloseable();
		this.selectLastTab();
	}

	public void openTexture(final TableOfContents toc) {
		final NtxFileReader read = new NtxFileReader(new ByteArrayInputStream(toc.getData()));

		try {
			read.readHeader();
			read.readAllTextures();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		this.addTab(toc.getName(), Icons.get("image"), new TexturePanel(read.getBlocks(), read.getTextures()));
		this.ensureTabsCloseable();
		this.selectLastTab();
	}

	public void openModel(final TableOfContents toc) {
		try {
			final File f = ExtractorFrame.getOrCreateTempFile(toc, ".obj");

			if (f.length() == 0) {
				try (final ByteArrayInputStream in = new ByteArrayInputStream(toc.getData());
						PrintWriter writer = new PrintWriter(f)) {
					final NvxFileReader reader = new NvxFileReader(in);
					reader.readAll();
					reader.writeObj(writer);
				}
			}

			Desktop.getDesktop().open(f);
		} catch (final IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addTab(String name, Component comp) {
		this.addTab(name, null, comp);
	}

	private void addTab(String name, Icon icon, Component comp) {
		if (comp instanceof TabListener listener) {
			this.listeners.add(listener);
		}

		this.tabbed.addTab(name, icon, comp);
	}

	private void paste() {
		try {
			final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();

			if (clip.isDataFlavorAvailable(TOCTransferable.NPK_CHILD_FLAVOR)) {
				final Object data = clip.getData(TOCTransferable.NPK_CHILD_FLAVOR);

				if (data instanceof TableOfContents toc) {
					this.pasteInternal(toc);
				}
			} else if (clip.isDataFlavorAvailable(ExtractorFrame.FLAVOR_FILE)) {
				// On Linux DataFlavor.javaFileListFlavor is broken as it reaches an unexpected
				// \0 in the last URL, so we have to reinvent the wheel
				final String[] files = ((String) clip.getData(ExtractorFrame.FLAVOR_FILE)).split("\n");

				for (final String f : files) {
					final File file = new File(new URI(f.strip().replace("\0", "")));
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

	private void pasteFile(final File f) throws IOException {
		final DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
		final TableOfContents selToc = (TableOfContents) selNode.getUserObject();

		final TableOfContents newToc = new TableOfContents();
		newToc.setDirectory(f.isDirectory());
		newToc.setFile(f.isFile());
		newToc.setName(f.getName());
		newToc.setData(Files.readAllBytes(f.toPath()));
		newToc.setLength(newToc.getData().length);

		final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newToc);

		if (selToc.isDirectory()) { // selected path is a dir, insert inside
			this.getTreeModel()
				.insertNodeInto(newNode, selNode, this.getTreeModel().getIndexOfChild(selNode.getParent(), selNode));
			selToc.getEntries().put(newToc.getName(), newToc);
		} else if (selToc.isFile()) { // selected path is file, insert as sibling
			final DefaultMutableTreeNode selParent = (DefaultMutableTreeNode) selNode.getParent();
			final TableOfContents selTocParent = (TableOfContents) selParent.getUserObject();
			this.getTreeModel()
				.insertNodeInto(newNode, selParent, this.getTreeModel().getIndexOfChild(selNode.getParent(), selNode));
			selTocParent.getEntries().put(newToc.getName(), newToc);
		}
	}

	private void pasteInternal(final TableOfContents toc) {
		final DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
		final TableOfContents selToc = (TableOfContents) selNode.getUserObject();

		if (selToc.isDirectory()) { // selected path is a dir, insert inside
			selToc.getEntries().put(toc.getName(), toc);
		} else if (selToc.isFile()) { // selected path is file, insert as sibling
			final DefaultMutableTreeNode selParent = (DefaultMutableTreeNode) selNode.getParent();
			final TableOfContents selTocParent = (TableOfContents) selParent.getUserObject();
			selTocParent.getEntries().put(toc.getName(), toc);
		}

		this.updateTreeModel();
	}

	private void cut() {
		this.copy();
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
		final TableOfContents parentToc = (TableOfContents) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
		parentToc.getEntries().remove(((TableOfContents) node.getUserObject()).getName());
		this.getTreeModel().removeNodeFromParent(node);
	}

	private void copy() {
		final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		final TOCTransferable trans = this.makeTocTransferable();
		clip.setContents(trans, this.clipboardOwner);
	}

	private TOCTransferable makeTocTransferable() {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();

		if (node != null && node.getUserObject() instanceof TableOfContents toc) {
			return new TOCTransferable(toc.deepClone());
		} else {
			throw new IllegalStateException();
		}
	}

	private DefaultMutableTreeNode getSelectedNode() {
		return (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
	}

	private void searchAll() {
		this.searchAllDialog.setVisible(true);
	}

	@Override
	public void searchEvent(final SearchEvent e) {
		if (e.getType() != SearchEvent.Type.FIND) {
			return;
		}

		final SearchContext ctx = e.getSearchContext();
		final List<TableOfContents> flat = this.toc.flatten();

		final int skip;
		if (this.getSelectedNode() != null) {
			skip = flat.indexOf(this.getSelectedNode().getUserObject());
		} else {
			skip = 0;
		}

		final Optional<TOCText> optNextToc = flat.stream()
			.skip(skip)
			.filter(toc -> toc.isFile() && (toc.getName().endsWith(".n") || toc.getName().endsWith(".txt")))
			.map(toc -> {
				final String text;

				if (toc.getName().endsWith(".n")) {
					try {
						final NOBParser parser = new NOBParser();
						parser.setClassModel(this.classModel);
						parser.read(toc.getData());

						final ByteArrayOutputStream out = new ByteArrayOutputStream();
						final TCLWriter writer = new TCLWriter();
						writer.setHeader(parser.getHeader());
						writer.setCalls(parser.getCalls());
						writer.write(out);

						text = new String(out.toByteArray());
					} catch (IOException | ParseException ex) {
						throw new RuntimeException(ex);
					}
				} else {
					text = new String(toc.getData(), Charset.forName("US-ASCII"));
				}

				return new TOCText(toc, text);
			})
			.filter(t -> t.text.contains(ctx.getSearchFor()))
			.findFirst();

		if (optNextToc.isPresent()) {
			System.out.println(optNextToc.get().toc.getName());

			final DefaultMutableTreeNode nextNode = this.nodeForToc(optNextToc.get().toc);
			final TreePath path = new TreePath(nextNode.getPath());
			this.tree.setSelectionPath(path);
			this.tree.scrollPathToVisible(path);
		} else {
			UIManager.getLookAndFeel().provideErrorFeedback(this);
		}
	}

	private DefaultMutableTreeNode nodeForToc(TableOfContents toc) {
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.tree.getModel().getRoot();
		final Enumeration<TreeNode> enu = root.preorderEnumeration();

		while (enu.hasMoreElements()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) enu.nextElement();

			if (node.getUserObject() == toc) {
				return node;
			}
		}

		return null;
	}

	@Override
	public String getSelectedText() {
		return null;
	}

	public void saveAsNPK() {
		this.openedNpk = null;
		this.saveNPK();
	}

	public void saveNPK() {
		for (TabListener listener : this.listeners) {
			listener.onBeforeSave();
		}

		if (this.openedNpk == null) {
			final JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(ExtractorFrame.NPK_FILTER);
			fc.setFileFilter(ExtractorFrame.NPK_FILTER);
			final int status = fc.showSaveDialog(null);
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
		try (FileOutputStream out = new FileOutputStream(this.openedNpk)) {
			NnpkFileWriter.updateTableOfContentsOffsets(this.toc);
			final NnpkFileWriter writer = new NnpkFileWriter(out);
			writer.setTableOfContents(this.toc);
			writer.writeFromMemory();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void openNPK() {
		final JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(ExtractorFrame.NPK_FILTER);
		fc.setFileFilter(ExtractorFrame.NPK_FILTER);
		final int status = fc.showOpenDialog(null);
		if (status != JFileChooser.APPROVE_OPTION) {
			return;
		}

		this.openedNpk = fc.getSelectedFile();

		CompletableFuture.runAsync(this::readNPK).exceptionally(t -> {
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
			final NnpkFileReader read = new NnpkFileReader(in);
			read.readAll();
			dataOffset = read.getDataOffset();
			this.toc = read.getTableOfContents();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		try (RandomAccessFile rand = new RandomAccessFile(this.openedNpk, "r")) {
			final NnpkInMemoryFileReader ex = new NnpkInMemoryFileReader(rand);
			ex.setDataOffset(dataOffset);
			ex.readTableOfContents(this.toc);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void updateTreeModel() {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.toc);
		this.buildTreeNodes(this.toc, root);
		final DefaultTreeModel mdl = new DefaultTreeModel(root);
		this.tree.setModel(mdl);
	}

	private void buildTreeNodes(final TableOfContents toc, final DefaultMutableTreeNode node) {
		if (toc.isDirectory()) {
			for (final TableOfContents child : toc.getEntries().values()) {
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
				this.tabbed.setTabComponentAt(i, new TabCloseButton(this.tabbed, this.tabbed.getComponentAt(i)));
			}
		}
	}

	private void selectLastTab() {
		this.tabbed.setSelectedIndex(this.tabbed.getTabCount() - 1);
	}

	private void loadClassModel() throws StreamReadException, DatabindException, IOException {
		if (this.settings.getClassModelUrl() == null) {
			final ClassModelSelectionDialog select = new ClassModelSelectionDialog();
			select.setVisible(true);
			this.settings.setClassModelUrl(select.getSelectedUrl());
		}

		this.classModel = ExtractorFrame.MAPPER.readValue(new URL(this.settings.getClassModelUrl()),
				new TypeReference<Map<String, NOBClazz>>() {
				});
	}

	private DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) this.tree.getModel();
	}

	private static File getOrCreateTempFile(final TableOfContents toc, final String extension) throws IOException {
		File f = ExtractorFrame.TEMP_EXPORT_CACHE.get(toc);

		if (f != null) {
			return f;
		}

		f = File.createTempFile(toc.getName(), extension);
		f.deleteOnExit();
		ExtractorFrame.TEMP_EXPORT_CACHE.put(toc, f);

		return f;
	}

	private record TOCText(TableOfContents toc, String text) {
	}

	private static class ExtractorClipboardOwner implements ClipboardOwner {
		@Override
		public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
		}
	}

	private class TabCloseButton extends JPanel {
		private static final long serialVersionUID = 1L;

		public TabCloseButton(final JTabbedPane tabs, final Component tabComp) {
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.setOpaque(false);

			final JLabel lblTitle = new JLabel(tabs.getTitleAt(tabs.indexOfComponent(tabComp)));
			this.add(lblTitle);

			final JButton btnClose = new JButton();
			btnClose.setIcon(Icons.get("cross"));
			btnClose.addActionListener(e -> {
				tabs.removeTabAt(tabs.indexOfComponent(tabComp));

				if (tabComp instanceof TabListener listener) {
					listener.onClose();
					listeners.remove(listener);
				}
			});
			this.add(btnClose);
		}
	}

	private static class NpkTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
				final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
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
