package models;

import java.util.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;
import javax.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
public class BeerOfTheWeek extends Model {

  @Id
  public Long productId;

  @Required
  public Long epochDay; // should correspond to a friday

  @Required
  public String name;

  @Required
  public String category;

  @Required
  public String producerName;

  @Required
  public String thumbnail;

  public static Finder<Long,BeerOfTheWeek> find = new Finder(
    Long.class, BeerOfTheWeek.class
  );
  
  public static BeerOfTheWeek fromJsonNode(JsonNode node, Long epochDay) {
    BeerOfTheWeek myBeer = new BeerOfTheWeek();
    myBeer.productId = node.get("id").longValue();
    myBeer.name = node.get("name").textValue();
    myBeer.category = node.get("secondary_category").textValue();
    myBeer.producerName = node.get("producer_name").textValue();
    myBeer.thumbnail = node.get("image_thumb_url").textValue();
    if("".equals(myBeer.thumbnail) || myBeer.thumbnail == null) {
        myBeer.thumbnail = "https://upload.wikimedia.org/wikipedia/commons/4/47/Comic_image_missing.png";
    }
    myBeer.epochDay = epochDay;
    return myBeer;
  }

}