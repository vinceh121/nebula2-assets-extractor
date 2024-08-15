package me.vinceh121.n2ae.gui;

import java.awt.Image;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.ImageIcon;

public final class Icons {
	private static final Map<Name, ImageIcon> ICONS = new EnumMap<>(Name.class);

	public static Image getImage(final Name i) {
		return Icons.get(i).getImage();
	}

	public static ImageIcon get(final Name iconName) {
		final ImageIcon icon = Icons.ICONS.get(iconName);
		if (icon == null) {
			throw new IllegalArgumentException("Icon not found " + iconName);
		}
		return icon;
	}

	public enum Name {
		BRICKS, CHART_LINE, DATABASE, FOLDER, IMAGE, PAGE_WHITE_ERROR, PAGE_WHITE_TEXT, SCRIPT, CROSS, MUSIC;

		public String getPath() {
			return "icons/" + this.name().toLowerCase() + ".png";
		}
	}

	static {
		for (final Name img : Name.values()) {
			Icons.ICONS.put(img, new ImageIcon(Icons.class.getClassLoader().getResource(img.getPath())));
		}
	}
}
