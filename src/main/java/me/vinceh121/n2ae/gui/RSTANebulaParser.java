package me.vinceh121.n2ae.gui;

import java.io.IOException;
import java.util.Map;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.ParseException;
import me.vinceh121.n2ae.script.tcl.TCLParser;

public class RSTANebulaParser extends AbstractParser {
	private final Map<String, NOBClazz> model;

	public RSTANebulaParser(final Map<String, NOBClazz> model) {
		this.model = model;
	}

	@Override
	public ParseResult parse(final RSyntaxDocument doc, final String style) {
		final DefaultParseResult res = new DefaultParseResult(this);

		final IParser parser = new TCLParser();
		parser.setClassModel(this.model);

		try {
			parser.read(doc.getText(0, doc.getLength()));
		} catch (final ParseException e) {
			res.addNotice(new DefaultParserNotice(this, e.getMessage(), e.getLine()));
		} catch (IOException | BadLocationException e) {
			throw new RuntimeException(e);
		}

		return res;
	}
}
