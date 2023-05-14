package io.github.cadiboo.nocubes.config;

import org.junit.Test;

import static io.github.cadiboo.nocubes.config.ColorParser.Color;
import static org.junit.Assert.assertEquals;

/**
 * @author Cadiboo
 */
public class ColorParserTests {

	@Test
	public void ints() {
		final Color first = new Color(255, 255, 255, 255);
		assertEquals(255, first.red);
		assertEquals(255, first.green);
		assertEquals(255, first.blue);
		assertEquals(255, first.alpha);
		final Color second = new Color(255, 255, 0, 255);
		assertEquals(255, second.red);
		assertEquals(255, second.green);
		assertEquals(0, second.blue);
		assertEquals(255, second.alpha);
	}

	@Test
	public void floats() {
		final Color first = new Color(1F, 1F, 1F, 1F);
		assertEquals(255, first.red);
		assertEquals(255, first.green);
		assertEquals(255, first.blue);
		assertEquals(255, first.alpha);
		final Color second = new Color(1F, 1F, 0F, 1F);
		assertEquals(255, second.red);
		assertEquals(255, second.green);
		assertEquals(0, second.blue);
		assertEquals(255, second.alpha);
		final Color third = new Color(1F, 1F, 0.5F, 1F);
		assertEquals(255, third.red);
		assertEquals(255, third.green);
		assertEquals(255 / 2, third.blue);
		assertEquals(255, third.alpha);
	}

	@Test
	public void parseRgbInts() {
		final Color aqua = ColorParser.parse("rgb(0, 255, 255)");
		assertEquals(0, aqua.red);
		assertEquals(255, aqua.green);
		assertEquals(255, aqua.blue);
		assertEquals(255, aqua.alpha);
	}

	@Test
	public void parseRgbaInts() {
		final Color aqua = ColorParser.parse("rgba(0, 255, 255, 0.2)");
		assertEquals(0, aqua.red);
		assertEquals(255, aqua.green);
		assertEquals(255, aqua.blue);
		assertEquals(51, aqua.alpha);
	}

	@Test
	public void parseHslInts() {
		final Color darkPurple = ColorParser.parse("hsl(270, 100%, 100%)");
		assertEquals(128, darkPurple.red);
		assertEquals(0, darkPurple.green);
		assertEquals(255, darkPurple.blue);
		assertEquals(255, darkPurple.alpha);
	}

	@Test
	public void parseHslaInts() {
		final Color darkPurple = ColorParser.parse("hsla(270, 100%, 100%, 0.5)");
		assertEquals(128, darkPurple.red);
		assertEquals(0, darkPurple.green);
		assertEquals(255, darkPurple.blue);
		assertEquals(128, darkPurple.alpha);
	}

	@Test
	public void parseRgbHex() {
		final Color aqua = ColorParser.parse("00FFFF");
		assertEquals(0, aqua.red);
		assertEquals(255, aqua.green);
		assertEquals(255, aqua.blue);
		assertEquals(255, aqua.alpha);
	}

	@Test
	public void parseRgbHexShort() {
		final Color aqua = ColorParser.parse("0FF");
		assertEquals(0, aqua.red);
		assertEquals(255, aqua.green);
		assertEquals(255, aqua.blue);
		assertEquals(255, aqua.alpha);
	}

	@Test
	public void parseRgbaHex() {
		final Color aqua = ColorParser.parse("00FFFF64");
		assertEquals(0, aqua.red);
		assertEquals(255, aqua.green);
		assertEquals(255, aqua.blue);
		assertEquals(100, aqua.alpha);
	}

	@Test
	public void parseRgbaHexShort() {
		final Color aqua = ColorParser.parse("0FF6");
		assertEquals(0, aqua.red);
		assertEquals(255, aqua.green);
		assertEquals(255, aqua.blue);
		assertEquals(0x66, aqua.alpha);
	}

	@Test
	public void parseName() {
		final Color red = ColorParser.parse("red");
		assertEquals(255, red.red);
		assertEquals(0, red.green);
		assertEquals(0, red.blue);
		assertEquals(255, red.alpha);
	}

	@Test
	public void argb() {
		final Color first = new Color(255, 255, 255, 255);
		assertEquals(0xFFFFFFFF, first.toARGB());
		final Color second = new Color(0, 255, 255, 255);
		assertEquals(0xFF00FFFF, second.toARGB());
		final Color third = new Color(0, 0, 255, 255);
		assertEquals(0xFF0000FF, third.toARGB());
		final Color fourth = new Color(0, 0, 0, 255);
		assertEquals(0xFF000000, fourth.toARGB());
		final Color fifth = new Color(0, 0, 0, 0);
		assertEquals(0x00000000, fifth.toARGB());
		final Color sixth = new Color(1.0F, 0, 0, 0);
		assertEquals(0x00FF0000, sixth.toARGB());
	}

	@Test
	public void rgba() {
		final Color first = new Color(255, 255, 255, 255);
		assertEquals(0xFFFFFFFF, first.toRGBA());
		final Color second = new Color(0, 255, 255, 255);
		assertEquals(0x00FFFFFF, second.toRGBA());
		final Color third = new Color(0, 0, 255, 255);
		assertEquals(0x0000FFFF, third.toRGBA());
		final Color fourth = new Color(0, 0, 0, 255);
		assertEquals(0x000000FF, fourth.toRGBA());
		final Color fifth = new Color(0, 0, 0, 0);
		assertEquals(0x00000000, fifth.toRGBA());
		final Color sixth = new Color(1.0F, 0, 0, 0);
		assertEquals(0xFF000000, sixth.toRGBA());
	}

}
