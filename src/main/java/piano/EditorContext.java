package piano;

import config.*;
import piano.state.note.*;
import piano.state.playback.*;

public class EditorContext {
    private final PlaybackService playback;
    private final NoteService notes;
    private final ViewSettings viewSettings;

    public EditorContext(PlaybackService playback, NoteService notes, ViewSettings settings) {
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
