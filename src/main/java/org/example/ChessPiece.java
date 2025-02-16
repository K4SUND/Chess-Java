package org.example;

enum Player {
    WHITE,
    BLACK,
}

enum Rank{
    KING,
    QUEEN,
    BISHOP,
    ROOK,
    KNIGHT,
    PAWN,

}
public class ChessPiece {

    //immutable   -> only constructor
    private final int col ;
    private final int row ;


    private final Player player;
    private final Rank rank;

    private final String imgName;

    public ChessPiece(int col, int row, Player player, Rank rank, String imgName) {
        this.col = col;
        this.row = row;
        this.player = player;
        this.rank = rank;
        this.imgName = imgName;

    }


    //getters (private final)

    //no setters -> so can't change

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public Player getPlayer() {
        return player;
    }

    public Rank getRank() {
        return rank;
    }

    public String getImgName() {
        return imgName;
    }
}
