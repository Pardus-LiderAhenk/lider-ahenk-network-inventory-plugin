package tr.org.liderahenk.network.inventory.commands;

import tr.org.liderahenk.lider.core.api.plugin.ICommand;

public abstract class BaseCommand implements ICommand {

	private static final String NAME = "network-inventory";
	private static final String VERSION = "1.0.0-SNAPSHOT";

	@Override
	public String getPluginName() {
		return NAME;
	}

	@Override
	public String getPluginVersion() {
		return VERSION;
	}

}
