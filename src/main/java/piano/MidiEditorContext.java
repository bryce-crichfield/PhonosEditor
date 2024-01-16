package piano;

import piano.note.*;
import piano.playback.*;
import piano.view.settings.*;

public class MidiEditorContext {
    private final PlaybackService playback;
    private final NoteService notes;
    private final ViewSettings viewSettings;

    public MidiEditorContext(PlaybackService playback, NoteService notes, ViewSettings settings) {
        this.playback = playback;
        this.notes = notes;
        this.viewSettings = settings;
    }

    public PlaybackService getPlayback() {
        return playback;
    }

    public NoteService getNoteService() {
        return notes;
    }

    public ViewSettings getViewSettings() {
        return viewSettings;
    }
}
