package me.vinceh121.n2ae;

import java.awt.GraphicsEnvironment;

import me.vinceh121.n2ae.cli.ExtractorCli;

public final class ExtractorMain {
	public static void main(String[] args) {
		if (args.length == 0 && !GraphicsEnvironment.isHeadless()) {
			// GUI
		} else {
			ExtractorCli.main(args);
		}
	}
}
