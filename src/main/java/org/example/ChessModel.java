package org.example;

import java.util.HashSet;
import java.util.Set;

public class ChessModel {


    //set of PCs
    private Set<ChessPiece> pieceSet = new HashSet<ChessPiece>();
    void reset(){

        for(int i=0;i<2;i++)
        {
            pieceSet.add( new ChessPiece(0+i*7,7,Player.BLACK,Rank.ROOK,"Rook-black"));
            pieceSet.add( new ChessPiece(1+i*5,7,Player.BLACK,Rank.KNIGHT,"Knight-black"));
            pieceSet.add( new ChessPiece(2+i*3,7,Player.BLACK,Rank.BISHOP,"Bishop-black"));

            pieceSet.add( new ChessPiece(0+i*7,0,Player.WHITE,Rank.ROOK,"Rook-white"));
            pieceSet.add( new ChessPiece(1+i*5,0,Player.WHITE,Rank.KNIGHT,"Knight-white"));
            pieceSet.add( new ChessPiece(2+i*3,0,Player.WHITE,Rank.BISHOP,"Bishop-white"));
        }

        for(int i=0;i<8;i++)
        {
            pieceSet.add( new ChessPiece(i,6,Player.BLACK,Rank.PAWN,"Pawn-black"));
            pieceSet.add( new ChessPiece(i,1,Player.WHITE,Rank.PAWN,"Pawn-white"));
        }

        pieceSet.add( new ChessPiece(3,7,Player.BLACK,Rank.QUEEN,"Queen-black"));
        pieceSet.add( new ChessPiece(3,0,Player.WHITE,Rank.QUEEN,"Queen-white"));


        pieceSet.add( new ChessPiece(4,7,Player.BLACK,Rank.KING,"King-black"));
        pieceSet.add( new ChessPiece(4,0,Player.WHITE,Rank.KING,"King-white"));



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
