

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * This class contains the state of the checkers board, as well as methods for
 * taking player input to simulate moves.
 * 
 * @author Tim Clancy
 * @version 1.0.0
 * @date 10.5.2017
 */
public class Board {


	
	private final int totalColumns = 8;
	private final int totalRows = 8;
	private final CellElements[][] board ;

	private final Player[] players;
	private final List<PlayerMove> currentPlayerMoves;
	private List<Integer[]> JUMPERS = new ArrayList<>();
	private int currentPlayerIndex = 0;

	public Board(final Player red, final Player white) {
		board = new CellElements[totalColumns][totalRows];
		players = new Player[2];
		players[0] = red;
		players[1] = white;
		currentPlayerMoves = new ArrayList<PlayerMove>();
		resetBoard();
	}

	private void resetBoard() {
		for(int i=0 ; i <=totalRows -1;i++){
			for(int j=0 ; j <= totalColumns-1;j++){
				if(i == 0 || i == 2){
					if(j%2 != 0){
						board[j][i] = CellElements.WHITE;
					}
					else{
						board[j][i] = CellElements.EMPTY;
					}
				}
			    else if(i == 1){
					if(j%2 == 0){
						board[j][i] = CellElements.WHITE;
					}
					else{
						board[j][i] = CellElements.EMPTY;
					}
				}
				else if(i == 3 || i == 4){
					board[j][i]  = CellElements.EMPTY;
				}
				else if(i == 5 || i == 7){
					if(j %2 == 0){
						board[j][i] = CellElements.RED;
					}
					else{
						board[j][i] = CellElements.EMPTY;
					}
				}
			    else if(i == 6){
					if(j %2 != 0){
						board[j][i] = CellElements.RED;
					}
					else{
						board[j][i] = CellElements.EMPTY;
					}
				}
			
			
			}
		}	
	}

	public void displayInputDetails(){
		System.out.println("Enter your moves in the form \"startRow,startColumn;endRow,endColumn\" "
							+ "OR"
							+"Enter your moves in the form \"xStart,yStsart;xEnd,yEnd\" "
							+ "where bottom left is (0,0)" 
							);
	}

	public void displayBoard() {
		for (int i = 0; i <= totalRows-1; i++) {
		    
			System.out.print(totalRows - i -1  + " " +  "|");
			for (int j = 0; j <= totalColumns-1; j++) {
				CellElements tile = board[j][i];
				System.out.print(tile.getSymbol() + "|");
			}
			System.out.print("\n");
		}
		System.out.println("   0 1 2 3 4 5 6 7");
	}


	public void promptPlayerMove(Scanner prompt) {

		// Tell the players who we're asking input from.
		Player currentPlayer = players[currentPlayerIndex];
		CellElements playerColor = currentPlayer.getColor();
		String currentPlayerName = currentPlayer.getName();
		System.out.println("It is " + currentPlayer.getName()
				+ "'s turn. Please specify a move: ");

		// If the player has a piece that can jump, they must choose
		// that piece. Find all of the player's pieces which have
		// eligible jumps.
		JUMPERS.clear();
		for (int w = 0; w < totalColumns; ++w) {
			for (int h = 0; h < totalRows; ++h) {
				CellElements tile = board[w][h];
				if (playerColor == tile) {
					boolean canJump = this.canPieceJump(w, 7 - h);
					if (canJump) {
						Integer[] jumpPosition = new Integer[2];
						jumpPosition[0] = w;
						jumpPosition[1] = (7 - h);
						JUMPERS.add(jumpPosition);
					}
				}
			}
		}

		// Keep asking the player to make moves until they run out of follow-up
		// moves (more jumps in the series).
		boolean hasMoves = true;
		while (hasMoves) {

			// Keep asking this player for input until they've given us an
			// answer which represents a valid move.
			String moveString = prompt.nextLine();
			Optional<PlayerMove> moveOption = parseValidMove(moveString);
			while (!moveOption.isPresent()) {
				System.out.println("Please enter a valid move, "
						+ currentPlayerName + ": ");
				moveString = prompt.nextLine();
				moveOption = parseValidMove(moveString);
			}
			PlayerMove validMove = moveOption.get();

			// Execute the player's move.
			executeMove(validMove);

			// Check if this player still has follow-up moves.
			hasMoves = hasFollowUpMoves(validMove);

			// If the player still has moves, tell them.
			if (hasMoves) {
				System.out.println();
				System.out.println("Continue, " + currentPlayerName);
			}
		}

		// Pass control of next turn to the other player.
		swapPlayerIndex();
		currentPlayerMoves.clear();
	}

