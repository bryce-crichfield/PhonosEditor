package encoding.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import piano.state.note.model.NoteData;
import piano.state.note.model.NotePitch;

import java.io.IOException;

public class NoteDeserializer extends SimpleModule {
    private static final JsonDeserializer<NotePitch> notePitchJsonDeserializer = new JsonDeserializer<>() {
        @Override
        public NotePitch deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectCodec codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);
            String noteName = node.get("noteName").asText();
            int noteIndex = node.get("noteIndex").asInt();
            return new NotePitch(noteName, noteIndex);
        }
    };

    private static final JsonDeserializer<NoteData> noteDataJsonDeserializer = new JsonDeserializer<>() {
        @Override
        public NoteData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectCodec codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);
            NotePitch pitch = codec.treeToValue(node.get("notePitch"), NotePitch.class);
            int startStep = node.get("startStep").asInt();
            int endStep = node.get("endStep").asInt();
            int velocity = node.get("velocity").asInt();
            return new NoteData(pitch, startStep, endStep, velocity);
        }
    };

    private static final JsonDeserializer<NoteData[]> noteDataArrayJsonDeserializer = new JsonDeserializer<>() {
        @Override
        public NoteData[] deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectCodec codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);
            NoteData[] noteData = new NoteData[node.size()];
            for (int i = 0; i < node.size(); i++) {
                noteData[i] = codec.treeToValue(node.get(i), NoteData.class);
            }
            return noteData;
        }
    };

    public NoteDeserializer() {
        addDeserializer(NotePitch.class, notePitchJsonDeserializer);
        addDeserializer(NoteData.class, noteDataJsonDeserializer);
        addDeserializer(NoteData[].class, noteDataArrayJsonDeserializer);
    }

    public ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new NoteDeserializer());
        return mapper;
    }
}
