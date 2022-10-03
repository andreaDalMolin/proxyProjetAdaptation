package probe;

import command.Command;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HTTPSStatusGetter implements StatusGetter {

    public StatusType getStatus(String augUrl, int minVal, int maxVal){
        Command command = new Command("CURCONFIG " + augUrl);
        String result  = "";
        try {
            result = requestFromUrl(command.getMatcher().group("url"), result);
            return checkBounds(Double.parseDouble(result), minVal, maxVal);
        } catch (IOException | ParseException e) {
            //Will return DOWN when connection time out
            return StatusType.DOWN;
        }
    }

    private StatusType checkBounds(Double currentVal, int minVal, int maxVal) {
        if (currentVal > maxVal || currentVal < minVal) return StatusType.ALARM;
        return StatusType.OK;
    }

    private String requestFromUrl(String urlInString, String result) throws IOException, ParseException {
        URL url = new URL(urlInString); //url
        URLConnection yc = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = br.readLine()) != null){
            result = inputLine;
        }
        br.close();
        return fetchValue(result);
    }

    private String fetchValue(String jsonText) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(jsonText);
        String value = "";
        for (Object key : obj.keySet()) {
            value = obj.get(key).toString();
        }
        return value;
    }
}
