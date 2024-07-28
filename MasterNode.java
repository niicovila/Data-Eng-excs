import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MasterNode {
    private static final int BASE_PORT = 5001;
    private static final int NUM_WORKERS = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Ping workers");
            System.out.println("2. Broadcast to workers");
            System.out.println("3. Round-Robin messaging");
            System.out.println("4. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
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
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void sendPing() throws IOException {
        System.out.println("Sending PING to all workers...");
        List<Socket> workerSockets = new ArrayList<>();

        try {
            // Initialize sockets
            for (int i = 0; i < NUM_WORKERS; i++) {
                workerSockets.add(new Socket("localhost", BASE_PORT + i));
            }

            // Send PING message to all workers
            for (Socket socket : workerSockets) {
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    out.write("PING\n");
                    out.flush();
                    String response = in.readLine();
                    System.out.println("Response: " + response);
                } catch (IOException e) {
                    System.err.println("Error with socket communication: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            }
        } finally {
            // Make sure to close all sockets
            for (Socket socket : workerSockets) {
                if (!socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.err.println("Error closing socket: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void sendBroadcast() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS); // Use as much threads as number of workers
        List<Socket> workerSockets = new ArrayList<>();

        try {
            System.out.println("Sending BROADCAST to all workers...");

            for (int i = 0; i < NUM_WORKERS; i++) {
                workerSockets.add(new Socket("localhost", BASE_PORT + i));
            }

            // Send broadcast message to all workers concurrently
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Socket socket : workerSockets) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                        out.write("BROADCAST\n");
                        out.flush();
                        String response = in.readLine();
                        System.out.println("Response: " + response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, executor);
                futures.add(future);
            }

            // Wait for all workers to ack to the broadcast
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Make sure to close all sockets
            for (Socket socket : workerSockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Close the thread pool
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private static void sendRoundRobin() throws IOException {
        System.out.println("Sending CHAIN message to first worker...");
        try (Socket firstWorkerSocket = new Socket("localhost", BASE_PORT);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(firstWorkerSocket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(firstWorkerSocket.getInputStream()))) {

            out.write("MasterNode\n");
            out.flush();

            // Receive the round-robin message back
            String response = in.readLine();
            System.out.println("Round-Robin Response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
