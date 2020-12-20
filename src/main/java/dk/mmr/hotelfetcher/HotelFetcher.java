package dk.mmr.hotelfetcher;

import dk.mmr.hotelfetcher.service.RoomClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
public class HotelFetcher {

    protected static Logger logger = LoggerFactory.getLogger(HotelFetcher.class.getName());

    private final static String QUEUE_NAME = "hotel";

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HotelFetcher.class, args);
        connectQueue();
    }

    public static void connectQueue() throws Exception {
        RoomClient roomClient = new RoomClient();
        // Same as the producer: tries to create a queue, if it wasn't already created
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("188.166.16.16");
        factory.setUsername("mmmrj1");
        factory.setPassword("mmmrj1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Register for a queue
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // Get notified, if a message for this receiver arrives
        DeliverCallback deliverCallback = (consumerTag, delivery) ->
        {
            String message = new String(delivery.getBody(), "UTF-8");
            logger.info("Received message in hotel module");
            System.out.println(" [x] Received '" + message + "'");
            roomClient.handleMessage(message);
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }
}
