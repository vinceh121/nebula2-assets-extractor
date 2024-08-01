package me.vinceh121.n2ae.gui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import me.vinceh121.n2ae.gltf.GLTFGenerator;
import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.pkg.TableOfContents;
import me.vinceh121.n2ae.texture.NtxFileReader;

public class TableOfContentPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private final TableOfContents toc;

	public TableOfContentPopupMenu(final DefaultTreeModel model, final TreePath treePath) {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

		final TableOfContents[] path = new TableOfContents[treePath.getPathCount()];
		for (int i = 0; i < treePath.getPathCount(); i++) {
			path[i] = (TableOfContents) ((DefaultMutableTreeNode) treePath.getPathComponent(i)).getUserObject();
		}

		this.toc = (TableOfContents) node.getUserObject();

		final JMenuItem itmRename = new JMenuItem("Rename");
		itmRename.addActionListener(e -> {
			final String newName = JOptionPane.showInputDialog(null, "Rename \"" + this.toc.getName() + "\"", this.toc.getName());
			this.toc.setName(newName);
			model.nodeChanged(node);
		});
		this.add(itmRename);

		final JMenuItem itmDelete = new JMenuItem("Delete");
		itmDelete.addActionListener(e -> {
			final TableOfContents parent = path[path.length - 2];
			parent.getEntries().remove(this.toc.getName());
			model.removeNodeFromParent(node);
		});
		this.add(itmDelete);

		if (this.toc.getName().endsWith(".nvx")) {
			this.addModelOptions();
		} else if (this.toc.getName().endsWith(".ntx")) {
			this.addTextureOptions();
		}
	}

	private void addTextureOptions() {
		this.addSeparator();

		final JMenu mnExtract = new JMenu("Extract to...");
		this.add(mnExtract);

		final JMenuItem mntPng = new JMenuItem("PNG");
		mntPng.addActionListener(e -> {
			final File outFile = this.saveExtract(".ntx", ".png");

			if (outFile == null) {
				return;
			}

			try (ByteArrayInputStream in = new ByteArrayInputStream(this.toc.getData())) {
				final NtxFileReader read = new NtxFileReader(in);
				read.readHeader();
				read.readAllTextures();

				ImageIO.write(read.getTextures().firstElement(), "png", outFile);
			} catch (final IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1);
			}
		});
		mnExtract.add(mntPng);
	}

	private void addModelOptions() {
		this.addSeparator();

		final JMenu mnExtract = new JMenu("Extract to...");
		this.add(mnExtract);

		final JMenuItem mntObj = new JMenuItem("OBJ");
		mntObj.addActionListener(e -> {
			final File outFile = this.saveExtract(".nvx", ".obj");

			if (outFile == null) {
				return;
			}

			try (ByteArrayInputStream in = new ByteArrayInputStream(this.toc.getData());
					PrintWriter out = new PrintWriter(outFile)) {
				final NvxFileReader read = new NvxFileReader(in);
				read.readAll();
				read.writeObj(out);
			} catch (final IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e);
			}
		});
		mnExtract.add(mntObj);

		final JMenuItem mntGltf = new JMenuItem("GLTF");
		mntGltf.addActionListener(e -> {
			final File outFile = this.saveExtract(".nvx", ".gltf");

			if (outFile == null) {
				return;
			}

			final File bufferOut =
					outFile.toPath().resolveSibling(outFile.getName().replaceFirst("\\.gltf$", ".bin")).toFile();

			try (ByteArrayInputStream in = new ByteArrayInputStream(this.toc.getData());
					FileOutputStream outGltf = new FileOutputStream(outFile);
					FileOutputStream outBin = new FileOutputStream(bufferOut)) {
				final NvxFileReader read = new NvxFileReader(in);
				read.readAll();

				final GLTFGenerator gen = new GLTFGenerator(outBin);
				gen.buildBasicScene("scene");
				gen.addMesh("skin", read.getTypes(), read.getVertices(), read.getTriangles(), -1);
				gen.buildBuffer(bufferOut.getName());
				ExtractorFrame.MAPPER.writerWithDefaultPrettyPrinter().writeValue(outGltf, gen.getGltf());
			} catch (final IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e);
			}
		});
		mnExtract.add(mntGltf);
	}

	private File saveExtract(final String originalExtension, final String extension) {
		final JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File(this.toc.getName().replace(originalExtension, extension)));
		final int status = fc.showSaveDialog(null);

		if (status != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return fc.getSelectedFile();
	}
}
