package com.skyblockfisher;

import com.skyblockfisher.gui.FisherScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SkyBlockFisherClient implements ClientModInitializer {

    private static KeyBinding keyOpenGui;
    private static KeyBinding keyToggle;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        // Register keybindings — Category.create(String) for 1.21.10
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("skyblockfisher", "keys"));

        keyOpenGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skyblockfisher.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        keyToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skyblockfisher.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                category
        ));

        // Client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Keybind checks
            while (keyOpenGui.wasPressed()) {
                client.setScreen(new FisherScreen());
            }
            while (keyToggle.wasPressed()) {
                FishingHandler.getInstance().toggle();
            }

            // Fishing tick
            FishingHandler.getInstance().onClientTick();
        });
    }
}
