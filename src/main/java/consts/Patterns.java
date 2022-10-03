package consts;

import java.util.regex.Pattern;

public class Patterns {
    public static String sp = "[\\u0020]";

    public static String port = "(?<port>(\\d){1,5})";
    public static String id = "(?<id>([a-zA-Z]|\\d){5,10})";
    public static String protocol = "(?<protocol>([a-zA-Z]|\\d){3,15})";
    public static String username = "(?<username>([a-zA-Z]|\\d){3,50})";
    public static String password = "(?<password>([\\u0021-\\u00FF]){3,50})";
    public static String host = "(?<host>([a-zA-Z]|\\d|\\.|\\_|\\-){3,50})";
    public static String path = "(?<path>(/([a-zA-Z]|\\d|\\.|\\_|\\-|\\/){3,50}))";
    public static String url = "(?<url>(" + protocol + "://" + "(" + username + "(:" + password +")?" + "@)?" + host + "(:" + port + ")?" + path +"))";
    public static String min = "(?<min>(\\d){1,8})";
    public static String max = "(?<max>(\\d){1,8})";
    public static String frequency = "(?<frequency>(\\d){1,8})";
    public static String augmented_url = "(?<augmentedUrl>(" + id + "!" + url + "!" + min + "!" + max + "!" + frequency + "))";
    public static String state = "(?<state>(OK|ALARM|DOWN))";
    public static String message = "(?<message>([\\u0020-\\u00FF]){1,200})";

    public static Pattern IAMHERECMD = Pattern.compile("IAMHERE.*");
    public static Pattern NOTIFYCMD = Pattern.compile("NOTIFY.*");
    public static Pattern CURCONFIGCMD = Pattern.compile("CURCONFIG.*");
    public static Pattern STATEREQCMD = Pattern.compile("STATEREQ.*");
    public static Pattern STATERESPCMD = Pattern.compile("STATERESP.*");
    public static Pattern ADDSRVCMD = Pattern.compile("ADDSRV.*");
    public static Pattern OKCMD = Pattern.compile("[\\u002B]OK.*");
    public static Pattern ERRCMD = Pattern.compile("[\\u002D]ERR.*");
    public static Pattern LISTSRVCMD = Pattern.compile("LISTSRV.*");
    public static Pattern SRVCMD = Pattern.compile("SRV.*");
    public static Pattern STATESRVCMD = Pattern.compile("STATESRV.*");
    public static Pattern STATECMD = Pattern.compile("STATE.*");
}


