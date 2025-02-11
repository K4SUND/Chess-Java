package org.example;

import java.util.HashSet;
import java.util.Set;

public class ChessModel {


    //set of PCs
    private Set<ChessPiece> pieceSet = new HashSet<ChessPiece>();
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



    }


    void movePiece(int fromCol, int fromRow, int toCol, int toRow)
    {
        ChessPiece candidate = pieceAt(fromCol,fromRow);
        if(candidate==null)
        {
            return;
        }

        ChessPiece target = pieceAt(toCol,toRow);
        if(target != null )
        {
            if(target.player == candidate.player){
                return ;
            }
            else{
                pieceSet.remove(target);

            }

        }
        candidate.col = toCol;
        candidate.row = toRow;

//        System.out.println(pieceSet.size());

    }



    ChessPiece pieceAt(int col, int row)
    {
        for (ChessPiece chessPiece:pieceSet
             )
        {
            if(chessPiece.col == col && chessPiece.row==row)
            {
                return chessPiece;
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
                switch (p.rank)
                {
                    case KING:
                        description += p.player == Player.WHITE ? "k" :"K";
                        break;

                    case QUEEN:
                        description += p.player == Player.WHITE ? "q" :"Q";
                        break;
                    case BISHOP:
                        description += p.player == Player.WHITE ? "b" :"B";
                        break;
                    case ROOK:
                        description += p.player == Player.WHITE ? "r" :"R";
                        break;
                    case KNIGHT:
                        description += p.player == Player.WHITE ? "n" :"N";
                        break;

                    case PAWN:
                        description += p.player == Player.WHITE ? "p" :"P";
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
