package tr.org.liderahenk.network.inventory.plugininfo;

import tr.org.liderahenk.lider.core.api.plugin.IPluginInfo;

public class PluginInfoImpl implements IPluginInfo {
	
	@Override
	public String getPluginName() {
		return "network-inventory";
	}

	@Override
	public String getPluginVersion() {
		return "1.0.0";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isMachineOriented() {
		return false;
	}

	@Override
	public boolean isUserOriented() {
		return false;
	}

	@Override
	public boolean isPolicyPlugin() {
		return false;
	}

	@Override
	public boolean isxBased() {
		return false;
	}
	
}