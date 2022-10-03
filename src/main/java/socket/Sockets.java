package socket;

import java.io.BufferedReader;
import java.io.IOException;

public interface Sockets {

    void sendMessage(String message) throws IOException;

    String receiveMessage() throws IOException;

    String readLine(BufferedReader b) throws IOException;
}
