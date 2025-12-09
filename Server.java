import java.io.*;
import java.net.*;

public class Server {

    static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Accept new client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Handle client in a separate thread
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Inner class to handle client connections in separate threads
    static class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String clientRequest;
                while ((clientRequest = clientInput.readLine()) != null) {
                    System.out.println("Received from client: " + clientRequest);
                    String response = processRequest(clientRequest);
                    clientOutput.println(response);
                    System.out.println("Sent response to client: " + response);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        // Method to process the client's request
        private String processRequest(String request) {
            try {
                String[] parts = request.split(",");
                if (parts.length != 2) {
                    return "ERROR: Invalid request format. Use 'base,exponent'";
                }

                double base = Double.parseDouble(parts[0]);
                double exponent = Double.parseDouble(parts[1]);

                double result = Math.pow(base, exponent);
                return String.valueOf(result);

            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }
    }
}
