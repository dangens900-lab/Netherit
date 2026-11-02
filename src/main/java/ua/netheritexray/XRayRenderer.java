
package ua.netheritexray;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class XRayRenderer {

    public static void render(WorldRenderContext context) {

        if (!NetheriteXRayClient.isEnabled()) return;

        if (context.matrixStack() == null) return;

        List<BlockPos> blocks =
            NetheriteXRayClient.getFoundBlocks();

        if (blocks.isEmpty()) return;

        Vec3d camera = context.camera().getPos();

        var matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(3.0f);

        BufferBuilder buffer = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR
        );

        for (BlockPos pos : blocks) {

            WorldRenderer.drawBox(
                matrices,
                buffer,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1,
                pos.getY() + 1,
                pos.getZ() + 1,
                1.0f,
                0.0f,
                0.0f,
                1.0f
            );
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }
}
