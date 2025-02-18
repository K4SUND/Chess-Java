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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//panel
//own class extend sub class
public class ChessView extends JPanel implements MouseListener, MouseMotionListener {
    private final Color lightWoodColor = new Color(222, 184, 135); // Light brown
    private final Color darkWoodColor = new Color(139, 69, 19);    // Dark brown

    private ChessDelegate chessDelegate ;

    double scaleFactor = 0.9;

    private int originX = -1;
    private int originY = -1;
    private int cellSide = -1;


    private Map<String, Image> keyNameValueImage = new HashMap<String, Image>();

    private Set<Point> legalMoves = new HashSet<>();


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
                    drawImage(g2,col,row, p.getImgName());
                }
            }
        }


        if(movingPiece != null){
            if(movingPiecePoint != null)
            {
                Image img = keyNameValueImage.get(movingPiece.getImgName());
                g2.drawImage(img,movingPiecePoint.x - cellSide/2,movingPiecePoint.y - cellSide/2,cellSide,cellSide,null);
            }else {
                drawImage(g2,fromCol,fromRow,movingPiece.getImgName());
            }
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




        //shows valid moves

        g2.setColor(new Color(0, 255, 0, 100));
        for (Point point : legalMoves) {
            // Draw the indicators using screen coordinates
            g2.fillOval(
                    originX + point.x * cellSide + cellSide / 4,
                    originY + point.y * cellSide + cellSide / 4,
                    cellSide / 2,
                    cellSide / 2
            );
        }



    }

    private void drawImage(Graphics2D g2, int col, int row, String imgName){
        Image img = keyNameValueImage.get(imgName);
        g2.drawImage(img,originX+col*cellSide,originY+(7-row)*cellSide,cellSide,cellSide, null);

    }

    private Image loadImage(String imgFileName) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL res = classLoader.getResource("img/"+imgFileName);
        if (res == null) {
            return null;
        } else {


            File imgFile = new File(res.toURI());

            return ImageIO.read(imgFile);


        }


    }


    private void drawSquare(Graphics2D g2, int col, int row, boolean light) {
        g2.setColor(light ? lightWoodColor : darkWoodColor);
        g2.fillRect(originX + col * cellSide, originY + row * cellSide, cellSide, cellSide);


    }


    private void drawBoard(Graphics2D g2) {
        for (int j = 0; j < 4; j++) {


            for (int i = 0; i < 4; i++) {

                drawSquare(g2, 2 * i, 2 * j, true);
                drawSquare(g2, 2 * i + 1, 2 * j + 1, true);
                drawSquare(g2, 2 * i + 1, 2 * j, false);
                drawSquare(g2, 2 * i, 2 * j + 1, false);
            }



        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {


        fromCol = (e.getPoint().x - originX)/cellSide;
        fromRow = 7 - (e.getPoint().y - originY)/cellSide;

        movingPiece = chessDelegate.pieceAt(fromCol,fromRow);

        legalMoves.clear();

        // Calculate legal moves for the selected piece
        if (movingPiece != null) {
            for (int col = 0; col < 8; col++) {
                for (int row = 0; row < 8; row++) {
                    if (chessDelegate.isValidMove(fromCol, fromRow, col, row)) {
                        // Store the legal moves in board coordinates
                        legalMoves.add(new Point(col, 7 - row));  // Flip the row for display
                    }
                }
            }
        }

        repaint();


    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if(movingPiece != null)
        {
            int col = (e.getPoint().x - originX)/cellSide;
            int row = 7- (e.getPoint().y - originY)/cellSide;

            if(fromCol != col || fromRow != row)
            {
                chessDelegate.movePiece(fromCol, fromRow, col, row);
            }


        }

        movingPiece = null;
        movingPiecePoint = null;
        legalMoves.clear();
        repaint();


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

        if(movingPiece != null)
        {
            movingPiecePoint = e.getPoint();
            repaint();
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}