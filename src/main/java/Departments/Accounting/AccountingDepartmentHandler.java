package Departments.Accounting;

import Advertiser.AdvertisementProducer;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class AccountingDepartmentHandler {
    private final static String PREV_QUEUE_NAME = "editing_queue";
    private final static String THIS_QUEUE_NAME = "accounting_queue";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            AccountingSocketHandler socketHandler = new AccountingSocketHandler("localhost", 5001);
            Consumer<String> invoiceConsumer = invoice -> {
                System.out.println("Sending invoice to Advertiser...");
                socketHandler.sendMessage(invoice);
            };

            channel.queueDeclare(PREV_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(THIS_QUEUE_NAME, false, false, false, null);

            System.out.println("Accounting Department Handler started. Waiting for messages...");

            channel.basicConsume(PREV_QUEUE_NAME, true, createDeliveryCallback(channel, invoiceConsumer), consumerTag -> {
            });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static DeliverCallback createDeliveryCallback(Channel channel, Consumer<String> invoiceConsumer) {
        Gson gson = new Gson();
        return (consumerTag, delivery) -> {
            byte[] messageBytes = delivery.getBody();
            String messageJson = new String(messageBytes, StandardCharsets.UTF_8);
            System.out.println("Received message from Editing Department!");
            AdvertisementProducer.Advertisement advertisement = gson.fromJson(messageJson, AdvertisementProducer.Advertisement.class);
            String invoice = processMessage(advertisement);
            invoiceConsumer.accept(invoice); // send back to main clause
        };
    }

    private static String processMessage(AdvertisementProducer.Advertisement advertisement) {
        try {
            String company = advertisement.getClient();
            String contact = advertisement.getContact();
            String phone = advertisement.getPhone();

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            String formattedDate = dateFormat.format(currentDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.MONTH, 2); // 2 month payment window before legal action
            Date dueDate = calendar.getTime();
            String formattedDueDate = dateFormat.format(dueDate);

            String invoiceNumber = generateInvoiceNumber();

            DecimalFormat df = new DecimalFormat("#.##"); // Pattern for 2 decimal places

            int campaignCost = new Random().nextInt(500);
            double advertisingFees = Double.parseDouble(df.format(advertisement.calculateCost()));
            double subtotal = Double.parseDouble(df.format(campaignCost + advertisingFees));
            double tax = Double.parseDouble(df.format(subtotal * 1.15));
            double totalAmountDue = Double.parseDouble(df.format(subtotal + tax));

            // Build the invoice
            String invoice = "========================================\n" +
                    "             INVOICE\n" +
                    "----------------------------------------\n" +
                    "Invoice Number: " + invoiceNumber + "\n" +
                    "Date: " + formattedDate + "\n" +
                    "Due Date: " + formattedDueDate + "\n" +
                    "\n" +
                    "Bill To:\n" +
                    "Customer Name: " + company + "\n" +
                    "Contact: " + contact + "\n" +
                    "Phone: " + phone + "\n" +
                    "\n" +
                    "----------------------------------------\n" +
                    "\n" +
                    "Description             Qty    Unit Price    Total\n" +
                    "----------------------------------------\n" +
                    "Marketing Campaign      1      $" + campaignCost + "          $" + campaignCost + "\n" +
                    "Advertising Fees        1      $" + advertisingFees + "          $" + advertisingFees + "\n" +
                    "----------------------------------------\n" +
                    "Subtotal:                                  $" + subtotal + "\n" +
                    "Tax (15%):                                 $" + tax + "\n" +
                    "----------------------------------------\n" +
                    "Total Amount Due:                         $" + totalAmountDue + "\n" +
                    "----------------------------------------\n" +
                    "\n" +
                    "Payment Information:\n" +
                    "----------------------------------------\n" +
                    "Accepted Payment Methods:\n" +
                    "- Bank Transfer\n" +
                    "- Credit Card\n" +
                    "- PayPal\n" +
                    "\n" +
                    "Bank Transfer Details:\n" +
                    "Account Number: 1234567890\n" +
                    "IBAN: GB29NWBK60161331926819\n" +
                    "\n" +
                    "Please include the invoice number in the reference when making the bank transfer.\n" +
                    "\n" +
                    "Payment is due within 60 days of the invoice date.\n" +
                    "========================================";

            return invoice;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the message: " + e.getMessage();
        }
    }

    private static String generateInvoiceNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(1000);
        return "INV-" + currentDate + "-" + String.format("%03d", randomNum);
    }
}
