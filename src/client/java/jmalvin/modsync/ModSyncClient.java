package jmalvin.modsync;

import jmalvin.modsync.config.ModConfig;
import jmalvin.modsync.tools.ModDownloader;
import net.fabricmc.api.ClientModInitializer;

public class ModSyncClient implements ClientModInitializer {

	public static final ModConfig CONFIG = new ModConfig();
	public static final ModDownloader DOWNLOADER = new ModDownloader();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}