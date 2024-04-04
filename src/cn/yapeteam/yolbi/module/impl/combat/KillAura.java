package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.Priority;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.event.impl.player.EventPostMotion;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Deprecated
@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, key = Keyboard.KEY_R)
public class KillAura extends Module {
    public KillAura() {
        min.setCallback((oldV, newV) -> newV > max.getValue() ? oldV : newV);
        max.setCallback((oldV, newV) -> newV < min.getValue() ? oldV : newV);
        addValues(range, min, max);
    }

    @Getter
    private Entity target = null;
    private final NumberValue<Float> range = new NumberValue<>("range", 3f, 1f, 6f, 0.1f);
    private final NumberValue<Integer> min = new NumberValue<>("min", 10, 0, 100, 1);
    private final NumberValue<Integer> max = new NumberValue<>("max", 20, 0, 100, 1);

    private long delay = 0, time = 0;

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) {
            setEnabled(false);
            return;
        }
        delay = random(min.getValue(), max.getValue());
        time = System.currentTimeMillis();
    }

    @Override
    protected void onDisable() {
        target = null;
    }

    public static long random(double minCPS, double maxCPS) {
        int min = (int) minCPS, max = (int) maxCPS;
        if (maxCPS - minCPS <= 0) return min;
        return newRandom().nextInt((max - min + 1)) + min;
    }

    private static Random newRandom() {
        return new Random((long) (Math.random() * Math.random() * 114514000L));
    }

    @Listener(Priority.LOW)
    private void onMotion(EventMotion e) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (target != null && target.getDistanceToEntity(mc.thePlayer) <= range.getValue()) {
            double[] rotations = getNeededRotations(target);
            //jitter
            e.setYaw((float) rotations[0] + (newRandom().nextInt(10) - 5) / 2f);
            e.setPitch((float) rotations[1] + (newRandom().nextInt(10) - 5) / 2f);
        } else if (target == null) {
            List<Entity> entityList = new ArrayList<>(mc.theWorld.loadedEntityList);
            entityList = entityList.stream().filter(entity ->
                    entity != mc.thePlayer &&
                            entity instanceof EntityLivingBase &&
                            entity.getDistanceToEntity(mc.thePlayer) <= range.getValue()
            ).sorted(
                    Comparator.comparingInt(entity -> (int) (entity.getDistanceToEntity(mc.thePlayer) * 100))
            ).collect(Collectors.toList());
            if (!entityList.isEmpty()) target = entityList.get(0);
        } else if (target.getDistanceToEntity(mc.thePlayer) > range.getValue())
            target = null;
        if (target != null && (target.isDead || !target.isEntityAlive()))
            target = null;
    }

    @Listener
    private void onPostMotion(EventPostMotion e) {
        if (delay != 0 && System.currentTimeMillis() - time >= (1000 / delay)) {
            delay = random(min.getValue(), max.getValue());
            time = System.currentTimeMillis();
            if (target != null) {
                mc.thePlayer.swingItem();
                mc.playerController.attackEntity(mc.thePlayer, target);
            }
        }
    }

    @Listener
    private void onRender(EventRender3D e) {
        if (target != null)
            RenderUtil.drawEntityBox((EntityLivingBase) target, new Color(-1), true, true, 1, e.getPartialTicks());
    }

    public double[] getNeededRotations(Entity entityIn) {
        double d0 = entityIn.posX - mc.thePlayer.posX;
        double d1 = entityIn.posZ - mc.thePlayer.posZ;
        double d2 = entityIn.posY + entityIn.getEyeHeight() - (mc.thePlayer.getEntityBoundingBox().minY + ((Entity) mc.thePlayer).getEyeHeight());
        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        double f = (MathHelper.atan2(d1, d0) * 180.0 / Math.PI) - 90.0f;
        double f1 = (-(MathHelper.atan2(d2, d3) * 180.0 / Math.PI));
        return new double[]{f, f1};
    }

    @Override
    public String getSuffix() {
        return range.getValue() + " | " + min.getValue() + "~" + max.getValue() + " : " + delay;
    }
}
