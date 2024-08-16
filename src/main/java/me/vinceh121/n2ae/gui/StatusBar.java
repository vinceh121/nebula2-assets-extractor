package me.vinceh121.n2ae.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class StatusBar extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final float STATIC_TIME = 1, GOING_AWAY_TIME = 3, GONE_TIME = 4;
	private static final int DELTA = 30;
	private static final String NBSP = "\u00A0";
	private final JLabel lbl = new JLabel(NBSP);
	private final LinkedList<String> queue = new LinkedList<>();
	private final Timer timer;
	private float time;

	public StatusBar() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.lbl.setOpaque(false);
		this.lbl.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lbl);

		this.timer = new Timer(DELTA, e -> {

			if (queue.size() > 0) {
				time += DELTA / 1000f;

				final String msg = queue.peek();
				lbl.setText(msg);

				if (time <= STATIC_TIME) {
					setAlpha(lbl, time);
				} else if (time <= GOING_AWAY_TIME) {
					setAlpha(lbl, 1);
				} else if (time <= GONE_TIME) {
					setAlpha(lbl, GONE_TIME - time);
				} else if (time > GONE_TIME) {
					queue.pop();
					time = 0;
				}
			} else {
				lbl.setText(NBSP);
			}

			this.repaint();
		});

		this.timer.start();
	}

	public void addMessage(String msg) {
		this.queue.add(msg);
	}

	private static void setAlpha(JLabel lbl, float alpha) {
		final Color c = lbl.getForeground();
		lbl.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
	}
}
