package services;

import javax.inject.*;
import play.*;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import play.libs.F.Promise;
import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.*;
import actors.ChooseBeerActor;
import actors.ChooseBeerProtocol.*;
import scala.concurrent.Future;
import scala.concurrent.Await;
import akka.util.Timeout;

import static akka.pattern.Patterns.ask;

/*
 * Glorified chron job - uses Akka and the actors.* to ping
 * Lcbo for Beers. This is done asynchronously to avoid customer
 * visible latency.
 */
@Singleton
public class LcboBeerDiscoveryService {

  @Inject LcboBeerDiscoveryService(ActorSystem actorSystem) {
    Logger.info("LcboBeerDiscoveryService created");

    ActorRef chooseBeerActor = actorSystem.actorOf(ChooseBeerActor.props);

    // since I doubt anyone will leave this running for a literal week
    // let's prime the database by requesting a few weeks up front.
    for(int i=0; i<ChooseBeerActor.WEEKS_OF_DATA_TO_PRIME+1; i++) {
        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        Future<Object> future = ask(chooseBeerActor, new QueryLcbo(), timeout);
        try {
            Await.result(future, timeout.duration());
        } catch(Exception e) {
            //TODO: logging
            throw new RuntimeException(e);
        }
    }

    actorSystem.scheduler().schedule(
        Duration.create(0, TimeUnit.MILLISECONDS), // Initial delay 0 milliseconds
        Duration.create(7, TimeUnit.DAYS),         // Frequency 7 days
        chooseBeerActor,
        new QueryLcbo(),
        actorSystem.dispatcher(),
        null
    );
  }
    
}