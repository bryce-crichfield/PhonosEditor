package piano;

import config.ViewSettings;
import piano.state.note.NoteService;
import piano.state.playback.PlaybackService;

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
