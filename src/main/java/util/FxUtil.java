package util;

import javafx.fxml.*;
import javafx.stage.*;

import java.util.*;

public class FxUtil {
    private static final FxUtil INSTANCE = new FxUtil();
    private final Map<String, FXMLLoader> loaders = new HashMap<>();

    public static FXMLLoader load(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));

        if (INSTANCE.loaders.containsKey(name)) {
            System.out.println("Returning cached loader for " + name);
            return INSTANCE.loaders.get(name);
        }

        FXMLLoader loader = new FXMLLoader(FxUtil.class.getResource(path));
        INSTANCE.loaders.put(name, loader);

        return loader;
    }

    public static Stage loadStage(String path) {
        FXMLLoader loader = load(path);
        Stage stage = new Stage();
        loader.setRoot(stage);
        return stage;
    }
}
