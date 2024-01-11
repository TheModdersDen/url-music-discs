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
    static void checkForExecutable() throws IOException {
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        // Verify that the ffmpeg folder exists, and if it doesn't, create it:
        try {
            FFmpegDirectory.mkdirs();
        } catch (Exception e) {
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

            zipFile.delete();
            zipInput.close();
        }
    }

    static void executeFFmpegCommand(String arguments) throws IOException, InterruptedException {

        URLMusicDiscs.LOGGER.info("Running FFMPEG with args: '" + arguments + "'");

        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        String FFmpeg = FFmpegDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            Runtime.getRuntime().exec("chmod +x " + FFmpeg);
        }

        Process resultProcess = Runtime.getRuntime().exec(FFmpeg + " " + arguments);

        resultProcess.waitFor();

        return;
    }
}
