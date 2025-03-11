package org.example;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
//import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChessController implements ChessDelegate, ActionListener {
    private static final String SOCKET_SERVER_IP = "localhost";
    private static final int PORT = 50000;
    private static final int DEFAULT_TIMER_MINUTES = 10; // 10 minutes per player

    private JFrame frame;
    private ChessModel chessModel = new ChessModel();
    private ChessView chessBoardPanel;
    private JButton resetBtn;
    private JButton serverBtn;
    private JButton clientBtn;
    private JLabel statusLabel;
    private JLabel timerLabel;

    // Server-specific components
    private ServerSocket listener;
    private ThreadPoolExecutor executor;
    private ScheduledExecutorService timerExecutor;
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();
    private final Queue<ClientHandler> waitingClients = new LinkedList<>();
    private boolean isServerRunning = false;
    private JFrame serverFrame;
    private JTextArea serverLog;

    // Client-specific components
    private Socket clientSocket;
    private PrintWriter clientWriter;
    private Scanner clientScanner;
    private String gameId;
    private Player clientPlayer;
    private boolean isConnectedAsClient = false;
    private int secondsRemaining;
    private boolean isMyTurn = false;
    private Timer clientTimer;

    public ChessController() {
        chessModel.reset();
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("Chess");
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());

        chessBoardPanel = new ChessView(this);
        frame.add(chessBoardPanel, BorderLayout.CENTER);

        var buttonsPanel = new JPanel(new GridLayout(3, 1));

        var controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(this);
        controlPanel.add(resetBtn);

        serverBtn = new JButton("Start Server");
        controlPanel.add(serverBtn);
        serverBtn.addActionListener(this);

        clientBtn = new JButton("Connect as Client");
        controlPanel.add(clientBtn);
        clientBtn.addActionListener(this);

        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        timerLabel = new JLabel("Time: --:--", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));

        buttonsPanel.add(controlPanel);
        buttonsPanel.add(statusLabel);
        buttonsPanel.add(timerLabel);

        frame.add(buttonsPanel, BorderLayout.PAGE_END);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cleanup();
            }
        });
    }

    private void setupServerUI() {
        serverFrame = new JFrame("Chess Server Control Panel");
        serverFrame.setSize(600, 400);
        serverFrame.setLayout(new BorderLayout());

        serverLog = new JTextArea();
        serverLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(serverLog);
        serverFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel serverControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton stopServerBtn = new JButton("Stop Server");
        stopServerBtn.addActionListener(e -> stopServer());
        serverControlPanel.add(stopServerBtn);

        JLabel activeSessions = new JLabel("Active Sessions: 0");
        serverControlPanel.add(activeSessions);

        JLabel waitingClients = new JLabel("Waiting Clients: 0");
        serverControlPanel.add(waitingClients);

        // Timer to update stats
        Timer statsTimer = new Timer(1000, e -> {
            activeSessions.setText("Active Sessions: " + gameSessions.size());
            waitingClients.setText("Waiting Clients: " + this.waitingClients.size());
        });
        statsTimer.start();

        serverFrame.add(serverControlPanel, BorderLayout.SOUTH);
        serverFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        serverFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
                serverFrame.dispose();
            }
        });

        serverFrame.setVisible(true);
    }

    private void stopServer() {
        if (isServerRunning) {
            isServerRunning = false;
            logToServer("Stopping server...");

            // Notify all clients of server shutdown
            for (GameSession session : gameSessions.values()) {
                session.notifyServerShutdown();
            }

            try {
                if (listener != null) {
                    listener.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (executor != null) {
                executor.shutdown();
            }

            // Re-enable server button on main window
            SwingUtilities.invokeLater(() -> {
                serverBtn.setEnabled(true);
                frame.setTitle("Chess");
            });

            logToServer("Server stopped.");
        }
    }

    private void cleanup() {
        if (clientWriter != null) clientWriter.close();
        if (clientTimer != null) clientTimer.stop();

        try {
            if (listener != null) listener.close();
            if (clientSocket != null) clientSocket.close();
            if (executor != null) executor.shutdown();
            if (timerExecutor != null) timerExecutor.shutdown();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (serverFrame != null) {
            serverFrame.dispose();
        }
    }

    @Override
    public ChessPiece pieceAt(int col, int row) {
        return chessModel.pieceAt(col, row);
    }

    @Override
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece piece = chessModel.pieceAt(fromCol, fromRow);
        if (piece == null) return;

        // Check if it's the correct player's turn
        if (isConnectedAsClient && piece.getPlayer() != clientPlayer) {
            return;
        }

        if (chessModel.isValidMove(fromCol, fromRow, toCol, toRow)) {
            chessModel.movePiece(fromCol, fromRow, toCol, toRow);
            chessBoardPanel.repaint();

            // If connected as client, send the move to the server
            if (isConnectedAsClient && clientWriter != null) {
                clientWriter.println("MOVE," + gameId + "," + fromCol + "," + fromRow + "," + toCol + "," + toRow);

                // Switch turn
                isMyTurn = false;
                updateTimerStatus();
            }
        }
    }

    @Override
    public boolean isValidMove(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece piece = chessModel.pieceAt(fromCol, fromRow);
        if (piece == null) return false;

        // Check if it's the correct player's turn
        if (isConnectedAsClient && piece.getPlayer() != clientPlayer) {
            return false;
        }

        // If not your turn, don't allow moves
        if (isConnectedAsClient && !isMyTurn) {
            return false;
        }

        return chessModel.isValidMove(fromCol, fromRow, toCol, toRow);
    }

    private void startServer() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20); // Support up to 20 concurrent connections
        timerExecutor = Executors.newScheduledThreadPool(1);

        // Make main window a client-only window
        frame.setTitle("Chess - Client Mode");
        resetBoard();
        updateStatus("Server started in separate window");

        // Create server control panel in a new window
        setupServerUI();

        executor.execute(() -> {
            try {
                listener = new ServerSocket(PORT);
                isServerRunning = true;
                logToServer("Server running on port " + PORT);

                while (isServerRunning) {
                    Socket clientSocket = listener.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    executor.execute(clientHandler);
                    logToServer("New client connected: " + clientSocket.getInetAddress());
                }
            } catch (IOException ex) {
                if (!isServerRunning) {
                    // Server was intentionally closed
                    logToServer("Server stopped");
                } else {
                    ex.printStackTrace();
                    logToServer("Server error: " + ex.getMessage());
                }
            }
        });
    }

    private void logToServer(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        SwingUtilities.invokeLater(() -> {
            if (serverLog != null) {
                serverLog.append("[" + timestamp + "] " + message + "\n");
                // Scroll to the bottom
                serverLog.setCaretPosition(serverLog.getDocument().getLength());
            }
        });
    }

    private void connectAsClient() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            try {
                clientSocket = new Socket(SOCKET_SERVER_IP, PORT);
                clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                clientScanner = new Scanner(clientSocket.getInputStream());

                isConnectedAsClient = true;
                updateStatus("Connected to server");

                // Request to join a game
                clientWriter.println("JOIN");

                // Handle server responses
                while (clientScanner.hasNextLine()) {
                    processServerMessage(clientScanner.nextLine());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                updateStatus("Connection error: " + ex.getMessage());

                // Re-enable connection buttons
                SwingUtilities.invokeLater(() -> {
                    clientBtn.setEnabled(true);
                    serverBtn.setEnabled(true);
                });
            }
        });
    }

    private void processServerMessage(String message) {
        String[] parts = message.split(",");
        String command = parts[0];

        switch (command) {
            case "GAME_CREATED":
                gameId = parts[1];
                clientPlayer = parts[2].equals("WHITE") ? Player.WHITE : Player.BLACK;
                secondsRemaining = DEFAULT_TIMER_MINUTES * 60;

                // Set initial turn state based on player color
                isMyTurn = clientPlayer == Player.WHITE;

                SwingUtilities.invokeLater(() -> {
                    frame.setTitle("Chess - " + clientPlayer + " - Game: " + gameId);
                    updateStatus("Game created. You are playing as " + clientPlayer);
                    resetBoard();

                    // Start the timer display
                    setupClientTimer();
                    updateTimerStatus();
                });
                break;

            case "MOVE":
                if (parts.length >= 6 && parts[1].equals(gameId)) {
                    int fromCol = Integer.parseInt(parts[2]);
                    int fromRow = Integer.parseInt(parts[3]);
                    int toCol = Integer.parseInt(parts[4]);
                    int toRow = Integer.parseInt(parts[5]);

                    SwingUtilities.invokeLater(() -> {
                        chessModel.movePiece(fromCol, fromRow, toCol, toRow);
                        chessBoardPanel.repaint();

                        // Switch turn
                        isMyTurn = true;
                        updateTimerStatus();
                    });
                }
                break;

            case "TIME":
                if (parts.length >= 3 && parts[1].equals(gameId)) {
                    int opponentTime = Integer.parseInt(parts[2]);
                    // We might use this to display opponent's time
                }
                break;

            case "TIMER_UPDATE":
                if (parts.length >= 3 && parts[1].equals(gameId)) {
                    secondsRemaining = Integer.parseInt(parts[2]);
                    updateTimerDisplay();
                }
                break;

            case "OPPONENT_DISCONNECTED":
                if (parts.length >= 2 && parts[1].equals(gameId)) {
                    SwingUtilities.invokeLater(() ->
                            updateStatus("Opponent disconnected. Waiting for new opponent."));
                }
                break;

            case "SERVER_SHUTDOWN":
                SwingUtilities.invokeLater(() -> {
                    updateStatus("Server has been shut down.");
                    JOptionPane.showMessageDialog(frame,
                            "The chess server has been shut down.",
                            "Server Shutdown",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Re-enable connection buttons
                    clientBtn.setEnabled(true);
                    serverBtn.setEnabled(true);
                });
                break;

            default:
                System.out.println("Unknown command: " + command);
        }
    }

    private void setupClientTimer() {
        if (clientTimer != null) {
            clientTimer.stop();
        }

        clientTimer = new Timer(1000, e -> {
            if (isMyTurn && secondsRemaining > 0) {
                secondsRemaining--;
                updateTimerDisplay();

                // Send time update to server every 5 seconds
                if (secondsRemaining % 5 == 0) {
                    clientWriter.println("TIME," + gameId + "," + secondsRemaining);
                }

                // Check for time out
                if (secondsRemaining <= 0) {
                    clientTimer.stop();
                    JOptionPane.showMessageDialog(frame,
                            "Time's up! You lost the game.",
                            "Time Out",
                            JOptionPane.WARNING_MESSAGE);
                    clientWriter.println("TIMEOUT," + gameId);
                }
            }
        });
        clientTimer.start();
    }

    private void updateTimerDisplay() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);

        SwingUtilities.invokeLater(() -> {
            if (isMyTurn) {
                timerLabel.setText("Your Turn - Time: " + timeString);
                timerLabel.setForeground(Color.RED);
            } else {
                timerLabel.setText("Opponent's Turn - Time: " + timeString);
                timerLabel.setForeground(Color.BLACK);
            }
        });
    }

    private void updateTimerStatus() {
        if (isMyTurn) {
            updateStatus("Your turn");
        } else {
            updateStatus("Waiting for opponent's move");
        }
        updateTimerDisplay();
    }

    private void resetBoard() {
        SwingUtilities.invokeLater(() -> {
            chessModel.reset();
            chessBoardPanel.repaint();
        });
    }

    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetBtn) {
            resetBoard();
            if (isConnectedAsClient && clientWriter != null) {
                clientWriter.println("RESET," + gameId);
            }
        } else if (e.getSource() == serverBtn) {
            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            startServer();
        } else if (e.getSource() == clientBtn) {
            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            connectAsClient();
        }
    }

    // Inner class to handle each client connection
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter writer;
        private Scanner scanner;
        private String gameId;
        private Player player;
        private int timeRemaining;
        private boolean isTurn;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.timeRemaining = DEFAULT_TIMER_MINUTES * 60; // 10 minutes in seconds
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                scanner = new Scanner(socket.getInputStream());

                while (scanner.hasNextLine()) {
                    processClientMessage(scanner.nextLine());
                }
            } catch (IOException e) {
                logToServer("Client disconnected: " + e.getMessage());
                handleClientDisconnection();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processClientMessage(String message) {
            String[] parts = message.split(",");
            String command = parts[0];

            switch (command) {
                case "JOIN":
                    synchronized (waitingClients) {
                        if (waitingClients.isEmpty()) {
                            // No waiting clients, add this client to the queue
                            waitingClients.add(this);
                            player = Player.WHITE;  // First player is WHITE
                            isTurn = true;  // White goes first
                            logToServer("Client added to waiting queue");
                        } else {
                            // Match with a waiting client
                            ClientHandler opponent = waitingClients.poll();
                            gameId = UUID.randomUUID().toString().substring(0, 8);
                            player = Player.BLACK;  // Second player is BLACK
                            isTurn = false;  // Black goes second

                            // Create a new game session
                            GameSession session = new GameSession(gameId, opponent, this);
                            gameSessions.put(gameId, session);

                            // Inform both clients about the game
                            opponent.gameId = gameId;
                            opponent.writer.println("GAME_CREATED," + gameId + ",WHITE");
                            writer.println("GAME_CREATED," + gameId + ",BLACK");

                            logToServer("New game created: " + gameId + " between WHITE and BLACK");
                        }
                    }
                    break;

                case "MOVE":
                    if (parts.length >= 6) {
                        String gameId = parts[1];
                        GameSession session = gameSessions.get(gameId);

                        if (session != null) {
                            // Forward the move to the opponent
                            session.broadcastToOpponent(this, message);

                            // Switch turns
                            isTurn = false;
                            ClientHandler opponent = this == session.whitePlayer ?
                                    session.blackPlayer : session.whitePlayer;
                            if (opponent != null) {
                                opponent.isTurn = true;
                            }

                            logToServer("Move in game " + gameId + ": " +
                                    parts[2] + "," + parts[3] + " to " + parts[4] + "," + parts[5]);
                        }
                    }
                    break;

                case "TIME":
                    if (parts.length >= 3) {
                        timeRemaining = Integer.parseInt(parts[2]);
                    }
                    break;

                case "TIMEOUT":
                    if (parts.length >= 2) {
                        String gameId = parts[1];
                        GameSession session = gameSessions.get(gameId);

                        if (session != null) {
                            // Notify opponent of win by timeout
                            session.broadcastToOpponent(this, "TIMEOUT_WIN," + gameId);
                            logToServer("Player timed out in game " + gameId);
                        }
                    }
                    break;

                case "RESET":
                    if (parts.length >= 2) {
                        String gameId = parts[1];
                        GameSession session = gameSessions.get(gameId);

                        if (session != null) {
                            // Forward the reset request to the opponent
                            session.broadcastToOpponent(this, "RESET," + gameId);

                            // Reset times
                            timeRemaining = DEFAULT_TIMER_MINUTES * 60;

                            // Reset turns
                            if (player == Player.WHITE) {
                                isTurn = true;
                                if (session.blackPlayer != null) {
                                    session.blackPlayer.isTurn = false;
                                }
                            } else {
                                isTurn = false;
                                if (session.whitePlayer != null) {
                                    session.whitePlayer.isTurn = true;
                                }
                            }

                            logToServer("Game reset: " + gameId);
                        }
                    }
                    break;

                default:
                    System.out.println("Unknown command from client: " + command);
            }
        }

        private void handleClientDisconnection() {
            // Remove from waiting queue if present
            synchronized (waitingClients) {
                waitingClients.remove(this);
            }

            // Notify opponent if in a game
            if (gameId != null) {
                GameSession session = gameSessions.get(gameId);
                if (session != null) {
                    session.handlePlayerDisconnect(this);
                    if (session.isEmpty()) {
                        gameSessions.remove(gameId);
                        logToServer("Game removed: " + gameId);
                    }
                }
            }
        }
    }

    // Class to manage a game between two clients
    private class GameSession {
        private final String gameId;
        private ClientHandler whitePlayer;
        private ClientHandler blackPlayer;

        public GameSession(String gameId, ClientHandler whitePlayer, ClientHandler blackPlayer) {
            this.gameId = gameId;
            this.whitePlayer = whitePlayer;
            this.blackPlayer = blackPlayer;
        }

        public void broadcastToOpponent(ClientHandler sender, String message) {
            if (sender == whitePlayer && blackPlayer != null) {
                blackPlayer.writer.println(message);
            } else if (sender == blackPlayer && whitePlayer != null) {
                whitePlayer.writer.println(message);
            }
        }

        public void handlePlayerDisconnect(ClientHandler player) {
            if (player == whitePlayer) {
                whitePlayer = null;
                if (blackPlayer != null) {
                    blackPlayer.writer.println("OPPONENT_DISCONNECTED," + gameId);
                }
                logToServer("White player disconnected from game " + gameId);
            } else if (player == blackPlayer) {
                blackPlayer = null;
                if (whitePlayer != null) {
                    whitePlayer.writer.println("OPPONENT_DISCONNECTED," + gameId);
                }
                logToServer("Black player disconnected from game " + gameId);
            }
        }

        public void notifyServerShutdown() {
            if (whitePlayer != null) {
                whitePlayer.writer.println("SERVER_SHUTDOWN");
            }
            if (blackPlayer != null) {
                blackPlayer.writer.println("SERVER_SHUTDOWN");
            }
        }

        public boolean isEmpty() {
            return whitePlayer == null && blackPlayer == null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessController::new);
    }
}