package cn.yapeteam.yolbi.module.impl;

import cn.yapeteam.yolbi.a_pretoload.Mapper;
import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@SuppressWarnings("RedundantCast")
@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, key = Keyboard.KEY_R)
public class KillAura extends Module {
    public KillAura() {
        try {
            clickMouse = Minecraft.class.getDeclaredMethod(Mapper.map("net/minecraft/client/Minecraft", "clickMouse", "()V", Mapper.Type.Method));
            clickMouse.setAccessible(true);
        } catch (Throwable e) {
            Logger.exception(e);
        }
        addValues(range, min, max);
    }

    private Entity target = null;
    private final NumberValue<Double> range = new NumberValue<>("range", 3d, 1d, 6d, 0.1d);
    private final NumberValue<Integer> min = new NumberValue<>("min", 10, 0, 100, 1);
    private final NumberValue<Integer> max = new NumberValue<>("max", 10, 0, 100, 1);
    private Method clickMouse = null;

    private long delay = 0, tim = 0;

    @Override
    public void onEnable() {
        delay = random(min.getValue(), max.getValue());
        tim = System.currentTimeMillis();
        rotationTimer = System.currentTimeMillis();
        rotate = new double[2];
        rotate[0] = ((Entity) mc.thePlayer).rotationYaw;
        rotate[1] = ((Entity) mc.thePlayer).rotationPitch;
    }

    @Override
    protected void onDisable() {
        target = null;
    }

    public static long random(double minCPS, double maxCPS) {
        int mi = Integer.parseInt(String.valueOf(minCPS).replace(".", "/").split("/")[0]), ma = Integer.parseInt(String.valueOf(maxCPS).replace(".", "/").split("/")[0]);
        if (maxCPS - minCPS <= 0) return mi;
        return new Random((long) (Math.random() * 1000)).nextInt((ma - mi)) + mi;
    }

    private Random newRandom() {
        return new Random((long) (Math.random() * 114514));
    }

    private long rotationTimer;
    private double[] rotate;

    @Listener
    private void onUpdate(EventUpdate e) throws Throwable {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (target != null && target.getDistanceToEntity(mc.thePlayer) <= range.getValue()) {
            if (System.currentTimeMillis() - rotationTimer >= 200)
                rotate = getNeededRotations(target);
            ((Entity) mc.thePlayer).rotationYaw += (float) (rotate[0] - ((Entity) mc.thePlayer).rotationYaw) / 1.1f;
            ((Entity) mc.thePlayer).rotationPitch += (float) (rotate[1] - ((Entity) mc.thePlayer).rotationPitch) / 1.1f;
            if (System.currentTimeMillis() - tim >= (1000 / delay)) {
                delay = random(min.getValue(), max.getValue());
                tim = System.currentTimeMillis();
                clickMouse.invoke(mc);
            }
        } else if (target == null) {
            List<Entity> entityList = new ArrayList<>(mc.theWorld.loadedEntityList);
            entityList = entityList.stream().filter(entity -> entity != mc.thePlayer && entity instanceof EntityLivingBase && entity.getDistanceToEntity(mc.thePlayer) <= range.getValue()).sorted(Comparator.comparingInt(entity -> (int) entity.getDistanceToEntity(mc.thePlayer))).collect(Collectors.toList());
            if (!entityList.isEmpty()) target = entityList.get(0);
        } else if (target.getDistanceToEntity(mc.thePlayer) > range.getValue()) target = null;
        if (target != null && (target.isDead || !target.isEntityAlive())) target = null;
    }

    @Listener
    private void onRender(EventRender3D e) {
        if (target != null)
            RenderUtil.drawEntityBox((EntityLivingBase) target, new Color(-1), true, true, 1, e.getPartialTicks());
    }

    public double[] getNeededRotations(Entity entityIn) {
        double d0 = entityIn.posX - ((Entity) mc.thePlayer).posX;
        double d1 = entityIn.posZ - ((Entity) mc.thePlayer).posZ;
        double d2 = entityIn.posY + entityIn.getEyeHeight() - (((Entity) mc.thePlayer).getEntityBoundingBox().minY + ((Entity) mc.thePlayer).getEyeHeight());
        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        double f = (MathHelper.atan2(d1, d0) * 180.0 / Math.PI) - 90.0f;
        double f1 = (-(MathHelper.atan2(d2, d3) * 180.0 / Math.PI));
        return new double[]{f, f1};
    }
}
