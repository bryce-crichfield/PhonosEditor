package piano;

import piano.control.NoteService;
import piano.playback.PlaybackService;
import piano.view.settings.ViewSettings;

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

    public NoteService getNotes() {
        return notes;
    }

    public ViewSettings getViewSettings() {
        return viewSettings;
    }
}
