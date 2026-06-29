package net.arm.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class ImageDynamicButton extends AbstractButton {
    private final Identifier texture;
    private final int baseWidth;
    private final int baseHeight;
    private final Runnable onPressAction;

    private static final int TEXTURE_FILE_WIDTH = 1024;
    private static final int TEXTURE_FILE_HEIGHT = 256;

    private float currentScale = 1.0f;
    private long lastScaleTime = 0;
    private static final float ANIMATION_SPEED = 0.005f;

    public ImageDynamicButton(int x, int y, int width, int height, Identifier texture, Runnable onPressAction) {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
        this.baseWidth = width;
        this.baseHeight = height;
        this.onPressAction = onPressAction;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long currentTime = Util.getMillis();

        if (this.lastScaleTime == 0) {
            this.lastScaleTime = currentTime;
        }

        long deltaTime = currentTime - this.lastScaleTime;
        this.lastScaleTime = currentTime;

        if (deltaTime > 100) {
            deltaTime = 16;
        }

        float targetScale = this.isHoveredOrFocused() ? 1.3f : 1.0f;

        if (this.currentScale < targetScale) {
            this.currentScale += deltaTime * ANIMATION_SPEED;
            if (this.currentScale > targetScale) this.currentScale = targetScale;
        } else if (this.currentScale > targetScale) {
            this.currentScale -= deltaTime * ANIMATION_SPEED;
            if (this.currentScale < targetScale) this.currentScale = targetScale;
        }

        int newWidth = (int) (this.baseWidth * this.currentScale);
        int newHeight = (int) (this.baseHeight * this.currentScale);

        int offsetX = (newWidth - this.baseWidth) / 2;
        int offsetY = (newHeight - this.baseHeight) / 2;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.texture,
                this.getX() - offsetX, this.getY() - offsetY,
                0.0F, 0.0F,
                newWidth, newHeight,
                TEXTURE_FILE_WIDTH, TEXTURE_FILE_HEIGHT,
                TEXTURE_FILE_WIDTH, TEXTURE_FILE_HEIGHT
        );
    }

    @Override
    public void onPress(InputWithModifiers modifiers) {
        this.onPressAction.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}