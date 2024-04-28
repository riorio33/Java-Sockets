package Departments.Accounting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class AccountingSocketHandler {
    private final String serverAddress;
    private final int serverPort;

    public AccountingSocketHandler(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void sendMessage(String message) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()) {
            message += "<END_OF_MESSAGE>"; // Add the termination sequence at the end of the message
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            byte[] responseBytes = new byte[1024];
            int bytesRead = inputStream.read(responseBytes);
            String response = new String(responseBytes, 0, bytesRead, StandardCharsets.UTF_8);
            System.out.println("Received response: " + response);
        } catch (IOException e) {
            System.out.println("Error sending message");
            e.printStackTrace();
        }
    }
}