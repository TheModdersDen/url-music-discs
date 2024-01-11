package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {

    /**
     * Check if the audio file exists in the client_downloads folder.
     * @param urlName the URL of the audio file to check
     * @return boolean if the audio file exists
     */
    public boolean checkForAudioFile(String urlName) {
        String hashedName = Hashing.Sha256(urlName);
        try {
            if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/")))
            {
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/"));
            } else {
                File audio = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());
                if (audio.exists()) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     *
     * @param urlName the URL of the audio file to download
     * @return boolean if the download was successful
     * @throws IOException if the URL is invalid
     * @throws InterruptedException if the download is interrupted
     */
    public CompletableFuture<Boolean> downloadAudioFile(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            String hashedName = Hashing.Sha256(urlName);
            File audioIn = new File(FabricLoader.getInstance().getConfigDir().toAbsolutePath()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".raw").toString());
            File audioOut = new File(FabricLoader.getInstance().getConfigDir().toAbsolutePath()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

            if (URLMusicDiscs.validateURL(urlName) || urlName.isEmpty()) {
                return false;
            } else {
                if (URLMusicDiscs.isYouTubeLink(urlName)) {
                    try {
                        YoutubeDL.executeYoutubeDLCommand(
                                String.format("--quiet -S res:144 -o " + audioIn.toPath().resolve(hashedName + ".raw").toAbsolutePath() + " " + urlName));
                    } catch (IOException | InterruptedException e) {
                        URLMusicDiscs.LOGGER.error("Failed to download audio file from YouTube.");
                        throw new RuntimeException(e);
                    }
                    // Download the audio file from the internet if it is not a YouTube link (but
                    // only on HTTPS URLs):
                } else {
                    try {
                        InputStream in = new URL(urlName).openStream();
                        // Remove all the whitespace from the URL name (along with any other characters, and remove the file extension and append the .raw extension)
                        Files.copy(in, audioIn.toPath().resolve(hashedName + ".raw").toAbsolutePath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        in.close();

                        // Convert the audio file with the help of the FFMpeg executable and class:
                        try {
                            // deepcode ignore NoStringConcat: <please specify a reason of ignoring this>
                            FFmpeg.executeFFmpegCommand(String.format(
                                    "-i '" + audioIn.toPath().resolve(hashedName + ".raw").toAbsolutePath() + "' -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 '" + audioOut.toPath().resolve(hashedName + ".ogg").toAbsolutePath() + "'"));
                            URLMusicDiscs.LOGGER.info("'" + urlName + "'' has been downloaded and converted.");
                            //audioIn.delete();
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    } catch (IOException e) {
                        URLMusicDiscs.LOGGER.error("Failed to download audio file from the internet.");
                        URLMusicDiscs.LOGGER.info("URL: '" + urlName + ".'");
                        throw new RuntimeException(e);
                    }
                }

                try {
                    FFmpeg.executeFFmpegCommand(String.format(
                                    "-i '" + audioIn.toPath().resolve(hashedName + ".raw").toAbsolutePath() + "' -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 '" + audioOut.toPath().resolve(hashedName + ".ogg").toAbsolutePath() + "'"));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        });

    }

    /**
     * Get the audio file from the client_downloads folder.
     * @param urlName the URL of the audio file to get the InputStream of
     * @return the InputStream of the audio file
     * @throws IOException if the URL is invalid and therefore cannot create an InputStream
     */
    public InputStream getAudioInputStream(String urlName) throws IOException {
        String hashedName = Hashing.Sha256(urlName);
        InputStream fileStream;
        File audio = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());
        try {
            fileStream = new FileInputStream(audio);
        } catch (FileNotFoundException e) {
            return null;
        }
        
        return fileStream;
    }
}
