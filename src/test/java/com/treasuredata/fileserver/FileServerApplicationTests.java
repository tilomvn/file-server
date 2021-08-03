package com.treasuredata.fileserver;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Robot;
import java.awt.event.KeyEvent;

class FileServerApplicationTests {

    //Test directory
    Path path = Paths.get("src", "test", "resources");

    //@Test
    void testRun() throws Exception {
        //Stop application after started
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_C);
                Thread.sleep(2000);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_Y);
                robot.keyPress(KeyEvent.VK_ENTER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        FileServerApplication.main(new String[] { path.toString() });
    }
}
