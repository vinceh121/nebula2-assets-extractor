package me.vinceh121.n2ae.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "nebula2-extractor", subcommands = { HelpCommand.class, CmdModel.class, CmdTexture.class })
public class ExtractorCli {
	public static void main(String[] args) {
		System.exit(new CommandLine(new ExtractorCli()).execute(args));
	}
}
