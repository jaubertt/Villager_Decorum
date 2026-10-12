package villagerdecorum;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VillagerDecorum implements ModInitializer {
	public static final String MOD_ID = "villager_decorum";
	public static final Logger LOGGER = LoggerFactory.getLogger("Villager Decorum");

	@Override
	public void onInitialize() {
		DecorumConfig.load();
	}
}