	private void swapPlayerIndex() {
		currentPlayerIndex = (currentPlayerIndex == 1)?0:1;
	}

	private boolean hasFollowUpMoves(PlayerMove move) {

		// If this piece just playerJumped it might have follow-up jumps.
		if (move.playerJumped) {

			// Find the two possible locations this piece could still jump to.
			Player currentPlayer = players[currentPlayerIndex];
			CellElements playerColor = currentPlayer.getColor();
			int directionModifier = 0;
			if (playerColor == CellElements.RED) {
				directionModifier = 1;
			} else {
				directionModifier = -1;
			}

			int endX = move.endX;
			int endY = move.endY;
			int targetOneX = endX - 2;
			int targetTwoX = endX + 2;
			int targetY = endY + (2 * directionModifier);
			String targetOne = endX + "," + endY + ";" + targetOneX + ","
					+ targetY;
			String targetTwo = endX + "," + endY + ";" + targetTwoX + ","
					+ targetY;
			Optional<PlayerMove> targetOneOption = parseValidMove(targetOne);
			Optional<PlayerMove> targetTwoOption = parseValidMove(targetTwo);

			boolean availableMoves = false;
			if (targetOneOption.isPresent()) {
				currentPlayerMoves.add(targetOneOption.get());
				availableMoves = true;
			}
			if (targetTwoOption.isPresent()) {
				currentPlayerMoves.add(targetTwoOption.get());
				availableMoves = true;
			}
			return availableMoves;
		}

		return false;
	}

	protected void executeMove(PlayerMove move) {
		int startX = move.startX;
		int startY = move.startY;
		int endX = move.endX;
		int endY = move.endY;

		// Move the piece to its new location.
		CellElements piece = board[startX][(totalRows - 1) - startY];
		this.board[endX][(totalRows - 1) - endY] = piece;
		this.board[startX][(totalRows - 1) - startY] = CellElements.EMPTY;

		// Remove whatever piece it might have killed.
		boolean playerJumped = move.playerJumped;
		if (playerJumped) {
			int capturedX = move.capturedX;
			int capturedY = move.capturedY;
			this.board[capturedX][(totalRows - 1)
					- capturedY] = CellElements.EMPTY;
		}
	}

	// Checks whether or not the piece at position x,y can jump.
	private boolean canPieceJump(int pieceX, int pieceY) {

		Player currentPlayer = players[currentPlayerIndex];
		CellElements playerColor = currentPlayer.getColor();
		int directionModifier = 0;
		if (playerColor == CellElements.RED) {
			directionModifier = 1;
		} else {
			directionModifier = -1;
		}

		int targetOneX = pieceX - 2;
		int targetTwoX = pieceX + 2;
		int targetY = pieceY + (2 * directionModifier);
		String targetOne = pieceX + "," + pieceY + ";" + targetOneX + ","
				+ targetY;
		String targetTwo = pieceX + "," + pieceY + ";" + targetTwoX + ","
				+ targetY;
		Optional<PlayerMove> targetOneOption = parseValidMove(targetOne);
		Optional<PlayerMove> targetTwoOption = parseValidMove(targetTwo);

		return (targetOneOption.isPresent() || targetTwoOption.isPresent());
	}

