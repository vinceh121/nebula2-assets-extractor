package me.vinceh121.n2ae.gui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.vinceh121.n2ae.script.CmdPrototype;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.NOBType;

public class PlantUMLBuilder {
	private final Map<String, NOBClazz> classModel;

	public PlantUMLBuilder(Map<String, NOBClazz> classModel) {
		this.classModel = classModel;
	}

	public String buildModel() {
		final StringBuilder sb = new StringBuilder();
		sb.append("@startuml\n");

		for (final NOBClazz clazz : this.classModel.values()) {

			if (clazz.getSuperclass() != null) {
				sb.append(clazz.getSuperclass());
				sb.append(" *-- ");
				sb.append(clazz.getName());
				sb.append("\n");
			}

			sb.append("class ");
			sb.append(clazz.getName());
			sb.append(" {\n");

			for (final CmdPrototype cmd : clazz.getMethods().values()) {
				sb.append("  ");
				sb.append(cmd.getReturnArgs()
					.stream()
					.map(t -> t.name().toLowerCase())
					.collect(Collectors.joining(", ")));
				sb.append(" ");
				sb.append(cmd.getName());
				sb.append("(");

				if (!List.of(NOBType.VOID).equals(cmd.getInArgs())) {
					sb.append(cmd.getInArgs()
						.stream()
						.map(t -> t.name().toLowerCase())
						.collect(Collectors.joining(", ")));
				}

				sb.append(")\n");
			}

			sb.append("}\n\n");
		}

		sb.append("@enduml\n");
		return sb.toString();
	}

	public String buildJava() {
		final StringBuilder sb = new StringBuilder();

		for (final NOBClazz clazz : this.classModel.values()) {

			sb.append("class ");
			sb.append(clazz.getName());

			if (clazz.getSuperclass() != null) {
				sb.append(" extends ");
				sb.append(clazz.getSuperclass());
			}

			sb.append(" {\n");

			for (final CmdPrototype cmd : clazz.getMethods().values()) {
				sb.append("  ");
				sb.append(cmd.getReturnArgs()
					.stream()
					.map(t -> t.name().toLowerCase())
					.collect(Collectors.joining(", ")));
				sb.append(" ");
				sb.append(cmd.getName());
				sb.append("(");

				if (!List.of(NOBType.VOID).equals(cmd.getInArgs())) {
					sb.append(
							cmd.getInArgs().stream().map(t -> t.name().toLowerCase()).collect(Collectors.joining(" ")));
				}

				sb.append(");\n");
			}

			sb.append("}\n\n");
		}

		return sb.toString();
	}
}
