/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.map.rendering;

import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.rendering.textures.Texture;
import com.wynntils.core.utils.objects.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class PointRenderer {

    public static void drawTexturedLines(Texture texture, List<Location> points, float width) {
        Location toCompare = points.get(0);
        for(Location loc : points) {
            Vec3d start = new Vec3d((int)loc.x, loc.y-.1f, (int)loc.z);
            Vec3d end = new Vec3d((int)toCompare.x, toCompare.y+1f, (int)toCompare.z);

            drawTexturedLine(texture, start, end, width);

            toCompare = loc;
        }
    }

    public static void drawTexturedLine(Texture texture, Vec3d start, Vec3d end, float width) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        //TODO make this vector actually point to the next one correctly
        //direction
        Vec3d normal = start.crossProduct(end).normalize();
        Vec3d scaled = normal.scale(width);

        //we need 4 points for rendering
        Vec3d p1 = start.add(scaled);
        Vec3d p2 = start.subtract(scaled);
        Vec3d p3 = end.add(scaled);
        Vec3d p4 = end.subtract(scaled);

        GlStateManager.color(1f, 1f, 1f, 1f);
        texture.bind();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        { buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            buffer.pos(p1.x - renderManager.viewerPosX, p1.y - renderManager.viewerPosY, p1.z - renderManager.viewerPosZ).tex(0f, 0f).endVertex();
            buffer.pos(p3.x - renderManager.viewerPosX, p3.y - renderManager.viewerPosY, p3.z - renderManager.viewerPosZ).tex(1f, 0f).endVertex();
            buffer.pos(p4.x - renderManager.viewerPosX, p4.y - renderManager.viewerPosY, p4.z - renderManager.viewerPosZ).tex(1f, 1f).endVertex();
            buffer.pos(p2.x - renderManager.viewerPosX, p2.y - renderManager.viewerPosY, p2.z - renderManager.viewerPosZ).tex(0f, 1f).endVertex();

        } tess.draw();
    }

    public static void drawLines(List<Location> locations, CustomColor color) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        GlStateManager.pushMatrix();

        GlStateManager.glLineWidth(3f);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Location toCompare = locations.get(0);
        for(Location loc : locations) {

            Vec3d start1 = new Vec3d(loc.toBlockPos());
            Vec3d end1 = new Vec3d(toCompare.toBlockPos());

            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR); {
                buffer.pos(
                        (loc.getX()) - renderManager.viewerPosX,
                        (loc.getY()) - renderManager.viewerPosY,
                        (loc.getZ()) - renderManager.viewerPosZ)
                        .color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(
                        (toCompare.getX()) - renderManager.viewerPosX,
                        (toCompare.getY()) - renderManager.viewerPosY,
                        (toCompare.getZ()) - renderManager.viewerPosZ)
                        .color(color.r, color.g, color.b, color.a).endVertex();
            } tess.draw();

            toCompare = loc;
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

    public static void drawCube(BlockPos point, CustomColor color) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        Location pointLocation = new Location(point);
        Location c = new Location(
            pointLocation.x - renderManager.viewerPosX,
            pointLocation.y - renderManager.viewerPosY,
            pointLocation.z - renderManager.viewerPosZ
        );

        GlStateManager.pushMatrix();

        GlStateManager.glLineWidth(3f);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderGlobal.drawBoundingBox(c.getX(), c.getY(), c.getZ(), c.getX()+1, c.getY()+1, c.getZ()+1, color.r, color.g, color.b, color.a);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

}
