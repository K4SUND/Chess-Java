package org.example;

import java.util.HashSet;
import java.util.Set;

public class ChessModel {




    //set of PCs
    private Set<ChessPiece> pieceSet = new HashSet<ChessPiece>();

    private  Player playerInTurn = Player.WHITE;

    void reset(){

        pieceSet.removeAll(pieceSet);

        for(int i=0;i<2;i++)
        {
            pieceSet.add( new ChessPiece(0+i*7,7,Player.BLACK,Rank.ROOK,ChessConstants.bRook));
            pieceSet.add( new ChessPiece(1+i*5,7,Player.BLACK,Rank.KNIGHT,ChessConstants.bKnight));
            pieceSet.add( new ChessPiece(2+i*3,7,Player.BLACK,Rank.BISHOP,ChessConstants.bBishop));

            pieceSet.add( new ChessPiece(0+i*7,0,Player.WHITE,Rank.ROOK,ChessConstants.wRook));
            pieceSet.add( new ChessPiece(1+i*5,0,Player.WHITE,Rank.KNIGHT,ChessConstants.wKnight));
            pieceSet.add( new ChessPiece(2+i*3,0,Player.WHITE,Rank.BISHOP,ChessConstants.wBishop));
        }

        for(int i=0;i<8;i++)
        {
            pieceSet.add( new ChessPiece(i,6,Player.BLACK,Rank.PAWN,ChessConstants.bPawn));
            pieceSet.add( new ChessPiece(i,1,Player.WHITE,Rank.PAWN,ChessConstants.wPawn));
        }

        pieceSet.add( new ChessPiece(3,7,Player.BLACK,Rank.QUEEN,ChessConstants.bQueen));
        pieceSet.add( new ChessPiece(3,0,Player.WHITE,Rank.QUEEN,ChessConstants.wQueen));


        pieceSet.add( new ChessPiece(4,7,Player.BLACK,Rank.KING,ChessConstants.bKing));
        pieceSet.add( new ChessPiece(4,0,Player.WHITE,Rank.KING,ChessConstants.wKing));

        playerInTurn = Player.WHITE;


    }


    void movePiece(int fromCol, int fromRow, int toCol, int toRow)
    {

        if (!isValidMove(fromCol, fromRow, toCol, toRow)) {
            return;
        }



        ChessPiece movingPiece = pieceAt(fromCol,fromRow);


        //restricted (3rd condition)
        if(movingPiece==null || movingPiece.getPlayer() != playerInTurn || fromCol == toCol && fromRow == toRow)
        {

            return;
        }

        ChessPiece target = pieceAt(toCol,toRow);
        if(target != null )
        {
            if(target.getPlayer() == movingPiece.getPlayer()){
                return ;
            }
            else{
                pieceSet.remove(target);

            }

        }

        pieceSet.remove(movingPiece);
        pieceSet.add(new ChessPiece(toCol,toRow,movingPiece.getPlayer(),movingPiece.getRank(), movingPiece.getImgName()));




        playerInTurn = playerInTurn == Player.WHITE ? Player.BLACK : Player.WHITE;


    }



    ChessPiece pieceAt(int col, int row)
    {
        for (ChessPiece chessPiece:pieceSet
             )
        {
            if(chessPiece.getCol() == col && chessPiece.getRow()==row)
            {
                return chessPiece;
            }


        }
        return null;
    }

    boolean isValidMove(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece piece = pieceAt(fromCol, fromRow);
        if (piece == null) return false;

        int colDiff = Math.abs(toCol - fromCol);
        int rowDiff = Math.abs(toRow - fromRow);

        switch (piece.getRank()) {
            case PAWN:
                int direction = (piece.getPlayer() == Player.WHITE) ? 1 : -1;

                // Move forward
                if (colDiff == 0 && toRow == fromRow + direction && pieceAt(toCol, toRow) == null) {
                    return true;
                }

                // Double move on first move
                if (colDiff == 0 && fromRow == (piece.getPlayer() == Player.WHITE ? 1 : 6)
                        && toRow == fromRow + 2 * direction
                        && pieceAt(toCol, fromRow + direction) == null
                        && pieceAt(toCol, toRow) == null) {
                    return true;
                }

                // Capture diagonally
                if (colDiff == 1 && toRow == fromRow + direction && pieceAt(toCol, toRow) != null) {
                    return true;
                }

                return false;

            case KNIGHT:
                return (colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2);
            case BISHOP:
                return colDiff == rowDiff;
            case ROOK:
                if (colDiff == 0 || rowDiff == 0) {
                    int stepCol = (toCol > fromCol) ? 1 : (toCol < fromCol) ? -1 : 0;
                    int stepRow = (toRow > fromRow) ? 1 : (toRow < fromRow) ? -1 : 0;

                    int colCheck = fromCol + stepCol;
                    int rowCheck = fromRow + stepRow;
                    while (colCheck != toCol || rowCheck != toRow) {
                        if (pieceAt(colCheck, rowCheck) != null) {
                            return false;
                        }
                        colCheck += stepCol;
                        rowCheck += stepRow;
                    }
                    return true;
                }
                return false;
            case QUEEN:
                return colDiff == 0 || rowDiff == 0 || colDiff == rowDiff;
            case KING:
                return colDiff <= 1 && rowDiff <= 1;
            default:
                return false;
        }
    }


    public boolean isInCheck(Player player) {
        ChessPiece king = findKing(player);
        if (king == null) return false;

        for (ChessPiece piece : pieceSet) {
            if (piece.getPlayer() != player && isValidMove(piece.getCol(), piece.getRow(), king.getCol(), king.getRow())) {
                return true;
            }
        }
        return false;
    }

    private ChessPiece findKing(Player player) {
        for (ChessPiece piece : pieceSet) {
            if (piece.getRank() == Rank.KING && piece.getPlayer() == player) {
                return piece;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String description = "";

        for(int row=7;row>=0;row--)
        {
            description += ""+row;  //7 -> "7"
            for(int col=0;col<8;col++)
        {
            ChessPiece p = pieceAt(col,row);
            if(p==null){
                description += " .";
            }
            else {
                description +=" ";
                switch (p.getRank())
                {
                    case KING:
                        description += p.getPlayer() == Player.WHITE ? "k" :"K";
                        break;

                    case QUEEN:
                        description += p.getPlayer() == Player.WHITE ? "q" :"Q";
                        break;
                    case BISHOP:
                        description += p.getPlayer() == Player.WHITE ? "b" :"B";
                        break;
                    case ROOK:
                        description += p.getPlayer() == Player.WHITE ? "r" :"R";
                        break;
                    case KNIGHT:
                        description += p.getPlayer() == Player.WHITE ? "n" :"N";
                        break;

                    case PAWN:
                        description += p.getPlayer() == Player.WHITE ? "p" :"P";
                        break;

                }

            }

        }
            description+="\n";
        }

        description += "  0 1 2 3 4 5 6 7";




        return description;
    }
}
