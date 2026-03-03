import java.util.*;

abstract class Piece {
    private boolean isWhite;
    protected boolean hasMoved = false;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setMoved() {
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public abstract boolean canMove(int sr, int sc, int er, int ec, Board board);

    public abstract String getSymbol();
}

// ================= PAWN =================
class Pawn extends Piece {

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    public boolean canMove(int sr, int sc, int er, int ec, Board board) {

        int direction = isWhite() ? -1 : 1;

        if (sc == ec && board.getPiece(er, ec) == null) {
            if (er == sr + direction)
                return true;

            if (!hasMoved && er == sr + 2 * direction &&
                    board.getPiece(sr + direction, sc) == null)
                return true;
        }

        if (Math.abs(sc - ec) == 1 && er == sr + direction) {
            Piece target = board.getPiece(er, ec);
            if (target != null && target.isWhite() != isWhite())
                return true;

            int[] ep = board.getEnPassantSquare();
            if (ep != null && ep[0] == er && ep[1] == ec)
                return true;
        }

        return false;
    }

    public String getSymbol() {
        return isWhite() ? "P" : "p";
    }
}

// ================= ROOK =================
class Rook extends Piece {
    public Rook(boolean isWhite) {
        super(isWhite);
    }

    public boolean canMove(int sr, int sc, int er, int ec, Board board) {
        if (sr != er && sc != ec)
            return false;
        return board.isPathClear(sr, sc, er, ec);
    }

    public String getSymbol() {
        return isWhite() ? "R" : "r";
    }
}

// ================= KNIGHT =================
class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    public boolean canMove(int sr, int sc, int er, int ec, Board board) {
        int r = Math.abs(sr - er);
        int c = Math.abs(sc - ec);
        return (r == 2 && c == 1) || (r == 1 && c == 2);
    }

    public String getSymbol() {
        return isWhite() ? "N" : "n";
    }
}

// ================= BISHOP =================
class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    public boolean canMove(int sr, int sc, int er, int ec, Board board) {
        if (Math.abs(sr - er) != Math.abs(sc - ec))
            return false;
        return board.isPathClear(sr, sc, er, ec);
    }

    public String getSymbol() {
        return isWhite() ? "B" : "b";
    }
}

// ================= QUEEN =================
class Queen extends Piece {
    public Queen(boolean isWhite) {
        super(isWhite);
    }

    public boolean canMove(int sr, int sc, int er, int ec, Board board) {
        if (sr == er || sc == ec ||
                Math.abs(sr - er) == Math.abs(sc - ec))
            return board.isPathClear(sr, sc, er, ec);
        return false;
    }

    public String getSymbol() {
        return isWhite() ? "Q" : "q";
    }
}

// ================= KING =================
class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite);
    }

    public boolean canMove(int sr, int sc, int er, int ec, Board board) {

        // Normal move
        if (Math.abs(sr - er) <= 1 &&
                Math.abs(sc - ec) <= 1)
            return true;

        // Castling
        if (!hasMoved && sr == er && Math.abs(sc - ec) == 2) {

            int direction = (ec > sc) ? 1 : -1;
            int rookCol = (direction == 1) ? 7 : 0;

            Piece rook = board.getPiece(sr, rookCol);

            if (rook instanceof Rook && !rook.hasMoved()) {
                if (board.isPathClear(sr, sc, sr, rookCol))
                    return true;
            }
        }

        return false;
    }

    public String getSymbol() {
        return isWhite() ? "K" : "k";
    }
}

// ================= BOARD =================
class Board {

    private Piece[][] board = new Piece[8][8];
    private int[] enPassantSquare = null;

    public Board() {
        initialize();
    }

    public Piece getPiece(int r, int c) {
        return board[r][c];
    }

    public int[] getEnPassantSquare() {
        return enPassantSquare;
    }

    private void initialize() {

        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn(false);
            board[6][i] = new Pawn(true);
        }

        board[0][0] = new Rook(false);
        board[0][7] = new Rook(false);
        board[7][0] = new Rook(true);
        board[7][7] = new Rook(true);

        board[0][1] = new Knight(false);
        board[0][6] = new Knight(false);
        board[7][1] = new Knight(true);
        board[7][6] = new Knight(true);

        board[0][2] = new Bishop(false);
        board[0][5] = new Bishop(false);
        board[7][2] = new Bishop(true);
        board[7][5] = new Bishop(true);

        board[0][3] = new Queen(false);
        board[7][3] = new Queen(true);

