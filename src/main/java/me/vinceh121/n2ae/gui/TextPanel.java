package me.vinceh121.n2ae.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.Charset;

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
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import me.vinceh121.n2ae.pkg.TableOfContents;

public class TextPanel extends JPanel implements SearchListener, TabListener {
	private static final long serialVersionUID = 1L;
	private static final Charset ASCII = Charset.forName("US-ASCII");
	private final RSyntaxTextArea text = new RSyntaxTextArea();
	private final RTextScrollPane scroll;
	private final FindDialog findDialog;
	private final ReplaceDialog replaceDialog;
	private final ErrorStrip errorStrip;
	private final TableOfContents textFile;

	public TextPanel(final TableOfContents textFile) {
		this.textFile = textFile;
		this.setLayout(new BorderLayout());

		this.text.setTabSize(4);
		this.text.setTabsEmulated(false);
		this.text.setMarkOccurrences(true);
		this.text.setCodeFoldingEnabled(true);
		this.text.requestFocusInWindow();

		if (textFile.getName().endsWith(".tcl")) {
			this.text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_TCL);
		}

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
		this.errorStrip = new ErrorStrip(this.text);
		this.add(this.errorStrip, BorderLayout.EAST);

		this.text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "find");
		this.text.getActionMap().put("find", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				TextPanel.this.findDialog.setVisible(true);
			}
		});

		this.text.getInputMap()
			.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
					"replace");
		this.text.getActionMap().put("replace", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				TextPanel.this.replaceDialog.setVisible(true);
			}
		});

		this.load();
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

	public void load() {
		this.text.setText(new String(this.textFile.getData(), ASCII));
	}

	@Override
	public void onBeforeSave() {
		textFile.setData(TextPanel.this.text.getText().getBytes(ASCII));
	}

	@Override
	public void onClose() {
		this.onBeforeSave();
	}
}
