package org.example;

import javax.swing.*;

public class ChessController implements ChessDelegate {


    private ChessModel chessModel = new ChessModel();
    private ChessView panel;

    @Override
    public ChessPiece pieceAt(int col, int row) {
       return chessModel.pieceAt(col,row);

    }


    public ChessController() {

        chessModel.reset();

        JFrame frame = new JFrame("Chess");
        frame.setSize(600,600);
//        frame.setLocation(0,1300);

        panel = new ChessView();
        panel.chessDelegate = this;

        frame.add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {

        /* added to constuctpr
//        System.out.println("Hello Chess");

        //window
        JFrame frame = new JFrame("Chess");
        frame.setSize(600,600);
        frame.setLocation(0,1300);


        //
//        frame.setLocation();
//        frame.setVisible(true);

        //panel
        //own class extend subclass


        //create panel object add it to frame
        panel = new ChessView();
        frame.add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

*/

        new ChessController();



    }
}
