package me.vinceh121.n2ae.gui;

import me.vinceh121.n2ae.pkg.TableOfContents;

public interface TabListener {
	void onBeforeSave();

	void onClose();

	TableOfContents getOpenedFile();
}
