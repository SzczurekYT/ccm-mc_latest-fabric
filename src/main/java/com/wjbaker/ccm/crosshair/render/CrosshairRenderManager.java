package com.wjbaker.ccm.crosshair.render;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wjbaker.ccm.crosshair.CustomCrosshair;
import com.wjbaker.ccm.crosshair.style.CrosshairStyle;
import com.wjbaker.ccm.crosshair.style.CrosshairStyleFactory;
import com.wjbaker.ccm.render.RenderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.util.Set;

public final class CrosshairRenderManager {

    private final CustomCrosshair crosshair;
    private final RenderManager renderManager;
    private final CrosshairStyleFactory crosshairStyleFactory;

    private final Set<Item> itemCooldownItems = ImmutableSet.of(
        Items.ENDER_PEARL,
        Items.CHORUS_FRUIT
    );

    public CrosshairRenderManager(final CustomCrosshair crosshair) {
        this.crosshair = crosshair;
        this.renderManager = new RenderManager();
        this.crosshairStyleFactory = new CrosshairStyleFactory();
    }

    public void draw(final MatrixStack matrixStack, final int x, final int y) {
        var computedProperties = new ComputedProperties(this.crosshair);

        if (!computedProperties.isVisible())
            return;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var style = this.crosshairStyleFactory.from(matrixStack, this.crosshair.style.get(), this.crosshair);
        var isItemCooldownEnabled = this.crosshair.isItemCooldownEnabled.get();
        var isDotEnabled = this.crosshair.isDotEnabled.get();

        if (isItemCooldownEnabled)
            this.drawItemCooldownIndicator(matrixStack, computedProperties, x, y);

        if (isDotEnabled && this.crosshair.style.get() != CrosshairStyle.DEFAULT)
            this.renderManager.drawCircle(matrixStack, x, y, 0.5F, 1.0F, this.crosshair.dotColour.get());

        this.preTransformation(matrixStack, x, y);

        style.draw(0, 0, computedProperties);

        this.postTransformation(matrixStack);
    }

    private void preTransformation(final MatrixStack matrixStack, final int x, final int y) {
        var rotation = this.crosshair.rotation.get();
        var scale = this.crosshair.scale.get() - 2;
        var windowScaling = (float)MinecraftClient.getInstance().getWindow().getScaleFactor() / 2.0F;

        matrixStack.push();
        matrixStack.translate(x, y, 0.0D);
        matrixStack.scale(scale / 100.0F / windowScaling, scale / 100.0F / windowScaling, 1.0F);
        matrixStack.multiply(new Quaternion(Vec3f.POSITIVE_Z, rotation, true));

        RenderSystem.applyModelViewMatrix();
    }

    private void postTransformation(final MatrixStack matrixStack) {
        matrixStack.pop();
    }

    private void drawItemCooldownIndicator(
        final MatrixStack matrixStack,
        final ComputedProperties computedProperties,
        final int x, final int y) {

        var player = MinecraftClient.getInstance().player;

        if (player == null)
            return;

        var colour = this.crosshair.itemCooldownColour.get();

        var width = this.crosshair.width.get();
        var height = this.crosshair.height.get();
        var maxSize = Math.max(width, height);
        var offset = 3;

        for (final Item item : this.itemCooldownItems) {
            var cooldown = player.getItemCooldownManager().getCooldownProgress(item, 0.0F);

            if (cooldown == 0.0F)
                continue;

            var progress = Math.round(360 - (360 * cooldown));

            this.renderManager.drawPartialCircle(
                matrixStack,
                x, y,
                computedProperties.gap() + maxSize + offset,
                0,
                progress,
                2.0F,
                colour);

            offset += 3;
        }
    }
}
