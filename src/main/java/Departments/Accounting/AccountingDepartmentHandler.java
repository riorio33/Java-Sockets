package Departments.Accounting;

import Departments.SeedingProducer;
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

public class AccountingDepartmentHandler {
    private final static String PREV_QUEUE_NAME = "editing_queue";
    private final static String THIS_QUEUE_NAME = "accounting_queue";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(PREV_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(THIS_QUEUE_NAME, false, false, false, null);

            System.out.println("Accounting Department Handler started. Waiting for messages...");

            channel.basicConsume(PREV_QUEUE_NAME, true, createDeliveryCallback(channel), consumerTag -> {
            });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static DeliverCallback createDeliveryCallback(Channel channel) {
        Gson gson = new Gson();

        return (consumerTag, delivery) -> {
            byte[] messageBytes = delivery.getBody();
            String messageJson = new String(messageBytes, StandardCharsets.UTF_8);
            System.out.println("Received message from Accounting Department!");
            SeedingProducer.Advertisement advertisement = gson.fromJson(messageJson, SeedingProducer.Advertisement.class);
            System.out.println(processMessage(advertisement));
        };
    }

    private static String processMessage(SeedingProducer.Advertisement advertisement) {
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

            int campaignCost = calculateCampaignCost(new Random().nextInt(500) + 1);
            double advertisingFees = advertisement.calculateCost();
            double subtotal = campaignCost + advertisingFees;
            double tax = subtotal * 1.15;
            double totalAmountDue = subtotal + tax;

            advertisingFees = Double.parseDouble(df.format(advertisingFees));
            subtotal = Double.parseDouble(df.format(subtotal));
            tax = Double.parseDouble(df.format(tax));
            totalAmountDue = Double.parseDouble(df.format(totalAmountDue));

            // Build the invoice
            StringBuilder invoice = new StringBuilder();
            invoice.append("========================================\n");
            invoice.append("             INVOICE\n");
            invoice.append("----------------------------------------\n");
            invoice.append("Invoice Number: ").append(invoiceNumber).append("\n");
            invoice.append("Date: ").append(formattedDate).append("\n");
            invoice.append("Due Date: ").append(formattedDueDate).append("\n");
            invoice.append("\n");
            invoice.append("Bill To:\n");
            invoice.append("Customer Name: ").append(company).append("\n");
            invoice.append("Contact: ").append(contact).append("\n");
            invoice.append("Phone: ").append(phone).append("\n");
            invoice.append("\n");
            invoice.append("----------------------------------------\n");
            invoice.append("\n");
            invoice.append("Description             Qty    Unit Price    Total\n");
            invoice.append("----------------------------------------\n");
            invoice.append("Marketing Campaign      1      $").append(campaignCost).append("          $").append(campaignCost).append("\n");
            invoice.append("Advertising Fees        1      $").append(advertisingFees).append("          $").append(advertisingFees).append("\n");
            invoice.append("----------------------------------------\n");
            invoice.append("Subtotal:                                  $").append(subtotal).append("\n");
            invoice.append("Tax (15%):                                 $").append(tax).append("\n");
            invoice.append("----------------------------------------\n");
            invoice.append("Total Amount Due:                         $").append(totalAmountDue).append("\n");
            invoice.append("----------------------------------------\n");
            invoice.append("\n");
            invoice.append("Payment Information:\n");
            invoice.append("----------------------------------------\n");
            invoice.append("Accepted Payment Methods:\n");
            invoice.append("- Bank Transfer\n");
            invoice.append("- Credit Card\n");
            invoice.append("- PayPal\n");
            invoice.append("\n");
            invoice.append("Bank Transfer Details:\n");
            invoice.append("Account Number: 1234567890\n");
            invoice.append("IBAN: GB29NWBK60161331926819\n");
            invoice.append("\n");
            invoice.append("Please include the invoice number in the reference when making the bank transfer.\n");
            invoice.append("\n");
            invoice.append("Payment is due within 60 days of the invoice date.\n");
            invoice.append("========================================");

            try {
                System.out.println("Accounting is processing the invoice");
                Thread.sleep(1000);


            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return invoice.toString();

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

    private static SeedingProducer.Advertisement parseAdvertisement(String message) {
        String[] parts = message.split(",");
        String id = parts[0];
        String company = parts[1];
        String contact = parts[2];
        String email = parts[3];
        String phone = parts[4];
        int size = 0;
        int placement = 0;

        for (String part : parts) {
            if (part.trim().startsWith("size=")) {
                try {
                    size = Integer.parseInt(part.trim().substring(5));
                } catch (NumberFormatException e) {
                    // Handle invalid input for size
                    System.err.println("Invalid size value: " + part.trim().substring(5));
                }
            } else if (part.trim().startsWith("placement=")) {
                try {
                    placement = Integer.parseInt(part.trim().substring(10));
                } catch (NumberFormatException e) {
                    // Handle invalid input for placement
                    System.err.println("Invalid placement value: " + part.trim().substring(10));
                }
            }
        }

        String content = parts[5];
        return new SeedingProducer.Advertisement(id, company, contact, phone, email, size, placement, content);
    }

    private static int calculateCampaignCost(int price) {
        return (int) (price * 1.10);
    }

}
