package Advertiser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class InvoiceConsumer {
    public static void main(String[] args) throws IOException {
        int serverPort = 5001;

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server started. Waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from client!");
                new Thread(() -> processClientConnection(clientSocket)).start();
            }
        }
    }

    private static void processClientConnection(Socket clientSocket) {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            byte[] invoiceBytes = new byte[1024];
            int bytesRead;
            StringBuilder invoiceString = new StringBuilder();
            while ((bytesRead = inputStream.read(invoiceBytes)) != -1) {
                String chunk = new String(invoiceBytes, 0, bytesRead, StandardCharsets.UTF_8);
                if (chunk.contains("<END_OF_MESSAGE>")) {
                    // End of message reached, break the loop
                    invoiceString.append(chunk.substring(0, chunk.indexOf("<END_OF_MESSAGE>")));
                    break;
                }
                invoiceString.append(chunk);
            }
            System.out.println("Received invoice:\n" + invoiceString);

            String response = "Invoice received by customer!";
            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Error handling client connection");
            e.printStackTrace();
        }
    }
}