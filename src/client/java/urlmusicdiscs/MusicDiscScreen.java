package urlmusicdiscs;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class MusicDiscScreen extends Screen {

    /**
     * The texture for the record input GUI.
     */
    private static final Identifier TEXTURE = new Identifier(URLMusicDiscs.MOD_ID, "textures/gui/record_input.png");

    /**
     * The texture for the text field.
     */
    private static final Identifier TEXT_FIELD_TEXTURE = new Identifier("minecraft", "container/anvil/text_field.png");

    /**
     * The text field widget.
     */
    private TextFieldWidget nameField;

    /**
     * The width of the background.
     */
    int backgroundWidth = 176;

    /**
     * The height of the background.
     */
    int backgroundHeight = 44;

    /**
     *  The default text for the text field.
     */
    String inputDefaultText;

    /**
     * Constructor for the MusicDiscScreen class.
     * @param title the title
     * @param player the player
     * @param item the item
     * @param inputDefaultText the default text for the text field
     */
    protected MusicDiscScreen(Text title, PlayerEntity player, ItemStack item, String inputDefaultText) {
        super(title);
        this.inputDefaultText = inputDefaultText;
    }

    /**
     * Called when the screen is opened and initializes the text field.
     * This updates the text field position.
     */
    public void updateTextPosition() {
        if (textRenderer == null) {
            return;
        }

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        this.nameField = new TextFieldWidget(textRenderer, x + 62, y + 18, 103, 12, (Text)Text.translatable((String)"container.repair"));
        this.nameField.setFocusUnlocked(false);
        this.nameField.setEditableColor(-1);
        this.nameField.setUneditableColor(-1);
        this.nameField.setDrawsBackground(false);
        this.nameField.setMaxLength(200);
        this.nameField.setChangedListener(this::onRenamed);
        this.nameField.setText(this.inputDefaultText);
        this.addSelectableChild(this.nameField);
        this.setInitialFocus(this.nameField);
        this.nameField.setEditable(true);
    }

    /**
     * Called when the screen is resized.
     * @param client MinecraftClient the client
     * @param width int the width
     * @param height int the height
     */
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        String string = this.nameField.getText();
        updateTextPosition();
        this.nameField.setText(string);
    }

    /**
     * Called when the screen is closed.
     * @param keyCode int the key code
     * @param scanCode int the scan code
     * @param modifiers int the modifiers
     * @return boolean if the key was pressed
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!this.nameField.getText().equals(this.inputDefaultText)) {
                PacketByteBuf bufInfo = PacketByteBufs.create();
                bufInfo.writeString(this.nameField.getText());

                ClientPlayNetworking.send(URLMusicDiscs.CUSTOM_RECORD_SET_URL, bufInfo);
            }

            assert Objects.requireNonNull(this.client).player != null;
            if (this.client.player != null) {
                this.client.player.closeHandledScreen();
            }
        }
        if (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.isActive()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Called when the text field is renamed.
     * @param s the string that was renamed (String)
     */
    private void onRenamed(String s) {
    }

    /**
     * Called when the screen is rendered.
     * @param context the draw context
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param delta the delta
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        context.drawTexture(TEXT_FIELD_TEXTURE, x + 59, y + 14, 0, 0, 110, 16);

        if (this.nameField == null) {
            updateTextPosition();
        }

        this.nameField.render(context, mouseX, mouseY, delta);
    }
}
