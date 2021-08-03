package com.treasuredata.fileserver.component;

import com.treasuredata.fileserver.connection.response.ConnectionResponse;

public interface IFileServerComponent {

    ConnectionResponse doConnect();

    ConnectionResponse doConnect(String clientId);

    String executeCommand(String clientId, String command) throws Exception;

    ConnectionResponse closeConnection(String clientId);
}
