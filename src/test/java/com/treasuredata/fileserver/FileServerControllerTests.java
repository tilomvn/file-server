package com.treasuredata.fileserver;

import com.treasuredata.fileserver.component.FileServerComponent;
import com.treasuredata.fileserver.component.exceptions.StorageFileNotFoundException;
import com.treasuredata.fileserver.connection.response.ConnectionResponse;
import com.treasuredata.fileserver.order.TestOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import static org.mockito.Mockito.when;

@TestOrder(2)
@SpringBootTest
@AutoConfigureMockMvc
class FileServerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    FileServerComponent fileServerComponent;

    String clientId = UUID.randomUUID().toString();

    @Test
    void testClientConnection() throws Exception {
        ConnectionResponse connectionResponse = ConnectionResponse.of(clientId, HttpStatus.ACCEPTED, "Wellcome " + clientId);
        when(fileServerComponent.doConnect()).thenReturn(connectionResponse);

        MvcResult mvcResult = mockMvc.perform(get("/client/connect"))
                .andExpect(status().isAccepted()).andReturn();

        assertEquals(connectionResponse.getClientId(), mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testCloseClientConnection() throws Exception {
        ConnectionResponse connectionResponse = ConnectionResponse.of(clientId, HttpStatus.ACCEPTED, "Connection closed.");
        when(fileServerComponent.closeConnection(eq(clientId))).thenReturn(connectionResponse);

        MvcResult mvcResult = mockMvc.perform(get("/client/disconnect/" + clientId))
                .andExpect(status().isOk()).andReturn();

        assertEquals("Connection closed.", mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testExecuteCommandReturnListFile() throws Exception {
        String listFiles = "NghiaHo 2021-05-08 13:00 2021-05-08 14:22 readme.txt";
        when(fileServerComponent.executeCommand(eq(clientId), eq("index"))).thenReturn(listFiles);

        MvcResult mvcResult = mockMvc.perform(get("/client/" + clientId + "/command/index"))
                .andExpect(status().isOk()).andReturn();

        assertEquals(listFiles, mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testExecuteCommandReturnFileNotExists() throws Exception {
        String command = "get readme.txt";
        String response = "File readme.txt does not exist.";
        doThrow(new StorageFileNotFoundException(response)).when(fileServerComponent).executeCommand(eq(clientId), eq(command));

        MvcResult mvcResult = mockMvc.perform(get("/client/" + clientId + "/command/" + command))
                .andExpect(status().isNotFound()).andReturn();

        assertEquals(response, mvcResult.getResponse().getContentAsString());
    }
}
