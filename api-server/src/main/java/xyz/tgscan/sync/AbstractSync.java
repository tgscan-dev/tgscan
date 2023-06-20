package xyz.tgscan.sync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AbstractSync {

  protected Long getLastId(String from) {
    try {
      Path path = Paths.get(from + "_last_id.txt");
      if (Files.exists(path)) {
        String content = Files.readString(path);
        return Long.parseLong(content.trim());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0L;
  }

  protected void updateLastId(String from, Long lastId) {
    try {
      Path path = Paths.get(from + "_last_id.txt");
      Files.writeString(path, lastId.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
