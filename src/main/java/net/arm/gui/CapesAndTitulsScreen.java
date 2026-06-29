package net.arm.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arm.cape.CapeManager;
import net.arm.CapesAndTitul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CapesAndTitulsScreen extends Screen {

    private static final Identifier CAPES_TEXTURE = Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "textures/capes.png");
    private static final Identifier TITLES_TEXTURE = Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "textures/titles.png");

    private boolean showPlayerPreview = false;

    private static record TitleData(String id, Component text, int bgStartColor, int bgEndColor) {}
    private static record CapeData(String id, String name, Identifier texture) {}

    private final List<TitleData> sampleTitles = new ArrayList<>();
    private final List<CapeData> capeList = new ArrayList<>();

    private int selectedTitleIndex = -1;

    private static final int COLUMNS = 3;
    private static final int TITLE_CELL_WIDTH = 180;
    private static final int TITLE_CELL_HEIGHT = 45;

    private static final int CAPE_CELL_WIDTH = 80;
    private static final int CAPE_CELL_HEIGHT = 120;
    private static final int PADDING = 30;

    public CapesAndTitulsScreen(int initialSection) {
        super(Component.literal("Capes & Titles Menu"));

        if (initialSection == 0) {
            this.showPlayerPreview = true;
        } else if (initialSection == 1) {
            this.showPlayerPreview = false;
        }

        loadDataFromJson();
    }

    private void loadDataFromJson() {
        this.sampleTitles.clear();
        this.capeList.clear();

        Identifier configId = Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "gui_data.json");
        try {
            var resourceOpt = Minecraft.getInstance().getResourceManager().getResource(configId);
            if (resourceOpt.isPresent()) {
                try (Reader reader = resourceOpt.get().openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    if (json.has("titles")) {
                        JsonArray titlesArray = json.getAsJsonArray("titles");
                        for (JsonElement el : titlesArray) {
                            JsonObject obj = el.getAsJsonObject();

                            String text = obj.get("text").getAsString();
                            String id = obj.has("id") ? obj.get("id").getAsString() : text;

                            String colorHex = obj.get("color").getAsString();
                            if (!colorHex.startsWith("#")) colorHex = "#" + colorHex;

                            boolean bold = obj.has("bold") && obj.get("bold").getAsBoolean();
                            boolean italic = obj.has("italic") && obj.get("italic").getAsBoolean();
                            boolean underlined = obj.has("underlined") && obj.get("underlined").getAsBoolean();

                            int bgStart = (int) Long.parseLong(obj.get("bgStart").getAsString(), 16);
                            int bgEnd = (int) Long.parseLong(obj.get("bgEnd").getAsString(), 16);

                            int colorInt = java.awt.Color.decode(colorHex).getRGB() & 0xFFFFFF;
                            Style style = Style.EMPTY.withColor(TextColor.fromRgb(colorInt));
                            if (bold) style = style.withBold(true);
                            if (italic) style = style.withItalic(true);
                            if (underlined) style = style.withUnderlined(true);

                            this.sampleTitles.add(new TitleData(id, Component.literal(text).withStyle(style), bgStart, bgEnd));
                        }
                    }

                    if (json.has("capes")) {
                        JsonArray capesArray = json.getAsJsonArray("capes");
                        for (JsonElement el : capesArray) {
                            JsonObject obj = el.getAsJsonObject();
                            String id = obj.get("id").getAsString();
                            String name = obj.get("name").getAsString();
                            String texturePath = obj.get("texture").getAsString();

                            Identifier texObj = texturePath.isEmpty() ? null : Identifier.parse(texturePath);
                            this.capeList.add(new CapeData(id, name, texObj));
                        }
                    }
                }
            }
        } catch (Exception e) {
            CapesAndTitul.LOGGER.error("Failed to load gui_data.json", e);
        }
    }

    @Override
    protected void init() {
        super.init();

        int btnWidth = 256;
        int btnHeight = 64;
        int topOffset = 40;
        int spacing = 35;
        int totalWidth = (btnWidth * 2) + spacing;
        int startX = (this.width - totalWidth) / 2;

        this.addRenderableWidget(new ImageDynamicButton(startX, topOffset, btnWidth, btnHeight, CAPES_TEXTURE, () -> this.showPlayerPreview = true));
        this.addRenderableWidget(new ImageDynamicButton(startX + btnWidth + spacing, topOffset, btnWidth, btnHeight, TITLES_TEXTURE, () -> this.showPlayerPreview = false));

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 115, 0xFFFFFF);

        if (this.showPlayerPreview) {
            renderCapesGrid(guiGraphics, mouseX, mouseY);
        } else {
            renderTitlesGrid(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderCapesGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int rows = (int) Math.ceil((double) capeList.size() / COLUMNS);
        int totalGridWidth = (CAPE_CELL_WIDTH * COLUMNS) + (PADDING * (COLUMNS - 1));
        int startX = (this.width - totalGridWidth) / 2;
        int totalGridHeight = (CAPE_CELL_HEIGHT * rows) + (PADDING * (rows - 1));
        int startY = (this.height - totalGridHeight) / 2 + 50;

        for (int i = 0; i < capeList.size(); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int x = startX + col * (CAPE_CELL_WIDTH + PADDING);
            int y = startY + row * (CAPE_CELL_HEIGHT + PADDING);

            CapeData cape = capeList.get(i);
            int centerX = x + (CAPE_CELL_WIDTH / 2);
            int centerY = y + (CAPE_CELL_HEIGHT / 2);

            boolean isUnlocked = CapeManager.isUnlocked(cape.id);

            if (cape.texture == null) {
                drawNoCapePreview(guiGraphics, centerX, centerY);
            } else {
                drawCape2D(guiGraphics, centerX, centerY, cape.texture);
            }

            if (!isUnlocked) {
                guiGraphics.fill(centerX - 30, centerY - 48, centerX + 30, centerY + 48, 0xAA000000);
            }

            if (!isUnlocked) {
                guiGraphics.drawCenteredString(this.font, "🔒", centerX, centerY - 10, 0xFF5555);
            }

            guiGraphics.drawCenteredString(this.font, cape.name, centerX, y + CAPE_CELL_HEIGHT + 5, 0xAAAAAA);

            if (CapeManager.currentCapeId.equals(cape.id) && isUnlocked) {
                drawPerfectCheckmark(guiGraphics, centerX - 3, y - 10);
            }
        }
    }

    private void drawCape2D(GuiGraphics guiGraphics, int x, int y, Identifier capeTexture) {
        int drawWidth = 60;
        int drawHeight = 96;
        int startX = x - (drawWidth / 2);
        int startY = y - (drawHeight / 2);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, capeTexture, startX, startY, 1.0F, 1.0F, drawWidth, drawHeight, 10, 16, 64, 32);
    }

    private void drawNoCapePreview(GuiGraphics guiGraphics, int x, int y) {
        int drawWidth = 60;
        int drawHeight = 96;
        int startX = x - (drawWidth / 2);
        int startY = y - (drawHeight / 2);

        guiGraphics.fill(startX, startY, startX + drawWidth, startY + drawHeight, 0x66000000);
        guiGraphics.fill(x - 15, y - 2, x + 15, y + 2, 0xFF888888);
        guiGraphics.fill(x - 2, y - 15, x + 2, y + 15, 0xFF888888);
    }

    private void renderTitlesGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int rows = (int) Math.ceil((double) sampleTitles.size() / COLUMNS);
        int totalGridWidth = (TITLE_CELL_WIDTH * COLUMNS) + (PADDING * (COLUMNS - 1));
        int startX = (this.width - totalGridWidth) / 2;
        int totalGridHeight = (TITLE_CELL_HEIGHT * rows) + (PADDING * (rows - 1));
        int startY = (this.height - totalGridHeight) / 2 + 50;
        long time = Util.getMillis();

        for (int i = 0; i < sampleTitles.size(); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int x = startX + col * (TITLE_CELL_WIDTH + PADDING);
            int y = startY + row * (TITLE_CELL_HEIGHT + PADDING);

            TitleData title = sampleTitles.get(i);
            String rawText = title.text().getString();

            String cleanText = rawText.contains("&#") ? rawText.replaceAll("&#[A-Fa-f0-9]{6}", "") : rawText;

            float scale = 1.5F;
            float scaledTextWidth = this.font.width(cleanText) * scale;
            float scaledTextHeight = this.font.lineHeight * scale;

            float textX = x + (TITLE_CELL_WIDTH - scaledTextWidth) / 2.0F;
            float textY = y + (TITLE_CELL_HEIGHT - scaledTextHeight) / 2.0F;

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(textX, textY);
            guiGraphics.pose().scale(scale, scale);

            int currentXOffset = 0;

            if (rawText.contains("&#")) {
                String[] parts = rawText.split("&#");
                int totalCharsProcessed = 0;
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    if (part.length() >= 7) {
                        String hexColor = "#" + part.substring(0, 6);
                        String character = part.substring(6);

                        int finalCharColor = 0xFFFFFF;
                        try {
                            finalCharColor = java.awt.Color.decode(hexColor).getRGB();
                        } catch (Exception ignored) {}

                        guiGraphics.drawString(this.font, character, currentXOffset, 0, finalCharColor, false);
                        currentXOffset += this.font.width(character);
                    } else {
                        guiGraphics.drawString(this.font, part, currentXOffset, 0, 0xAAAAAA, false);
                        currentXOffset += this.font.width(part);
                    }
                }
            } else {
                int colorStart = title.bgStartColor() | 0xFF000000;
                int colorEnd = title.bgEndColor() | 0xFF000000;

                for (int charIndex = 0; charIndex < rawText.length(); charIndex++) {
                    String singleChar = String.valueOf(rawText.charAt(charIndex));
                    float t = (float) Math.sin((time * 0.004D) + (charIndex * 0.5D)) * 0.5F + 0.5F;

                    int r = (int) (Mth.lerp(t, (colorStart >> 16 & 255) / 255.0F, (colorEnd >> 16 & 255) / 255.0F) * 255.0F);
                    int g = (int) (Mth.lerp(t, (colorStart >> 8 & 255) / 255.0F, (colorEnd >> 8 & 255) / 255.0F) * 255.0F);
                    int b = (int) (Mth.lerp(t, (colorStart & 255) / 255.0F, (colorEnd & 255) / 255.0F) * 255.0F);

                    int finalCharColor = java.awt.Color.decode(String.valueOf((r << 16) | (g << 8) | b)).brighter().getRGB();
                    guiGraphics.drawString(this.font, singleChar, currentXOffset, 0, finalCharColor, false);
                    currentXOffset += this.font.width(singleChar);
                }
            }
            guiGraphics.pose().popMatrix();

            if (title.id().equals(CapeManager.currentTitleId)) {
                this.selectedTitleIndex = i;
                drawPerfectCheckmark(guiGraphics, (int) (textX + scaledTextWidth + 8), (int) (textY + (scaledTextHeight - 8) / 2.0F));
            }
        }
    }

    private void drawPerfectCheckmark(GuiGraphics guiGraphics, int checkX, int checkY) {
        guiGraphics.fill(checkX, checkY + 4, checkX + 2, checkY + 6, 0xFF55FF55);
        guiGraphics.fill(checkX + 1, checkY + 5, checkX + 3, checkY + 7, 0xFF55FF55);
        guiGraphics.fill(checkX + 2, checkY + 6, checkX + 4, checkY + 4, 0xFF55FF55);
        guiGraphics.fill(checkX + 3, checkY + 5, checkX + 5, checkY + 2, 0xFF55FF55);
        guiGraphics.fill(checkX + 4, checkY + 3, checkX + 6, checkY + 0, 0xFF55FF55);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (event.button() == 0) {
            if (this.showPlayerPreview) {
                int rows = (int) Math.ceil((double) capeList.size() / COLUMNS);
                int totalGridWidth = (CAPE_CELL_WIDTH * COLUMNS) + (PADDING * (COLUMNS - 1));
                int startX = (this.width - totalGridWidth) / 2;
                int totalGridHeight = (CAPE_CELL_HEIGHT * rows) + (PADDING * (rows - 1));
                int startY = (this.height - totalGridHeight) / 2 + 50;

                for (int i = 0; i < capeList.size(); i++) {
                    int col = i % COLUMNS;
                    int row = i / COLUMNS;
                    int x = startX + col * (CAPE_CELL_WIDTH + PADDING);
                    int y = startY + row * (CAPE_CELL_HEIGHT + PADDING);

                    if (mouseX >= x && mouseX <= x + CAPE_CELL_WIDTH && mouseY >= y && mouseY <= y + CAPE_CELL_HEIGHT) {
                        CapeData cape = capeList.get(i);

                        if (CapeManager.isUnlocked(cape.id)) {
                            net.arm.util.ModMessages.sendToServer(new net.arm.util.EquipItemPacket(0, cape.id));

                            CapeManager.currentCapeId = cape.id;
                            if (cape.id.equals("none") || cape.texture == null || cape.texture.getPath().isEmpty()) {
                                CapeManager.currentCapeTexture = null;
                            } else {
                                CapeManager.currentCapeTexture = cape.texture;
                            }

                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.displayClientMessage(Component.literal("Выбран плащ: " + cape.name), true);
                            }
                        } else {
                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.displayClientMessage(Component.literal("§cУ вас нет прав на этот плащ!"), true);
                            }
                        }
                        return true;
                    }
                }
            } else {
                int rows = (int) Math.ceil((double) sampleTitles.size() / COLUMNS);
                int totalGridWidth = (TITLE_CELL_WIDTH * COLUMNS) + (PADDING * (COLUMNS - 1));
                int startX = (this.width - totalGridWidth) / 2;
                int totalGridHeight = (TITLE_CELL_HEIGHT * rows) + (PADDING * (rows - 1));
                int startY = (this.height - totalGridHeight) / 2 + 50;

                for (int i = 0; i < sampleTitles.size(); i++) {
                    int col = i % COLUMNS;
                    int row = i / COLUMNS;
                    int x = startX + col * (TITLE_CELL_WIDTH + PADDING);
                    int y = startY + row * (TITLE_CELL_HEIGHT + PADDING);

                    if (mouseX >= x && mouseX <= x + TITLE_CELL_WIDTH && mouseY >= y && mouseY <= y + TITLE_CELL_HEIGHT) {
                        TitleData title = sampleTitles.get(i);

                        if (CapeManager.isUnlocked(title.id())) {
                            net.arm.util.ModMessages.sendToServer(new net.arm.util.EquipItemPacket(1, title.id()));

                            CapeManager.currentTitleId = title.id();
                            this.selectedTitleIndex = i;

                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.refreshDisplayName();
                                this.minecraft.player.displayClientMessage(Component.literal("Выбран титул: ").append(title.text()), true);
                            }
                            return true;
                        } else {
                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.displayClientMessage(Component.literal("§cУ вас нет прав на этот титул!"), true);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}