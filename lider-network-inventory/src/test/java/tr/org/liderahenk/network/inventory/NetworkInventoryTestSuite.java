package tr.org.liderahenk.network.inventory;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tr.org.liderahenk.network.inventory.commands.AhenkInstallationCommandTest;
import tr.org.liderahenk.network.inventory.commands.FileDistributionCommandTest;
import tr.org.liderahenk.network.inventory.commands.NetworkScanCommandTest;
import tr.org.liderahenk.network.inventory.utils.network.NetworkUtilsTest;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
	NetworkUtilsTest.class,
	SetupUtilsTest.class,
	FileDistributionCommandTest.class,
	NetworkScanCommandTest.class,
	AhenkInstallationCommandTest.class
})
public class NetworkInventoryTestSuite {

}
