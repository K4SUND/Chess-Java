package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
//import java.util.concurrent.Executor;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class ChessController implements ChessDelegate, ActionListener{



    //run 2 different computers
    private String SOCKET_SERVER_IP = "localhost";

    private int PORT = 50000;

    private JFrame frame;
    private ChessModel chessModel = new ChessModel();
    private ChessView chessBoardPanel;

    private JButton resetBtn;
    private JButton serverBtn;
    private JButton clientBtn;


    private ServerSocket listener;

    //for close socket
    private Socket socket;

    private PrintWriter printWriter;
//    private Scanner scanner;




    public ChessController() {

        chessModel.reset();

//        JFrame frame = new JFrame("Chess");
        frame = new JFrame("Chess");
        frame.setSize(500,550);
//        frame.setLocation(0,1300);
        frame.setLayout(new BorderLayout());


        chessBoardPanel = new ChessView(this);


        frame.add(chessBoardPanel, BorderLayout.CENTER);

//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        var buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if(printWriter != null) printWriter.close();
//                printWriter.close();
//                scanner.close();
                try {
                    if(listener != null) listener.close();
                    if(socket != null) socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public ChessPiece pieceAt(int col, int row) {
        return chessModel.pieceAt(col,row);

    }

    @Override
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        chessModel.movePiece(fromCol,fromRow,toCol,toRow);
        chessBoardPanel.repaint();

        if(printWriter != null)
        {
            printWriter.println( fromCol+","+fromRow+","+toCol+","+toRow);

        }

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

    private void recieveMove(Scanner scanner){

        while(scanner.hasNextLine()) {

            var moveString = scanner.nextLine(); //0,1,0,3
            System.out.println("chess move received: " + moveString);

            var moveStrArray = moveString.split(","); // ["0","1","0","2"]

            var fromCol = Integer.parseInt(moveStrArray[0]);
            var fromRow = Integer.parseInt(moveStrArray[1]);
            var toCol = Integer.parseInt(moveStrArray[2]);
            var toRow = Integer.parseInt(moveStrArray[3]);


            //update UI thread
            SwingUtilities.invokeLater(new Runnable() {

                //Client side
                @Override
                public void run() {
                    chessModel.movePiece(fromCol, fromRow, toCol, toRow);
                    chessBoardPanel.repaint();
                }
            });

        }
    }


        private void runSocketServer(){
            Executors.newFixedThreadPool(1).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener = new ServerSocket(PORT);

                        System.out.println("Server is listening on port 50000");



                        socket = listener.accept();
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        var scanner = new Scanner(socket.getInputStream());






                        //keep reading
                        //duplicate from client

                        recieveMove(scanner);

                    /*
                    while (scanner.hasNextLine())
                    {
                        var moveString = scanner.nextLine(); //0,1,0,3
                        System.out.println("From server: "+ moveString);

                        var moveStrArray = moveString.split(","); // ["0","1","0","2"]

                        var  fromCol = Integer.parseInt(moveStrArray[0]);
                        var  fromRow = Integer.parseInt(moveStrArray[1]);
                        var  toCol = Integer.parseInt(moveStrArray[2]);
                        var  toRow = Integer.parseInt(moveStrArray[3]);


                        //update UI thread
                        SwingUtilities.invokeLater(new Runnable() {

                            //Client side
                            @Override
                            public void run() {
                                chessModel.movePiece(fromCol,fromRow,toCol,toRow);
                                chessBoardPanel.repaint();
                            }
                        });

                    }

                    */

//                    printWriter.println("0,1,0,3");
//                    System.out.println("Server: Sending a move to client");


                    }catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            });

        }

        private void runSocketClient(){

            try {

//                    only once use button
                socket = new Socket(SOCKET_SERVER_IP,PORT);
                System.out.println("Client connected to port "+ PORT);
                var scanner = new Scanner(socket.getInputStream());
                printWriter = new PrintWriter(socket.getOutputStream(), true);





                //create new thread
                Executors.newFixedThreadPool(1).execute(new Runnable() {
                    @Override
                    public void run() {
                        recieveMove(scanner);
                    }
                });

                /* Recieve Move


                //keep reading
                while(scanner.hasNextLine())
                {

                    var moveString = scanner.nextLine(); //0,1,0,3
                    System.out.println("From server: "+ moveString);

                    var moveStrArray = moveString.split(","); // ["0","1","0","2"]

                    var  fromCol = Integer.parseInt(moveStrArray[0]);
                    var  fromRow = Integer.parseInt(moveStrArray[1]);
                    var  toCol = Integer.parseInt(moveStrArray[2]);
                    var  toRow = Integer.parseInt(moveStrArray[3]);


                    //update UI thread
                    SwingUtilities.invokeLater(new Runnable() {

                        //Client side
                        @Override
                        public void run() {
                            chessModel.movePiece(fromCol,fromRow,toCol,toRow);
                            chessBoardPanel.repaint();
                        }
                    });

                }
                */




            } catch (IOException ex) {
                ex.printStackTrace();
            }


        }





    //buttons
    @Override
    public void actionPerformed(ActionEvent e) {
//        System.out.println(e.getSource());

        if(e.getSource() == resetBtn)
        {
//            System.out.println("Reset Clicked");
            chessModel.reset();
            chessBoardPanel.repaint();
            try {
                if(listener!=null)
                {
                    listener.close();
                }
                if(socket!=null)
                {
                    socket.close();

                }
                serverBtn.setEnabled(true);
                clientBtn.setEnabled(true);


            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } else if (e.getSource()== serverBtn) {
/*

Move to an another thread
//            System.out.println("Listen (for socket server) Clicked");
            try(var listener = new ServerSocket(50000)){
                System.out.println("Server is listening to port 50000");
                while(true)
                {
                    try(var socket = listener.accept()){
                        var out = new PrintWriter(socket.getOutputStream(),true);
                        out.println("from (0,1) to (0,2)");
                    }
                }
            } catch (IOException ex) {
               ex.printStackTrace();
            }
*/

            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            frame.setTitle("Chess Server");

            //only need one backgrount thread -> peer to peer


            runSocketServer();
            JOptionPane.showMessageDialog(frame,"Listening on PORT"+PORT);




        }else if (e.getSource()== clientBtn) {

            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            frame.setTitle("Chess Client");
//            System.out.println("Connect (for socket client) Clicked");
            runSocketClient();
            JOptionPane.showMessageDialog(frame,"Connected to port "+PORT);

    }




//    //server side
//    @Override
//    public void run() {
//
//        //thread to listen and connect server and client
//
//        try(var listener = new ServerSocket(PORT)) {
//
//            System.out.println("Server is listening on port 50000");
//
//
//
//                socket = listener.accept();
//                printWriter = new PrintWriter(socket.getOutputStream(), true);
//                var scanner = new Scanner(socket.getInputStream());
//
//
//
//
//
//
//                //keep reading
//                //duplicate from client
//
//                recieveMove(scanner);
//
//                    /*
//                    while (scanner.hasNextLine())
//                    {
//                        var moveString = scanner.nextLine(); //0,1,0,3
//                        System.out.println("From server: "+ moveString);
//
//                        var moveStrArray = moveString.split(","); // ["0","1","0","2"]
//
//                        var  fromCol = Integer.parseInt(moveStrArray[0]);
//                        var  fromRow = Integer.parseInt(moveStrArray[1]);
//                        var  toCol = Integer.parseInt(moveStrArray[2]);
//                        var  toRow = Integer.parseInt(moveStrArray[3]);
//
//
//                        //update UI thread
//                        SwingUtilities.invokeLater(new Runnable() {
//
//                            //Client side
//                            @Override
//                            public void run() {
//                                chessModel.movePiece(fromCol,fromRow,toCol,toRow);
//                                chessBoardPanel.repaint();
//                            }
//                        });
//
//                    }
//
//                    */
//
////                    printWriter.println("0,1,0,3");
////                    System.out.println("Server: Sending a move to client");
//
//
//        }
//        catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }


}}

