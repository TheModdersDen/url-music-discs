package urlmusicdiscs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

public class URLMusicDiscsClient implements ClientModInitializer {

	/**
	 * The HashMap of playing sounds.
	 */
	HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();

	/**
	 * Gets the file extension of a file.
	 * @param filename the filename
	 * @return String the file extension of the filename provided
	 */
	public static String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}

	/**
	 * The Client Mod Initializer.
	 */
	@Override
	public void onInitializeClient() {
		// Download FFmpeg and YoutubeDL if they are not already downloaded.
		try {
			FFmpeg.checkForExecutable();
			YoutubeDL.checkForExecutable();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		/**
		 * Client Music Started Event
		 * @param client MinecraftClient the client
		 * @param handler ClientPlayNetworkHandler the handler
		 * @param buf PacketByteBuf the buffer
		 * @param responseSender PacketSender the response sender
		 */
		ClientPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, (client, handler, buf, responseSender) -> {

			BlockPos blockPos = buf.readBlockPos();
 			Vec3d blockPosition = blockPos.toCenterPos();
 			String fileUrl = buf.readString();

			client.execute(() -> {

				FileSound currentSound = playingSounds.get(blockPosition);

				if (currentSound != null) {
					client.getSoundManager().stop(currentSound);
				}

				if (fileUrl.isEmpty()) {
					return;
				}

				AudioHandlerClient audioHandler = new AudioHandlerClient();

				if (!audioHandler.checkForAudioFile(fileUrl)) {
                    assert client.player != null;
                    client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."));

					try {
						audioHandler.downloadAudioFile(fileUrl).thenApply((in) -> {
							client.player.sendMessage(Text.literal("Downloading complete!"));

							FileSound fileSound = new FileSound();
							fileSound.position = blockPosition;
							fileSound.fileUrl = fileUrl;

							playingSounds.put(blockPosition, fileSound);

							client.getSoundManager().play(fileSound);

							return null;
						});
					} catch (IOException e) {
						client.player.sendMessage(Text.literal("Failed to download music!"));
						
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				FileSound fileSound = new FileSound();
				fileSound.position = blockPosition;
				fileSound.fileUrl = fileUrl;

				playingSounds.put(blockPosition, fileSound);

				if (URLMusicDiscs.DEBUG_MODE) {
					URLMusicDiscs.LOGGER.info("FileURL: " + fileUrl + "\n");

					URLMusicDiscs.LOGGER.info("Playing sound: " + fileSound.getId().toString());
				}
				
				
				client.getSoundManager().play(fileSound);
				
				if (URLMusicDiscs.DEBUG_MODE)
					URLMusicDiscs.LOGGER.info("#1: Music should be playing now. :)");
			});
		});

		/**
		 * Client Music Stopped Event
		 * @param client MinecraftClient the client
		 * @param handler ClientPlayNetworkHandler the handler
		 * @param buf PacketByteBuf the buffer
		 * @param responseSender PacketSender the response sender
		 */
		ClientPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_GUI, (client, handler, buf, responseSender) -> {

			ItemStack item = buf.readItemStack();

			client.execute(() -> {

				NbtCompound itemNbt = item.getNbt();

				if (itemNbt == null) {
					itemNbt = new NbtCompound();
				}

				String currentUrl = itemNbt.getString("music_url");

				client.setScreen(new MusicDiscScreen(Text.translatable("test"), client.player, item, !Objects.equals(currentUrl, "") ? currentUrl : "URL"));
			});
		});
	}
}