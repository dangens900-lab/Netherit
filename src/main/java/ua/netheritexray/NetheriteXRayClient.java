
package ua.netheritexray;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NetheriteXRayClient implements ClientModInitializer {

    private static boolean enabled = false;
    private static final int RADIUS = 32;
    private static final int MAX_FOUND = 128;
    private static final List<BlockPos> FOUND = new ArrayList<>();

    private static KeyBinding toggleKey;
    private static int scanTimer = 0;

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.netheritexray.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.netheritexray"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (toggleKey.wasPressed()) {
                client.setScreen(new XRayScreen());
            }

            if (!enabled || client.player == null || client.world == null) {
                return;
            }

            if (++scanTimer >= 5) {
                scanTimer = 0;
                scan(client.player.getBlockPos(), client.world);
            }
        });
WorldRenderEvents.LAST.register(
    XRayRenderer::render
);
    }

    private static void scan(
        BlockPos center,
        net.minecraft.client.world.ClientWorld world
    ) {
        FOUND.clear();

        // Scan loaded client-world blocks in a sphere around the player.
        int minY = Math.max(world.getBottomY(), center.getY() - RADIUS);
        int maxY = Math.min(world.getBottomY() + world.getHeight() - 1, center.getY() + RADIUS);

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {

                    if (x * x + (y - center.getY()) * (y - center.getY()) + z * z > RADIUS * RADIUS) {
                        continue;
                    }

                    pos.set(
                        center.getX() + x,
                        y,
                        center.getZ() + z
                    );

                    if (world.getBlockState(pos).isOf(Blocks.ANCIENT_DEBRIS)) {
                        FOUND.add(pos.toImmutable());

                        if (FOUND.size() >= MAX_FOUND) {
                            return;
                        }
                    }
                }
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        FOUND.clear();
        scanTimer = 20;
    }

    public static List<BlockPos> getFoundBlocks() {
        return FOUND;
    }
}
