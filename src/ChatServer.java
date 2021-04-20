import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {


    private final int PORT = 8888;
    private ServerSocket serverSocket;
    private Hashtable<String, ClientConnection> clientConnectionsMap;
    private ExecutorService allConnectionsExecutor;


    public static void main(String[] args) {

        ChatServer chatServer = new ChatServer();
        chatServer.start();

    }

    public ChatServer() {
        clientConnectionsMap = new Hashtable<>();
    }

    public void start() {

        try {

            serverSocket = new ServerSocket(PORT);
            System.out.println("Binding to port " + PORT);
            System.out.println("Server started: " + serverSocket.toString());

            listenForConnections();

        } catch (IOException e) {

            System.err.println("\nSomething went wrong. Check stack trace bellow:\n");
            e.printStackTrace();

        }

    }

    // Listen for connections and add new socket to ClientSocketsArray
    public void listenForConnections() throws IOException {

        // Multi-threading manager
        allConnectionsExecutor = Executors.newCachedThreadPool();

        while (true) {

            // Wait and accept new client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected at: " + clientSocket.getLocalSocketAddress());

            // Instantiate client connection
            ClientConnection clientConnection = new ClientConnection(clientSocket);

            // Add client socket to executor for multi-threading execution
            allConnectionsExecutor.submit(clientConnection);

        }

    }


    // Send message to all users
    private void broadcast(ClientConnection sender, String message) {
        for (ClientConnection receiver : clientConnectionsMap.values()) {
            if (!sender.equals(receiver)) {
                receiver.sendMessage(sender.getNickname() + ": " + message);
            }
        }
    }


    // Send message to specific user
    private void whisper(String sender, String message, String destination) {
        clientConnectionsMap.get(destination).sendMessage("From " + sender + " to you: " + message);
    }


    private String getAllOnlineUsers() {

        StringBuilder allUsers = new StringBuilder();
        allUsers.append("Online users: ");

        for (String nickname : clientConnectionsMap.keySet()) {
            allUsers.append(nickname).append(", ");
        }
        allUsers.delete(allUsers.length() - 2, allUsers.length());

        return allUsers.toString();
    }


    private class ClientConnection implements Runnable {

        private String clientNickname;
        private Socket mySocket;
        private BufferedReader receivedFromSocket;
        private PrintWriter sendThroughSocket;

        public ClientConnection(Socket socket) {
            mySocket = socket;
        }

        public String getNickname() {
            return clientNickname;
        }

        public void sendMessage(String message) {
            sendThroughSocket.println(message);
        }


        @Override
        public void run() {

            try {

                receivedFromSocket = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
                sendThroughSocket = new PrintWriter(mySocket.getOutputStream(), true);
                setClientNickname();

            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {

                try {

                    String message = receivedFromSocket.readLine();

                    if (message == null || message.equals("/quit")) {
                        clientConnectionsMap.remove(clientNickname);
                        broadcast(this, "--- Logged Out ---");
                        break;
                    }

                    if (message.equals("/list")) {
                        sendMessage(getAllOnlineUsers());
                        continue;
                    }

                    if (message.matches("@[\\w_-]+\\s\\w.*")) {
                        String recipient = message.split(" ")[0].substring(1);
                        String realMessage = message.substring(message.indexOf(" ") + 1);
                        whisper(clientNickname, realMessage, recipient);
                        continue;
                    }

                    broadcast(this, message);

                } catch (IOException e) {

                    break;

                }

            }

            try {

                mySocket.close();
                receivedFromSocket.close();
                sendThroughSocket.close();

            } catch (IOException e) {

                System.err.println("Couldn't close socket");
            }

        }

        private void setClientNickname() throws IOException {

            sendThroughSocket.println("Welcome! Choose a nickname: ");

            boolean nicknameIsAvailable;
            String clientNicknameChoice;

            do {

                do {
                    clientNicknameChoice = receivedFromSocket.readLine();
                    if (clientNicknameChoice.matches("[\\w_-]+")) {
                        break;
                    }

                    sendThroughSocket.println("Nickname can only contain: letters, numbers, underscore(_) and dash(-)");
                    sendThroughSocket.println("Choose again:");

                } while (true);

                nicknameIsAvailable = true;

                // Check if nickname already exists
                for (String existingNickname : clientConnectionsMap.keySet()) {
                    if (clientNicknameChoice.equals(existingNickname)) {
                        nicknameIsAvailable = false;
                        sendThroughSocket.println("Sorry, nickname already exists. Choose another: ");
                    }
                }

            } while (!nicknameIsAvailable);

            clientNickname = clientNicknameChoice;

            sendThroughSocket.println("Welcome " + clientNickname + "!");
            broadcast(this, "--- Logged In ---");

            // Add client connection to hashtable of all online connections
            clientConnectionsMap.put(clientNickname, this);

        }

    }

}