package org.example;

import junit.framework.TestCase;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class ChessModelTest extends ChessModel {


    @Test
    public void testReset(){
        ChessModel chessModel = new ChessModel();
        chessModel.reset();
        chessModel.movePiece(0,1,0,2);
        chessModel.reset();
        assertNull(chessModel.pieceAt(0,2));
        System.out.println(chessModel);

    }
    @Test
    public void testMovePiece(){
        ChessModel chessModel = new ChessModel();
        chessModel.reset();
        assertNull(chessModel.pieceAt(0,2));
        System.out.println(chessModel);

        chessModel.movePiece(0,1,0,2);
        assertNotNull(chessModel.pieceAt(0,2));
        System.out.println(chessModel);

    }

    @Test
    public void testPieceAt(){
        ChessModel chessModel = new ChessModel();
        assertNull(chessModel.pieceAt(0,0));
        chessModel.reset();
        assertNotNull(chessModel.pieceAt(0,0));

        assertEquals(Player.WHITE, chessModel.pieceAt(0,0).player);
        assertEquals(Rank.ROOK, chessModel.pieceAt(0,0).rank);

    }



    @Test
    public void testToString() {

        ChessModel chessModel = new ChessModel();
        assertTrue(chessModel.toString().contains("0 . . . . . . . ."));
        chessModel.reset();
        System.out.println(chessModel);
        assertTrue(chessModel.toString().contains("0 r n b q k b n r"));



    }
}