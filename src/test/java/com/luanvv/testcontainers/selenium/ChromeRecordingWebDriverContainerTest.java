package com.luanvv.testcontainers.selenium;

import com.google.common.io.PatternFilenameFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.luanvv.testcontainers.selenium.HelloWorldContainerExtension.NETWORK;
import static org.assertj.core.api.Assertions.assertThat;

public class ChromeRecordingWebDriverContainerTest extends BaseWebDriverContainer {

    /**
     * Guaranty a minimum video length for FFmpeg re-encoding.
     */
    private static final int MINIMUM_VIDEO_DURATION_MILLISECONDS = 200;

    @Nested
    class ChromeThatRecordsAllTests {

        @TempDir
        public Path vncRecordingDirectory;

        @Test
        public void recordingTestThatShouldBeRecordedAndRetainedInFlvFormatAsDefault() throws InterruptedException {
            File target = vncRecordingDirectory.toFile();
            System.out.println("Record folder: " + target.getAbsolutePath());
            try (
                    // recordAll {
                    // To do this, simply add extra parameters to the rule constructor, so video will default to FLV format:
                    BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
                            .withCapabilities(new ChromeOptions())
                            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, target)
                            // }
                            .withRecordingFileFactory(new DefaultRecordingFileFactory())
                            .withNetwork(NETWORK)
            ) {
                File[] files = runSimpleExploreInContainer(chrome, "PASSED-.*\\.flv");
                assertThat(files).as("Recorded file found").hasSize(1);
            }
        }

        private File[] runSimpleExploreInContainer(BrowserWebDriverContainer<?> container, String fileNamePattern)
                throws InterruptedException {
            container.start();

            TimeUnit.MILLISECONDS.sleep(MINIMUM_VIDEO_DURATION_MILLISECONDS);
            doSimpleExplore(container);
            container.afterTest(
                    new TestDescription() {
                        @Override
                        public String getTestId() {
                            return getFilesystemFriendlyName();
                        }

                        @Override
                        public String getFilesystemFriendlyName() {
                            return "ChromeThatRecordsAllTests-recordingTestThatShouldBeRecordedAndRetained";
                        }
                    },
                    Optional.empty()
            );

            return vncRecordingDirectory.toFile().listFiles(new PatternFilenameFilter(fileNamePattern));
        }

        @Test
        public void recordingTestShouldHaveFlvExtension() throws InterruptedException {
            File target = vncRecordingDirectory.toFile();
            System.out.println("Record folder: " + target.getAbsolutePath());
            try (
                    // recordFlv {
                    // Set (explicitly) FLV format for recorded video:
                    BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
                            .withCapabilities(new ChromeOptions())
                            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, target, VncRecordingFormat.FLV)
                            // }
                            .withRecordingFileFactory(new DefaultRecordingFileFactory())
                            .withNetwork(NETWORK)
            ) {
                File[] files = runSimpleExploreInContainer(chrome, "PASSED-.*\\.flv");
                assertThat(files).as("Recorded file found").hasSize(1);
            }
        }

        @Test
        public void recordingTestShouldHaveMp4Extension() throws InterruptedException {
            File target = vncRecordingDirectory.toFile();
            System.out.println("Record folder: " + target.getAbsolutePath());
            try (
                    // recordMp4 {
                    // Set MP4 format for recorded video:
                    BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
                            .withCapabilities(new ChromeOptions())
                            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, target, VncRecordingFormat.MP4)
                            // }
                            .withRecordingFileFactory(new DefaultRecordingFileFactory())
                            .withNetwork(NETWORK)
            ) {
                File[] files = runSimpleExploreInContainer(chrome, "PASSED-.*\\.mp4");
                assertThat(files).as("Recorded file found").hasSize(1);
            }
        }

        @Test
        public void recordingTestThatShouldHaveCorrectDuration() throws IOException, InterruptedException {
            MountableFile mountableFile;
            try (
                    BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
                            .withCapabilities(new ChromeOptions())
                            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, vncRecordingDirectory.toFile())
                            .withRecordingFileFactory(new DefaultRecordingFileFactory())
                            .withNetwork(NETWORK)
            ) {
                File[] recordedFiles = runSimpleExploreInContainer(chrome, "PASSED-.*\\.flv");
                mountableFile = MountableFile.forHostPath(recordedFiles[0].getCanonicalPath());
            }

            try (
                    GenericContainer<?> container = new GenericContainer<>(
                            DockerImageName.parse("testcontainers/vnc-recorder:1.2.0")
                    )
            ) {
                String recordFileContainerPath = "/tmp/chromeTestRecord.flv";
                container
                        .withCopyFileToContainer(mountableFile, recordFileContainerPath)
                        .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withEntrypoint("ffmpeg"))
                        .withCommand("-i", recordFileContainerPath, "-f", "null", "-")
                        .waitingFor(
                                new LogMessageWaitStrategy()
                                        .withRegEx(".*Duration.*")
                                        .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
                        )
                        .start();
                String ffmpegOutput = container.getLogs();

                assertThat(ffmpegOutput)
                        .as("Duration starts with 00:")
                        .contains("Duration: 00:")
                        .doesNotContain("Duration: 00:00:00.00");
            }
        }
    }

    @Nested
    class ChromeThatRecordsFailingTests {

        @TempDir
        public Path vncRecordingDirectory;

        @Test
        public void recordingTestThatShouldBeRecordedButNotPersisted() {
            try (
                    // withRecordingFileFactory {
                    BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
                            // }
                            .withCapabilities(new ChromeOptions())
                            // withRecordingFileFactory {
                            .withRecordingFileFactory(new CustomRecordingFileFactory())
                            // }
                            .withNetwork(NETWORK)
            ) {
                chrome.start();

                doSimpleExplore(chrome);
            }
        }

        @Test
        public void recordingTestThatShouldBeRecordedAndRetained() throws InterruptedException {
            File target = vncRecordingDirectory.toFile();
            System.out.println("Record folder: " + target.getAbsolutePath());
            try (
                    // recordFailing {
                    // or if you only want videos for test failures:
                    BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
                            .withCapabilities(new ChromeOptions())
                            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING, target)
                            // }
                            .withRecordingFileFactory(new DefaultRecordingFileFactory())
                            .withNetwork(NETWORK)
            ) {
                chrome.start();

                TimeUnit.MILLISECONDS.sleep(MINIMUM_VIDEO_DURATION_MILLISECONDS);
                doSimpleExplore(chrome);
                chrome.afterTest(
                        new TestDescription() {
                            @Override
                            public String getTestId() {
                                return getFilesystemFriendlyName();
                            }

                            @Override
                            public String getFilesystemFriendlyName() {
                                return "ChromeThatRecordsFailingTests-recordingTestThatShouldBeRecordedAndRetained";
                            }
                        },
                        Optional.of(new RuntimeException("Force writing of video file."))
                );

                String[] files = vncRecordingDirectory.toFile().list(new PatternFilenameFilter("FAILED-.*\\.flv"));
                assertThat(files).as("recorded file count").hasSize(1);
            }
        }

        class CustomRecordingFileFactory extends DefaultRecordingFileFactory {}
    }
}