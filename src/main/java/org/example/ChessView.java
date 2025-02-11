package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//panel
//own class extend sub class
public class ChessView extends JPanel implements MouseListener, MouseMotionListener {

    private ChessDelegate chessDelegate ;

    double scaleFactor = 0.9;

    private int originX = -1;
    private int originY = -1;
    private int cellSide = -1;


    private Map<String, Image> keyNameValueImage = new HashMap<String, Image>();
    private int fromCol = -1;
    private int fromRow = -1;

    private ChessPiece movingPiece;

    private Point movingPiecePoint;



    ChessView(ChessDelegate chessDelegate) {

        this.chessDelegate = chessDelegate;

        String[] imageNames = {

                ChessConstants.bBishop,
                ChessConstants.wBishop,
                ChessConstants.bKing,
                ChessConstants.wKing,
                ChessConstants.bKnight,
                ChessConstants.wKnight,
                ChessConstants.bPawn,
                ChessConstants.wPawn,
                ChessConstants.bQueen,
                ChessConstants.wQueen,
                ChessConstants.bRook,
                ChessConstants.wRook,

        };


        try {
            for (String imgName : imageNames
            ) {
                Image img = loadImage(imgName+".png");
                keyNameValueImage.put(imgName,img);


            }
        } catch (Exception e) {
          e.printStackTrace();
        }

        addMouseListener(this);
        addMouseMotionListener(this);



    }

    public void drawPieces(Graphics2D g2){

        for (int row = 0; row<8; row++)
        {
            for (int col = 0; col<8; col++)
            {
                ChessPiece p = chessDelegate.pieceAt(col,row);

                if(p!=null && p != movingPiece)
                {
                    drawImage(g2,col,row, p.imgName);
                }
            }
        }
//        drawImage(g2,0,0,"Rook-black");
//        drawImage(g2,0,1,"Pawn-black");

        if(movingPiece != null && movingPiecePoint != null)
        {
            //not descrete
            Image img = keyNameValueImage.get(movingPiece.imgName);
            g2.drawImage(img,movingPiecePoint.x - cellSide/2,movingPiecePoint.y-cellSide/2,cellSide,cellSide, null);

        }
    }



//overwrite one method
@Override
protected void paintChildren(Graphics g) {
    super.paintChildren(g);

    int smaller = Math.min(getSize().width,getSize().height);
    cellSide = (int) (((double)smaller)*scaleFactor/8);

    originX = (getSize().width-8 * cellSide)/2;
    originY = (getSize().height-8 * cellSide)/2;



    Graphics2D g2 = (Graphics2D) g;

    drawBoard(g2);
    drawPieces(g2);



//        int cellSide = 600/8;
//        g2.fillRect(0,0,cellSide,cellSide);
//        g2.fillRect(2*cellSide,0,cellSide,cellSide);
//        g2.fillRect(4*cellSide,0,cellSide,cellSide);
//        g2.fillRect(6*cellSide,0,cellSide,cellSide);
//        g2.fillRect(8*cellSide,0,cellSide,cellSide);

   /*     for(    int j = 0;    j<4;j++)

        {
//        g2.setColor(Color.white); //default color is black

        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i,2*j,true);
//            g2.fillRect(originX + (i * 2 + 1) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i+1,2*j+1,true);
            drawSquare(g2,2*i+1,2*j,false);
            drawSquare(g2,2*i,2*j+1,false);
        }


//        g2.setColor(Color.gray); //default color is black

      /*
        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX +(i * 2 + 1) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i+1,2*j,false);
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i,2*j+1,false);
        }


       */


}

private void drawImage(Graphics2D g2, int col, int row, String imgName){
    Image img = keyNameValueImage.get(imgName);
    g2.drawImage(img,originX+col*cellSide,originY+row*cellSide,cellSide,cellSide, null);

}

private Image loadImage(String imgFileName) throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    URL res = classLoader.getResource("img/"+imgFileName);
    if (res == null) {
        return null;
    } else {
        System.out.println("Yeah");

        File imgFile = new File(res.toURI());

        return ImageIO.read(imgFile);


    }


}


private void drawSquare(Graphics2D g2, int col, int row, boolean light) {
    g2.setColor(light ? Color.white : Color.gray);
    g2.fillRect(originX + col * cellSide, originY + row * cellSide, cellSide, cellSide);


}


private void drawBoard(Graphics2D g2) {
    for (int j = 0; j < 4; j++) {
//        g2.setColor(Color.white); //default color is black

        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2, 2 * i, 2 * j, true);
//            g2.fillRect(originX + (i * 2 + 1) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2, 2 * i + 1, 2 * j + 1, true);
            drawSquare(g2, 2 * i + 1, 2 * j, false);
            drawSquare(g2, 2 * i, 2 * j + 1, false);
        }


//        g2.setColor(Color.gray); //default color is black

      /*
        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX +(i * 2 + 1) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i+1,2*j,false);
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i,2*j+1,false);
        }


       */

    }
}


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {


       fromCol = (e.getPoint().x - originX)/cellSide;
       fromRow = (e.getPoint().y - originY)/cellSide;

       movingPiece = chessDelegate.pieceAt(fromCol,fromRow);


    }

    @Override
    public void mouseReleased(MouseEvent e) {



        int col = (e.getPoint().x - originX)/cellSide;
        int row = (e.getPoint().y - originY)/cellSide;

//        System.out.println("from "+fromCol+" to "+col);

        chessDelegate.movePiece(fromCol,fromRow,col,row);

        movingPiece = null;
        movingPiecePoint = null;

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }





    /*Mouse Move Listener*/
    @Override
    public void mouseDragged(MouseEvent e) {

//        System.out.println(e.getPoint());
        movingPiecePoint = e.getPoint();
        repaint();



    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