	protected Optional<PlayerMove> parseValidMove(String moveString) {

		// Check if the input can split into two cells.
		String[] splitMove = moveString.split(";");
		if (splitMove.length != 2) {
			return Optional.empty();
		}

		// Check if the two cells can split into two coordinates.
		String[] cellFrom = (splitMove[0]).split(",");
		String[] cellTo = (splitMove[1]).split(",");
		if (cellFrom.length != 2 || cellTo.length != 2) {
			return Optional.empty();
		}

		// Check if the cell coordinates can be properly parsed.
		try {
			int startX = Integer.parseInt(cellFrom[0]);
			int startY = Integer.parseInt(cellFrom[1]);
			int endX = Integer.parseInt(cellTo[0]);
			int endY = Integer.parseInt(cellTo[1]);

			Player currentPlayer = players[currentPlayerIndex];
			CellElements playerColor = currentPlayer.getColor();

			// Check to see if these coordinates are valid on the board.
			if (startX >= 0 && startX <= 7 && startY >= 0 && startY <= 7 && endX >= 0
					&& endX <= 7 && endY >= 0 && endY <= 7) {

				// Prevent the user from moving from and to the same tile.
				if (startX != endX && startY != endY) {

					// Make sure this player is trying to move their own piece.
					// The offset value is to translate the origin for the
					// players. They would expect 0,0 to be bottom-left.
					CellElements movingPiece = board[startX][(totalRows
							- 1) - startY];
					if (playerColor != movingPiece) {
						return Optional.empty();
					}

					// Make sure this player is trying to move to an empty spot.
					CellElements destination = board[endX][(totalRows - 1)
							- endY];
					if (destination != CellElements.EMPTY) {
						return Optional.empty();
					}

					// Figure out what direction this player is allowed to move
					// in. RED moves up the board, WHITE moves down it.
					int directionModifier = 0;
					if (playerColor == CellElements.RED) {
						directionModifier = 1;
					} else {
						directionModifier = -1;
					}

					// Figure out if this move is valid for distance reasons.
					// This restricts each piece to its four valid destinations.
					int xDist = Math.abs(startX - endX);
					int expectedY;
					boolean isJump = false;
					if (xDist == 1) {
						expectedY = startY + directionModifier;
					} else if (xDist == 2) {
						isJump = true;
						expectedY = startY + (2 * directionModifier);
					} else {
						return Optional.empty();
					}

					// Make sure we moved in the right direction.
					if (expectedY != endY) {
						return Optional.empty();
					}

					// If this is a jump, make sure we actually playerJumped over an
					// opponent's piece.
					PlayerMove validMove = new PlayerMove(startX, startY, endX, endY);
					if (isJump) {
						int opponentY = (expectedY - directionModifier);
						int opponentX;
						if (startX > endX) {
							opponentX = startX - 1;
						} else {
							opponentX = startX + 1;
						}

						// Actually perform the check to see opponent state.
						CellElements opponentPiece = board[opponentX][(totalRows
								- 1) - opponentY];
						if ((playerColor == CellElements.RED
								&& opponentPiece != CellElements.WHITE)
								|| (playerColor == CellElements.WHITE
										&& opponentPiece != CellElements.RED)) {
							return Optional.empty();
						} else {
							validMove.capturedX = opponentX;
							validMove.capturedY = opponentY;
							validMove.playerJumped = true;
						}
					}

					// We've passed all potential invalidity checks, but the
					// player might be trying to follow a jump with a valid move
					// that's not directly in the jump's chain. We need to
					// restrict their valid moves in this case.
					boolean isValidOption = false;
					for (PlayerMove m : currentPlayerMoves) {
						int mX = m.endX;
						int mY = m.endY;
						if (endX == mX && endY == mY) {
							isValidOption = true;
							break;
						}
					}

					// If we found jump-capable pieces, is this one?
					// And did it jump?
					if (JUMPERS.size() > 0) {
						boolean wasJumper = false;
						for (Integer[] position : JUMPERS) {
							int posX = position[0];
							int posY = position[1];
							if (posX == startX && posY == startY) {
								wasJumper = true;
								break;
							}
						}
						if (!wasJumper) {
							return Optional.empty();
						} else if (!isJump) {
							return Optional.empty();
						}
					}

					// The player either has complete freedom to choose this
					// move, or it's part of a jump chain. If they're trying to
					// cheat their way out of a jump chain, then this is not a
					// valid move.
					if (currentPlayerMoves.size() == 0 || isValidOption) {
						return Optional.of(validMove);
					} else {
						return Optional.empty();
					}
				}
			}
		} catch (NumberFormatException e) {
			return Optional.empty();
		}

		// This input failed to parse into a valid move.
		return Optional.empty();
	}

	public boolean isGameRunning() {
		return true;

		// TODO: this is where I ran out of time.
		// I would check, for the player whose turn it is, whether any of their
		// pieces have valid moves. If not, then the game must be over.
	}




}
