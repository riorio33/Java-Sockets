package Advertiser;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Random;

public class AdvertisementProducer {
    private static final String QUEUE_NAME = "advertisement_queue";
    private static final Advertisement[] advertisements = {
            // Advertisement(String id, String company, String contact, String email, String phone, int size, int placement, String content)
            new Advertisement("abb3a102-4254-430f-8340-cb6b42981d4e", "Schaden-Terry", "Kit Vaulkhard", "kvaulkhard0@narod.ru", "3817242344", 3, 22, "Nulla tellus. In sagittis dui vel nisl."),
            new Advertisement("de2b9238-fd5b-435d-9664-5e47fcb92295", "Marks-Mayer", "Agata Colmer", "acolmer1@last.fm", "4018665037", 3, 17, "Donec ut dolor. Morbi vel lectus in quam fringilla rhoncus."),
            new Advertisement("caf78c1f-252d-44fe-991b-1ceb63479730", "D'Amore Group", "Tiphany Tamsett", "ttamsett2@spiegel.de", "5585113840", 5, 16, "Vestibulum rutrum rutrum neque. Aenean auctor gravida sem. Praesent id massa id nisl venenatis lacinia."),
            new Advertisement("fc812ee4-d13f-45f4-a50d-07619221b861", "Durgan, Kuvalis and Wisozk", "Aurelia Treversh", "atreversh3@mozilla.org", "4062030243", 3, 32, "Aliquam quis turpis eget elit sodales scelerisque. Mauris sit amet eros."),
            new Advertisement("2c6c4c2c-b7a5-4160-877e-40ccc63bd373", "Carter LLC", "Stern Parry", "sparry4@usgs.gov", "1197033908", 2, 19, "Integer non velit. Donec diam neque, vestibulum eget, vulputate ut, ultrices vel, augue. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Donec pharetra, magna vestibulum aliquet ultrices, erat tortor sollicitudin mi, sit amet lobortis sapien sapien non mi. Integer ac neque. Duis bibendum."),
            new Advertisement("62d1d7f9-2267-437e-a43d-f6ba573d5393", "Champlin LLC", "Kacey Winkell", "kwinkell5@nature.com", "8668235021", 5, 1, "Cras mi pede, malesuada in, imperdiet et, commodo vulputate, justo."),
            new Advertisement("9d6087b3-0a2c-457e-8910-53a979d18a6c", "Breitenberg, Weissnat and Wisoky", "Wesley Amiable", "wamiable6@shareasale.com", "2834383735", 3, 31, "Donec semper sapien a libero. Nam dui."),
            new Advertisement("39260409-0e60-47cb-aea1-8d3c17e27a53", "Harvey Inc", "Orelle Triswell", "otriswell7@wp.com", "2487556364", 5, 5, "Vivamus in felis eu sapien cursus vestibulum. Proin eu mi. Nulla ac enim. In tempor, turpis nec euismod scelerisque, quam turpis adipiscing lorem, vitae mattis nibh ligula nec sem."),
            new Advertisement("7e06b7e7-2063-40c9-98f0-796db6f765da", "Schmitt-Koepp", "Abie Harby", "aharby8@aboutads.info", "1301512199", 2, 22, "In congue."),
            new Advertisement("b88cba3f-3e76-470c-8d6f-b195acea5fb8", "Pouros-Torphy", "Alix Fenkel", "afenkel9@imgur.com", "3405566703", 5, 18, "Morbi vestibulum, velit id pretium iaculis, diam erat fermentum justo, nec condimentum neque sapien placerat ante. Nulla justo. Aliquam quis turpis eget elit sodales scelerisque."),
            new Advertisement("acf06d8e-5d7a-416d-9fc4-b36087d033f3", "Emmerich, Kilback and Nolan", "Karon Woodstock", "kwoodstocka@xinhuanet.com", "1975965058", 4, 12, "Ut at dolor quis odio consequat varius. Integer ac leo. Pellentesque ultrices mattis odio. Donec vitae nisi."),
            new Advertisement("3122e024-e867-43b4-b83b-c6d3dd437907", "Hoppe, Carroll and Conroy", "Ana Chittem", "achittemb@tinypic.com", "4038622760", 4, 33, "Nullam orci pede, venenatis non, sodales sed, tincidunt eu, felis. Fusce posuere felis sed lacus. Morbi sem mauris, laoreet ut, rhoncus aliquet, pulvinar sed, nisl. Nunc rhoncus dui vel sem."),
            new Advertisement("d10e1ba3-7a3d-4e4f-9579-13a1d63c85a4", "Gusikowski, Mills and Hartmann", "Ase Sugge", "asuggec@nps.gov", "9933936941", 1, 30, "Vivamus metus arcu, adipiscing molestie, hendrerit at, vulputate vitae, nisl."),
            new Advertisement("a4a729c0-a765-4ff6-bc53-2b53b179b89e", "Padberg-Luettgen", "Ronni Corbally", "rcorballyd@163.com", "9215521395", 2, 5, "Vivamus vel nulla eget eros elementum pellentesque. Quisque porta volutpat erat. Quisque erat eros, viverra eget, congue eget, semper rutrum, nulla. Nunc purus. Phasellus in felis."),
            new Advertisement("c1034604-9f6b-49b2-b466-4a87f07c0ce2", "Aufderhar LLC", "Johannes Clemetts", "jclemettse@stanford.edu", "7249087100", 1, 27, "Aliquam quis turpis eget elit sodales scelerisque. Mauris sit amet eros. Suspendisse accumsan tortor quis turpis. Sed ante."),
            new Advertisement("f269ac34-9a14-4d2d-a948-0ea0ae7d1ec1", "Schaefer-Goyette", "Debbi Blennerhassett", "dblennerhassettf@java.com", "9648602724", 5, 25, "Quisque erat eros, viverra eget, congue eget, semper rutrum, nulla. Nunc purus. Phasellus in felis. Donec semper sapien a libero."),
            new Advertisement("fa57b34e-147e-4341-8144-b9999c3cc6c1", "Gleason-Willms", "Fiorenze Weathers", "fweathersg@independent.co.uk", "6337328022", 4, 26, "Vivamus vel nulla eget eros elementum pellentesque."),
            new Advertisement("2daa2588-841d-4f50-a147-dbb09a38f187", "Morar-Tillman", "Pamella Darell", "pdarellh@chron.com", "6575356894", 5, 18, "Praesent blandit lacinia erat. Vestibulum sed magna at nunc commodo placerat."),
            new Advertisement("eadbe873-5da3-462f-ae9b-8263947b6a60", "Feil and Sons", "Smitty Corroyer", "scorroyeri@oracle.com", "6121229654", 3, 30, "Morbi odio odio, elementum eu, interdum eu, tincidunt in, leo. Maecenas pulvinar lobortis est. Phasellus sit amet erat. Nulla tempus. Vivamus in felis eu sapien cursus vestibulum."),
            new Advertisement("fc7dddd3-c70a-4ac5-b8d7-a01538a6de10", "Schneider-Mayert", "Jessalyn Sharpling", "jsharplingj@chronoengine.com", "4349521371", 1, 28, "Nunc rhoncus dui vel sem. Sed sagittis.")
    };

