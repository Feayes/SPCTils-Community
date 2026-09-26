package com.reveth.spctils.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class SpcServersScreen extends Screen {
    private final Screen parent;

    public SpcServersScreen(Screen parent) {
        super(Text.literal("SPC Servers"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 4 + 48;

        // Button 1: play.spcid.net
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("IP 1 : play.spcid.net (play)"),
                button -> connect("play.spcid.net", "SPC Play"))
                .dimensions(centerX - 100, centerY, 200, 20)
                .build());

        // Button 2: mc.spcid.net
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("IP 2 : mc.spcid.net (mc)"),
                button -> connect("mc.spcid.net", "SPC Survival"))
                .dimensions(centerX - 100, centerY + 24, 200, 20)
                .build());

        // Button 3: indie.spcid.net
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("IP 3 : indie.spcid.net (indie)"),
                button -> connect("indie.spcid.net", "SPC Indie"))
                .dimensions(centerX - 100, centerY + 48, 200, 20)
                .build());

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.client.setScreen(this.parent))
                .dimensions(centerX - 100, centerY + 96, 200, 20)
                .build());
    }

    private void connect(String ip, String name) {
        ServerInfo info = new ServerInfo(name, ip, ServerInfo.ServerType.OTHER);
        ConnectScreen.connect(this.parent, this.client, ServerAddress.parse(ip), info, false, null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
