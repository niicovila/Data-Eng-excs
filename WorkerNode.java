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
    static class WorkerHandler implements Runnable { // Manages communication for each worker
        private Socket socket; // represents the connection with a client (a worker node or master node)

        public WorkerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                String message = in.readLine();
                if (message == null) return;

                System.out.println("Received: " + message);

                if (message.equals("PING")) {
                    out.write("PONG\n");
                    out.flush();
                } else if (message.equals("BROADCAST")) {
                    out.write("BROADCAST_RECEIVED\n");
                    out.flush();
                } else {
                    handleChainMessage(message, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleChainMessage(String message, BufferedWriter out) throws IOException {
            // Append this worker's id to the message
            String chainMessage = message + " -> Worker" + PORT;
            System.out.println("Forwarding: " + chainMessage);

            // Forward message to next worker
            int nextWorkerPort = PORT + 1;
            try (Socket nextWorkerSocket = new Socket("localhost", nextWorkerPort);
                BufferedWriter nextWorkerOut = new BufferedWriter(new OutputStreamWriter(nextWorkerSocket.getOutputStream()));
                BufferedReader nextWorkerIn = new BufferedReader(new InputStreamReader(nextWorkerSocket.getInputStream()))) {

                nextWorkerOut.write(chainMessage + "\n");
                nextWorkerOut.flush();
                String response = nextWorkerIn.readLine();
                out.write(response + "\n");
                out.flush();
            } catch (ConnectException e) {
                // Last worker in the chain returns the message to the master
                out.write(chainMessage + "\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
