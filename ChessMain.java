import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

enum State {

    ACTIV, INACTIVE, NONE;
}

enum Color {

    WHITE, BLACK, NONE;
}

enum Turn {

    ENGINE, PLAYER, END;
}

/*
 * Clasa Position este folosita pentru a determina pozitia unei piese pe tabla.
 */
class Position {

    public int line;
    public int column;

    public Position(int line, int column) {

        this.line = line;
        this.column = column;
    }
}

/*
 * Clasa Moves este folosita pentru a crea mutarile de pe tabla.
 */
class Moves {

    public int currentLine;
    public int currentColumn;
    public int futureLine;
    public int futureColumn;
    public int order;

    public Moves(int currentLine, int currentColumn, int futureLine,
            int futureColumn,int order) {

        this.currentLine = currentLine;
        this.currentColumn = currentColumn;
        this.futureLine = futureLine;
        this.futureColumn = futureColumn;
        this.order = order;
    }

    public Moves() {
        this(0, 0, 0, 0 ,0);
    }

    public Moves clone() {
        return new Moves(currentLine, currentColumn, futureLine, futureColumn, order);
    }

    public String toString() {
        String s = new String();
        s = "(" + currentLine + " " + currentColumn + " " + futureLine + " "
                + futureColumn + ")";
        return s;
    }
}

class Pieces {

    public static final int WHITE_PAWN = 1;
    public static final int BLACK_PAWN = -1;
    public static final int WHITE_HORSE = 2;
    public static final int BLACK_HORSE = -2;
    public static final int WHITE_BISHOP = 3;
    public static final int BLACK_BISHOP = -3;
    public static final int WHITE_ROOK = 4;
    public static final int BLACK_ROOK = -4;
    public static final int WHITE_QUEEN = 5;
    public static final int BLACK_QUEEN = -5;
    public static final int WHITE_KING = 6;
    public static final int BLACK_KING = -6;
    public static final int BLANK = 0;

}

public class ChessMain {

    /*
     * Starea initiala a jocului este inactiva, engine-ul are culoarea implicita
     * negru si primul la rand pentru a muta este jucatorul.
     */
    public Color engineColor = Color.BLACK;
    public Turn turn = Turn.PLAYER;
    public static final int COLUMNS = 8;
    public static final int ROWS = 8;
    public int[][] table;
    public State state = State.INACTIVE;
    public int colorState = -1;
    public boolean colorChanged = false;
    public static ArrayList<Moves> allmoves = new ArrayList<Moves>();
    public static String castling[] = {"move e1g1" , "move e8g8"};
    public static String wild_castling[] = {"move e1c1","move e8c8"};
    public boolean rook1_moves = true;
    public boolean rook2_moves = true;
    public boolean rook3_moves = true;
    public boolean rook4_moves = true;
    public static String finalcommand;
    public static int MAXDEPTH = 4;
    public static int movesNumber = 0;
    public static String movesHistory = "";
    public static String movesHistory2 = ""; 

    /*
     * Bonus pentru piese in functie de pozitia ocupata.
     */
    private static int[][] PawnTableBlack = new int[][]{
        {0, 0, 0, 0, 0, 0, 0, 0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5, 5, 10, 25, 25, 10, 5, 5},
        {0, 0, 0, 20, 20, 0, 0, 0},
        {5, -5, -10, 0, 0, -10, -5, 5},
        {5, 10, 10, -20, -20, 10, 10, 5},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private static int[][] PawnTableWhite = new int[][]{
        {0, 0, 0, 0, 0, 0, 0, 0},
        {5, 10, 10, -20, -20, 10, 10, 5},
        {5, -5, -10, 0, 0, -10, -5, 5},
        {0, 0, 0, 20, 20, 0, 0, 0},
        {5, 5, 10, 25, 25, 10, 5, 5},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private static int[][] KnightTableBlack = new int[][]{
        {-50, -40, -30, -30, -30, -30, -40, -50},
        {-40, -20, 0, 0, 0, 0, -20, -40},
        {-30, 0, 10, 15, 15, 10, 0, -30},
        {-30, 5, 15, 20, 20, 15, 5, -30},
        {-30, 0, 15, 20, 20, 15, 0, -30},
        {-30, 5, 10, 15, 15, 10, 5, -30},
        {-40, -20, 0, 5, 5, 0, -20, -40},
        {-50, -40, -20, -30, -30, -20, -40, -50}
    };

    private static int[][] KnightTableWhite = new int[][]{
        {-50, -40, -20, -30, -30, -20, -40, -50},
        {-40, -20, 0, 5, 5, 0, -20, -40},
        {-30, 5, 10, 15, 15, 10, 5, -30},
        {-30, 0, 15, 20, 20, 15, 0, -30},
        {-30, 5, 15, 20, 20, 15, 5, -30},
        {-30, 0, 10, 15, 15, 10, 0, -30},
        {-40, -20, 0, 0, 0, 0, -20, -40},
        {-50, -40, -30, -30, -30, -30, -40, -50}
    };

    private static int[][] BishopTableBlack = new int[][]{
        {-20, -10, -10, -10, -10, -10, -10, -20},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-10, 0, 5, 10, 10, 5, 0, -10},
        {-10, 5, 5, 10, 10, 5, 5, -10},
        {-10, 0, 10, 10, 10, 10, 0, -10},
        {-10, 10, 10, 10, 10, 10, 10, -10},
        {-10, 5, 0, 0, 0, 0, 5, -10},
        {-20, -10, -10, -10, -10, -10, -10, -20}
    };

    private static int[][] BishopTableWhite = new int[][]{
        {-20, -10, -10, -10, -10, -10, -10, -20},
        {-10, 5, 0, 0, 0, 0, 5, -10},
        {-10, 10, 10, 10, 10, 10, 10, -10},
        {-10, 0, 10, 10, 10, 10, 0, -10},
        {-10, 5, 5, 10, 10, 5, 5, -10},
        {-10, 0, 5, 10, 10, 5, 0, -10},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-20, -10, -10, -10, -10, -10, -10, -20}
    };

    private static int[][] RookTableBlack = new int[][]{
        {0, 0, 0, 0, 0, 0, 0, 0},
        {5, 10, 10, 10, 10, 10, 10, 5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {0, 0, 0, 5, 5, 0, 0, 0}
    };

    private static int[][] RookTableWhite = new int[][]{
        {0, 0, 0, 5, 5, 0, 0, 0},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {5, 10, 10, 10, 10, 10, 10, 5},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private static int[][] QuuenTableBlack = new int[][]{
        {-20, -10, -10, -5, -5, -10, -10, -20},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-10, 0, 5, 5, 5, 5, 0, -10},
        {-5, 0, 5, 5, 5, 5, 0, -5},
        {0, 0, 5, 5, 5, 5, 0, -5},
        {-10, 5, 5, 5, 5, 5, 0, -10},
        {-10, 0, 5, 0, 0, 0, 0, -10},
        {-20, -10, -10, -5, -5, -10, -10, -20}
    };

    private static int[][] QuuenTableWhite = new int[][]{
        {-20, -10, -10, -5, -5, -10, -10, -20},
        {-10, 0, 5, 0, 0, 0, 0, -10},
        {-10, 5, 5, 5, 5, 5, 0, -10},
        {0, 0, 5, 5, 5, 5, 0, -5},
        {-5, 0, 5, 5, 5, 5, 0, -5},
        {-10, 0, 5, 5, 5, 5, 0, -10},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-20, -10, -10, -5, -5, -10, -10, -20}
    };

