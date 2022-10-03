package threads;

import command.Command;
import probe.StatusType;
import security.AES;
import socket.SocketCommunicator;
import task.Protocol;
import task.Task;

import javax.crypto.SecretKey;
import java.net.Socket;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable {
    private final BlockingQueue<Task> mTasksQueue;
    private final SecretKey mKey;
    //id, status
    private final Map<String, StatusType> mServices;
    //id, augmented url
    private final Map<String, String> mServicesToSupervise;
    private final Set<Protocol> mProtocolsSupervised;

    public Worker(SecretKey key, BlockingQueue<Task> tasksQueue, Map<String, String> servicesToSupervise, Map<String, StatusType> services, Set<Protocol> protocolsSupervised) {
        mKey = key;
        mServicesToSupervise = servicesToSupervise;
        mServices = services;
        mProtocolsSupervised = protocolsSupervised;
        mTasksQueue = tasksQueue;
    }

    @Override
    public void run() {
        while(true)
        {
            try {
                Task task = mTasksQueue.take();
                try(Socket socket = new Socket(task.getAddress(), task.getPort())) {
                    SocketCommunicator socketCommunicator = new SocketCommunicator(socket);
                    switch (task.getType()) {
                        case CURCONFIG:
                            curConfigHandler(socketCommunicator, task.getProtocol());
                            break;
                        case STATEREQ:
                            stateReqHandler(socketCommunicator, task.getProtocol());
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void curConfigHandler(SocketCommunicator socket, Protocol protocol) throws Exception {
        String paquet = "CURCONFIG";
        if(mProtocolsSupervised.contains(protocol)) {
            for (String augmentedUrl : mServicesToSupervise.values()) {
                Command command = new Command("CURCONFIG" + " " + augmentedUrl);
                if(Protocol.valueOf(command.getMatcher().group("protocol").toUpperCase()) == protocol) paquet = paquet.concat(" ").concat(augmentedUrl);
            }
            socket.sendMessage(AES.encrypt(paquet, mKey));
        }
    }

    private void stateReqHandler(SocketCommunicator socket, Protocol protocol) throws Exception {
        if(!mProtocolsSupervised.contains(protocol)) return;

        for (String id : mServicesToSupervise.keySet()) {
            Command command = new Command("CURCONFIG" + " " + mServicesToSupervise.get(id));
            if (Protocol.valueOf(command.getMatcher().group("protocol").toUpperCase()) == protocol) {
                socket.sendMessage(AES.encrypt("STATEREQ" + " " + id, mKey));
                command = new Command(AES.decrypt(socket.receiveMessage(), mKey));
                if (command.isSyntaxicallyCorrect())
                    mServices.put(command.getMatcher().group("id"), StatusType.valueOf(command.getMatcher().group("state")));
            }
        }
    }
}
