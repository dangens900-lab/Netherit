package ua.netheritexray;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;

import java.util.List;

public class XRayRenderer {

    private static final float E = 0.002f;

    public static void render(WorldRenderContext context) {

        if (!NetheriteXRayClient.isEnabled()) return;

        List<BlockPos> blocks = NetheriteXRayClient.getFoundBlocks();
        if (blocks.isEmpty()) return;

        Camera camera = context.camera();
        Vec3d cam = camera.getPos();

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) {
            // Запасний варіант: власна матриця виду
            matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        }

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Matrix4f m = matrices.peek().getPositionMatrix();

        // 1) Напівпрозора заливка
        BufferBuilder fill = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_COLOR
        );
        for (BlockPos p : blocks) {
            float x0 = p.getX() - E, y0 = p.getY() - E, z0 = p.getZ() - E;
            float x1 = p.getX() + 1 + E, y1 = p.getY() + 1 + E, z1 = p.getZ() + 1 + E;
            quad(fill, m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1); // низ
            quad(fill, m, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0); // верх
            quad(fill, m, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0); // північ
            quad(fill, m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1); // південь
            quad(fill, m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0); // захід
            quad(fill, m, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1); // схід
        }
        BufferRenderer.drawWithGlobalProgram(fill.end());

        // 2) Контур
        BufferBuilder lines = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR
        );
        for (BlockPos p : blocks) {
            VertexRendering.drawBox(
                matrices, lines,
                p.getX(), p.getY(), p.getZ(),
                p.getX() + 1, p.getY() + 1, p.getZ() + 1,
                1.0f, 0.2f, 0.2f, 1.0f
            );
        }
        BufferRenderer.drawWithGlobalProgram(lines.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    private static void quad(BufferBuilder b, Matrix4f m,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz) {
        b.vertex(m, ax, ay, az).color(1.0f, 0.1f, 0.1f, 0.35f);
        b.vertex(m, bx, by, bz).color(1.0f, 0.1f, 0.1f, 0.35f);
        b.vertex(m, cx, cy, cz).color(1.0f, 0.1f, 0.1f, 0.35f);
        b.vertex(m, dx, dy, dz).color(1.0f, 0.1f, 0.1f, 0.35f);
    }
}