    private static int[][] KingTableMiddleWhite = new int[][]{
        {20, 30, 10, 0, 0, 10, 30, 20},
        {20, 20, 0, 0, 0, 0, 20, 20},
        {-10, -20, -20, -20, -20, -20, -20, -10},
        {-20, -30, -30, -40, -40, -30, -30, -20},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30}
    };

    private static int[][] KingTableMiddleBlack = new int[][]{
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-20, -30, -30, -40, -40, -30, -30, -20},
        {-10, -20, -20, -20, -20, -20, -20, -10},
        {20, 20, 0, 0, 0, 0, 20, 20},
        {20, 30, 10, 0, 0, 10, 30, 20}
    };

    private static int[][] KingTableEndBlack = new int[][]{
        {-50, -40, -30, -20, -20, -30, -40, -30},
        {-30, -20, -10, 0, 0, -10, -20, -30},
        {-30, -10, 20, 30, 30, 20, -10, -30},
        {-30, -10, 30, 40, 40, 30, -10, -30},
        {-30, -10, 30, 40, 40, 30, -10, -30},
        {-30, -10, 20, 30, 30, 20, -10, -30},
        {-30, -30, 0, 0, 0, 0, -30, -30},
        {-50, -30, -30, -30, -30, -30, -30, -50}
    };

    private static int[][] KingTableEndWhite = new int[][]{
        {-50, -30, -30, -30, -30, -30, -30, -50},
        {-30, -30, 0, 0, 0, 0, -30, -30},
        {-30, -10, 20, 30, 30, 20, -10, -30},
        {-30, -10, 30, 40, 40, 30, -10, -30},
        {-30, -10, 30, 40, 40, 30, -10, -30},
        {-30, -10, 20, 30, 30, 20, -10, -30},
        {-30, -20, -10, 0, 0, -10, -20, -30},
        {-50, -40, -30, -20, -20, -30, -40, -30}
    };

    public static void main(String[] args) throws IOException {

        String command = new String();
        BufferedReader buff = new BufferedReader(new InputStreamReader(
                System.in));
        ChessMain chess = new ChessMain();

        System.out.println("feature sigint=0");
        System.out.flush();
        System.out.println("feature sigterm=0");
        System.out.flush();
        System.out.println("feature usermove=1");
        System.out.flush();
        
        do {
            if (chess.turn == Turn.ENGINE && chess.state == State.ACTIV) {
                if (chess.moveEngine() == false) {
                    chess.turn = Turn.END;
                } else {
                    chess.turn = Turn.PLAYER;
                }
            }
            /*
             * Daca engine-ul nu mai are mutari valide posibile si starea
             * jocului este activa atunci dam resign apoi avem varianta new sau
             * quit.
             */
            if (chess.turn == Turn.END && chess.state == State.ACTIV) {
                chess.state = State.INACTIVE;
                System.out.println(finalcommand);
                System.out.flush();
                continue;
            }

            command = buff.readLine();

            if (command.startsWith("usermove") && chess.state == State.ACTIV) {
                chess.movePlayer(command.split(" ")[1]);
                chess.turn = Turn.ENGINE;
                continue;
            }

            if (command.startsWith("xboard")) {
                chess.state = State.ACTIV;
                continue;
            }
            /*
             * Sunt reinitializate toate variabilele.
             */
            if (command.startsWith("new")) {
                chess.colorState = -1;
                chess.turn = Turn.PLAYER;
                chess.engineColor = Color.BLACK;
                chess.state = State.ACTIV;
                chess.initTable();
                chess.colorChanged = false;
                continue;
            }

            if (command.startsWith("white") && chess.state == State.ACTIV) {
                if (chess.colorChanged == true) {
                    chess.turn = Turn.ENGINE;
                    chess.engineColor = Color.WHITE;
                    chess.colorState = 1;
                    chess.colorChanged = false;
                } else {
                    chess.colorChanged = true;
                }
                continue;
            }

            if (command.startsWith("black") && chess.state == State.ACTIV) {
                if (chess.colorChanged == true) {
                    chess.turn = Turn.ENGINE;
                    chess.engineColor = Color.BLACK;
                    chess.colorState = -1;
                    chess.colorChanged = false;
                } else {
                    chess.colorChanged = true;
                }
                continue;
            }
            if (command.startsWith("force")) {
                continue;
            }
            if (command.startsWith("go")) {
                continue;
            }
            if (command.startsWith("quit")) {
                return;
            }

        } while (true);
    }

    public void initTable() {

        table = new int[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; ++i) {
            for (int j = 0; j < COLUMNS; ++j) {
                table[i][j] = 0;
            }
        }

        for (int i = 0; i < COLUMNS; ++i) {
            table[1][i] = Pieces.WHITE_PAWN;
            table[6][i] = Pieces.BLACK_PAWN;
        }

        table[0][1] = table[0][6] = Pieces.WHITE_HORSE;
        table[7][1] = table[7][6] = Pieces.BLACK_HORSE;

        table[0][2] = table[0][5] = Pieces.WHITE_BISHOP;
        table[7][2] = table[7][5] = Pieces.BLACK_BISHOP;

        table[0][0] = table[0][7] = Pieces.WHITE_ROOK;
        table[7][0] = table[7][7] = Pieces.BLACK_ROOK;

        table[0][3] = Pieces.WHITE_QUEEN;
        table[7][3] = Pieces.BLACK_QUEEN;

        table[0][4] = Pieces.WHITE_KING;
        table[7][4] = Pieces.BLACK_KING;
    }

    /*
     * Metoda pentru a decodifica comanda primita de la xboard din partea
     * playerul-ui si returneaza mutarea reprezentand indicii din matrice.
     */
    public Moves decodeMove(String moveCommand) {

        int currentLine, currentColumn, futureLine, futureColumn;

        currentColumn = moveCommand.charAt(0) - 'a';
        currentLine = moveCommand.charAt(1) - '1';
        futureColumn = moveCommand.charAt(2) - 'a';
        futureLine = moveCommand.charAt(3) - '1';

        Moves move = new Moves(currentLine, currentColumn, futureLine,
                futureColumn , 0);

        return move;
    }

    /*
     * Metoda pentru a codifica o mutare a engine-ului pentru a transmite-o
     * xbordului, returneaza StringBufferul de forma a2a3 etc. .
     */
     public static String encodeMove(Moves move) {

        String moveEngine = "";
        moveEngine +="move ";
        char move1[] = new char[4];
        move1[0] = (char)('a' + move.currentColumn);
        move1[1] = (char) ('1' + move.currentLine);
        move1[2] = (char) ('a' + move.futureColumn);
        move1[3] = (char) ('1' + move.futureLine);       
        String abc = new String(move1);
        moveEngine += abc;
        return moveEngine;
    }

    /*
     * Mutarea playerul-ui.
     */
    public boolean movePlayer(String moveCommand) throws IOException {

        Moves move = decodeMove(moveCommand);
        movesNumber ++ ;
        movesHistory += moveCommand + " ";
        if (moveCommand.endsWith("q")) {
            if (makeMove(move, 1, table)) {
                return true;
            } else {
                return false;
            }
        }

        if (castling[0].toString().contains(moveCommand) 
                || castling[1].toString().contains(moveCommand)) {
            makeMoveCastling(move, table);
            return true;
        }

        if (wild_castling[0].toString().contains(moveCommand) 
                || wild_castling[1].toString().contains(moveCommand)) {
            makeMoveCastlingWild(move, table);
            return true;
        }

        makeMove(move, 0, table);
        return true;
    }

    /*
     * Metoda genereaza lista de mutari posibile ale tuturor pieselor
     * engine-ului, obtinand lista de "allmoves".
     */
    public ArrayList<Moves> generateAllMoves(int[][] tabela) {

        ArrayList<Moves> allmoves = new ArrayList<Moves>();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if ((-colorState) * tabela[i][j] < 0
                        && tabela[i][j] == Pieces.BLACK_PAWN * (-colorState)) {
                    allmoves.addAll(generateAllMovesPaws(new Position(i, j), tabela));
                }
                if ((-colorState) * tabela[i][j] < 0
                        && tabela[i][j] == Pieces.BLACK_HORSE * (-colorState)) {
                    allmoves.addAll(generateAllMovesHorse(new Position(i, j), tabela));
                }
                if ((-colorState) * tabela[i][j] < 0
                        && tabela[i][j] == Pieces.BLACK_BISHOP * (-colorState)) {
                    allmoves.addAll(generateAllMovesBishop(new Position(i, j), tabela));
                }
                if ((-colorState) * tabela[i][j] < 0
                        && tabela[i][j] == Pieces.BLACK_ROOK * (-colorState)) {
                    allmoves.addAll(generateAllMovesRook(new Position(i, j), tabela));
                }
                if ((-colorState) * tabela[i][j] < 0
                        && tabela[i][j] == Pieces.BLACK_QUEEN * (-colorState)) {
                    allmoves.addAll(generateAllMovesQueen(new Position(i, j), tabela));
                }
                if ((-colorState) * tabela[i][j] < 0
                        && tabela[i][j] == Pieces.BLACK_KING * (-colorState)) {
                    allmoves.addAll(generateAllMovesKing(new Position(i, j), tabela));
                }
            }
        }

        allmoves.addAll(generateCastlingMoves(tabela));
        return allmoves;
    }

    /*
     * Metoda returneaza mutarile posibile pentru un pion printre care si
     * deschiderea pionului cu doua pozitii in fata. (-colorState) *
     * table[initialposition.line + colorState][initialposition.column - 1] > 0
     * se traduce prin faptul ca pe acea pozitie din tabla se afla o piesa a
     * adversarului, < 0 inseamna ca este piesa proprie engine-ului.
     * initialposition.line + colorState - linia creste sau scade in functie de
     * culoarea engine-ului.
     */
    public ArrayList<Moves> generateAllMovesPaws(Position initialposition, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        if (initialposition.line + colorState < ROWS
                && initialposition.line + colorState >= 0) {
            if (tabela[initialposition.line + colorState][initialposition.column] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line
                        + colorState, initialposition.column,0));
            }
        }

        if (initialposition.line + colorState < ROWS
                && initialposition.column - 1 >= 0
                && initialposition.line + colorState >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line + colorState][initialposition.column - 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line + colorState][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + colorState][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line
                        + colorState, initialposition.column - 1,1));
            }
        }

        if (initialposition.line + colorState < ROWS
                && initialposition.column + 1 < COLUMNS
                && initialposition.line + colorState >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line + colorState][initialposition.column + 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line + colorState][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + colorState][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line
                        + colorState, initialposition.column + 1,1));
            }
        }

        if (colorState == 1) {
            if (initialposition.line == 1) {
                if (tabela[initialposition.line + 1][initialposition.column] == 0) {
                    if (tabela[initialposition.line + 2][initialposition.column] == 0) {
                        moves.add(new Moves(initialposition.line,
                                initialposition.column,
                                initialposition.line + 2,
                                initialposition.column,0));
                    }
                }
            }
        }

        if (colorState == -1) {
            if (initialposition.line == 6) {
                if (tabela[initialposition.line - 1][initialposition.column] == 0) {
                    if (tabela[initialposition.line - 2][initialposition.column] == 0) {
                        moves.add(new Moves(initialposition.line,
                                initialposition.column,
                                initialposition.line - 2,
                                initialposition.column,0));
                    }
                }
            }
        }

        return moves;
    }

    /*
     * Metoda returneaza mutarile posibile pentru cal, avand maxim opt mutari
     * valide. Mutarea este valida daca pe pozitia viitoare se gaseste o piesa a
     * adversarului sau este libera.
     */
    public ArrayList<Moves> generateAllMovesHorse(Position initialposition, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        if (initialposition.line + 2 < ROWS
                && initialposition.column + 1 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line + 2][initialposition.column + 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 2,
                        initialposition.column + 1 , 0));
            }
            
             if ((-colorState)
                    * tabela[initialposition.line + 2][initialposition.column + 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 2,
                        initialposition.column + 1 , 1));
            }
            
            
        }

        if (initialposition.line + 2 < ROWS && initialposition.column - 1 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line + 2][initialposition.column - 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 2,
                        initialposition.column - 1 , 0));
            }
            
            if ((-colorState)
                    * tabela[initialposition.line + 2][initialposition.column - 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 2][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 2,
                        initialposition.column - 1 , 1));
            }
        }

        if (initialposition.line + 1 < ROWS
                && initialposition.column + 2 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 2] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column + 2, 0));
            }
            
            
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 2] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column + 2, 1));
            }
        }

        if (initialposition.line + 1 < ROWS && initialposition.column - 2 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 2] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column - 2, 0));
            }
            
            
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 2] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column - 2, 1));
            }
        }

        if (initialposition.line - 2 >= 0
                && initialposition.column + 1 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line - 2][initialposition.column + 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 2,
                        initialposition.column + 1 , 0));
            }
            
            if ((-colorState)
                    * tabela[initialposition.line - 2][initialposition.column + 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 2,
                        initialposition.column + 1 , 1));
            }
        }

        if (initialposition.line - 2 >= 0 && initialposition.column - 1 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line - 2][initialposition.column - 1] >= 0
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 2,
                        initialposition.column - 1,0));
            }
            
            
            if ((-colorState)
                    * tabela[initialposition.line - 2][initialposition.column - 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 2][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 2,
                        initialposition.column - 1, 1));
            }
        }

        if (initialposition.line - 1 >= 0
                && initialposition.column + 2 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 2] == 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column + 2,0));
            }
            
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 2] > 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column + 2,1));
            }
        }

        if (initialposition.line - 1 >= 0 && initialposition.column - 2 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 2] >= 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column - 2,  0));
            }
            
            
             if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 2] >= 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 2] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 2] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column - 2 , 1));
            }
        }

        return moves;
    }

    /*
     * Metoda returneaza toate mutarile valide pentru nebun. (More in Readme)
     */
    public ArrayList<Moves> generateAllMovesBishop(Position initialposition, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int line, column;
        line = initialposition.line;
        column = initialposition.column;

        while ((line + 1) < ROWS && (column + 1) < COLUMNS) {
            if (tabela[line + 1][column + 1] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line + 1, column + 1 , 0));
                line++;
                column++;
            } else if ((-colorState) * tabela[line + 1][column + 1] > 0
                    && (-colorState) * tabela[line + 1][column + 1] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line + 1][column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line + 1, column + 1, 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line + 1) < ROWS && (column - 1) >= 0) {
            if (tabela[line + 1][column - 1] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line + 1, column - 1 ,0));
                line++;
                column--;
            } else if ((-colorState) * tabela[line + 1][column - 1] > 0
                    && (-colorState) * tabela[line + 1][column - 1] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line + 1][column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line + 1, column - 1, 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line - 1) >= 0 && (column + 1) < COLUMNS) {
            if (tabela[line - 1][column + 1] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line - 1, column + 1 ,0));
                line--;
                column++;
            } else if ((-colorState) * tabela[line - 1][column + 1] > 0
                    && (-colorState) * tabela[line - 1][column + 1] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line - 1][column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line - 1, column + 1, 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line - 1) >= 0 && (column - 1) >= 0) {
            if (tabela[line - 1][column - 1] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line - 1, column - 1, 0));
                line--;
                column--;
            } else if ((-colorState) * tabela[line - 1][column - 1] > 0
                    && (-colorState) * tabela[line - 1][column - 1] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line - 1][column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line - 1, column - 1 , 1));
                break;
            } else {
                break;
            }
        }

        return moves;
    }

    /*
     * Metoda returneaza toate mutarile valide pentru tura. (More in Readme)
     */
    public ArrayList<Moves> generateAllMovesRook(Position initialposition, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int line, column;
        line = initialposition.line;
        column = initialposition.column;

        while ((line + 1) < ROWS) {
            if (tabela[line + 1][column] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line + 1, column ,0 ));
                line++;
            } else if ((-colorState) * tabela[line + 1][column] > 0
                    && (-colorState) * tabela[line + 1][column] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line + 1][column] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line + 1, column , 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line - 1) >= 0) {
            if (tabela[line - 1][column] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line - 1, column,0));
                line--;
            } else if ((-colorState) * tabela[line - 1][column] > 0
                    && (-colorState) * tabela[line - 1][column] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line - 1][column] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line - 1, column, 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((column + 1) < COLUMNS) {
            if (tabela[line][column + 1] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line, column + 1 ,0));
                column++;
            } else if ((-colorState) * tabela[line][column + 1] > 0
                    && (-colorState) * tabela[line][column + 1] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line][column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line, column + 1, 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((column - 1) >= 0) {
            if (tabela[line][column - 1] == 0) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line, column - 1 ,0));
                column--;
            } else if ((-colorState) * tabela[line][column - 1] > 0
                    && (-colorState) * tabela[line][column - 1] != Pieces.WHITE_KING
                    && (-colorState) * tabela[line][column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, line, column - 1, 1));
                break;
            } else {
                break;
            }
        }

        return moves;
    }

    /*
     * Metoda returneaza toate mutarile valide pentru regina, combinand mutarile
     * posibile pentru comportamentul asemanator unei ture cu cele posibile
     * pentru un comportament asemenator cu al unui nebun.
     */
    public ArrayList<Moves> generateAllMovesQueen(Position initialposition, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        moves.addAll(generateAllMovesBishop(initialposition, tabela));
        moves.addAll(generateAllMovesRook(initialposition, tabela));

        return moves;
    }

    /*
     * Metoda genereaza toate mutarile valide pentru rege, avand maxim opt
     * mutari valide.
     */
    public ArrayList<Moves> generateAllMovesKing(Position initialposition, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        if (initialposition.line + 1 < ROWS) {
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column] != Pieces.BLACK_KING) {

                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column, 0));
            }
            
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column] != Pieces.BLACK_KING) {

                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column, 1));
            }
        }

        if (initialposition.line - 1 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column] == 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column, 0));
            }
            
            
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column] > 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column ,1));
            }
        }

        if (initialposition.column + 1 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line][initialposition.column + 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line,
                        initialposition.column + 1, 0));
            }
            
            if ((-colorState)
                    * tabela[initialposition.line][initialposition.column + 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line,
                        initialposition.column + 1, 1));
            } 
        }

        if (initialposition.column - 1 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line][initialposition.column - 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line,
                        initialposition.column - 1, 0));
            }
            
            if ((-colorState)
                    * tabela[initialposition.line][initialposition.column - 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line,
                        initialposition.column - 1 , 1));
            }
        }

        if (initialposition.line + 1 < ROWS
                && initialposition.column + 1 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column + 1 ,0));
            }
            
             if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column + 1 , 1));
            }
        }

        if (initialposition.line - 1 >= 0
                && initialposition.column + 1 < COLUMNS) {
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column + 1 ,0));
            }
            
            
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column + 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column + 1 , 1));
            }
        }

        if (initialposition.line + 1 < ROWS && initialposition.column - 1 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column - 1 , 0));
            }
            
             if ((-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line + 1][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line + 1,
                        initialposition.column - 1 , 1));
            }
        }

        if (initialposition.line - 1 >= 0 && initialposition.column - 1 >= 0) {
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 1] == 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column - 1 ,0));
            }
            if ((-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 1] > 0
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 1] != Pieces.WHITE_KING
                    && (-colorState)
                    * tabela[initialposition.line - 1][initialposition.column - 1] != Pieces.BLACK_KING) {
                moves.add(new Moves(initialposition.line,
                        initialposition.column, initialposition.line - 1,
                        initialposition.column - 1 , 1));
            }
            
        }

        return moves;
    }

    public ArrayList<Moves> generateCastlingMoves(int[][] tabela) {
        ArrayList<Moves> moves = new ArrayList<Moves>();
        if (colorState == -1) {
            if (rook4_moves && kingCastlingCondition(tabela)) {
                moves.add(new Moves(7, 4, 7, 6 ,0));
            }
            if (rook3_moves && queenCastlingCondition(tabela)) {
                moves.add(new Moves(7, 4, 7, 2 ,0));
            }
        }

        if (colorState == 1) {
            if (rook2_moves && kingCastlingCondition(tabela)) {
                moves.add(new Moves(0, 4, 0, 6 ,0));
            }
            if (rook1_moves && queenCastlingCondition(tabela)) {
                moves.add(new Moves(0, 4, 0, 2 ,0));
            }
        }

        return moves;
    }

    /*
     * Metoda creeaza si returneaza o copia a tablei(matrice) de joc.
     */
    public int[][] tableCopy(int[][] tabela) {

        int[][] table_copy = new int[ROWS][COLUMNS];

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                table_copy[i][j] = tabela[i][j];
            }
        }

        return table_copy;
    }

    /*
     * Metoda construieste lista de mutari valide, prin mutari valide
     * intelegandu-se acele mutari care scot regele din pozitia de sah in cazul
     * in care se afla in aceasta situatie sau nu lasa regele in pozitie de sah
     * prin mutarea facuta. (More in Readme)
     */
    public ArrayList<Moves> checkMoves(ArrayList<Moves> allmoves, int[][] tabela) {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int[][] table_copy;

        for (Moves move : allmoves) {
            table_copy = tableCopy(tabela);
            table_copy[move.futureLine][move.futureColumn] = table_copy[move.currentLine][move.currentColumn];
            table_copy[move.currentLine][move.currentColumn] = Pieces.BLANK;
            if (!check(table_copy)) {
                moves.add(move);
            }
        }
        return moves;
    }
    
    public int doubledPawns(int [][] tabela){
        int  numberOfDoubledPawns = 0;
        ArrayList<Position> pawnPosition = new ArrayList<Position>();
        if(colorState == 1){
            for(int i = 1 ; i < ROWS ; i++){
                for(int j = 0 ; j < COLUMNS ; j++){
                    if(tabela[i][j] == 1)
                        pawnPosition.add(new Position(i,j));
                }
            }
            
            for( int i = 0 ; i < pawnPosition.size() - 1 ; i++)
                for ( int j = i+1 ; j < pawnPosition.size() ; j++)
                    if (pawnPosition.get(i).column == pawnPosition.get(j).column)
                        numberOfDoubledPawns++;
            
            return numberOfDoubledPawns;
        }
        
        else{
            for(int i = 0 ; i < ROWS - 1  ; i++){
                for(int j = 0 ; j < COLUMNS ; j++){
                    if(tabela[i][j] == -1)
                        pawnPosition.add(new Position(i,j));
                }
            }
            
            for( int i = 0 ; i < pawnPosition.size() - 1 ; i++)
                for ( int j = i+1 ; j < pawnPosition.size() ; j++)
                    if (pawnPosition.get(i).column == pawnPosition.get(j).column)
                        numberOfDoubledPawns++;
            
            return numberOfDoubledPawns;
        }
    }
    
    public int blockedPawns(int [][] tabela){
        int  numberOfblockedPawns = 0;
        int line;
        int column;
        ArrayList<Position> pawnPosition = new ArrayList<Position>();
        if(colorState == 1){
            for(int i = 1 ; i < ROWS - 1 ; i++){
                for(int j = 0 ; j < COLUMNS ; j++){
                    if(tabela[i][j] == 1)
                        pawnPosition.add(new Position(i,j));
                }
            }
            
            for( int i = 0 ; i < pawnPosition.size() - 1 ; i++){
                line = pawnPosition.get(i).line ;
                column = pawnPosition.get(i).column;
                if(tabela[line + 1][column] != 0)
                    numberOfblockedPawns ++;
            }
            return numberOfblockedPawns;
        }
        
        else{
            for(int i = 1 ; i < ROWS - 1  ; i++){
                for(int j = 0 ; j < COLUMNS ; j++){
                    if(tabela[i][j] == -1)
                        pawnPosition.add(new Position(i,j));
                }
            }
            
             for( int i = 0 ; i < pawnPosition.size() - 1 ; i++){
                line = pawnPosition.get(i).line ;
                column = pawnPosition.get(i).column;
                if(tabela[line - 1][column] != 0)
                    numberOfblockedPawns ++;
            }
            return numberOfblockedPawns;
        }
    }
    
    
    public int isolatedPawns(int [][] tabela){
        int  numberOfIsolatedPawns = 0;
        int line;
        int column;
        ArrayList<Position> pawnPosition = new ArrayList<Position>();
        if(colorState == 1){
            for(int i = 1 ; i < ROWS - 1 ; i++){
                for(int j = 1 ; j < COLUMNS - 1 ; j++){
                    if(tabela[i][j] == 1)
                        pawnPosition.add(new Position(i,j));
                }
            }
            
            for( int i = 0 ; i < pawnPosition.size() - 1 ; i++){
                line = pawnPosition.get(i).line ;
                column = pawnPosition.get(i).column;
                if(tabela[line - 1][column - 1] != 1 && tabela[line - 1][column + 1] != 1)
                    numberOfIsolatedPawns ++;
            }
            return numberOfIsolatedPawns;
        }
        
        else{
            for(int i = 1 ; i < ROWS - 1 ; i++){
                for(int j = 1 ; j < COLUMNS - 1 ; j++){
                    if(tabela[i][j] == -1)
                        pawnPosition.add(new Position(i,j));
                }
            }
            
             for( int i = 0 ; i < pawnPosition.size() - 1 ; i++){
                line = pawnPosition.get(i).line ;
                column = pawnPosition.get(i).column;
                if(tabela[line - 1][column - 1] != 1 && tabela[line - 1][column + 1] != -1)
                    numberOfIsolatedPawns ++;
            }
            return numberOfIsolatedPawns;
        
        }
    }
    
    public int bishopCount(int [][]tabela){
        int bishopCount = 0;
        for(int i = 0 ; i < ROWS ; i++){
            for(int j = 0 ; j < COLUMNS; j++){
                if(tabela [i][j] * colorState == 3)
                    bishopCount ++;
            }
        }
        
        return bishopCount;
    }
    
    public int pieceNumber(int [][]tabela){
        int pieceNumber = 0;
        for(int i = 0 ; i < ROWS ; i++){
            for(int j = 0 ; j < COLUMNS; j++){
                if(tabela [i][j] != 0)
                    pieceNumber ++;
            }
        }
        
        return pieceNumber;
    }
    
    public int eval(int[][] tabela, int depth) {
        int score = 0;
        int pieceNumber = pieceNumber(tabela);
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tabela[i][j] == 1) {
                    score += (100 + PawnTableWhite[i][j]);
                }
                if (tabela[i][j] == -1) {
                    score += (-100 - PawnTableBlack[i][j]);
                }

                if (tabela[i][j] == 2) {
                    score += (320 + KnightTableWhite[i][j]);
                }
                if (tabela[i][j] == -2) {
                    score += (-320 - KnightTableBlack[i][j]);
                }

                if (tabela[i][j] == 3) {
                    score += (330 + BishopTableWhite[i][j]);
                }
                if (tabela[i][j] == -3) {
                    score += (-330 - BishopTableBlack[i][j]);
                }

                if (tabela[i][j] == 4) {
                    score += (500 + RookTableWhite[i][j]);
                }
                if (tabela[i][j] == -4) {
                    score += (-500 - RookTableBlack[i][j]);
                }

                if (tabela[i][j] == 5) {
                    score += (900 + QuuenTableWhite[i][j]);
                }
                if (tabela[i][j] == -5) {
                    score += (-900 - QuuenTableBlack[i][j]);
                }

                if (tabela[i][j] == 6) {
                    if(pieceNumber >= 10)
                        score += (20000 + KingTableMiddleWhite[i][j]);
                    else
                        score +=  (20000 + KingTableEndWhite[i][j]);
                }
                if (tabela[i][j] == -6) {
                    if(pieceNumber >= 10)
                        score += (-20000 + KingTableMiddleBlack[i][j]);
                    else
                        score +=  (-20000 + KingTableEndBlack[i][j]);   
                }
            }
        }
        
        // Negative score
        
        int Wscore = 0 ;
        int Bscore = 0 ;
        
        if (colorState == -1) {
            Bscore = (doubledPawns(tabela) + blockedPawns(tabela) + isolatedPawns(tabela)) * 50;
        } else {
            Wscore = (doubledPawns(tabela) + blockedPawns(tabela) + isolatedPawns(tabela)) * 50;
        }

        colorState *= -1;

        if (colorState == -1) {
            Bscore = (doubledPawns(tabela) + blockedPawns(tabela) + isolatedPawns(tabela)) * 50;
        } else {
            Wscore = (doubledPawns(tabela) + blockedPawns(tabela) + isolatedPawns(tabela)) * 50;
        }
  
        colorState *= -1;
       
        int nscore = (Wscore - Bscore) * (-colorState);
       
        // Mobility score
        int black_size = 0;
        int white_size = 0;

        if (colorState == -1) {
            black_size = checkMoves(generateAllMoves(tabela), tabela).size();
        } else {
            white_size = checkMoves(generateAllMoves(tabela), tabela).size();
        }

        colorState *= -1;

        if (colorState == -1) {
            black_size = checkMoves(generateAllMoves(tabela), tabela).size();
        } else {
            white_size = checkMoves(generateAllMoves(tabela), tabela).size();
        }

        colorState *= -1;

        //Sah mat bonus
        int mobilityscore = 0;
        if (black_size == 0 && colorState == -1) {
            score += 10000;
        }
        else if (white_size == 0 && colorState == 1) 
            score -= 10000;
        else if (white_size == 0 && colorState == -1) 
            score -= 10000;
        else if (black_size == 0 && colorState == 1) 
            score += 10000;        
        
        else{
            mobilityscore = (white_size - black_size) * colorState * 5;
        }
        
        return score * colorState + mobilityscore + nscore;
    }

    public Pair<Moves, Integer> pruning(int[][] level_table, int depth, int alfa, int beta)
            throws IOException {

        ArrayList<Moves> playerMoves = new ArrayList<Moves>();
        playerMoves = generateAllMoves(level_table);
        playerMoves = checkMoves(playerMoves, level_table);
        Collections.sort(playerMoves , new Comparator<Moves>(){
            public int compare(Moves m1 ,Moves m2){
                return m2.order - m1.order; 
            }
        });

        if (depth == 0 || playerMoves.size() == 0) {
            return new Pair<Moves, Integer>(new Moves(), eval(level_table, depth));
        }

        Pair<Moves, Integer> max = new Pair<Moves, Integer>(new Moves(), Integer.MIN_VALUE);
        Pair<Moves, Integer> score;
        ArrayList<Boolean> rooks_moves = new ArrayList<Boolean>(4);
        Moves mutare = new Moves();

        rooks_moves.add(rook1_moves);
        rooks_moves.add(rook2_moves);
        rooks_moves.add(rook3_moves);
        rooks_moves.add(rook4_moves);

        for (Moves move : playerMoves) {

            int[][] table_copy = tableCopy(level_table);
            String moveEngine = encodeMove(move);
            
            if (castling[0].equals(moveEngine) || castling[1].equals(moveEngine)){
                makeMoveCastling(move, table_copy);   
            } else if (wild_castling[0].equals(moveEngine) || wild_castling[1].equals(moveEngine)){
                makeMoveCastlingWild(move, table_copy);
            } else if (move.futureLine == 0 || move.futureLine == 7) {
                makeMove(move, 1, table_copy);
            } else {
                makeMove(move, 0, table_copy);
            }
            colorState *= -1;
            score = pruning(table_copy, depth - 1, -beta, -alfa);
            colorState *= -1;

            rook1_moves = rooks_moves.get(0);
            rook2_moves = rooks_moves.get(1);
            rook3_moves = rooks_moves.get(2);
            rook4_moves = rooks_moves.get(3);

            score.second = 0 - score.second;     
             
            if (score.second >= beta) {
                return new Pair<Moves, Integer>(move, beta);
            }

            if (score.second > max.second) {
                max.second = score.second;
                max.first = move;

                if (score.second > alfa) {
                    alfa = score.second;
                    mutare = move;
                }
            }
        }
        return max;
    }

    public boolean drawCondition(int tabela[][]) {
        int nr = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (tabela[i][j] != 0) {
                    nr++;
                }
            }
        }

        if (nr == 2) {
            return true;
        }
        return false;
    }

    public boolean moveEngine() throws IOException {

        ArrayList<Moves> allPosibleMoves = new ArrayList<Moves>();
        ArrayList<Moves> validMoves = new ArrayList<Moves>();
        allPosibleMoves = generateAllMoves(table);
        validMoves = checkMoves(allPosibleMoves, table);

        if (validMoves.size() == 0 && check(table)) {
            if (colorState == -1) {
                finalcommand = "1-0 {White mates}";
            } else if (colorState == 1 && check(table)) {
                finalcommand = "0-1 {Black mates}";
            }
            return false;
        }

        if (validMoves.size() == 0 && !check(table)) {
            if (colorState == -1) {
                finalcommand = "1/2-1/2 {Stalemate}";
            } else if (colorState == 1 && !check(table)) {
                finalcommand = "1/2-1/2 {Stalemate}";
            }
            return false;
        }

        if (drawCondition(table)) {
            finalcommand = "1/2-1/2 {Draw}";
        }

        Pair<Moves, Integer> move = pruning (table, MAXDEPTH ,Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);;
        String moveEngine = encodeMove(move.first);
        System.out.println(moveEngine);
        System.out.flush();

        movesNumber ++ ;
        movesHistory += moveEngine + " ";
        
        if (castling[0].equals(moveEngine) || castling[1].equals(moveEngine)) {
            makeMoveCastling(move.first, table);
            return true;
        }

        if (wild_castling[0].equals(moveEngine) || wild_castling[1].equals(moveEngine)) {
            makeMoveCastlingWild(move.first, table);
            return true;
        }

        if (move.first.futureLine == 0 || move.first.futureLine == 7) {
            makeMove(move.first, 1, table);
        } else {
            makeMove(move.first, 0, table);
        }

        colorState = 0 - colorState;
        allPosibleMoves = new ArrayList<Moves>();
        validMoves = new ArrayList<Moves>();
        allPosibleMoves = generateAllMoves(table);
        validMoves = checkMoves(allPosibleMoves, table);

        if (validMoves.size() == 0 && check(table)) {
            if (-colorState == -1) {
                finalcommand = "0-1 {Black mates}";
            } else if (-colorState == 1) {
                finalcommand = "1-0 {White mates}";
            }
            return false;
        }

        if (validMoves.size() == 0 && !check(table)) {
            if (-colorState == -1) {
                finalcommand = "1/2-1/2 {Stalemate}";
            } else if (-colorState == 1) {
                finalcommand = "1/2-1/2 {Stalemate}";
            }
            return false;
        }

        //Revenim la culoarea Engine-ului nostru
        colorState = 0 - colorState;
        return true;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
     * coloana si situat deasupra. (More in Readme)
     */
    public boolean check_up(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingLine + 1; i < ROWS; i++) {
            if ((-colorState) * table_copy[i][kingColumn] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[i][kingColumn] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[i][kingColumn] == Pieces.BLACK_ROOK * colorState
                        || table_copy[i][kingColumn] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
     * coloana si situat dedesubt. (More in Readme)
     */
    public boolean check_down(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingLine - 1; i >= 0; i--) {
            if ((-colorState) * table_copy[i][kingColumn] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[i][kingColumn] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[i][kingColumn] == Pieces.BLACK_ROOK * colorState
                        || table_copy[i][kingColumn] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
     * linie si situat la dreapta. (More in Readme)
     */
    public boolean check_right(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingColumn + 1; i < COLUMNS; i++) {
            if ((-colorState) * table_copy[kingLine][i] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[kingLine][i] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[kingLine][i] == Pieces.BLACK_ROOK * colorState
                        || table_copy[kingLine][i] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
     * linie si situat la stanga. (More in Readme)
     */
    public boolean check_left(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingColumn - 1; i >= 0; i--) {
            if ((-colorState) * table_copy[kingLine][i] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[kingLine][i] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[kingLine][i] == Pieces.BLACK_ROOK * colorState
                        || table_copy[kingLine][i] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata in
     * diagonala sa si anume in partea dreapta deasupra. (More in Readme)
     */
    public boolean check_rightUp(int[][] table_copy, int kingLine,
            int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line < ROWS - 1 && column < COLUMNS - 1) {
            line++;
            column++;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP
                        * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
                if (colorState == 1) {
                    if (table_copy[line][column] == Pieces.BLACK_PAWN) {
                        if (line == kingLine + 1 && column == kingColumn + 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata in
     * diagonala sa si anume in partea dreapta dedesubt. (More in Readme)
     */
    public boolean check_rightDown(int[][] table_copy, int kingLine,
            int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line > 0 && column < COLUMNS - 1) {
            line--;
            column++;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP
                        * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
                if (colorState == -1) {
                    if (table_copy[line][column] == Pieces.WHITE_PAWN) {
                        if (line == kingLine - 1 && column == kingColumn + 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata in
     * diagonala sa si anume in partea stanga deasupra. (More in Readme)
     */
    public boolean check_leftUp(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line < ROWS - 1 && column > 0) {
            line++;
            column--;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP
                        * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
                if (colorState == 1) {
                    if (table_copy[line][column] == Pieces.BLACK_PAWN) {
                        if (line == kingLine + 1 && column == kingColumn - 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de o piesa aflata in
     * diagonala sa si anume in partea stanga dedesubt. (More in Readme)
     */
    public boolean check_leftDown(int[][] table_copy, int kingLine,
            int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line > 0 && column > 0) {
            line--;
            column--;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP
                        * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN
                        * colorState) {
                    return true;
                }
                if (colorState == -1) {
                    if (table_copy[line][column] == Pieces.WHITE_PAWN) {
                        if (line == kingLine - 1 && column == kingColumn - 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este pus in sah de un cal, ce se poate afla
     * intr-un din cele opt pozitii posibile.
     */
    public boolean check_Horse(int[][] table_copy, int kingLine, int kingColumn) {

        if (kingLine + 2 < ROWS && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine + 2][kingColumn + 1] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine + 2 < ROWS && kingColumn - 1 >= 0) {
            if (table_copy[kingLine + 2][kingColumn - 1] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn + 2 < COLUMNS) {
            if (table_copy[kingLine + 1][kingColumn + 2] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn - 2 >= 0) {
            if (table_copy[kingLine + 1][kingColumn - 2] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 2 >= 0 && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine - 2][kingColumn + 1] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 2 >= 0 && kingColumn - 1 >= 0) {
            if (table_copy[kingLine - 2][kingColumn - 1] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn - 2 >= 0) {
            if (table_copy[kingLine - 1][kingColumn - 2] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn + 2 < COLUMNS) {
            if (table_copy[kingLine - 1][kingColumn + 2] == Pieces.BLACK_HORSE
                    * colorState) {
                return true;
            }
        }

        return false;
    }

    /*
     * Metoda verifica daca regele este mutat in apropierea regelui advers,
     * mutarea fiind ilegala.
     */
    public boolean check_King(int[][] table_copy, int kingLine, int kingColumn) {

        if (kingLine + 1 < ROWS) {
            if (table_copy[kingLine + 1][kingColumn] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0) {
            if (table_copy[kingLine - 1][kingColumn] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingColumn - 1 >= 0) {
            if (table_copy[kingLine][kingColumn - 1] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine][kingColumn + 1] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine + 1][kingColumn + 1] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine - 1][kingColumn + 1] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn - 1 >= 0) {
            if (table_copy[kingLine + 1][kingColumn - 1] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn - 1 >= 0) {
            if (table_copy[kingLine - 1][kingColumn - 1] == Pieces.BLACK_KING
                    * colorState) {
                return true;
            }
        }

        return false;
    }

    /*
     * Metoda determina pozitia actuala a regelui pe tabla si apoi apeleaza
     * toate variantele posibile prin care acesta s-ar putea afla in pozitie de
     * sah, returnand true daca este in sah si false in caz contrar.
     */
    public boolean check(int[][] table_copy) {

        int kingLine, kingColumn, i = 0, j = 0, found = 0;

        for (i = 0; i < ROWS; i++) {
            for (j = 0; j < COLUMNS; j++) {
                if (table_copy[i][j] == Pieces.BLACK_KING * (-colorState)) {
                    found = 1;
                    break;
                }
            }
            if (found == 1) {
                break;
            }
        }

        kingLine = i;
        kingColumn = j;

        if (check_up(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_down(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_left(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_right(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_rightUp(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_rightDown(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_leftUp(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_leftDown(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_Horse(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_King(table_copy, kingLine, kingColumn)) {
            return true;
        }

        return false;
    }

    /*
     * Metodă ce realizeaza mutarea pe tablă (matrice), daca o piesa ajunge pe
     * ultima linie din tabla atunci changePawn este egal cu 1, si atunci se
     * verifica daca acea piesa este PAWN, deoarece acesta se schimba in regina
     * odata ajuns pe ultima linie.
     */
    public boolean makeMove(Moves move, int changePawn, int[][] tabela) throws IOException {

        if (tabela[move.currentLine][move.currentColumn] == -6) {
            rook3_moves = false;
            rook4_moves = false;
        } else if (tabela[move.currentLine][move.currentColumn] == -4
                && move.currentColumn == 0) {
            rook3_moves = false;
        } else if (tabela[move.currentLine][move.currentColumn] == -4
                && move.currentColumn == 7) {
            rook4_moves = false;
        } else

        if (tabela[move.currentLine][move.currentColumn] == 6) {
            rook1_moves = false;
            rook2_moves = false;
        } else if (tabela[move.currentLine][move.currentColumn] == 4
                && move.currentColumn == 0) {
            rook1_moves = false;
        } else if (tabela[move.currentLine][move.currentColumn] == 4
                && move.currentColumn == 7) {
            rook2_moves = false;
        }

        if (tabela[move.futureLine][move.futureColumn] == 0
                && tabela[move.currentLine][move.currentColumn] == 1
                && tabela[move.futureLine - 1][move.futureColumn] == -1) {
            tabela[move.futureLine][move.futureColumn] = tabela[move.currentLine][move.currentColumn];
            tabela[move.currentLine][move.currentColumn] = Pieces.BLANK;
            tabela[move.futureLine - 1][move.futureColumn] = Pieces.BLANK;
            return true;
        }

        if (tabela[move.futureLine][move.futureColumn] == 0
                && tabela[move.currentLine][move.currentColumn] == -1
                && tabela[move.futureLine + 1][move.futureColumn] == 1) {
            tabela[move.futureLine][move.futureColumn] = tabela[move.currentLine][move.currentColumn];
            tabela[move.currentLine][move.currentColumn] = Pieces.BLANK;
            tabela[move.futureLine + 1][move.futureColumn] = Pieces.BLANK;
            return true;
        }

        if (changePawn == 0) {
            tabela[move.futureLine][move.futureColumn] = tabela[move.currentLine][move.currentColumn];
            tabela[move.currentLine][move.currentColumn] = Pieces.BLANK;
        } else {
            if (tabela[move.currentLine][move.currentColumn] == 1) {
                tabela[move.futureLine][move.futureColumn] = Pieces.WHITE_QUEEN;
            } else if (tabela[move.currentLine][move.currentColumn] == -1) {
                tabela[move.futureLine][move.futureColumn] = Pieces.BLACK_QUEEN;
            } else {
                tabela[move.futureLine][move.futureColumn] = tabela[move.currentLine][move.currentColumn];
            }
            tabela[move.currentLine][move.currentColumn] = Pieces.BLANK;
        }

        return true;
    }

    public boolean makeMoveCastling(Moves move, int[][] tabela) {

        tabela[move.futureLine][move.futureColumn] = tabela[move.currentLine][move.currentColumn];
        tabela[move.currentLine][move.currentColumn] = Pieces.BLANK;
        tabela[move.currentLine][move.currentColumn + 1] = tabela[move.futureLine][move.currentColumn + 3];
        tabela[move.futureLine][move.currentColumn + 3] = Pieces.BLANK;

        return true;
    }

    public boolean makeMoveCastlingWild(Moves move, int[][] tabela) {

        tabela[move.futureLine][move.futureColumn] = tabela[move.currentLine][move.currentColumn];
        tabela[move.currentLine][move.currentColumn] = Pieces.BLANK;
        tabela[move.futureLine][move.futureColumn + 1] = tabela[move.futureLine][move.futureColumn - 2];
        tabela[move.futureLine][move.futureColumn - 2] = Pieces.BLANK;

        return true;
    }
    /*
     * Metoda determina si returneaza pozitia regelui pe tabla primita.
     */

    public Position whereIsTheKing(int[][] table_copy) {

        Position kingPosition;
        int i = 0, j = 0, found = 0;

        for (i = 0; i < ROWS; i++) {
            for (j = 0; j < COLUMNS; j++) {
                if (table_copy[i][j] == Pieces.BLACK_KING * (-colorState)) {
                    found = 1;
                    break;
                }
            }
            if (found == 1) {
                break;
            }

        }
        kingPosition = new Position(i, j);

        return kingPosition;
    }

    /*
     * Implementare rocada engine.
     */
    public boolean kingCastlingCondition(int[][] tabela) {

        int linie;
        int table_copy[][];

        if (engineColor == Color.WHITE) {
            linie = 0;
        } else {
            linie = 7;
        }
        Position king = whereIsTheKing(tabela);
        if (king.line != linie || king.column != 4) {
            return false;
        }
        if (tabela[king.line][king.column + 1] != 0
                || tabela[king.line][king.column + 2] != 0) {
            return false;
        }

        if (check(tabela)) {
            return false;
        }

        table_copy = tableCopy(tabela);
        table_copy[king.line][king.column + 1] = table_copy[king.line][king.column];
        table_copy[king.line][king.column] = Pieces.BLANK;
        if (check(table_copy)) {
            return false;
        }
        table_copy = tableCopy(tabela);
        table_copy[king.line][king.column + 2] = table_copy[king.line][king.column];
        table_copy[king.line][king.column] = Pieces.BLANK;
        if (check(table_copy)) {
            return false;
        }

        return true;
    }

    /*
     * Implementare rocada engine.
     */
      

    public boolean queenCastlingCondition(int[][] tabela) {

        int linie;
        int table_copy[][];

        if (engineColor == Color.WHITE) {
            linie = 0;
        } else {
            linie = 7;
        }
        Position king = whereIsTheKing(tabela);
        if (king.line != linie || king.column != 4) {
            return false;
        }
        if (tabela[king.line][king.column - 1] != 0
                || tabela[king.line][king.column - 2] != 0
                || tabela[king.line][king.column - 3] != 0) {
            return false;
        }
        if (check(tabela)) {
            return false;
        }

        table_copy = tableCopy(tabela);
        table_copy[king.line][king.column - 1] = table_copy[king.line][king.column];
        table_copy[king.line][king.column] = Pieces.BLANK;
        if (check(table_copy)) {
            return false;
        }
        table_copy = tableCopy(tabela);
        table_copy[king.line][king.column - 2] = table_copy[king.line][king.column];
        table_copy[king.line][king.column] = Pieces.BLANK;
        if (check(table_copy)) {
            return false;
        }

        return true;
    }

    public void printTable() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (table[i][j] < 0) {
                    System.out.print(" " + table[i][j]);
                } else {
                    System.out.print("  " + table[i][j]);
                }

            }
            System.out.println();
        }
    }

    public String printTabletoString() {
        String s = new String();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (table[i][j] < 0) {
                    s = s + " " + table[i][j];
                } else {
                    s = s + "  " + table[i][j];
                }
            }
            s = s + '\n';
        }
        return s;
    }
}

class Pair<F, S> {

    public F first;
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}
