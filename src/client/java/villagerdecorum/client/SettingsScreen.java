package villagerdecorum.client;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import villagerdecorum.DecorumConfig;

public final class SettingsScreen extends OptionsSubScreen {
	private static final String KEY = "villager_decorum.settings.";

	private final DecorumConfig config = DecorumConfig.get().copy();

	public SettingsScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable(KEY + "title"));
	}

	@Override
	protected void addOptions() {
		if (this.list == null) {
			return;
		}
		DecorumConfig c = this.config;
		this.list.addHeader(Component.translatable(KEY + "section"));
		this.list.addBig(this.toggle("workstations", c.workstations, v -> c.workstations = v));
		this.list.addBig(this.toggle("beds", c.beds, v -> c.beds = v));
		this.list.addBig(this.toggle("bells", c.bells, v -> c.bells = v));
	}

	private CycleButton<Boolean> toggle(String key, boolean value, Consumer<Boolean> setter) {
		Tooltip tooltip = Tooltip.create(Component.translatable(KEY + key + ".tooltip"));
		return CycleButton.onOffBuilder(value)
			.withTooltip(v -> tooltip)
			.create(Component.translatable(KEY + key), (button, v) -> {
				setter.accept(v);
				DecorumConfig.set(this.config);
			});
	}

	@Override
	public void removed() {
		super.removed();
		DecorumConfig.save();
	}
}
