public class Player {

	private final String PLAYER_NAME;
	private final CellState PLAYER_COLOR;

	public Player(final String name, final CellState color) {
		this.PLAYER_NAME = name;
		this.PLAYER_COLOR = color;
	}

	public String getName() {
		return this.PLAYER_NAME;
	}

	public CellState getColor() {
		return this.PLAYER_COLOR;
	}
}
