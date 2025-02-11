package org.example;

import javax.swing.*;

public class Chess {

    public static void main(String[] args) {
//        System.out.println("Hello Chess");

        //window
        JFrame frame = new JFrame("Chess");
        frame.setSize(600,600);

        //
//        frame.setLocation();
//        frame.setVis ible(true);

        //panel
        //own class extend subclass


        //create panel object add it to frame
        ChessPanel panel = new ChessPanel();
        frame.add(panel);
        frame.setVisible(true);




    }
}
