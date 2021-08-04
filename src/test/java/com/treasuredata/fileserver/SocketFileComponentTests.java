package com.treasuredata.fileserver;

import com.treasuredata.fileserver.component.SocketFileServerComponent;
import com.treasuredata.fileserver.component.exceptions.StorageException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import com.treasuredata.fileserver.order.TestOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opentest4j.AssertionFailedError;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestOrder(3)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SocketFileComponentTests {

    //Test directory
    Path path = Paths.get("src", "test", "resources");

    SocketFileServerComponent socketFileServerComponent;

    ServerSocket serverSocket;

    String clientId;

    boolean clientDone1;
    boolean clientDone2;

    short testPort = 4002;

    @BeforeAll
    void init() throws Exception {
        serverSocket = new ServerSocket(4002);
        socketFileServerComponent = new SocketFileServerComponent(serverSocket);
    }

    @Test
    void testRunFailedByNonArgument() {
        assertThrows(StorageException.class,() -> socketFileServerComponent.run(new String[] {}));
    }

    @Test
    void testDoConnect() {
        clientId = UUID.randomUUID().toString();
        ConnectionResponse connection = socketFileServerComponent.doConnect(clientId);
        assertEquals(clientId, connection.getClientId());
    }

    @Test
    void testCloseConnect() {
        ConnectionResponse closedConnection = socketFileServerComponent.closeConnection(clientId);
        assertEquals(clientId, closedConnection.getClientId());
    }

    @Test
    void testClientProcessor() {
        clientDone1 = false;
        clientDone2 = false;
        //Using another thread to stop socket after run test function.
        new Thread(() -> {
            try {
                while (!clientDone1 || !clientDone2) {}
                serverSocket.close();
                System.out.println("Closed test server socket");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        //Will cause the SocketException because of the stopping above.

        //Using this thread to simulate client request
        //Client processor crash
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Socket client = new Socket("localhost", testPort);
                Thread.sleep(2000);
                client.close();
                clientDone1 = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        //Normal case
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Socket client = new Socket("localhost", testPort);
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                //Get List File ==================================================
                byte[] commands = "index".getBytes();
                out.writeInt(commands.length);
                out.write(commands);
                DataInputStream in = new DataInputStream(client.getInputStream());
                byte[] response = new byte[in.readInt()];
                in.readFully(response, 0, response.length);
                String responseStr = new String(response);
                try {
                    assertTrue(responseStr.contains("myFile.txt"));
                    assertTrue(responseStr.contains("textFile.txt"));
                } catch (AssertionFailedError e) {
                    e.printStackTrace();
                    //Stop testcase and cause test failed.
                    System.exit(1);
                }

                //Get File Content ====================================================
                commands = "get textFile.txt".getBytes();
                out.writeInt(commands.length);
                out.write(commands);

                response = new byte[in.readInt()];
                in.readFully(response, 0, response.length);
                responseStr = new String(response);

                String expectedContent = new StringBuilder().append("Hi I am developer !!")
                        .append(System.getProperty("line.separator"))
                        .append("This is how you see something...").toString();
                try {
                    assertEquals(expectedContent, responseStr);
                } catch (AssertionFailedError e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                //Stop client processor ======================================================
                commands = "exit".getBytes();
                out.writeInt(commands.length);
                out.write(commands);

                response = new byte[in.readInt()];
                in.readFully(response, 0, response.length);
                responseStr = new String(response);

                try {
                    assertEquals("DONE", responseStr);
                } catch (AssertionFailedError e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                in.close();
                out.close();
                client.close();

                clientDone2 = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        assertThrows(SocketException.class, () -> socketFileServerComponent.run(new String[] {path.toString()}));
    }
}
