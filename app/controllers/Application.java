package controllers;

import java.time.LocalDate;
import java.util.List;
import javax.inject.Inject;
import play.*;
import play.mvc.*;
import models.BeerOfTheWeek;
import services.LcboBeerDiscoveryService;
import gateways.LcboApiGateway;

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
        BeerOfTheWeek beer = BeerOfTheWeek.fromJsonNode(
            // cheap hack; just use today's date
            // and don't save the BeerOfTheWeek object
            lcboApiGateway.getRandomBeer(), LocalDate.now().toEpochDay()
        );
        return ok(randomBeer.render(beer));
    }

}
