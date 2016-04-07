package tr.org.liderahenk.network.inventory.commands;

import tr.org.liderahenk.lider.core.api.plugin.ICommand;
import tr.org.liderahenk.network.inventory.plugininfo.PluginInfoImpl;

public abstract class BaseCommand implements ICommand {

	PluginInfoImpl pluginInfo = new PluginInfoImpl();

	@Override
	public String getPluginName() {
		return pluginInfo.getPluginName();
	}

	@Override
	public String getPluginVersion() {
		return pluginInfo.getPluginVersion();
	}

}
