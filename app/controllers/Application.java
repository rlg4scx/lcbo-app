package controllers;

import java.time.LocalDate;
import java.util.List;
import javax.inject.Inject;
import play.*;
import play.mvc.*;
import models.BeerOfTheWeek;
import services.LcboBeerDiscoveryService;
import gateways.LcboApiGateway;
import com.fasterxml.jackson.databind.JsonNode;

import views.html.*;

public class Application extends Controller {

    @Inject LcboBeerDiscoveryService lcboBeerDiscoveryService;
    @Inject LcboApiGateway lcboApiGateway;

    public Result index() {
        Logger.info("controllers.Application::index()");
        LocalDate now = LocalDate.now();
        LocalDate lastFriday = now.minusDays((now.getDayOfWeek().getValue() + 2) % 7);
        return beerOfTheWeek(lastFriday.toEpochDay());
    }

    public Result beerOfTheWeek(long epochDay) {
        Logger.info("controllers.Application::beerOfTheWeek(long)");
        LocalDate date = LocalDate.ofEpochDay(epochDay);
        if(date.compareTo(LocalDate.now()) > 0) {
            return notFound("No peeking ahead!");
        }
        //TODO: this Friday rounding-off code appears in two places - consolidate/refactor.
        LocalDate lastFriday = date.minusDays((date.getDayOfWeek().getValue() + 2) % 7);
        List<BeerOfTheWeek> beers = BeerOfTheWeek.find.where().eq("epochDay", lastFriday.toEpochDay()).findList();
        if(beers.size() < 1) {
            return notFound("No beer was found for that week. Did you go too far back?");
        }

        LocalDate prevWeek = lastFriday.minusWeeks(1);
        LocalDate nextWeek = lastFriday.plusWeeks(1);

        return ok(beerOfTheWeek.render(beers.get(0), lastFriday, prevWeek, nextWeek));
    }

    public Result random() {
        Logger.info("controllers.Application::random()");
        JsonNode node = lcboApiGateway.getRandomBeer();
        return redirect("/beer/" + node.get("id"));
    }

    public Result viewBeer(Integer productId) {
        Logger.info("controllers.Application::viewBeer(Integer)");
        BeerOfTheWeek beer = BeerOfTheWeek.fromJsonNode(
            // cheap hack; just use today's date
            // and don't save the BeerOfTheWeek object
            lcboApiGateway.getById(productId), LocalDate.now().toEpochDay()
        );
        return ok(viewBeer.render(beer));
    }

}
