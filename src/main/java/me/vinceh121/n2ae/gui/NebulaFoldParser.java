package me.vinceh121.n2ae.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;

public class NebulaFoldParser implements FoldParser {
	@Override
	public List<Fold> getFolds(final RSyntaxTextArea textArea) {
		try {
			final List<Fold> folds = new ArrayList<>();
			Fold f = null;
			for (int i = 0; i < textArea.getLineCount(); i++) {
				final int offStart = textArea.getLineStartOffset(i);
				final int offEnd = textArea.getLineEndOffset(i);
				String l = textArea.getText(offStart, offEnd - offStart);
				l = l.trim();
				if (l.startsWith("new")) {
					if (f == null) {
						f = new Fold(FoldType.CODE, textArea, offStart);
						folds.add(f);
					} else {
						f = f.createChild(FoldType.CODE, offStart);
					}
				} else if (l.startsWith("sel ..") && (f != null)) {
					f.setEndOffset(offEnd - 1);
					f = f.getParent();
				}
			}
			return folds;
		} catch (final BadLocationException e) {
			throw new RuntimeException(e); // shouldn't happen
		}
	}
}
