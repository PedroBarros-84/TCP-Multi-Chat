import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final int PORT = 8888;
    private Socket socketConnection;


    public static void main(String[] args) {

        ChatClient chatClient = new ChatClient();
        chatClient.start();

    }


    public void start() {

        try {

            socketConnection = new Socket("localhost", PORT);
            System.out.println("Connected to: " + socketConnection.toString());

            initiateChat();

            socketConnection.close();

        } catch (IOException e) {

            System.err.println("\nCouldn't connect to server.\n");

        }

    }



    public void initiateChat() throws IOException {

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter sendToSocket = new PrintWriter(socketConnection.getOutputStream(), true);
        BufferedReader receivedFromSocket = new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));

        Thread messageReceivedListener = new Thread(new Runnable() {

            @Override
            public void run() {

                while(true) {

                    try {

                        String message = receivedFromSocket.readLine();

                        if (message == null) {
                            System.err.println("Server is down!");
                            System.exit(1);
                        }

                        System.out.println(message);

                    } catch (IOException e) {

                        System.out.println("Session ended!");
                        System.exit(1);

                    }
                }

            }
        });

        messageReceivedListener.start();

        // Listen for console/user input
        while (true) {

            String message = consoleReader.readLine();

            // When client disconnects message will automatically be null
            if (message == null) {
                sendToSocket.println(message);
            }

            //assert message != null;
            if (message.equals("/quit")) {
                sendToSocket.println(message);
                break;
            }

            // Only send message if it has content
            if (!message.equals("")) {
                sendToSocket.println(message);
            }

        }

    }

}
