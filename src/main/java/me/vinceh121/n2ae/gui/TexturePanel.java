package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import me.vinceh121.n2ae.texture.Block;

public class TexturePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final List<Block> blocks;
	private final List<BufferedImage> imgs;
	private final JLabel lblBlockInfo, lblViewLabel;

	public TexturePanel(List<Block> blocks, List<BufferedImage> imgs) {
		this.blocks = blocks;
		this.imgs = imgs;

		this.setLayout(new BorderLayout());

		this.lblViewLabel = new JLabel();
		this.add(this.lblViewLabel, BorderLayout.CENTER);

		JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.add(topBar, BorderLayout.NORTH);

		JComboBox<Block> blockSelect = new JComboBox<>(new Vector<>(blocks));
		blockSelect.setRenderer(new BasicComboBoxRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				this.setText("Block " + blocks.indexOf(value)); // because index might be -1
				return this;
			}
		});
		blockSelect.addActionListener(e -> this.showBlock(blockSelect.getSelectedIndex()));
		topBar.add(blockSelect);

		final JPopupMenu popup = new JPopupMenu();
		JMenuItem itmSave = new JMenuItem("Save as...");
		itmSave.addActionListener(e -> {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "PNG image";
				}

				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(".png") || f.isDirectory();
				}
			});
			int status = fc.showSaveDialog(null);
			if (status != JFileChooser.APPROVE_OPTION) {
				return;
			}

			try {
				ImageIO.write(imgs.get(blockSelect.getSelectedIndex()), "png", fc.getSelectedFile());
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to save texture: " + e1);
			}
		});
		popup.add(itmSave);

		this.lblBlockInfo = new JLabel();
		topBar.add(this.lblBlockInfo);

		this.addMouseListener(new MouseAdapter() {
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
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		this.showBlock(0);
	}

	private void showBlock(int idx) {
		this.lblViewLabel.setIcon(new ImageIcon(this.imgs.get(idx)));
		Block b = this.blocks.get(idx);
		this.lblBlockInfo.setText(b.getWidth() + "×" + b.getHeight() + "×" + b.getDepth() + "\t" + b.getFormat());
	}
}
