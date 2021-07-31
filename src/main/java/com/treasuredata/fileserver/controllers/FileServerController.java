package com.treasuredata.fileserver.controllers;

import com.treasuredata.fileserver.component.FileServerComponent;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class FileServerController {

    @Autowired
    private FileServerComponent fileServerComponent;

    /**
     * Client connection gateway.
     */
    @GetMapping("/client/connect")
    public ResponseEntity<String> listenToClient() throws Exception {
        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        String body = connectionResponse.getClientId();
        if (body == null) {
            body = connectionResponse.getMessage();
        }
        return ResponseEntity.status(connectionResponse.getStatus()).body(body);
    }

    /**
     * Close connection.
     */
    @GetMapping("/client/disconnect/{clientId}")
    public ResponseEntity<String> closeConnection(@PathVariable("clientId") String clientId) {
        ConnectionResponse connectionResponse = fileServerComponent.closeConnection(clientId);
        return ResponseEntity.ok().body(connectionResponse.getMessage());
    }

    /**
     * Execute client command.
     */
    @GetMapping("/client/command/{command}")
    public ResponseEntity<String> executeCommand(@PathVariable("command") String command) {
        return ResponseEntity.ok().body(fileServerComponent.executeCommand(command));
    }
}
