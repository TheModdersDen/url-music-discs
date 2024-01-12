package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FFmpeg {

    /**
     * Check if the ffmpeg executable exists in the ffmpeg folder.
     * @throws IOException if the ffmpeg executable cannot be downloaded
     */
    static void checkForExecutable() throws IOException {

        // Get the ffmpeg directory:
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        // Verify that the ffmpeg folder exists, and if it doesn't, create it:
        try {
            if (!FFmpegDirectory.exists())
            {
                // Create the ffmpeg directory:
                boolean dirCreationSuccess = FFmpegDirectory.mkdirs();
                if (!dirCreationSuccess) {
                    URLMusicDiscs.LOGGER.error("Failed to create ffmpeg directory. Look at the stacktrace for more info.");
                    throw new RuntimeException("Failed to create ffmpeg directory.");
                } else {
                    // The directory was created successfully, so log it (but in a debug message):
                    URLMusicDiscs.LOGGER.debug("FFmpeg directory was created successfully.");
                }
            } else {
                // The directory already exists, so log it (but in a debug message):
                URLMusicDiscs.LOGGER.debug("FFmpeg directory exists and/or created successfully. Continuing...");
            }
        } catch (Exception e) {
            URLMusicDiscs.LOGGER.error("Failed to create ffmpeg directory. Stacktrace:\n" + e);
            throw new RuntimeException(e);
        }

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        if (!FFmpegDirectory.toPath().resolve(fileName).toFile().exists()) {
            File zipFile = FFmpegDirectory.toPath().resolve("ffmpeg.zip").toFile();

            InputStream in = null;

            if (!FFmpegDirectory.toPath().resolve("ffmpeg.zip").toFile().exists()) {
                if (SystemUtils.IS_OS_LINUX) {
                    in = new URL("https://tmd-tv-tech-2000.s3.us-west-2.amazonaws.com/MC-Misc/Mods/urlmusicdiscs/ffmpeg-linux.zip").openStream();

                    Files.copy(in, FFmpegDirectory.toPath().resolve("ffmpeg.zip"), StandardCopyOption.REPLACE_EXISTING);

                    if (!zipFile.exists()) {
                        return;
                    }
                } else if (SystemUtils.IS_OS_MAC) {
                    in = new URL("https://evermeet.cx/ffmpeg/ffmpeg-6.1.zip").openStream();

                    Files.copy(in, FFmpegDirectory.toPath().resolve("ffmpeg.zip"), StandardCopyOption.REPLACE_EXISTING);

                    if (!zipFile.exists()) {
                        return;
                    }
                } else if (SystemUtils.IS_OS_WINDOWS) {
                    in = new URL("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip").openStream();

                    Files.copy(in, FFmpegDirectory.toPath().resolve("ffmpeg.zip"), StandardCopyOption.REPLACE_EXISTING);

                    if (!zipFile.exists()) {
                        return;
                    }
                }

                in.close();
            }

            

            ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry zipEntry = zipInput.getNextEntry();

            while (zipEntry != null) {
                if (zipEntry.getName().endsWith("ffmpeg.exe") || zipEntry.getName().endsWith("ffmpeg")) {
                    Files.copy(zipInput, FFmpegDirectory.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zipInput.getNextEntry();
            }

            if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
                Runtime.getRuntime().exec("chmod +x '" + FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath() + fileName + "'");
            }

            zipFile.delete();
            zipInput.close();
        }
    }

    static void executeFFmpegCommand(String arguments) throws IOException, InterruptedException {

        if (URLMusicDiscs.DEBUG_MODE)
            URLMusicDiscs.LOGGER.info("Running FFMPEG with args: '" + arguments + "'");

        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        String FFmpeg = FFmpegDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        Process resultProcess = Runtime.getRuntime().exec(FFmpeg + " " + arguments);

        resultProcess.waitFor();

        return;
    }
}
