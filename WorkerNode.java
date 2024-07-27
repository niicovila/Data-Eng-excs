import java.io.*;
import java.net.*;

public class WorkerNode {
    private static int PORT;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java WorkerNode <port>");
            return;
        }
        
        PORT = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Worker Node started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new WorkerHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class WorkerHandler implements Runnable {
        private Socket socket;

        public WorkerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String message = in.readLine();
                if (message == null) return;

                System.out.println("Received: " + message);

                if (message.equals("PING")) {
                    out.println("PONG");
                } else if (message.equals("BROADCAST")) {
                    out.println("BROADCAST_RECEIVED");
                } else if (message.startsWith("CHAIN")) {
                    handleChainMessage(message, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleChainMessage(String message, PrintWriter out) throws IOException {
            String[] parts = message.split("\\|");
            String chainMessage = parts[1] + " -> Worker" + PORT;

            // Forward message to next worker
            int nextWorkerPort = PORT + 1;
            try {
                Socket nextWorkerSocket = new Socket("localhost", nextWorkerPort);
                PrintWriter nextWorkerOut = new PrintWriter(nextWorkerSocket.getOutputStream(), true);
                BufferedReader nextWorkerIn = new BufferedReader(new InputStreamReader(nextWorkerSocket.getInputStream()));

                nextWorkerOut.println("CHAIN|" + chainMessage);
                String response = nextWorkerIn.readLine();
                nextWorkerSocket.close();

                out.println(response);
            } catch (ConnectException e) {
                // Last worker in the chain, return the message to the master
                out.println(chainMessage);
            }
        }
    }
}
