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
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class ChessController implements ChessDelegate, ActionListener {
    private String SOCKET_SERVER_IP = "172.20.10.2";
    private int PORT = 50000;
    private JFrame frame;
    private ChessModel chessModel = new ChessModel();
    private ChessView chessBoardPanel;
    private JButton resetBtn;
    private JButton serverBtn;
    private JButton clientBtn;
    private ServerSocket listener;
    private Socket socket;
    private PrintWriter printWriter;
    private boolean isServer = false;
    private boolean isClient = false;

    public ChessController() {
        chessModel.reset();
        frame = new JFrame("Chess");
        frame.setSize(500, 550);
        frame.setLayout(new BorderLayout());

        chessBoardPanel = new ChessView(this);
        frame.add(chessBoardPanel, BorderLayout.CENTER);

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
                if (printWriter != null) printWriter.close();
                try {
                    if (listener != null) listener.close();
                    if (socket != null) socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public ChessPiece pieceAt(int col, int row) {
        return chessModel.pieceAt(col, row);
    }

    @Override
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece piece = chessModel.pieceAt(fromCol, fromRow);
        if (piece == null) return;

        // Check if it's the correct player's turn based on connection type
        if ((isServer && piece.getPlayer() != Player.WHITE) ||
                (isClient && piece.getPlayer() != Player.BLACK)) {
            return;
        }

        if (chessModel.isValidMove(fromCol, fromRow, toCol, toRow)) {
            chessModel.movePiece(fromCol, fromRow, toCol, toRow);
            chessBoardPanel.repaint();

            if (printWriter != null) {
                printWriter.println(fromCol + "," + fromRow + "," + toCol + "," + toRow);
            }
        }
    }

    @Override
    public boolean isValidMove(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece piece = chessModel.pieceAt(fromCol, fromRow);
        if (piece == null) return false;

        // Check if it's the correct player's turn based on connection type
        if ((isServer && piece.getPlayer() != Player.WHITE) ||
                (isClient && piece.getPlayer() != Player.BLACK)) {
            return false;
        }

        return chessModel.isValidMove(fromCol, fromRow, toCol, toRow);
    }

    private void receiveMove(Scanner scanner) {
        while (scanner.hasNextLine()) {
            var moveString = scanner.nextLine();
            System.out.println("chess move received: " + moveString);

            var moveStrArray = moveString.split(",");
            var fromCol = Integer.parseInt(moveStrArray[0]);
            var fromRow = Integer.parseInt(moveStrArray[1]);
            var toCol = Integer.parseInt(moveStrArray[2]);
            var toRow = Integer.parseInt(moveStrArray[3]);

            SwingUtilities.invokeLater(() -> {
                chessModel.movePiece(fromCol, fromRow, toCol, toRow);
                chessBoardPanel.repaint();
            });
        }
    }

    private void runSocketServer() {
        Executors.newFixedThreadPool(1).execute(() -> {
            try {
                listener = new ServerSocket(PORT);
                System.out.println("Server is listening on port 50000");

                socket = listener.accept();
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                var scanner = new Scanner(socket.getInputStream());

                isServer = true;
                receiveMove(scanner);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void runSocketClient() {
        try {
            socket = new Socket(SOCKET_SERVER_IP, PORT);
            System.out.println("Client connected to port " + PORT);
            var scanner = new Scanner(socket.getInputStream());
            printWriter = new PrintWriter(socket.getOutputStream(), true);

            isClient = true;
            Executors.newFixedThreadPool(1).execute(() -> {
                receiveMove(scanner);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetBtn) {
            chessModel.reset();
            chessBoardPanel.repaint();
            try {
                if (listener != null) {
                    listener.close();
                }
                if (socket != null) {
                    socket.close();
                }
                serverBtn.setEnabled(true);
                clientBtn.setEnabled(true);
                isServer = false;
                isClient = false;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == serverBtn) {
            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            frame.setTitle("Chess Server (White)");
            runSocketServer();
            JOptionPane.showMessageDialog(frame, "Listening on PORT " + PORT);
        } else if (e.getSource() == clientBtn) {
            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            frame.setTitle("Chess Client (Black)");
            runSocketClient();
            JOptionPane.showMessageDialog(frame, "Connected to port " + PORT);
        }
    }

    public static void main(String[] args) {
        new ChessController();
    }
}