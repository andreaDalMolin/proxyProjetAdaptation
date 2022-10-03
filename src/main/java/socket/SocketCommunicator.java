package socket;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static consts.Const.DEBUG_MODE;

public class SocketCommunicator implements Sockets{
    public Socket socket;

    public SocketCommunicator() {
        socket = null;
    }

    public SocketCommunicator(Socket socket) {
        this.socket = socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     *
     * @param message the message to send to this socket. If message is null, nothing will be sent
     * @throws IOException an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    public void sendMessage(String message) throws IOException {
        if(message != null) {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            writer.print(message + "\r\n");
            writer.flush();
            if(DEBUG_MODE) System.out.println("Sending to " + socket.getInetAddress() + ": " + message);
        }
    }

    /**
     *
     * @return String, message received by client
     */
    public String receiveMessage() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String receivedMessage = readLine(reader);
        if(DEBUG_MODE) System.out.println("Received from " + socket.getInetAddress() + ": " + receivedMessage);
        return receivedMessage;
    }

    public String readLine(BufferedReader b) throws IOException {
        String line = b.readLine();
        if(line!=null && line.length()>2 && line.startsWith("\uFEFF"))
            return line.substring("\uFEFF".length());
        return line;
    }
}

