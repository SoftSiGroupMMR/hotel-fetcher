package dk.mmr.hotelfetcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.mmr.hotelfetcher.entity.HotelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import get.dk.si.route.MetaData;
import get.dk.si.route.Root;
import get.dk.si.route.Route;
import get.dk.si.route.Util;

import java.io.IOException;

@Service
public class RoomClient {
    RestTemplate rt = new RestTemplate();
    private final Gson gson = new Gson();
    protected Logger logger = LoggerFactory.getLogger(RoomClient.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getHotelRooms(HotelRequest request) throws IOException {
        String url = "http://206.81.29.87:8069/hotel/rooms";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> json = new HttpEntity<>(gson.toJson(request), headers);
        String result = null;
        try {
            result = rt.postForObject(url, request, String.class);
        } catch (Exception e) {
            logger.error("An error occured while fetching hotel rooms: " + e.getLocalizedMessage());
        }
        return result;
    }

    public void handleMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

        String numberOfGuests = jsonMessage.get("metaData").getAsJsonObject().get("travelRequest").getAsJsonObject().get("numberOfGuests").getAsString();
        String city = jsonMessage.get("metaData").getAsJsonObject().get("travelRequest").getAsJsonObject().get("cityTo").getAsString();
        Long dateFrom = jsonMessage.get("metaData").getAsJsonObject().get("travelRequest").getAsJsonObject().get("dateFrom").getAsLong();

        HotelRequest hr = new HotelRequest(city, numberOfGuests, numberOfGuests, dateFrom);

        try {
            logger.info("Attempting to fetch hotel offers");
            String results = getHotelRooms(hr);
            JsonNode jsonresult = objectMapper.readTree(results);

            logger.info("got resulsts: " + results);
            Util util = new Util();
            Root root = util.rootFromJson(message);
            MetaData metaData = root.getMetaData();
            metaData.put("rooms", jsonresult);

            root.setMetaData(metaData);

            Route route = root.nextRoute();
            String json = util.rootToJson(root);
            System.out.println(json);
            util.sendToRoute(route, json);
            logger.info("Successfully added rooms to message");

        } catch (Exception e) {
            logger.error("An error occured while fetching rooms: " + e.getLocalizedMessage());
        }


    }


}
