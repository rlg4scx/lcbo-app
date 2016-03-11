package gateways;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URLEncoder;
import java.util.Random;
import javax.inject.*;
import play.Logger;
import play.libs.F.Promise;
import play.libs.ws.WS;

/*
 * Gateway object responsible for calling the LCBO service.
 * Only exposes one method: `getRandomBeer()`, which requests a random page from the LCBO API
 * and checks that it's a Beer product before returining the JSON node for that product.
 */
public class LcboApiGateway {

    private static int perPage = 100;
    private static int totalRecordCount = 7447; // this is just the total record count on the day I wrote the code
    private static Random rand = new Random();

    public JsonNode getRandomBeer() {
        int  n = rand.nextInt(totalRecordCount);
        int page = n/perPage + 1;
        Promise<JsonNode> jsonPromise = this.getProducts(page, perPage);
        JsonNode response = jsonPromise.get(10000L);
        // update the total record count
        totalRecordCount = response.get("pager").get("total_record_count").intValue();
        JsonNode result = response.get("result");
        int offset = n%perPage;
        Logger.info("Response from LCBO size: " + result.size());
        for(int i=0; i<result.size(); i++) { // TODO: exponential backoff
            int index = (offset + i)%result.size();
            JsonNode node = result.get(index);
            Long productId = node.get("id").longValue();
            boolean isBeer = "Beer".equals(node.get("primary_category").textValue());
            if(isBeer) {
                return node;
            }
        }
        Logger.info("Unable to find a Beer product on page " + page + " - retrying");
        return null;
    }

    private Promise<JsonNode> getProducts(int page, int perPage) {
        String pageParam;
        String perPageParam;
        try {
            pageParam = URLEncoder.encode(Integer.toString(page), "UTF-8");
            perPageParam = URLEncoder.encode(Integer.toString(perPage), "UTF-8");
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        String url = String.format(
            "http://lcboapi.com/products?store_id=511&page=%s&per_page=%s", pageParam, perPageParam
        );
        Logger.info("Invoking the LCBO API: " + url);
        Promise<JsonNode> jsonPromise = WS.url(url).get().map(response -> {
            return response.asJson();
        });
        return jsonPromise;
    }

}