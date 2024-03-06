package util;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileUtil {
    public static void write(String absolutePath, String value) {
        Path path = Paths.get(absolutePath);
        File file = path.toFile();
        System.out.println(file.getAbsolutePath());
        try (var writer = new FileWriter(file)) {
            writer.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<String> read(String absolutePath) {
        try {
            Path path = Paths.get(absolutePath);
            File file = path.toFile();
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            return Optional.of(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
