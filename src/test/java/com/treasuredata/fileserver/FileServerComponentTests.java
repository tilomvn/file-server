package com.treasuredata.fileserver;

import com.treasuredata.fileserver.component.FileServerComponent;
import com.treasuredata.fileserver.component.exceptions.StorageException;
import com.treasuredata.fileserver.component.exceptions.StorageFileNotFoundException;
import com.treasuredata.fileserver.component.exceptions.StorageNotExistException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import com.treasuredata.fileserver.order.TestOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestOrder(1)
class FileServerComponentTests {

    Path testDirectory = Paths.get("src", "test", "resources");

    FileServerComponent fileServerComponent = new FileServerComponent();

    @Test
    void testDoInitDirectorySuccess() {
        fileServerComponent.run(new DefaultApplicationArguments(testDirectory.toString()));
    }

    @Test
    void testDoInitDirectoryWithoutCommandLineParameter() {
        assertThrows(StorageException.class, () -> fileServerComponent.run(new DefaultApplicationArguments()));
    }

    @Test
    void testDoInitDirectoryReturnDirectoryNotExist() {
        assertThrows(StorageNotExistException.class, () -> fileServerComponent.run(new DefaultApplicationArguments(testDirectory.toString() + "/test")));
    }

    @Test
    void testExecuteCommandReturnListFile() throws Exception {
        String responseFile1 = "myFile.txt";
        String responseFile2 = "textFile.txt";

        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        String clientId = connectionResponse.getClientId();
        fileServerComponent.run(new DefaultApplicationArguments(testDirectory.toString()));

        String response = fileServerComponent.executeCommand(clientId, "index");

        assertTrue(response.indexOf(responseFile1) > 0);
        assertTrue(response.indexOf(responseFile2) > 0);
    }

    @Test
    void testExecuteCommandReturnFileContent() throws Exception {
        String testFile = "textFile.txt";
        String NEW_LINE = System.getProperty("line.separator");

        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        String clientId = connectionResponse.getClientId();
        fileServerComponent.run(new DefaultApplicationArguments(testDirectory.toString()));

        String response = fileServerComponent.executeCommand(clientId, "get " + testFile);

        Path file = Paths.get("src", "test", "resources", testFile);
        String expectedFileContent = Files.lines(file).collect(Collectors.joining(NEW_LINE));
        assertEquals(expectedFileContent, response);
    }

    @Test
    void testExecuteCommandReturnFileDoesNotExist() {
        String testFile = "textFileAAA.txt";

        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        String clientId = connectionResponse.getClientId();
        fileServerComponent.run(new DefaultApplicationArguments(testDirectory.toString()));

        assertThrows(StorageFileNotFoundException.class, () -> fileServerComponent.executeCommand(clientId, "get " + testFile));
    }

    @Test
    void testDoConnect() {
        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        assertNotNull(connectionResponse.getClientId());
        assertEquals(HttpStatus.ACCEPTED.value(), connectionResponse.getStatus());
    }

    @Test
    void testCloseConnect() {
        ConnectionResponse connectionResponse = fileServerComponent.doConnect();
        String clientId = connectionResponse.getClientId();

        connectionResponse = fileServerComponent.closeConnection(clientId);
        assertEquals(clientId, connectionResponse.getClientId());
        assertEquals(HttpStatus.ACCEPTED.value(), connectionResponse.getStatus());
    }

    @Test
    void testExecuteCommandWithoutConnection() throws Exception {
        String response = fileServerComponent.executeCommand("", "index");
        assertEquals("Untrusted Request.", response);
    }
}
