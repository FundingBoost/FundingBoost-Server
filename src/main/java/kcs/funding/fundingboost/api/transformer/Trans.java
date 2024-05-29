package kcs.funding.fundingboost.api.transformer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Trans {

    public static String token(String rtn, JsonParser parser) {
        JsonElement element = parser.parse(rtn);
        return element.getAsJsonObject().get("access_token").getAsString();
    }
}