        board[0][4] = new King(false);
        board[7][4] = new King(true);
    }

    public boolean isPathClear(int sr, int sc, int er, int ec) {

        int dr = Integer.compare(er, sr);
        int dc = Integer.compare(ec, sc);

        sr += dr;
        sc += dc;

        while (sr != er || sc != ec) {
            if (board[sr][sc] != null)
                return false;
            sr += dr;
            sc += dc;
        }

        return true;
    }

    public boolean move(int sr, int sc, int er, int ec, boolean whiteTurn) {

        Piece piece = board[sr][sc];

        if (piece == null || piece.isWhite() != whiteTurn)
            return false;

        if (!piece.canMove(sr, sc, er, ec, this))
            return false;

        Piece target = board[er][ec];

        // Castling rook movement
        if (piece instanceof King &&
                Math.abs(ec - sc) == 2) {

            int direction = (ec > sc) ? 1 : -1;
            int rookStart = (direction == 1) ? 7 : 0;
            int rookEnd = sc + direction;

            Piece rook = board[sr][rookStart];
            board[sr][rookEnd] = rook;
            board[sr][rookStart] = null;
            rook.setMoved();
        }

        // En passant
        if (piece instanceof Pawn && enPassantSquare != null &&
                er == enPassantSquare[0] &&
                ec == enPassantSquare[1] &&
                target == null) {

            int direction = piece.isWhite() ? 1 : -1;
            board[er + direction][ec] = null;
        }

        board[er][ec] = piece;
        board[sr][sc] = null;
        piece.setMoved();

        // Promotion
        if (piece instanceof Pawn) {
            if ((piece.isWhite() && er == 0) ||
                    (!piece.isWhite() && er == 7)) {

                Scanner scn = new Scanner(System.in);
                System.out.println("Promote to (Q R B N): ");
                char choice = scn.next().toUpperCase().charAt(0);

                switch (choice) {
                    case 'R': board[er][ec] = new Rook(piece.isWhite()); break;
                    case 'B': board[er][ec] = new Bishop(piece.isWhite()); break;
                    case 'N': board[er][ec] = new Knight(piece.isWhite()); break;
                    default: board[er][ec] = new Queen(piece.isWhite());
                }
            }
        }

        enPassantSquare = null;
        if (piece instanceof Pawn && Math.abs(er - sr) == 2)
            enPassantSquare = new int[]{(sr + er) / 2, sc};

        if (isKingInCheck(whiteTurn)) {
            board[sr][sc] = piece;
            board[er][ec] = target;
            return false;
        }

        return true;
    }

    public boolean isKingInCheck(boolean whiteKing) {

        int kr = -1, kc = -1;

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (board[i][j] instanceof King &&
                        board[i][j].isWhite() == whiteKing) {
                    kr = i;
                    kc = j;
                }

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                Piece p = board[i][j];
                if (p != null && p.isWhite() != whiteKing) {
                    if (p.canMove(i, j, kr, kc, this))
                        return true;
                }
            }

        return false;
    }

    public boolean hasLegalMove(boolean white) {

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {

                Piece p = board[i][j];

                if (p != null && p.isWhite() == white) {

                    for (int r = 0; r < 8; r++)
                        for (int c = 0; c < 8; c++) {

                            Piece temp = board[r][c];

                            if (p.canMove(i, j, r, c, this)) {

                                board[r][c] = p;
                                board[i][j] = null;

                                boolean check = isKingInCheck(white);

                                board[i][j] = p;
                                board[r][c] = temp;

                                if (!check)
                                    return true;
                            }
                        }
                }
            }

        return false;
    }

    public void printBoard() {
        System.out.println("\n  0 1 2 3 4 5 6 7");
        for (int i = 0; i < 8; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == null)
                    System.out.print(". ");
                else
                    System.out.print(board[i][j].getSymbol() + " ");
            }
            System.out.println();
        }
    }
}

// ================= MAIN =================
public class ChessGame {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Board board = new Board();
        boolean whiteTurn = true;

        while (true) {

            board.printBoard();
            System.out.println((whiteTurn ? "White" : "Black") + " move:");
            System.out.print("Enter startRow startCol endRow endCol: ");

            int sr = sc.nextInt();
            int sc1 = sc.nextInt();
            int er = sc.nextInt();
            int ec = sc.nextInt();

            if (!board.move(sr, sc1, er, ec, whiteTurn)) {
                System.out.println("Invalid move!");
                continue;
            }

            boolean opponent = !whiteTurn;

            if (board.isKingInCheck(opponent))
                System.out.println("CHECK!");

            if (!board.hasLegalMove(opponent)) {
                board.printBoard();
                if (board.isKingInCheck(opponent))
                    System.out.println("CHECKMATE!");
                else
                    System.out.println("STALEMATE!");
                break;
            }

            whiteTurn = opponent;
        }
    }
}