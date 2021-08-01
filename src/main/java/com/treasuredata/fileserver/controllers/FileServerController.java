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
    public ResponseEntity<String> listenToClient() {
        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        return ResponseEntity.status(connectionResponse.getStatus()).body(connectionResponse.getClientId());
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
    @GetMapping("/client/{clientId}/command/{command}")
    public ResponseEntity<String> executeCommand(@PathVariable("clientId") String clientId,
                                                 @PathVariable("command") String command) throws Exception {
        return ResponseEntity.ok().body(fileServerComponent.executeCommand(clientId, command));
    }
}
