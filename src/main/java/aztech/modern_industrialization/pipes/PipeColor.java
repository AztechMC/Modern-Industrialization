package aztech.modern_industrialization.pipes;

public enum PipeColor {
	REGULAR("", 0xffffffff),
	WHITE("white_", 16383998),
	ORANGE("orange_", 16351261),
	MAGENTA("magenta_", 13061821),
	LIGHT_BLUE("light_blue_", 3847130),
	YELLOW("yellow_", 16701501),
	LIME("lime_", 8439583),
	PINK("pink_", 15961002),
	GRAY("gray_", 4673362),
	LIGHT_GRAY("light_gray_", 10329495),
	CYAN("cyan_", 1481884),
	PURPLE("purple_", 8991416),
	BLUE("blue_", 3949738),
	BROWN("brown_", 8606770),
	GREEN("green_", 6192150),
	RED("red_", 11546150),
	BLACK("black_", 1908001);

	public final String prefix;
	public final int color;

	PipeColor(String prefix, int color) {
		this.prefix = prefix;
		this.color = color;
	}
}
