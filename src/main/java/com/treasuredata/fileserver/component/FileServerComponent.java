package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageException;
import com.treasuredata.fileserver.component.exceptions.StorageFileNotFoundException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class FileServerComponent implements ApplicationRunner {

    private final String NEW_LINE = System.getProperty("line.separator");

    //Default 100 connections, can be changed by modifying in application.properties.
    @Value("${fileserver.connection.maximum:100}")
    private final int MAX_CONNECTION = 100;

    private FileSystemStorage fileSystemStorage = new FileSystemStorage();

    //Thread-safety connection storage
    private Map<String, ConnectionResponse> connections = new Hashtable<>();

    //Connection pool, allow maximum 100 connection at same time
    //otherwise, connection must wait for idle pool
    private ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONNECTION);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Do init directory.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (args.getNonOptionArgs().size() == 0) {
            System.out.println("Missing directory parameter.");
            System.out.println("Application failed on starting.");
            System.exit(0);
        }

        try {
            fileSystemStorage.setRootLocation(args.getNonOptionArgs().get(0));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }

    /**
     * Do connection.
     */
    public ConnectionResponse doConnect() throws ExecutionException, InterruptedException {
        Future<ConnectionResponse> future = executorService.submit(() -> {
            if (connections.size() == MAX_CONNECTION) {
                return ConnectionResponse.of(null, HttpStatus.INSUFFICIENT_STORAGE, "Connection is full. Please try again later.");
            }
            String clientId = UUID.randomUUID().toString();
            ConnectionResponse response = ConnectionResponse.of(clientId, HttpStatus.ACCEPTED, "Welcome " + clientId);
            connections.put(clientId, response);
            System.out.println("Client[" + clientId + "] connected.");
            return response;
        });
        return future.get();
    }

    /**
     * Execute command.
     */
    public String executeCommand(String command) {
        if (COMMAND.index.name().equals(command)) {
            return fileSystemStorage.loadAll().filter(f -> f.toFile().getName().endsWith(".txt"))
                    .map(f -> {
                        String fileName = f.toFile().getName();
                        BasicFileAttributeView basicFileAttributeView;
                        FileOwnerAttributeView fileOwnerAttributeView;
                        try {
                            basicFileAttributeView = Files.getFileAttributeView(f, BasicFileAttributeView.class);
                            fileOwnerAttributeView = Files.getFileAttributeView(f, FileOwnerAttributeView.class);
                            BasicFileAttributes basicFileAttributes = basicFileAttributeView.readAttributes();
                            return fileOwnerAttributeView.getOwner().getName() + " " +
                                    formatter.format(basicFileAttributes.creationTime().toInstant()) + " " +
                                    formatter.format(basicFileAttributes.lastModifiedTime().toInstant()) + " " +
                                    fileName;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return "Error while fetching file " + fileName + "'s info on server.";
                        }
                    })
                    .collect(Collectors.joining(NEW_LINE));
        } else if (command.startsWith(COMMAND.get.value)) {
            String fileName = command.replace(COMMAND.get.value, "");
            StringBuilder sb = new StringBuilder();
            try {
                Path file = fileSystemStorage.load(fileName);
                if (!Files.exists(file)) {
                    throw new StorageFileNotFoundException("File " + fileName + " does not exist.");
                }
                Files.lines(file).forEach(line -> sb.append(line + NEW_LINE));
            } catch (StorageFileNotFoundException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new StorageException("Error when reading file: " + fileName, ex);
            }
            return sb.toString();
        } else {
            return COMMAND.unknown.value;
        }
    }

    /**
     * Close connection.
     */
    public ConnectionResponse closeConnection(String clientId) {
        ConnectionResponse response = connections.remove(clientId);
        if (response == null) {
            return ConnectionResponse.of(clientId, HttpStatus.OK, "Connection closed.");
        }
        response.setMessage("Connection closed.");
        System.out.println("Client[" + clientId + "] connection closed.");
        return response;
    }

    enum COMMAND {
        index("index"), get("get "), unknown("unknown command");

        private final String value;

        COMMAND(String value) {
            this.value = value;
        }
    }
}
