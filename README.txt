============
Chess Engine
============

The project represents an iterative development of a
chess engine that gradually incorporates more advanced
features, starting from basic movement rules and
progressing to the integration of the Negamax algorithm
with Alpha-Beta Pruning for strategic decision-making.

-Chess Game Logic:
The project likely includes the fundamental logic required
for playing chess, including move generation, piece movements,
and capturing.
-Check Detection:
The check method checks whether the king is in check. It considers
various threats from different types of pieces (bishops, queens,
knights, etc.).
-Castling Implementation:
Castling is implemented with separate methods for king-side
(makeMoveCastling) and queen-side (makeMoveCastlingWild) castling.
-Piece Movements:
The makeMove method handles regular piece movements, including pawn
promotion when a pawn reaches the last rank.
-King Position Tracking:
The whereIsTheKing method determines the position of the king on the
chessboard. This information is crucial for check detection and castling
conditions.
-Rook Movement Tracking:
There are boolean flags (rook1_moves, rook2_moves, rook3_moves, rook4_moves)
that seem to track whether certain rooks have moved. This might be relevant
for castling conditions.
-Printing the Chessboard:
The printTable and printTabletoString methods are used to display the current
state of the chessboard. These methods can be helpful for debugging and
visualizing the game state.
-Engine Color and Conditions:
The code references an engineColor variable, suggesting the existence of an
engine (chess AI). The castling conditions, like kingCastlingCondition and
queenCastlingCondition, indicate that the engine might make decisions based
on game state.
-Modular Code Structure:
The code is organized into methods, each handling specific aspects of the
chess game. This modular approach makes the code more readable and maintainable.
