package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChessController implements ChessDelegate, ActionListener {


    private ChessModel chessModel = new ChessModel();
    private ChessView chessBoardPanel;

    private JButton resetBtn;
    private JButton serverBtn;
    private JButton clientBtn;



    public ChessController() {

        chessModel.reset();

        JFrame frame = new JFrame("Chess");
        frame.setSize(600,600);
//        frame.setLocation(0,1300);
        frame.setLayout(new BorderLayout());


        chessBoardPanel = new ChessView(this);


        frame.add(chessBoardPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(this);
        buttonsPanel.add(resetBtn);

        serverBtn = new JButton("Listen");
        buttonsPanel.add(serverBtn);
        serverBtn.addActionListener(this);

        clientBtn = new JButton("Connect");
        buttonsPanel.add(clientBtn);
        clientBtn.addActionListener(this);

        frame.add(buttonsPanel, BorderLayout.PAGE_END);




        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    @Override
    public ChessPiece pieceAt(int col, int row) {
        return chessModel.pieceAt(col,row);

    }

    @Override
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        chessModel.movePiece(fromCol,fromRow,toCol,toRow);
        chessBoardPanel.repaint();
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


    @Override
    public void actionPerformed(ActionEvent e) {
//        System.out.println(e.getSource());

        if(e.getSource() == resetBtn)
        {
//            System.out.println("Reset Clicked");
            chessModel.reset();
            chessBoardPanel.repaint();
        } else if (e.getSource()== serverBtn) {

            System.out.println("Listen (for socket server) Clicked");

        }else if (e.getSource()== clientBtn) {

            System.out.println("Connect (for socket client) Clicked");

        }

    }
}
