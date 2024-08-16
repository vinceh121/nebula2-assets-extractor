package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import me.vinceh121.n2ae.pkg.TableOfContents;
import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.ParseException;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.nob.NOBWriter;
import me.vinceh121.n2ae.script.tcl.TCLParser;
import me.vinceh121.n2ae.script.tcl.TCLWriter;

public class ScriptPanel extends JPanel implements SearchListener, TabListener {
	private static final long serialVersionUID = 1L;
	private final RSyntaxTextArea text = new RSyntaxTextArea();
	private final RTextScrollPane scroll;
	private final TableOfContents script;
	private final Map<String, NOBClazz> model;
	private final FindDialog findDialog;
	private final ReplaceDialog replaceDialog;
	private final ErrorStrip errorStrip;

	public ScriptPanel(final Map<String, NOBClazz> model, final TableOfContents script) {
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
			Theme.load(this.getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"))
				.apply(this.text);
		} catch (final IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to apply theme: " + e);
		}

		this.scroll = new RTextScrollPane(this.text, true);
		this.add(this.scroll, BorderLayout.CENTER);

		this.findDialog = new FindDialog((Dialog) null, this);
		this.replaceDialog = new ReplaceDialog((Dialog) null, this);

		this.text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "find");
		this.text.getActionMap().put("find", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				ScriptPanel.this.findDialog.setVisible(true);
			}
		});

		this.text.getInputMap()
			.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
					"replace");
		this.text.getActionMap().put("replace", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				ScriptPanel.this.replaceDialog.setVisible(true);
			}
		});

		this.errorStrip = new ErrorStrip(this.text);
		this.add(this.errorStrip, BorderLayout.EAST);

		this.text.addParser(new RSTANebulaParser(model));

		try {
			this.decompile();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to decompile: " + e);
		}
	}

	private void recompile() throws IOException, ParseException {
		final IParser parser = new TCLParser();
		parser.setClassModel(this.model);
		parser.read(this.text.getText());

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final NOBWriter writer = new NOBWriter();
		writer.setHeader(parser.getHeader());
		writer.setCalls(parser.getCalls());
		writer.write(out);

		this.script.setData(out.toByteArray());
	}

	private void decompile() throws IOException, ParseException {
		final NOBParser parser = new NOBParser();
		parser.setClassModel(this.model);
		parser.read(this.script.getData());

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final TCLWriter writer = new TCLWriter();
		writer.setHeader(parser.getHeader());
		writer.setCalls(parser.getCalls());
		writer.write(out);

		this.text.setText(new String(out.toByteArray()));
		this.text.setCaretPosition(0);
	}

	@Override
	public void searchEvent(final SearchEvent e) {
		final SearchEvent.Type type = e.getType();
		final SearchContext context = e.getSearchContext();
		SearchResult result;

		switch (type) {
		case MARK_ALL:
			result = SearchEngine.markAll(this.text, context);
			break;
		case FIND:
			result = SearchEngine.find(this.text, context);
			if (!result.wasFound() || result.isWrapped()) {
				UIManager.getLookAndFeel().provideErrorFeedback(this.text);
			}
			break;
		case REPLACE:
			result = SearchEngine.replace(this.text, context);
			if (!result.wasFound() || result.isWrapped()) {
				UIManager.getLookAndFeel().provideErrorFeedback(this.text);
			}
			break;
		case REPLACE_ALL:
			result = SearchEngine.replaceAll(this.text, context);
			JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
			break;
		}
	}

	@Override
	public String getSelectedText() {
		return this.text.getSelectedText();
	}

	@Override
	public void onBeforeSave() {
		try {
			ScriptPanel.this.recompile();
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to recompile: " + e1);
		}
	}

	@Override
	public void onClose() {
		this.onBeforeSave();
	}

	@Override
	public TableOfContents getOpenedFile() {
		return this.script;
	}

	static {
		FoldParserManager.get().addFoldParserMapping(SyntaxConstants.SYNTAX_STYLE_TCL, new NebulaFoldParser());
	}
}
