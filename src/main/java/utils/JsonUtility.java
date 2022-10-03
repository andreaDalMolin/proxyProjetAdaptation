package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import task.Protocol;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonUtility {

    private static final String monitorConfigPath = "src/main/resources/monitorConfig.json";
    private static final JSONParser parser = new JSONParser();
    private static JSONObject monitorConfig;

    static {
        try {
            monitorConfig = (JSONObject) parser.parse(new FileReader(monitorConfigPath));
        } catch (IOException | ParseException e) {
            System.out.println("Unable to find config file !");
        }
    }

    public static String getMulticastAddress(){
        return monitorConfig.get("multicastAddress").toString();
    }

    public static int getMulticastPort(){
        return Integer.parseInt(monitorConfig.get("multicastPort").toString());
    }

    public static int getProtocolUnicastPort(Protocol protocol){
        if (protocol.equals(Protocol.HTTPS)) return Integer.parseInt((String) monitorConfig.get("httpsUnicastPort"));
        else return Integer.parseInt((String) monitorConfig.get("snmpUnicastPort"));
    }

    public static SecretKey getAesKey(){
        String decodedKey = monitorConfig.get("AESKey").toString();
        return new SecretKeySpec(decodedKey.getBytes(StandardCharsets.UTF_8), 0, decodedKey.length(), "AES");
    }

    public static int getClientPort() { return Integer.parseInt(monitorConfig.get("clientPort").toString()); }
}
