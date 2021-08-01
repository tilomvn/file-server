package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageFileNotFoundException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class FileServerComponent implements ApplicationRunner {

    private final String NEW_LINE = System.getProperty("line.separator");
    private final String SPACE = " ";

    private final FileSystemStorage fileSystemStorage;

    //Thread-safety connection storage
    private Map<String, ConnectionResponse> connections = new Hashtable<>();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public FileServerComponent() {
        fileSystemStorage = new FileSystemStorage();
    }

    /**
     * Do init directory.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (args.getNonOptionArgs().size() == 0) {
            return;
        }
        fileSystemStorage.setRootLocation(args.getNonOptionArgs().get(0));
    }

    /**
     * Do connection.
     */
    public ConnectionResponse doConnect() {
        String clientId = UUID.randomUUID().toString();
        ConnectionResponse response = ConnectionResponse.of(clientId, HttpStatus.ACCEPTED, "Wellcome " + clientId);
        connections.put(clientId, response);
        System.out.println("Client[" + clientId + "] connected.");
        return response;
    }

    /**
     * Execute command.
     */
    public String executeCommand(String clientId, String command) throws IOException {
        if (connections.get(clientId) == null) {
            return "Untrusted Request.";
        }
        if (COMMAND.index.name().equals(command)) {
            List<Path> listFile = fileSystemStorage.loadAll();
            StringBuilder sb = new StringBuilder();
            for (Path file : listFile) {
                String fileName = file.toFile().getName();
                BasicFileAttributeView basicFileAttributeView = Files.getFileAttributeView(file, BasicFileAttributeView.class);
                FileOwnerAttributeView fileOwnerAttributeView = Files.getFileAttributeView(file, FileOwnerAttributeView.class);
                BasicFileAttributes basicFileAttributes = basicFileAttributeView.readAttributes();
                sb.append(fileOwnerAttributeView.getOwner().getName());
                sb.append(SPACE);
                sb.append(formatter.format(basicFileAttributes.creationTime().toInstant()));
                sb.append(SPACE);
                sb.append(formatter.format(basicFileAttributes.lastModifiedTime().toInstant()));
                sb.append(SPACE);
                sb.append(fileName);
                sb.append(NEW_LINE);
            }
            return sb.toString();
        } else if (command.startsWith(COMMAND.get.value)) {
            String fileName = command.replace(COMMAND.get.value, "");
            Path file = fileSystemStorage.load(fileName);
            if (!Files.exists(file)) {
                throw new StorageFileNotFoundException("File " + fileName + " does not exist.");
            }
            StringBuilder sb = new StringBuilder();
            Files.lines(file).forEach(line -> sb.append(line + NEW_LINE));
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