    public static void main(String[] args) {
        Advertisement randomAd = chooseRandomAdvertisement();
        sendAdvertisement(randomAd);
    }

    private static Advertisement chooseRandomAdvertisement() {
        Random random = new Random();
        int index = random.nextInt(advertisements.length);
        return advertisements[index];
    }

    private static void sendAdvertisement(Advertisement advertisement) {
        String rabbitmqHost = "localhost";
        int rabbitmqPort = 5672;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setPort(rabbitmqPort);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            Gson gson = new Gson();
            String adDetails = gson.toJson(advertisement);
            channel.basicPublish("", QUEUE_NAME, null, adDetails.getBytes());
            System.out.println("Sent advertisement to Marketing Department");
            System.out.println(adDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Advertisement {
        private final String id;
        private final String company;
        private final String contact;
        private final String email;
        private final String phone;
        private final int size;
        private final int placement;
        private final String description;

        public Advertisement(String id, String company, String contact, String email, String phone, int size, int placement, String description) {
            this.id = id;
            this.company = company;
            this.contact = contact;
            this.email = email;
            this.phone = phone;
            this.size = size;
            this.placement = placement;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getClient() {
            return company;
        }

        public String getContact() {
            return contact;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public int getSize() {
            return size;
        }

        public int getPlacement() {
            return placement;
        }

        public String getDescription() {
            return description;
        }

        public int calculateCost() {
            return size * placement * 100;
        }

        @Override
        public String toString() {
            return "Advertisement{" +
                    "id='" + id + '\'' +
                    ", company='" + company + '\'' +
                    ", contact='" + contact + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", size=" + size +
                    ", placement=" + placement + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}

