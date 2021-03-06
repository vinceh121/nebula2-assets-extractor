package me.vinceh121.n2ae.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "nebula2-extractor", subcommands = { HelpCommand.class, CmdModel.class, CmdTexture.class,
		CmdUnpack.class, CmdFullExtract.class, CmdScript.class, CmdExtractClasses.class })
public class ExtractorCli {
	public static void main(final String[] args) {
		System.exit(new CommandLine(new ExtractorCli()).execute(args));
	}
}
