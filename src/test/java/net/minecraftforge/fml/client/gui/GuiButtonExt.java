/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.gui.GuiButtonClickConsumer;

/**
 * This class provides a button that fixes several bugs present in the vanilla GuiButton drawing code.
 * The gist of it is that it allows buttons of any size without gaps in the graphics and with the
 * borders drawn properly. It also prevents button text from extending out of the sides of the button by
 * trimming the end of the string and adding an ellipsis.<br/><br/>
 * <p>
 * The code that handles drawing the button is in GuiUtils.
 *
 * @author bspkrs
 */
public class GuiButtonExt extends GuiButtonClickConsumer {

	public GuiButtonExt(int id, int xPos, int yPos, String displayString, DoubleBiConsumer onClick) {
		super(id, xPos, yPos, displayString, onClick);
	}

	public GuiButtonExt(int id, int xPos, int yPos, int width, int height, String displayString, DoubleBiConsumer onClick) {
		super(id, xPos, yPos, width, height, displayString, onClick);
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partial) {
		if (this.visible) {
			final Minecraft mc = Minecraft.getInstance();
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			final int hoverState = this.getHoverState(this.hovered);
			GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.x, this.y, 0, 46 + hoverState * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
			this.renderBg(mc, mouseX, mouseY);
			int color = 0xE0E0E0;

			if (this.packedFGColor != 0) {
				color = this.packedFGColor;
			} else if (!this.enabled) {
				color = 0xA0A0A0;
			} else if (this.hovered) {
				color = 0xFFFFA0;
			}

			String buttonText = this.displayString;
			final FontRenderer fontRenderer = mc.fontRenderer;
			int strWidth = fontRenderer.getStringWidth(buttonText);
			int ellipsisWidth = fontRenderer.getStringWidth("...");

			if (strWidth > width - 6 && strWidth > ellipsisWidth)
				buttonText = fontRenderer.trimStringToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";

			this.drawCenteredString(fontRenderer, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
		}
	}

}
