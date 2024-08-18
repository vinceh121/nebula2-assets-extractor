package me.vinceh121.n2ae.gui;

import java.awt.FlowLayout;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class StatusBar extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String NBSP = "\u00A0";
	private final JLabel lbl = new JLabel(NBSP);
	private final LinkedList<String> queue = new LinkedList<>();
	private final Timer timer;

	public StatusBar() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.lbl.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lbl);

		this.timer = new Timer(5000, e -> {
			final Timer self = (Timer) e.getSource();

			if (queue.size() > 0) {
				final String msg = queue.pop();
				lbl.setText(msg);
			} else {
				lbl.setText(NBSP);
				self.stop();
			}

		});

		this.timer.setInitialDelay(0);
	}

	public void addMessage(String msg) {
		this.queue.add(msg);

		this.timer.start();
	}
}
