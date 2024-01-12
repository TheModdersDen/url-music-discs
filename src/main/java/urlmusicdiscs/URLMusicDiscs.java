package urlmusicdiscs;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlmusicdiscs.items.URLDiscItem;

public class URLMusicDiscs implements ModInitializer {

	/**
	 * The mod ID.
	 */
	public static final String MOD_ID = "urlmusicdiscs";

	/**
	 * The mod Logger.
	 */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * The Custom Record Packet Identifier.
	 */
	public static final Identifier CUSTOM_RECORD_PACKET_ID = new Identifier(MOD_ID, "play_sound");

	/**
	 * The Custom Record GUI Identifier.
	 */
	public static final Identifier CUSTOM_RECORD_GUI = new Identifier(MOD_ID, "record_gui");

	/**
	 * The Custom Record Set URL Identifier.
	 */
	public static final Identifier CUSTOM_RECORD_SET_URL = new Identifier(MOD_ID, "record_set_url");

	/**
	 * The Placeholder Sound Identifier.
	 */
	public static final Identifier PLACEHOLDER_SOUND_IDENTIFIER = new Identifier(MOD_ID, "placeholder_sound");

	/**
	 * The Placeholder Sound Event.
	 */
	public static final SoundEvent PLACEHOLDER_SOUND = Registry.register(
			Registries.SOUND_EVENT,
			PLACEHOLDER_SOUND_IDENTIFIER,
			SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER)
	);

	/**
	 * The Custom Record Item.
	 */
	public static final Item CUSTOM_RECORD = Registry.register(
			Registries.ITEM,
			new Identifier(MOD_ID, "custom_record"),
			new URLDiscItem(
17, PLACEHOLDER_SOUND, new FabricItemSettings().maxCount(1), 1
			)
	);

	/**
	 * The Debug Mode variable.
	 */
	public static final boolean DEBUG_MODE = true;

	/**
	 * Initialize the mod.
	 */
	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> {
			content.add(CUSTOM_RECORD);
		});

		// Server event handler for setting the URL on the Custom Record
		ServerPlayNetworking.registerGlobalReceiver(CUSTOM_RECORD_SET_URL, (server, player, handler, buf, responseSender) -> {
			ItemStack currentItem = player.getStackInHand(player.getActiveHand());

			if (currentItem.getItem() != CUSTOM_RECORD) {
				return;
			}

			String urlName = buf.readString();

			if (URLMusicDiscs.validateURL(urlName)) {
				player.sendMessage(Text.literal("Song URL must be a YouTube link or a valid HTTPS URL to a song file!"));
				return;
			}

			if (urlName.length() >= 200) {
				player.sendMessage(Text.literal("Song URL is too long!"));
				return;
			}

			player.playSound(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);

			NbtCompound currentNbt = currentItem.getNbt();

			if (currentNbt == null) {
				currentNbt = new NbtCompound();
			}

			currentNbt.putString("music_url", urlName);

			currentItem.setNbt(currentNbt);
		});
	}

	/**
     * Validates whether a URL for a music disc is correct.
     * If it is a valid URL, it returns true, otherwise, it returns false.
     * 
     * @param URL - The URL to validate.
     * @return boolean - Whether the URL is valid or not.
     */
    public static boolean validateURL(String URL) {
        return (!URL.startsWith("https://") && !URL.startsWith("https://youtu.be") && !URL.startsWith("https://www.youtube.com") && !URL.startsWith("https://youtube.com")) || URL.length() > 200;
    }

	/**
     * Check if the URL is a valid YouTube link using RegEx.
     * @param URL the URL to check if it is a valid YouTube link
     * @return boolean true if the URL is a valid YouTube link, false if it is not.
     */
    public static boolean isYouTubeLink(String URL) {
        return URL.matches("^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$");
    }
}

