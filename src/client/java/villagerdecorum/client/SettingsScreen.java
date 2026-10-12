package villagerdecorum.client;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
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
		this.list.addHeader(Component.translatable(KEY + "radius_section"));
		this.list.addBig(this.radius("bed_radius", c.bedRadius, v -> c.bedRadius = v));
		this.list.addBig(this.radius("bell_radius", c.bellRadius, v -> c.bellRadius = v));
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

	private OptionInstance<Integer> radius(String key, int value, IntConsumer setter) {
		Tooltip tooltip = Tooltip.create(Component.translatable(KEY + key + ".tooltip"));
		return new OptionInstance<>(KEY + key, v -> tooltip,
			(caption, v) -> Component.translatable("options.generic_value", caption, Component.translatable(KEY + "blocks", v)),
			new OptionInstance.IntRange(1, DecorumConfig.MAX_RADIUS), DecorumConfig.clampRadius(value), v -> {
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
