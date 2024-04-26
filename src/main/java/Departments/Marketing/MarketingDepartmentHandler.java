package Departments.Marketing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MarketingDepartmentHandler {
    private final static String PREV_QUEUE_NAME = "advertisement_queue";
    private final static String THIS_QUEUE_NAME = "marketing_queue";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ running on localhost

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(PREV_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(THIS_QUEUE_NAME, false, false, false, null);

            System.out.println("Marketing Department Handler started. Waiting for messages...");

            channel.basicConsume(PREV_QUEUE_NAME, true, createDeliveryCallback(channel), consumerTag -> {
            });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static DeliverCallback createDeliveryCallback(Channel channel) {
        return (consumerTag, delivery) -> {
            byte[] message = delivery.getBody();
            System.out.println("Received message from Advertiser!");
            processMessage();
            sendMessageToNextDepartment(channel, message);
        };
    }


    private static void processMessage() {
        try {
            System.out.print("Recording advertisement details in PostgreSQL DB");
            Thread dotThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        System.out.print(".");
                        Thread.sleep(500);
                        System.out.print("\b");
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            dotThread.start();
            Thread.sleep(3000);
            dotThread.interrupt();

            System.out.println("\nAdvertisement stored in the database.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sendMessageToNextDepartment(Channel channel, byte[] message) throws IOException {
        channel.basicPublish("", THIS_QUEUE_NAME, null, message);
        System.out.println("Sent message to the Editing department!");
    }
}
