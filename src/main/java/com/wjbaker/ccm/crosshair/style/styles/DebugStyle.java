package com.wjbaker.ccm.crosshair.style.styles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wjbaker.ccm.crosshair.CustomCrosshair;
import com.wjbaker.ccm.crosshair.render.ComputedProperties;
import com.wjbaker.ccm.crosshair.style.AbstractCrosshairStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class DebugStyle extends AbstractCrosshairStyle {

    public DebugStyle(final MatrixStack matrixStack, final CustomCrosshair crosshair) {
        super(matrixStack, crosshair);
    }

    @Override
    public void draw(final DrawContext drawContext, final int x, final int y, final ComputedProperties computedProperties) {
        var camera = this.mc.gameRenderer.getCamera();

        var matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw()));
        matrixStack.scale(-1, -1, -1);

        RenderSystem.applyModelViewMatrix();

        RenderSystem.renderCrosshair(10);

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }
}
