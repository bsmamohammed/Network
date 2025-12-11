import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    // Define connection parameters for the server.
    // NOTE: ServerHost must be updated to the actual Server IP address (if running on diff LAN).
    static final String ServerHost = "192.168.100.24";
    static final int ServerPort = 8080; // The port the server is set on.

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Socket Socket = null;
        try {
            // Project Requirement: Establish a persistent TCP connection to the server.
             Socket = new Socket(ServerHost, ServerPort);
            System.out.println("Connected to the server at " + ServerHost + " on port " + ServerPort);

            // Set up input and output streams for network communication.
            // reader is for receiving the server response.
            BufferedReader reader = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
            // writer is for sending the client request. 'true' enables auto-flush.
            PrintWriter writer = new PrintWriter(Socket.getOutputStream(), true);

            // Start the main client session loop, allowing multiple requests.
            startClientSession(scanner, reader, writer);

            // The Socket will automatically close when the main try block exits or the application terminates.

        } catch (IOException e) {
            // Handle errors during connection establishment (e.g., Connection Refused).
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    // Method responsible for the continuous interaction loop with the server.
    private static void startClientSession(Scanner scanner, BufferedReader reader, PrintWriter writer)
            throws IOException {
        while (true) { // Loop for multiple requests over the same connection.
            String input = getUserInput(scanner);
            if (shouldExit(input)) {
                socket.close();
                break; // Exit the loop and close the connection.
            }

            String[] requestData = parseInput(input);
            if (requestData == null) {
                continue; // Prompt user again if input is invalid.
            }

            // Convert input strings to double values for calculation.
            double base = Double.parseDouble(requestData[0]);
            double exponent = Double.parseDouble(requestData[1]);

            // Create the request string in the required format.
            String request = String.format("%.2f,%.2f", base, exponent);

            //  Record start time for Round-Trip Time (RTT) measurement.
            // Using nanoTime for high-precision measurement.
            long startTime = System.nanoTime();

            // Send the request message to the server.
            sendRequest(writer, request);

            // receive the computed result from the server.
            String response = receiveResponse(reader);

            // Record end time immediately after receiving the response.
            long endTime = System.nanoTime();

            // Calculate RTT: (endTime - startTime) in nanoseconds, converted to milliseconds (ms).
            double rtt = (endTime - startTime) / 1000000.0;

            if (response != null && response.startsWith("ERROR")) {
                System.out.println("Server Error: " + response);
            } else if (response == null) {
                 // Handle case where server closed connection unexpectedly.
                 System.err.println("Server closed the connection unexpectedly.");
                 break;
            } else {
                // Display the calculated result received from the server.
                displayResult(base, exponent, response);
                // Print the measured RTT.
                System.out.printf("Round-Trip Time: %.4f ms %n", rtt);
            }
        }
        System.out.println("Disconnecting from server...");
        // The socket streams will be implicitly closed upon exiting the try block or main method.
    }

    //  method to prompt for and retrieve user input.
    private static String getUserInput(Scanner scanner) {
        System.out.print("Enter base and exponent in format (e.g. 3,4) (or 'exit' to EXIT): ");
        return scanner.nextLine().trim();
    }

    //  method to check for client exit command.
    private static boolean shouldExit(String input) {
        return "exit".equalsIgnoreCase(input);
    }

    //  method to validate and split the user input into base and exponent.
    private static String[] parseInput(String input) {
        String[] parts = input.split(",");
        if (parts.length != 2) {
            System.out.println("Invalid input! Please enter base and exponent separated by ','.");
            return null;
        }
        return parts;
    }

    //  method to send the request string over the network stream.
    private static void sendRequest(PrintWriter writer, String request) {
        writer.println(request);
    }

    //  method to read one line (the response) from the server.
    private static String receiveResponse(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    //  method to format and display the final result.
    private static void displayResult(double base, double exponent, String response) {
        double result = Double.parseDouble(response);
        System.out.printf("Result: %.2f ^ %.2f = %.4f%n", base, exponent, result);
    }
}
