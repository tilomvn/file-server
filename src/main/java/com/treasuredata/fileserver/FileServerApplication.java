package com.treasuredata.fileserver;

import com.treasuredata.fileserver.component.SocketFileServerComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class FileServerApplication {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting File Server Application...");
        Path path = Paths.get("src", "main", "resources", "application.properties");
        List<String> properties = Files.lines(path).collect(Collectors.toList());
        String runMode = properties.get(0);
        if (runMode.endsWith(RUNMODE.SOCKET.name())) {
            short port = Short.parseShort(properties.stream().filter(p -> p.startsWith("socket.server.port"))
                    .map(p -> p.split("=")[1]).findFirst().get());
            SocketFileServerComponent socketFileServerComponent = new SocketFileServerComponent(port);
            System.out.println("Started File Server Application");
            socketFileServerComponent.run(args);
        } else if (runMode.endsWith(RUNMODE.SERVICE.name())) {
            SpringApplication.run(FileServerApplication.class, args);
            System.out.println("Started File Server Application");
        }
	}

	enum RUNMODE {
		SERVICE, SOCKET
	}
}
