package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, key = Keyboard.KEY_R)
public class KillAura extends Module {
    public KillAura() {
        min.setCallback((oldV, newV) -> newV > max.getValue() ? oldV : newV);
        max.setCallback((oldV, newV) -> newV < min.getValue() ? oldV : newV);
        addValues(range, min, max);
    }

    private Entity target = null;
    private final NumberValue<Double> range = new NumberValue<>("range", 3d, 1d, 6d, 0.1d);
    private final NumberValue<Integer> min = new NumberValue<>("min", 10, 0, 100, 1);
    private final NumberValue<Integer> max = new NumberValue<>("max", 20, 0, 100, 1);

    private long delay = 0, tim = 0;

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) {
            setEnabled(false);
            return;
        }
        delay = random(min.getValue(), max.getValue());
        tim = System.currentTimeMillis();
        rotationTimer = System.currentTimeMillis();
        rotate = new double[2];
        rotate[0] = mc.thePlayer.rotationYaw;
        rotate[1] = mc.thePlayer.rotationPitch;
        yaw = mc.thePlayer.rotationYaw;
        pitch = mc.thePlayer.rotationPitch;
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
    private float yaw, pitch;

    @Listener
    private void onUpdate(EventUpdate e) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (target != null && target.getDistanceToEntity(mc.thePlayer) <= range.getValue()) {
            if (System.currentTimeMillis() - rotationTimer >= 500) {
                rotate = getNeededRotations(target);
                rotate[0] += (newRandom().nextInt(10) - 5) / 2f;
                rotate[1] += (newRandom().nextInt(10) - 5) / 2f;
            }
            yaw += (float) (rotate[0] - yaw) / 1.2f;
            pitch += (float) (rotate[1] - pitch) / 1.2f;
            if (System.currentTimeMillis() - tim >= (1000 / delay)) {
                delay = random(min.getValue(), max.getValue());
                tim = System.currentTimeMillis();
                mc.thePlayer.swingItem();
                mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(mc.objectMouseOver.entityHit, C02PacketUseEntity.Action.ATTACK));
            }
        } else if (target == null) {
            List<Entity> entityList = new ArrayList<>(mc.theWorld.loadedEntityList);
            entityList = entityList.stream().filter(entity -> entity != mc.thePlayer && entity instanceof EntityLivingBase && entity.getDistanceToEntity(mc.thePlayer) <= range.getValue()).sorted(Comparator.comparingInt(entity -> (int) entity.getDistanceToEntity(mc.thePlayer))).collect(Collectors.toList());
            if (!entityList.isEmpty()) target = entityList.get(0);
        } else if (target.getDistanceToEntity(mc.thePlayer) > range.getValue()) target = null;
        if (target != null && (target.isDead || !target.isEntityAlive())) target = null;
    }

    @Listener
    private void onMotion(EventMotion e) {
        if (target != null) {
            e.setYaw(yaw);
            e.setPitch(pitch);
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
