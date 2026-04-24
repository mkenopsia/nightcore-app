package ru.mkenopsia.nightcore;

import java.nio.file.Path;

public interface AudioProcessingService {

    ProcessingResponse processAudio(Path inputPath, Preset preset);
}
