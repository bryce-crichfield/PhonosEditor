package encoding.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import piano.state.note.model.NoteData;
import piano.state.note.model.NoteEntry;
import piano.state.note.model.NotePitch;
import piano.state.note.model.NoteRegistry;

import java.io.IOException;

public class NoteSerializer extends SimpleModule {
    private static final JsonSerializer<NotePitch> notePitchSerializer = new JsonSerializer<>() {
        @Override
        public void serialize(NotePitch notePitch, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("noteName", notePitch.getNoteName());
            jsonGenerator.writeNumberField("noteIndex", notePitch.getNoteIndex());
            jsonGenerator.writeEndObject();
        }
    };

    private static final JsonSerializer<NoteData> noteDataSerializer = new JsonSerializer<>() {
        @Override
        public void serialize(NoteData noteData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("notePitch", noteData.getPitch());
            jsonGenerator.writeNumberField("startStep", noteData.getStartStep());
            jsonGenerator.writeNumberField("endStep", noteData.getEndStep());
            jsonGenerator.writeNumberField("velocity", noteData.getVelocity());
            jsonGenerator.writeEndObject();
        }
    };

    private static final JsonSerializer<NoteRegistry> noteRegistrySerializer = new JsonSerializer<>() {
        @Override
        public void serialize(NoteRegistry noteRegistry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartArray();
            for (NoteEntry entry : noteRegistry.getEntries()) {
                NoteData data = entry.get();
                jsonGenerator.writeObject(data);
            }
            jsonGenerator.writeEndArray();
        }
    };

    public NoteSerializer() {
        addSerializer(NotePitch.class, notePitchSerializer);
        addSerializer(NoteData.class, noteDataSerializer);
        addSerializer(NoteRegistry.class, noteRegistrySerializer);
    }

    public ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new NoteSerializer());
        return mapper;
    }
}
