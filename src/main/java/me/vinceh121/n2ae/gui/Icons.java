package me.vinceh121.n2ae.gui;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public final class Icons {
	private static final Map<String, ImageIcon> ICONS = new HashMap<>();

	public static Image getImage(String i) {
		return get(i).getImage();
	}

	public static ImageIcon get(final String iconName) {
		final ImageIcon icon = ICONS.get(iconName);
		if (icon == null) {
			throw new IllegalArgumentException("Icon not found " + iconName);
		}
		return icon;
	}

	static {
		final String[] loadList = { "bricks", "chart_line", "database", "folder", "image", "page_white_error",
				"page_white_text", "script", "cross" };

		for (final String img : loadList) {
			ICONS.put(img, new ImageIcon(Icons.class.getClassLoader().getResource("icons/" + img + ".png")));
		}
	}
}
