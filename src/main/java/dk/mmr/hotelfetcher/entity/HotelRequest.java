package dk.mmr.hotelfetcher.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelRequest {
    String city, numberOfRooms, numberOfGuests;
    Long dateFrom;
}
