package me.vinceh121.wanderer.launcher.pntheme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

public class PnDarkLaf extends FlatDarkLaf {
	private static final long serialVersionUID = 1L;

	public static boolean setup() {
		return FlatLaf.setup(new PnDarkLaf());
	}

	@Override
	public String getName() {
		return "PnDarkLaf";
	}
}
