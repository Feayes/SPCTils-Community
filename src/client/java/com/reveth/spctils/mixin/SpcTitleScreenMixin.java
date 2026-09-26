package com.reveth.spctils.mixin;

import com.reveth.spctils.gui.SpcServersScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;

@Mixin(TitleScreen.class)
public class SpcTitleScreenMixin extends Screen {

    protected SpcTitleScreenMixin(Text title) {
        super(title);
    }

    private ButtonWidget spcButton;
    private net.minecraft.client.gui.widget.TexturedButtonWidget spcIconWidget;
    private static final Identifier SPC_ICON = Identifier.of("spctils", "spc1");

    @Inject(method = "init", at = @At("TAIL"))
    private void addSpcButton(CallbackInfo ci) {
        int spcX = this.width / 2 + 104; // Default position just in case
        int spcY = this.height / 4 + 48 + 24 * 1;

        if (this.client != null && this.client.isDemo()) {
            spcY += 24;
        }

        for (net.minecraft.client.gui.Element element : this.children()) {
            if (element instanceof ButtonWidget button) {
                if (button.getMessage().getContent() instanceof net.minecraft.text.TranslatableTextContent textContent) {
                    if (textContent.getKey().equals("menu.multiplayer")) {
                        spcY = button.getY();
                        break;
                    }
                }
            }
        }

        this.spcButton = ButtonWidget.builder(
                Text.empty(),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new SpcServersScreen(this));
                    }
                }
        ).dimensions(spcX, spcY, 20, 20).build();

        this.spcIconWidget = new net.minecraft.client.gui.widget.TexturedButtonWidget(
                spcX + 2, spcY + 2, 16, 16,
                new net.minecraft.client.gui.screen.ButtonTextures(SPC_ICON, SPC_ICON),
                button -> {},
                Text.empty()
        );

        this.addDrawableChild(this.spcButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderSpcIcon(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.spcButton != null && this.spcButton.visible && this.spcIconWidget != null) {
            this.spcIconWidget.setX(this.spcButton.getX() + 2);
            this.spcIconWidget.setY(this.spcButton.getY() + 2);
            this.spcIconWidget.render(context, mouseX, mouseY, delta);
        }
    }
}
