package actors;

import play.Logger;
import java.util.List;
import javax.inject.*;
import akka.actor.*;
import actors.ChooseBeerProtocol.*;
import models.BeerOfTheWeek;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import gateways.LcboApiGateway;

public class ChooseBeerActor extends UntypedActor {

    public static final int WEEKS_OF_DATA_TO_PRIME = 5; // number of weeks to go back in time
    public static Props props = Props.create(ChooseBeerActor.class);

    private static final int RETRY = 5;
    private static int perPage = 100;
    private static int totalRecordCount = 7447; // this is just the total record count on the day I wrote the code

    private LcboApiGateway lcboApiGateway = new LcboApiGateway();

    public void onReceive(Object msg) throws Exception {
        Logger.info("actors.ChooseBeerActor::OnReceive");
        if (msg instanceof QueryLcbo) { // Is this really necessary?
            for(int i=0; i<RETRY; i++) { // if the product we get is a duplicate we may need to retry.
                JsonNode node = lcboApiGateway.getRandomBeer();
                if(node != null) {
                    Long productId = node.get("id").longValue();
                    boolean isDuplicate = (BeerOfTheWeek.find.byId(node.get("id").longValue()) != null);
                    if(isDuplicate) {
                        Logger.info("Product ID " + productId + " is a duplicate");
                    }
                    if(!isDuplicate) {
                        LocalDate nextDate;
                        List<BeerOfTheWeek> previous = BeerOfTheWeek.find.orderBy("epochDay desc").findList();
                        if(previous != null && previous.size() > 0) {
                            nextDate = LocalDate.ofEpochDay(previous.get(0).epochDay).plusWeeks(1);
                        }
                        else {
                            LocalDate now = LocalDate.now();
                            // change it to a Friday
                            nextDate = now.minusDays((now.getDayOfWeek().getValue() + 2) % 7).minusWeeks(WEEKS_OF_DATA_TO_PRIME);
                        }
                        BeerOfTheWeek myBeer = BeerOfTheWeek.fromJsonNode(node, nextDate.toEpochDay());
                        
                        Logger.info("Creating beer: " + myBeer.name + " for date " + nextDate);
                        
                        myBeer.save();
                        sender().tell(myBeer, self());
                        return;
                    }
                }
            }
            Logger.info("Spamming the LCBO API" + RETRY + " times yielded no unique Beer");
            sender().tell(null, self());
        }
    }

}