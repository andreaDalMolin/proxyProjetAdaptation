import command.Command;
import socket.SocketCommunicator;
import task.Protocol;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientConnection extends SocketCommunicator implements Runnable {
    private final Monitor mMonitor;

    public ClientConnection(Socket socket, Monitor monitor) {
        super(socket);
        mMonitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (true)
            {
                String receivedMessage = receiveMessage();
                if(receivedMessage == null) break;
                Command command = new Command(receivedMessage);

                if(!command.isSyntaxicallyCorrect()) continue;

                switch (command.getType())
                {
                    case ADDSRV:
                        addSrvHandler(command);
                        break;
                    case LISTSRV:
                        listSrvHandler();
                        break;
                    case STATESRV:
                        stateSrvHandler(command);
                        break;
                }
            }
        } catch (SocketException e) {
            System.out.println("Client disconnected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stateSrvHandler(Command command) throws IOException {
        String id = command.getMatcher().group("id");
        if(mMonitor.isServiceSupervised(id)) {
            String message = "STATE" + " " + id + " " + mMonitor.getUrl(id) + " " + mMonitor.getStatus(id);
            sendMessage(message);
        }
    }

    private void listSrvHandler() throws IOException {
        String message = "SRV";
        for(String id : mMonitor.getServicesIds())
            message = message.concat(" ").concat(id);
        sendMessage(message);
    }

    private void addSrvHandler(Command command) throws IOException {
        try {
            if(!mMonitor.isProtocolAccepted(Protocol.valueOf(command.getMatcher().group("protocol").toUpperCase()))) sendMessage("-ERR This protocol is not accepted");
            else if(mMonitor.isServiceSupervised(command.getMatcher().group("id"))) sendMessage("-ERR Service is already supervised");
            else {
                mMonitor.addNewService(command.getMatcher().group("augmentedUrl"));
                sendMessage("+OK Service added");
            }
        } catch (IllegalArgumentException | IOException e) {
            sendMessage("-ERR Protocol unknown");
        }
    }
}
