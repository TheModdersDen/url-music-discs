package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {

    /**
     * Check if the audio file exists in the client_downloads folder.
     * 
     * @param urlName the URL of the audio file to check
     * @return boolean if the audio file exists
     */
    public boolean checkForAudioFile(String urlName) {
        String hashedName = Hashing.Sha256(urlName);
        try {
            if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/"))) {
                Files.createDirectories(
                        FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/"));
            } else {
                File audio = new File(FabricLoader.getInstance().getConfigDir()
                        .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());
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
     * @throws IOException          if the URL is invalid
     * @throws InterruptedException if the download is interrupted
     */
    public CompletableFuture<Boolean> downloadAudioFile(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            String hashedName = Hashing.Sha256(urlName);
            File audioIn = new File(FabricLoader.getInstance().getConfigDir().toAbsolutePath()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName).toString());
            File audioOut = new File(FabricLoader.getInstance().getConfigDir().toAbsolutePath()
                    .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

            // Get the audioIn file extension by splitting the URL name by the dot and
            // getting the last element in the array:
            String[] audioConvertExt = audioIn.toString().split("\\.");
            String fileExtension = audioConvertExt[audioConvertExt.length - 1];

            if (URLMusicDiscs.validateURL(urlName) || urlName.isEmpty()) {
                return false;
            } else {
                if (URLMusicDiscs.isYouTubeLink(urlName)) {
                    try {

                        YoutubeDL.executeYoutubeDLCommand(
                                String.format("--quiet -S res:144 -o '"
                                        + audioIn.toPath().resolve(hashedName).toAbsolutePath() + "' '" + urlName
                                        + "'"));
                    } catch (IOException | InterruptedException e) {
                        URLMusicDiscs.LOGGER.error("Failed to download audio file from YouTube.");
                        throw new RuntimeException(e);
                    }

                    // Download the audio file from the internet if it is not a YouTube link (but
                    // only on HTTPS URLs):
                } else {
                    try {
                        InputStream in = new URL(urlName).openStream();
                        // Remove all the whitespace from the URL name (along with any other characters,
                        // and remove the file extension and append the .raw extension)
                        Files.copy(in, audioIn.toPath().resolve(hashedName + ".raw").toAbsolutePath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        in.close();

                    } catch (IOException e) {
                        URLMusicDiscs.LOGGER.error("Failed to download audio file from the internet.");
                        URLMusicDiscs.LOGGER.info("URL: '" + urlName + ".'");
                        throw new RuntimeException(e);
                    }
                }

                File DownloadedDir = new File(FabricLoader.getInstance().getConfigDir().toAbsolutePath()
                        .resolve("urlmusicdiscs/client_downloads/").toString());
                File[] matches = DownloadedDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith(hashedName);
                    }
                });

                if (matches.length == 0) {
                    if (URLMusicDiscs.DEBUG_MODE)
                        URLMusicDiscs.LOGGER.info(" Failed to find a file with the hashed name of '" + hashedName
                                + "' in the client_downloads folder.");
                    return false;
                }

                try {
                    // Iterate through the matches array and try to convert the audio file:
                    for (File match : matches) {
                        if (URLMusicDiscs.DEBUG_MODE)
                            URLMusicDiscs.LOGGER.info("Match: " + match.toString());
                        if (URLMusicDiscs.DEBUG_MODE)
                            URLMusicDiscs.LOGGER.info("File extension: " + fileExtension);
                        if (URLMusicDiscs.DEBUG_MODE)
                            URLMusicDiscs.LOGGER.info("File name: " + match.getName());
                        FFmpeg.executeFFmpegCommand(String.format("-i '" + match + "' -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 '" + audioOut.toPath().resolve(hashedName + ".ogg").toAbsolutePath() + "'"));
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        });

    }

    /**
     * Get the audio file from the client_downloads folder.
     * 
     * @param urlName the URL of the audio file to get the InputStream of
     * @return the InputStream of the audio file
     * @throws IOException if the URL is invalid and therefore cannot create an
     *                     InputStream
     */
    public InputStream getAudioInputStream(String urlName) throws IOException {
        String hashedName = Hashing.Sha256(urlName);
        InputStream fileStream;
        File audio = new File(FabricLoader.getInstance().getConfigDir()
                .resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());
        try {
            fileStream = new FileInputStream(audio);
        } catch (FileNotFoundException e) {
            return null;
        }

        return fileStream;
    }
}
