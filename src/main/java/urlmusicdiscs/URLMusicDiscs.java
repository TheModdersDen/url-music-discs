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
	public static final String MOD_ID = "urlmusicdiscs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier CUSTOM_RECORD_PACKET_ID = new Identifier(MOD_ID, "play_sound");
	public static final Identifier CUSTOM_RECORD_GUI = new Identifier(MOD_ID, "record_gui");
	public static final Identifier CUSTOM_RECORD_SET_URL = new Identifier(MOD_ID, "record_set_url");
	public static final Identifier PLACEHOLDER_SOUND_IDENTIFIER = new Identifier(MOD_ID, "placeholder_sound");
	public static final SoundEvent PLACEHOLDER_SOUND = Registry.register(
			Registries.SOUND_EVENT,
			PLACEHOLDER_SOUND_IDENTIFIER,
			SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER)
	);
	public static final Item CUSTOM_RECORD = Registry.register(
			Registries.ITEM,
			new Identifier(MOD_ID, "custom_record"),
			new URLDiscItem(
17, PLACEHOLDER_SOUND, new FabricItemSettings().maxCount(1), 1
			)
	);


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

			if (URLMusicDiscs.validateURL(urlName) == false) {
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

	/*
     * Validates whether a URL for a music disc is correct.
     * If it is a valid URL, it returns true, otherwise, it returns false.
     * 
     * @param URL - The URL to validate.
     * @returns boolean - Whether the URL is valid or not.
     */
    public static boolean validateURL(String URL) {
        
        if ((URL.startsWith("https://") || URL.startsWith("https://youtu.be") || URL.startsWith("https://www.youtube.com") || URL.startsWith("https://youtube.com")) && URL.length() <= 200) {
			return true;
        } else {
            return false;
        }
    }
}

