package me.vinceh121.n2ae.script.json;

import java.util.Hashtable;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;

public enum WikiBox {
	GUNTOWER_MACHINEGUN("Guntower Machinegun infobox",
			new String[][] { { "cost", "/vhcl/setartefactuseenergy" },
					{ "buildtime", "/vhcl/bauphase/statewatch/settimeout" }, { "health", "/vhcl/setenergy" },
					{ "damage", "/turret/vhcl/normal/gun/setdamage" }, { "rate", "/turret/vhcl/normal/gun/setmgspeed" }
			/*
			 * Can't have barrel count because single-barrel towers don't have that property
			 * , { "guns", "/turret/vhcl/normal/gun" }
			 */ }),
	GUNTOWER_FIREMISSILE("Guntower Firemissile infobox",
			new String[][] { { "cost", "/vhcl/setartefactuseenergy" },
					{ "buildtime", "/vhcl/bauphase/statewatch/settimeout" }, { "health", "/vhcl/setenergy" },
					{ "damage", "/weapon/vhcl/explode/explo/setenergy" },
					{ "delay", "/turret/vhcl/normal/weapon/setshotdelay" },
					{ "speed", "/turret/vhcl/normal/weapon/setstartspeed" }
			// Can't have barrel count because it is multiple calls
			});
	;

	private final String templateName;
	private final Map<String, JsonPointer> properties = new Hashtable<>();

	WikiBox(final String templateName, final String[][] properties) {
		this.templateName = templateName;
		for (final String[] prop : properties) {
			this.properties.put(prop[0], JsonPointer.compile(prop[1]));
		}
	}

	public String getTemplateName() {
		return this.templateName;
	}

	public Map<String, JsonPointer> getProperties() {
		return this.properties;
	}
}
