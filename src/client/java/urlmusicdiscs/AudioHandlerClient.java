package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {

    public boolean checkForAudioFile(String urlName) {
        String hashedName = Hashing.Sha256(urlName);

        // Verify that the client_downloads folder exists, and if it doesn't, create it:
        if (!FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/").toFile().exists()) {
            FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/").toFile().mkdirs();
        }

        // Verify that the audio file exists, and if it doesn't, return false:
        if (!FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg")
                .toFile().exists()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param urlName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public CompletableFuture<Boolean> downloadAudioFile(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            String hashedName = Hashing.Sha256(urlName);
            File audioIn = new File(FabricLoader.getInstance().getConfigDir()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".raw").toString());
            File audioOut = new File(FabricLoader.getInstance().getConfigDir()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

            if (URLMusicDiscs.validateURL(urlName) == false || urlName.isEmpty()) {
                return false;
            } else {
                if (URLMusicDiscs.isYouTubeLink(urlName)) {
                    try {
                        YoutubeDL.executeYoutubeDLCommand(
                                String.format("--quiet -S res:144 -o " + audioIn.getAbsolutePath() + " " + urlName));
                    } catch (IOException | InterruptedException e) {
                        URLMusicDiscs.LOGGER.error("Failed to download audio file from YouTube.");
                        throw new RuntimeException(e);
                    }
                    // Download the audio file from the internet if it is not a YouTube link (but
                    // only on HTTPS URLs):
                } else {
                    try {
                        InputStream in = new URL(urlName).openStream();
                        Files.copy(in, audioIn.toPath());
                        in.close();

                        // Convert the audio file with the help of the FFMpeg executable and class:
                        try {
                            // deepcode ignore NoStringConcat: <please specify a reason of ignoring this>
                            FFmpeg.executeFFmpegCommand(String.format(
                                    "-i '" + audioIn.getAbsolutePath() + "' -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 '" + audioOut.getAbsolutePath() + "'"));
                            URLMusicDiscs.LOGGER.debug("'" + urlName + "'' has been downloaded and converted.");
                            audioIn.delete();
                            return true;
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    } catch (IOException e) {
                        URLMusicDiscs.LOGGER.error("Failed to download audio file from the internet.");
                        URLMusicDiscs.LOGGER.debug("URL: '" + urlName + ".'");
                        throw new RuntimeException(e);
                    }
                }

                try {
                    FFmpeg.executeFFmpegCommand(String.format(
                        "-i '" + audioIn.getAbsolutePath() + "' -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 '" + audioOut.getAbsolutePath() + "'"));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });

    }

    public InputStream getAudioInputStream(String urlName) throws IOException {
        InputStream fileStream;
        try {
            String hashedName = Hashing.Sha256(urlName);
            File audio = new File(FabricLoader.getInstance().getConfigDir()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

            fileStream = new FileInputStream(audio);
        } catch (FileNotFoundException e) {
            return null;
        }
        fileStream.close();
        return fileStream;
    }
}
