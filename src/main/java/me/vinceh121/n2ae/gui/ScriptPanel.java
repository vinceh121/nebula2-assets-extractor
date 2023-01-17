package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.vinceh121.n2ae.pkg.TableOfContents;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.nob.NOBWriter;
import me.vinceh121.n2ae.script.tcl.TCLParser;
import me.vinceh121.n2ae.script.tcl.TCLWriter;

public class ScriptPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final RSyntaxTextArea text = new RSyntaxTextArea();
	private final RTextScrollPane scroll;
	private final TableOfContents script;
	private final Map<String, NOBClazz> model;

	public ScriptPanel(Map<String, NOBClazz> model, TableOfContents script) {
		this.script = script;
		this.model = model;

		this.setLayout(new BorderLayout());

		this.text.setTabSize(4);
		this.text.setTabsEmulated(false);
		this.text.setMarkOccurrences(true);
		this.text.setCodeFoldingEnabled(true);
		this.text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_TCL);
		this.text.requestFocusInWindow();

		try {
			Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml")).apply(text);
		} catch (final IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to apply theme: " + e);
		}

		this.scroll = new RTextScrollPane(text, true);
		this.add(this.scroll, BorderLayout.CENTER);

		this.text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
		this.text.getActionMap().put("save", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					recompile();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to recompile: " + e1);
				}
			}
		});

		JToolBar bar = new JToolBar();
		this.add(bar, BorderLayout.NORTH);

		JButton btnSave = new JButton("Recompile and save");
		btnSave.addActionListener(e -> {
			try {
				this.recompile();
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to recompile: " + e1);
			}
		});
		bar.add(btnSave);

		try {
			this.decompile();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to decompile: " + e);
		}
	}

	private void recompile() throws IOException {
		TCLParser parser = new TCLParser();
		parser.setClassModel(this.model);
		parser.read(this.text.getText());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		NOBWriter writer = new NOBWriter();
		writer.setHeader(parser.getHeader());
		writer.setCalls(parser.getCalls());
		writer.write(out);

		this.script.setData(out.toByteArray());
	}

	private void decompile() throws IOException {
		NOBParser parser = new NOBParser();
		parser.setClassModel(this.model);
		parser.read(this.script.getData());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCLWriter writer = new TCLWriter();
		writer.setHeader(parser.getHeader());
		writer.setCalls(parser.getCalls());
		writer.write(out);

		this.text.setText(new String(out.toByteArray()));
		this.text.setCaretPosition(0);
	}

	static {
		FoldParserManager.get().addFoldParserMapping(SyntaxConstants.SYNTAX_STYLE_TCL, new NebulaFoldParser());
	}
}
