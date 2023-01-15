package me.vinceh121.n2ae;

import java.awt.GraphicsEnvironment;

import me.vinceh121.n2ae.cli.ExtractorCli;
import me.vinceh121.n2ae.gui.ExtractorFrame;

public final class ExtractorMain {
	public static void main(String[] args) {
		if (args.length == 0 && !GraphicsEnvironment.isHeadless()) {
			ExtractorFrame.main(args);
		} else {
			ExtractorCli.main(args);
		}
	}
}
