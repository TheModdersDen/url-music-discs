package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    public boolean checkForAudioFile(String urlName) {
        String hashedName = Hashing.Sha256(urlName);
        
        // Verify that the client_downloads folder exists, and if it doesn't, create it:
        if (!FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/").toFile().exists()) {
            FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/").toFile().mkdirs();
        }
        
        // Verify that the audio file exists, and if it doesn't, return false:
        if (!FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toFile().exists()) {
            return false;
        } else {
            return true;
        }
    }

    public CompletableFuture<Boolean> downloadVideoAsOgg(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            String hashedName = Hashing.Sha256(urlName);
            File audioIn = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".raw").toString());
            File audioOut = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());
            
            if (urlName.isEmpty()) {
                return false;
            }

            try {
                YoutubeDL.executeYoutubeDLCommand(String.format("--quiet -S res:144 -o \"%s\" %s", audioIn.getAbsolutePath(), urlName));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                FFmpeg.executeFFmpegCommand(String.format("-i \"%s\" -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 \"%s\"", audioIn.getAbsolutePath(), audioOut.getAbsolutePath()));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            return true;
        });


    }

    public InputStream getAudioInputStream(String urlName) throws IOException {
        InputStream fileStream;
        try {
            String hashedName = Hashing.Sha256(urlName);
            File audio = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

            fileStream = new FileInputStream(audio);
        } catch (FileNotFoundException e) {
            return null;
        }
        fileStream.close();
        return fileStream;
    }
}
