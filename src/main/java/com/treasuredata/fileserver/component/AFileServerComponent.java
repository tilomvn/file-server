package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageFileNotFoundException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

public abstract class AFileServerComponent implements IFileServerComponent {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final String NEW_LINE = System.getProperty("line.separator");
    private final String SPACE = " ";

    //Thread-safety connections storage
    Map<String, ConnectionResponse> connections = new Hashtable<>();

    final FileSystemStorage fileSystemStorage;

    public AFileServerComponent() {
        fileSystemStorage = new FileSystemStorage();
    }

    /**
     * Execute command.
     */
    @Override
    public String executeCommand(String clientId, String command) throws Exception {
        if (connections.get(clientId) == null) {
            return "Untrusted Request.";
        }
        if (FileServerComponent.COMMAND.index.name().equals(command)) {
            List<Path> listFile = fileSystemStorage.loadAll();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < listFile.size(); i++) {
                Path file = listFile.get(i);
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
                if (i < listFile.size() - 1) {
                    sb.append(NEW_LINE);
                }
            }
            return sb.toString();
        } else if (command.startsWith(FileServerComponent.COMMAND.get.value)) {
            String fileName = command.replace(FileServerComponent.COMMAND.get.value, "");
            Path file = fileSystemStorage.load(fileName);
            if (!Files.exists(file)) {
                throw new StorageFileNotFoundException("File " + fileName + " does not exist.");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(Files.lines(file).collect(Collectors.joining(NEW_LINE)));
            return sb.toString();
        } else {
            return FileServerComponent.COMMAND.unknown.value;
        }
    }

    /**
     * Close connection.
     */
    @Override
    public ConnectionResponse closeConnection(String clientId) {
        ConnectionResponse response = connections.remove(clientId);
        response.setMessage("Connection closed.");
        System.out.println("Client[" + clientId + "] connection closed.");
        return response;
    }


    @Override
    public ConnectionResponse doConnect(String clientId) {
        return null;
    }

    @Override
    public ConnectionResponse doConnect() {
        return null;
    }

    enum COMMAND {
        exit("exit"), index("index"), get("get "), unknown("unknown command");

        final String value;

        COMMAND(String value) {
            this.value = value;
        }
    }
}
