package probe;

import command.Command;
import exceptions.ServiceNotFoundException;
import security.AES;
import socket.SocketCommunicator;

import javax.crypto.SecretKey;
import java.net.Socket;
import java.util.regex.Matcher;

public class ClientConnection extends SocketCommunicator implements Runnable {
    private final ServicesListener mServicesListener;
    private final SecretKey mKey;

    public ClientConnection(Socket connection, ServicesListener servicesListener, SecretKey key) {
        super(connection);
        mServicesListener = servicesListener;
        mKey = key;
    }

    @Override
    public void run() {
        try {
            while (true)
            {
                String receivedMessage = receiveMessage();
                if(receivedMessage == null) break;
                Command command = new Command(AES.decrypt(receivedMessage, mKey));

                if(!command.isSyntaxicallyCorrect()) continue;

                switch (command.getType())
                {
                    case CURCONFIG:
                        curConfigHandler(command.getCommand());
                        break;
                    case STATEREQ:
                        stateReqHandler(command.getCommand());
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void curConfigHandler(String receivedMessage) {
        if(receivedMessage.length() < 11) return;
        for(String augmentedUrl : receivedMessage.substring(10).split(" ")) {
            Command command = new Command("CURCONFIG" + " " + augmentedUrl);
            Matcher matcher = command.getMatcher();
            mServicesListener.listenForNewService(matcher.group("id"), augmentedUrl, Integer.parseInt(matcher.group("min")), Integer.parseInt(matcher.group("max")), Integer.parseInt(matcher.group("frequency")));
        }
    }

    private void stateReqHandler(String receivedMessage) throws Exception {
        Command command = new Command(receivedMessage);
        try {
            StatusType status = mServicesListener.getStatus(command.getMatcher().group("id"));
            sendMessage(AES.encrypt("STATERESP" + " " + command.getMatcher().group("id") + " " + status.name(), mKey));
        } catch (ServiceNotFoundException e) {
            System.out.println("Service must be listened before queried!");
            sendMessage(AES.encrypt("STATERESP" + " " + command.getMatcher().group("id") + " " + StatusType.DOWN, mKey));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
