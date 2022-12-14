package com.luanvv.testcontainers.selenium;

import org.junit.runner.Description;
import org.testcontainers.containers.VncRecordingContainer;

import java.io.File;

public interface RecordingFileFactory {
    @Deprecated
    default File recordingFileForTest(File vncRecordingDirectory, Description description, boolean succeeded) {
        return recordingFileForTest(
                vncRecordingDirectory,
                description.getTestClass().getSimpleName() + "-" + description.getMethodName(),
                succeeded
        );
    }

    default File recordingFileForTest(
            File vncRecordingDirectory,
            String prefix,
            boolean succeeded,
            VncRecordingContainer.VncRecordingFormat recordingFormat
    ) {
        return recordingFileForTest(vncRecordingDirectory, prefix, succeeded);
    }

    File recordingFileForTest(File vncRecordingDirectory, String prefix, boolean succeeded);
}
