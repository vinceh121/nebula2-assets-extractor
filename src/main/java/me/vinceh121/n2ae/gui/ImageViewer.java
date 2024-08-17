package me.vinceh121.n2ae.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;

public class ImageViewer extends JComponent implements MouseMotionListener, MouseListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private final List<Consumer<Float>> zoomListeners = new ArrayList<>();
	private Point lastPress;
	private int offsetX, offsetY;
	private float scale = 1;
	private BufferedImage image;

	public ImageViewer() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.setBackground(Color.BLACK);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (this.image != null) {
			g.drawImage(this.image,
					offsetX,
					offsetY,
					(int) (this.image.getWidth() * this.scale),
					(int) (this.image.getHeight() * this.scale),
					this.getBackground(),
					this);
		}
	}

	public void setImage(final BufferedImage image) {
		this.image = image;

		this.repaint();
	}

	public BufferedImage getImage() {
		return this.image;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.offsetX = e.getX() - this.lastPress.x;
		this.offsetY = e.getY() - this.lastPress.y;

		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.lastPress = e.getPoint();

		this.lastPress.x -= this.offsetX;
		this.lastPress.y -= this.offsetY;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		this.scale += e.getWheelRotation() * 0.2f;

		if (this.scale < 0) {
			this.scale = 0;
		}

		if (this.scale > 5) {
			this.scale = 5;
		}

		this.scale = Math.round(this.scale * 100f) / 100f;

		for (final Consumer<Float> c : this.zoomListeners) {
			c.accept(this.scale);
		}

		this.repaint();
	}

	public void addZoomListener(Consumer<Float> listener) {
		this.zoomListeners.add(listener);
	}
}
