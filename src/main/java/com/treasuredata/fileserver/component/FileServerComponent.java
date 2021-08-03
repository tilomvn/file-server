package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.component.exceptions.StorageException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FileServerComponent extends AFileServerComponent implements ApplicationRunner {

    public FileServerComponent() {
        super();
    }

    /**
     * Do init directory from commandline parameter.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (args.getNonOptionArgs().size() != 0) {
            fileSystemStorage.setRootLocation(args.getNonOptionArgs().get(0));
        } else {
            System.out.println("Missing directory parameter.");
            throw new StorageException("Missing directory parameter.");
        }
    }

    /**
     * Do connection.
     */
    @Override
    public ConnectionResponse doConnect() {
        String clientId = UUID.randomUUID().toString();
        ConnectionResponse response = ConnectionResponse.of(clientId, HttpStatus.ACCEPTED, "Wellcome " + clientId);
        connections.put(clientId, response);
        System.out.println("Client[" + clientId + "] connected.");
        return response;
    }
}
