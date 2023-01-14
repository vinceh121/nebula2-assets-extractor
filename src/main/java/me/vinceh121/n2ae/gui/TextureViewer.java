package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import me.vinceh121.n2ae.texture.Block;

public class TextureViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private final List<Block> blocks;
	private final List<BufferedImage> imgs;
	private final JLabel lblBlockInfo, lblViewLabel;

	public TextureViewer(List<Block> blocks, List<BufferedImage> imgs) {
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

		this.lblBlockInfo = new JLabel();
		topBar.add(this.lblBlockInfo);

		this.showBlock(0);
	}

	private void showBlock(int idx) {
		this.lblViewLabel.setIcon(new ImageIcon(this.imgs.get(idx)));
		Block b = this.blocks.get(idx);
		this.lblBlockInfo.setText(b.getWidth() + "×" + b.getHeight() + "×" + b.getDepth() + "\t" + b.getFormat());
	}
}
