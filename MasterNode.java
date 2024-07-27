import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MasterNode {
    private static final int BASE_PORT = 5001;
    private static final int NUM_WORKERS = 3;
    private static List<Socket> workerSockets = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Attempt to connect to worker nodes
            for (int i = 0; i < NUM_WORKERS; i++) {
                while (true) {
                    try {
                        Socket workerSocket = new Socket("localhost", BASE_PORT + i);
                        workerSockets.add(workerSocket);
                        System.out.println("Connected to Worker " + (BASE_PORT + i));
                        break;
                    } catch (ConnectException e) {
                        System.out.println("Worker " + (BASE_PORT + i) + " not available, retrying...");
                        Thread.sleep(1000); // Wait for a second before retrying
                    }
                }
            }

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Choose an option:");
                System.out.println("1. Ping workers");
                System.out.println("2. Broadcast to workers");
                System.out.println("3. Round-Robin messaging");
                System.out.println("4. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        sendPing();
                        break;
                    case 2:
                        sendBroadcast();
                        break;
                    case 3:
                        sendRoundRobin();
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        closeConnections();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendPing() throws IOException {
        System.out.println("Sending PING to all workers...");
        for (Socket socket : workerSockets) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("PING");
        }

        // Read responses
        BufferedReader in;
        for (Socket socket : workerSockets) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Response: " + in.readLine());
        }
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("Sending BROADCAST to all workers...");
        for (Socket socket : workerSockets) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("BROADCAST");
        }

        // Read responses
        BufferedReader in;
        for (Socket socket : workerSockets) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Response: " + in.readLine());
        }
    }

    private static void sendRoundRobin() throws IOException {
        System.out.println("Sending CHAIN message to first worker...");
        PrintWriter out = new PrintWriter(workerSockets.get(0).getOutputStream(), true);
        out.println("CHAIN|Master");

        // Receive the round-robin message back
        BufferedReader in = new BufferedReader(new InputStreamReader(workerSockets.get(0).getInputStream()));
        String response = in.readLine();
        System.out.println("Round-Robin Response: " + response);
    }

    private static void closeConnections() throws IOException {
        for (Socket socket : workerSockets) {
            socket.close();
        }
    }
}
