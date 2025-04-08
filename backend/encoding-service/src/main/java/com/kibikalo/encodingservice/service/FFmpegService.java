package com.kibikalo.encodingservice.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FFmpegService {

    @Value("${app.ffmpeg.path}")
    private String ffmpegPath;

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})"
    );

    @Getter
    private Long lastDurationMillis = null;

    public boolean runDashEncoding(
            Path inputFile,
            Path outputDirectory,
            String manifestName,
            List<Integer> bitratesKbps,
            int segmentDuration,
            String codec
    ) {
        lastDurationMillis = null;
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(inputFile.toAbsolutePath().toString());

        // --- CHANGE: Add explicit map for EACH bitrate ---
        for (int i = 0; i < bitratesKbps.size(); i++) {
            command.add("-map");
            command.add("0:a"); // Map input audio for each output representation
        }
        // -------------------------------------------------

        // Add representations for each bitrate
        for (int i = 0; i < bitratesKbps.size(); i++) {
            command.add("-b:a:" + i);
            command.add(bitratesKbps.get(i) + "k");
            command.add("-c:a:" + i);
            command.add(codec); // Should be libopus based on config
        }

        // DASH specific options
        command.add("-f");
        command.add("dash");
        command.add("-seg_duration");
        command.add(String.valueOf(segmentDuration));
        command.add("-use_template");
        command.add("1");
        command.add("-use_timeline");
        command.add("0");

        // Naming convention for segments (Java strings are fine here)
        command.add("-init_seg_name");
        command.add("init-stream$RepresentationID$.m4s"); // No extra quotes needed in Java code
        command.add("-media_seg_name");
        command.add("chunk-stream$RepresentationID$-$Number%05d$.m4s"); // No extra quotes needed

        // --- CHANGE: Add strict experimental ---
        command.add("-strict");
        command.add("experimental"); // Or "-2"
        // ---------------------------------------

        // Output manifest file path
        command.add(
                outputDirectory.resolve(manifestName).toAbsolutePath().toString()
        );

        // Log the command exactly as it will be executed
        log.info("Executing FFmpeg command list: {}", command);
        // Log the command joined for easier manual copy/paste if needed again
        log.info("Executing FFmpeg command joined: {}", String.join(" ", command));


        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    )
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg output: {}", line);
                    Matcher matcher = DURATION_PATTERN.matcher(line);
                    if (matcher.find()) {
                        long hours = Long.parseLong(matcher.group(1));
                        long minutes = Long.parseLong(matcher.group(2));
                        long seconds = Long.parseLong(matcher.group(3));
                        long centiseconds = Long.parseLong(matcher.group(4));
                        lastDurationMillis = TimeUnit.HOURS.toMillis(hours)
                                + TimeUnit.MINUTES.toMillis(minutes)
                                + TimeUnit.SECONDS.toMillis(seconds)
                                + centiseconds * 10;
                        log.info("Parsed duration: {} ms", lastDurationMillis);
                    }
                }
            }

            boolean exited = process.waitFor(5, TimeUnit.MINUTES);
            if (!exited) {
                log.error("FFmpeg process timed out!");
                process.destroyForcibly();
                return false;
            }

            int exitCode = process.exitValue();
            log.info("FFmpeg process finished with exit code: {}", exitCode);
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            log.error("Error executing FFmpeg command: {}", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}