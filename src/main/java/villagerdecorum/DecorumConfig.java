package villagerdecorum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class DecorumConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static volatile DecorumConfig current = new DecorumConfig();

	public boolean workstations = true;
	public boolean beds = true;
	public boolean bells = true;

	public static DecorumConfig get() {
		return current;
	}

	public DecorumConfig copy() {
		return GSON.fromJson(GSON.toJson(this), DecorumConfig.class);
	}

	public static void set(DecorumConfig config) {
		current = config.copy();
	}

	static void load() {
		Path path = file();
		DecorumConfig loaded = null;
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				loaded = GSON.fromJson(reader, DecorumConfig.class);
			} catch (IOException | JsonParseException e) {
				Path broken = path.resolveSibling(path.getFileName() + ".broken");
				VillagerDecorum.LOGGER.error("Could not read {}, using the defaults (the file is kept as {})", path, broken.getFileName(), e);
				try {
					Files.copy(path, broken, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException ignored) {
				}
			}
		}
		current = loaded != null ? loaded : new DecorumConfig();
		save();
	}

	public static void save() {
		Path path = file();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException | JsonIOException e) {
			VillagerDecorum.LOGGER.error("Could not save {}", path, e);
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve(VillagerDecorum.MOD_ID + ".json");
	}
}
