package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.pkg.NnpkInMemoryFileExtractor;
import me.vinceh121.n2ae.pkg.TableOfContents;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.texture.NtxFileReader;

public class ExtractorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final JTree tree;
	private final JTabbedPane tabbed;
	private GuiSettings settings;
	private File openedNpk;
	private TableOfContents toc;
	private Map<String, NOBClazz> classModel;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Failed to set LAF");
			e.printStackTrace();
		}

		ExtractorFrame frame = new ExtractorFrame();
		frame.setVisible(true);
	}

	public ExtractorFrame() {
		try {
			this.settings = GuiSettings.load();
			this.loadClassModel();
		} catch (FileNotFoundException e) {
			this.settings = new GuiSettings();
			this.classModel = new HashMap<>();
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

					TableOfContentPopupMenu pop = new TableOfContentPopupMenu((DefaultTreeModel) tree.getModel(),
							tree.getSelectionPath());
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

		JMenuItem mntQuit = new JMenuItem("Quit");
		mntQuit.setMnemonic('q');
		mntQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		mntQuit.addActionListener(e -> System.exit(0));
		mnFile.add(mntQuit);
	}

	public void openScript(TableOfContents toc) {
		this.tabbed.addTab(toc.getName(), new ScriptPanel(classModel, toc));
		this.ensureTabsCloseable();
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
	}

	private void openNPK() {
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

	private void loadClassModel() throws StreamReadException, DatabindException, IOException {
		this.classModel = MAPPER.readValue(new File(this.settings.getClassModelPath()),
				new TypeReference<Map<String, NOBClazz>>() {
				});
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
