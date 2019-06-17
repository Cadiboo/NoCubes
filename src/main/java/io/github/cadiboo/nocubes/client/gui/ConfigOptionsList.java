package io.github.cadiboo.nocubes.client.gui;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
class ConfigOptionsList extends AbstractOptionList<ConfigOptionsList.Entry> {

	private final NoCubesConfigGui configGui;
	private final int maxListLabelWidth;

	public ConfigOptionsList(NoCubesConfigGui configGui, Minecraft mcIn) {
		super(mcIn, configGui.width + 45, configGui.height, 43, configGui.height - 32, 20);
		this.configGui = configGui;

		final List<ConfigOptionsList.Entry> entries = new ArrayList<>();

		// Client
		{
			entries.add(new CategoryEntry(MOD_ID + ".config.client"));
			getConfigValues(ConfigHolder.CLIENT).forEach((configValue, name) -> {
				final ConfigEntry e = new ConfigEntry(configValue, name);
				entries.add(e);
				this.addEntry(e);
			});
		}

		// Server
		if (this.minecraft.world != null) {
			if (this.minecraft.getIntegratedServer() != null) {
				entries.add(new CategoryEntry(MOD_ID + ".config.server"));
				getConfigValues(ConfigHolder.SERVER).forEach((configValue, name) -> {
					final ConfigEntry e = new ConfigEntry(configValue, name);
					entries.add(e);
					this.addEntry(e);
				});
			}
		}

		FontRenderer fontRenderer = mcIn.fontRenderer;
		int maxListLabelWidth = 0;
		for (final Entry entry : entries) {
			int i = fontRenderer.getStringWidth(entry.getTranslatedText());
			if (i > maxListLabelWidth) {
				maxListLabelWidth = i;
			}
		}
		this.maxListLabelWidth = maxListLabelWidth;

	}

	private Map<ConfigValue, String> getConfigValues(final Object config) {
		final Map<ConfigValue, String> configValuesAndNames = new HashMap<>();
		for (final Field declaredField : config.getClass().getDeclaredFields()) {
			declaredField.setAccessible(true);
			try {
				final Object o = declaredField.get(config);
				if (o instanceof ConfigValue) {
					configValuesAndNames.put((ConfigValue) o, declaredField.getName());
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return configValuesAndNames;
	}

	public int getRowWidth() {
		return super.getRowWidth() + 32;
	}

	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 15 + 20;
	}

	abstract static class Entry extends AbstractOptionList.Entry<ConfigOptionsList.Entry> {

		abstract String getTranslatedText();

	}

	@OnlyIn(Dist.CLIENT)
	public class CategoryEntry extends ConfigOptionsList.Entry {

		private final String labelText;
		private final int labelWidth;

		public CategoryEntry(String name) {
			this.labelText = I18n.format(name);
			this.labelWidth = ConfigOptionsList.this.minecraft.fontRenderer.getStringWidth(this.labelText);
		}

		public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
			ConfigOptionsList.this.minecraft.fontRenderer.drawString(this.labelText, (float) (ConfigOptionsList.this.minecraft.currentScreen.width / 2 - this.labelWidth / 2), (float) (p_render_2_ + p_render_5_ - 9 - 1), 16777215);
		}

		public List<? extends IGuiEventListener> children() {
			return Collections.emptyList();
		}

		public boolean changeFocus(boolean p_changeFocus_1_) {
			return false;
		}

		@Override
		String getTranslatedText() {
			return labelText;
		}

	}

	@OnlyIn(Dist.CLIENT)
	public class ConfigEntry extends ConfigOptionsList.Entry {

		private final ConfigValue configValue;
		private final String text;

		private ConfigEntry(final ConfigValue configValue, final String name) {
			this.configValue = configValue;
			this.text = I18n.format(MOD_ID + ".config." + name);
		}

		public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
			ConfigOptionsList.this.minecraft.fontRenderer.drawString(this.text, (float) (p_render_3_ + 90 - ConfigOptionsList.this.maxListLabelWidth), (float) (p_render_2_ + p_render_5_ / 2 - 9 / 2), 0xFFFFFF);
//			this.btnReset.x = p_render_3_ + 190 + 20;
//			this.btnReset.y = p_render_2_;
//			this.btnReset.active = !this.configValue.isDefault();
//			this.btnReset.render(p_render_6_, p_render_7_, p_render_9_);
//			this.btnChangeKeyBinding.x = p_render_3_ + 105;
//			this.btnChangeKeyBinding.y = p_render_2_;
//			this.btnChangeKeyBinding.setMessage(this.configValue.getLocalizedName());
//			boolean flag1 = false;
//			boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
//			if (!this.configValue.isInvalid()) {
//				for (KeyBinding keybinding : ConfigOptionsList.this.minecraft.gameSettings.keyBindings) {
//					if (keybinding != this.configValue && this.configValue.conflicts(keybinding)) {
//						flag1 = true;
//						keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.configValue);
//					}
//				}
//			}
//
//			if (flag) {
//				this.btnChangeKeyBinding.setMessage(TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.getMessage() + TextFormatting.WHITE + " <");
//			} else if (flag1) {
//				this.btnChangeKeyBinding.setMessage((keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED) + this.btnChangeKeyBinding.getMessage());
//			}
//
//			this.btnChangeKeyBinding.render(p_render_6_, p_render_7_, p_render_9_);
		}

		public List<? extends IGuiEventListener> children() {
//			return ImmutableList.of(this.btnChangeKeyBinding, this.btnReset);
			return ImmutableList.of();
		}

		@Override
		String getTranslatedText() {
			return text;
		}

	}

}
