import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import piano.Editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class KeybindingsLoader {
    Scene scene;
    Editor editor;
    Map<String, KeyCombination> keybindings;

    public KeybindingsLoader(Scene scene, Editor editor) {
        this.scene = scene;
        this.editor = editor;
    }

    public void load() {
        keybindings = loadKeybindings();

        get("midiEditor.selectPencil", keyCombination -> {
            scene.getAccelerators().put(keyCombination, () -> editor.toggleToolPencil.fire());
        });

        get("midiEditor.selectSelect", keyCombination -> {
            scene.getAccelerators().put(keyCombination, () -> editor.toggleToolSelect.fire());
        });

        get("midiEditor.selectPlayhead", keyCombination -> {
            scene.getAccelerators().put(keyCombination, () -> editor.toggleToolPlayhead.fire());
        });

        get("midiEditor.selectSlice", keyCombination -> {
            scene.getAccelerators().put(keyCombination, () -> editor.toggleToolSlice.fire());
        });
    }

    private void get(String key, Consumer<KeyCombination> ifPresent) {
        if (keybindings.containsKey(key)) {
            ifPresent.accept(keybindings.get(key));
        }
    }

    private static Map<String, KeyCombination> loadKeybindings() {
        try {
            // Load JSON from resource file
            String jsonStr = loadJsonFromResource("keybindings.json");

            // Parse JSON into a Map<String, String>
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(jsonStr, Map.class);

            // Convert the values to KeyCombination objects
            Map<String, KeyCombination> keybinds = new HashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                KeyCombination keyCombination = KeyCombination.valueOf(value);
                keybinds.put(key, keyCombination);
            }

            return keybinds;

        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static String loadJsonFromResource(String resourceName) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(KeybindingsLoader.class.getResourceAsStream(resourceName)))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }
}
