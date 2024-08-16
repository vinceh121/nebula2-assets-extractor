package me.vinceh121.n2ae.gui;

import java.awt.Cursor;
import java.awt.Graphics;

import javax.swing.JComponent;

public class ImageViewer extends JComponent {
	private static final long serialVersionUID = 1L;

	public ImageViewer() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}
