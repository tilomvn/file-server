package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class SocketFileServerComponent extends AFileServerComponent implements CommandLineRunner {

    final ServerSocket serverSocket;

    public SocketFileServerComponent(short port) throws Exception {
        super();
        this.serverSocket = new ServerSocket(port);
    }

    public SocketFileServerComponent(ServerSocket serverSocket) {
        super();
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(String[] args) throws Exception {
        if (args.length != 0) {
            fileSystemStorage.setRootLocation(args[0]);
        } else {
            System.out.println("Missing directory parameter.");
            throw new StorageException("Missing directory parameter.");
        }

        while (true) {
            Socket client = serverSocket.accept();//establishes connection
            new ClientSocketProcessor(client, UUID.randomUUID().toString()).start();
        }
    }

    @Override
    public ConnectionResponse doConnect(String clientId) {
        System.out.println("Client[" + clientId + "] connected.");
        ConnectionResponse connection = ConnectionResponse.of(clientId, HttpStatus.ACCEPTED, "Wellcome " + clientId);
        connections.put(clientId, connection);
        return connection;
    }

    class ClientSocketProcessor extends Thread {

        String clientId;
        Socket clientSocket;
        DataOutputStream out;
        DataInputStream in;

        public ClientSocketProcessor(Socket clientSocket, String clientId) {
            this.clientSocket = clientSocket;
            this.clientId = clientId;
        }

        void writeContent(byte[] content) throws Exception {
            out.writeInt(content.length);
            out.write(content);
        }

        @Override
        public void run() {
            try {
                doConnect(clientId);
 
                out = new DataOutputStream(clientSocket.getOutputStream());
                in = new DataInputStream(clientSocket.getInputStream());

                String command;
                byte[] inputContent;
                int inputLength;
                while ((inputLength = in.readInt()) > 0) {
                    inputContent = new byte[inputLength];
                    in.readFully(inputContent, 0, inputLength);
                    command = new String(inputContent, StandardCharsets.UTF_8);
                    if (COMMAND.exit.value.equals(command)) {
                        closeConnection(clientId);
                        writeContent("DONE".getBytes());
                        break;
                    } else {
                        if (COMMAND.index.value.equals(command) || command.startsWith(COMMAND.get.value)) {
                            try {
                                writeContent(executeCommand(clientId, command).getBytes());
                            } catch (StorageException e) {
                                writeContent(e.getMessage().getBytes());
                            } catch (Exception ex) {
                                throw ex;
                            }
                        } else {
                            writeContent(COMMAND.unknown.value.getBytes());
                        }
                    }
                }

                in.close();
                out.close();
                clientSocket.close();

                System.out.println("Processor of client[" + clientId + "] stopped.");
            } catch (Exception e) {
                System.out.println("Processor of client[" + clientId + "] crashed.");
                closeConnection(clientId);
                e.printStackTrace();
            }
        }
    }
}
