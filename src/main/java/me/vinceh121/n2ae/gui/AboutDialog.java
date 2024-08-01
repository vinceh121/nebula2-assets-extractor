package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public AboutDialog() {
		this.setTitle("About");
		this.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		final JTextArea text = new JTextArea();
		text.setEditable(false);
		// @formatter:off
		text.setText("nebula2-assets-extractor Copyright (C) 2022-present vinceh121"
				+ "\n"
				+ "\n"
				+ "This program is free software: you can redistribute it and/or modify\n"
				+ "it under the terms of the GNU General Public License as published by\n"
				+ "the Free Software Foundation, either version 3 of the License, or\n"
				+ "(at your option) any later version.\n"
				+ "This program is distributed in the hope that it will be useful,\n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
				+ "GNU General Public License for more details.\n"
				+ "You should have received a copy of the GNU General Public License\n"
				+ "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
				+ "\n"
				+ "Astolfo is cute\n"
				+ "\n"
				);
		// @formatter:on
		this.add(text, BorderLayout.CENTER);

		this.pack();
		this.setLocationRelativeTo(null);
	}
}
