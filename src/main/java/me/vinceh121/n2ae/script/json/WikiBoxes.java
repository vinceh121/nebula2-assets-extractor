package me.vinceh121.n2ae.script.json;

public enum WikiBoxes {
	GUNTOWER_MACHINEGUN(new WikiBox("Guntower Machinegun infobox").addEntry("cost", "/vhcl/setartefactuseenergy")
		.addEntry("buildtime", "/vhcl/bauphase/statewatch/settimeout")
		.addEntry("health", "/vhcl/setenergy")
		.addEntry("damage", "/turret/vhcl/normal/gun/setdamage")
		.addEntry("rate", "/turret/vhcl/normal/gun/setmgspeed")
		.addEntry("armor", "/vhcl/setarmor/0/1")
		.addEntry("reach", "/turret/vhcl/normal/gun/setreach")
	// .addEntry("guns", "/turret/vhcl/normal/gun", 0)
	),

	GUNTOWER_FIREMISSILE(new WikiBox("Guntower Firemissile infobox").addEntry("cost", "/vhcl/setartefactuseenergy")
		.addEntry("buildtime", "/vhcl/bauphase/statewatch/settimeout")
		.addEntry("health", "/vhcl/setenergy")
		.addEntry("damage", "/weapon/vhcl/explode/explo/setenergy")
		.addEntry("delay", "/turret/vhcl/normal/weapon/setshotdelay")
		.addEntry("speed", "/turret/vhcl/normal/weapon/setstartspeed")
		// Can't have barrel count because it is multiple calls
		.addEntry("armor", "/vhcl/setarmor/0/1")
		.addEntry("missile_timeout", "/weapon/vhcl/normal/statewatch/settimeout"));

	private final WikiBox wikiBox;

	WikiBoxes(final WikiBox wikiBox) {
		this.wikiBox = wikiBox;
	}

	public WikiBox getWikiBox() {
		return this.wikiBox;
	}
}
