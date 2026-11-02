package ua.netheritexray;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class XRayScreen extends Screen {

    public XRayScreen() {
        super(Text.literal("Netherite X-Ray"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        addDrawableChild(ButtonWidget.builder(toggleText(), button -> {
            NetheriteXRayClient.setEnabled(!NetheriteXRayClient.isEnabled());
            button.setMessage(toggleText());
        }).dimensions(centerX - 100, centerY - 10, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрити"), button -> close())
            .dimensions(centerX - 100, centerY + 18, 200, 20).build());
    }

    private static Text toggleText() {
        return Text.literal("X-Ray: " + (NetheriteXRayClient.isEnabled() ? "УВІМКНЕНО" : "ВИМКНЕНО"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 48, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Показує стародавні уламки поблизу (радіус 24 блоки)"),
            width / 2, height / 2 - 30, 0xBBBBBB);
        super.render(context, mouseX, mouseY, delta);
    }
}
