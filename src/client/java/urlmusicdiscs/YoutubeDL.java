package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class YoutubeDL {

    /**
     * Check if the yt-dlp executable exists in the youtube-dl folder.
     * @throws IOException if the yt-dlp executable cannot be downloaded
     */
    static void checkForExecutable() throws IOException {
        File YoutubeDLDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl/").toAbsolutePath().toFile();

        YoutubeDLDirectory.mkdirs();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp";

        if (!YoutubeDLDirectory.toPath().resolve(fileName).toFile().exists()) {
            InputStream fileStream = null;

            if (SystemUtils.IS_OS_LINUX) {
                fileStream = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux").openStream();
            } else if (SystemUtils.IS_OS_MAC) {
                fileStream = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos").openStream();
            } else if (SystemUtils.IS_OS_WINDOWS) {
                fileStream = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe").openStream();
            }

            Files.copy(fileStream, YoutubeDLDirectory.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        
            fileStream.close();
        }
    }

    /**
     * Executes a command with yt-dlp.
     * @param arguments the arguments to pass to yt-dlp
     * @return the output of the command
     * @throws IOException 
     * @throws InterruptedException
     */
    public static String executeYoutubeDLCommand(String arguments) throws IOException, InterruptedException {
        URLMusicDiscs.LOGGER.info("Running yt-dlp with args: '" + arguments + "'");
        File YoutubeDLDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp";

        String YoutubeDL = YoutubeDLDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            Runtime.getRuntime().exec("chmod +x " + YoutubeDL);
        }

        Process resultProcess = Runtime.getRuntime().exec(YoutubeDL + " " + arguments);

        resultProcess.waitFor();

        return "";
    }
}
