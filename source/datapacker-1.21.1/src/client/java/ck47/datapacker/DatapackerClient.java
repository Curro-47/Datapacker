package ck47.datapacker;

import ck47.datapacker.keybind.KeybindManagerClient;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatapackerClient implements ClientModInitializer {
    public static final String MOD_ID = "datapacker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        KeybindManagerClient.Register();
    }
}
