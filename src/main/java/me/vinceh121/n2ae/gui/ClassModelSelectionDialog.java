package me.vinceh121.n2ae.gui;

import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

public class ClassModelSelectionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Map<String, String> URLS = new LinkedHashMap<>();
	private final JComboBox<String> combo;
	private final JTextField txtUrl;

	public ClassModelSelectionDialog() {
		this.setTitle("Class model selection");
		this.setLayout(new FlowLayout());
		this.setModalityType(ModalityType.APPLICATION_MODAL);

		this.combo = new JComboBox<>(new Vector<>(ClassModelSelectionDialog.URLS.keySet()));
		this.add(this.combo);

		this.txtUrl = new JTextField(35);
		this.add(this.txtUrl);

		final JButton btnOk = new JButton("OK");
		btnOk.addActionListener(e -> {
			this.setVisible(false);
		});
		this.add(btnOk);

		this.pack();

		this.combo.addActionListener(e -> {
			final String url = ClassModelSelectionDialog.URLS.get(this.combo.getSelectedItem());

			if (url.isEmpty()) {
				this.txtUrl.setText("");
				this.txtUrl.setEditable(true);
			} else {
				this.txtUrl.setText(url);
				this.txtUrl.setEditable(false);
			}
		});

		this.combo.setSelectedIndex(0);
	}

	public String getSelectedUrl() {
		final String url = ClassModelSelectionDialog.URLS.get(this.combo.getSelectedItem());

		if (url.isEmpty()) {
			return this.txtUrl.getText();
		} else {
			return url;
		}
	}

	static {
		ClassModelSelectionDialog.URLS.put("Project Nomads",
				"https://raw.githubusercontent.com/vinceh121/nebula2-assets-extractor/master/project-nomads.classmodel.json");
		ClassModelSelectionDialog.URLS.put("Custom", "");
	}
}
