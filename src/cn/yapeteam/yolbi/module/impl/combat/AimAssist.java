package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.ModeValue;
import cn.yapeteam.loader.api.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.misc.TimerUtil;
import cn.yapeteam.yolbi.utils.player.FixedRotations;
import cn.yapeteam.yolbi.utils.player.RotationsUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Comparator;

@ModuleInfo(name = "AimAssist", category = ModuleCategory.COMBAT)
public class AimAssist extends Module {

    private final ModeValue<String> filter = new ModeValue<>("Filter", "Range", "Range", "Health");
    private final NumberValue<Double> range = new NumberValue<>("Range", 4.5, 3.0, 8.0, 0.1);

    private final NumberValue<Integer> speed = new NumberValue<>("Speed", 10, 1, 40, 1);

    private final TimerUtil timer = new TimerUtil();

    private FixedRotations rotations;

    public AimAssist() {
        this.addValues(filter, range, speed);
    }

    @Override
    public void onEnable() {
        rotations = new FixedRotations(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    @Listener
    public void onRender(EventRender2D event) {
        EntityPlayer target = findTarget();

        if (target != null && Mouse.isButtonDown(0) && mc.currentScreen == null) {
            float[] rots = RotationsUtil.getRotationsToEntity(target, false);

            float yaw = rots[0];
            float currentYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);

            float diff = Math.abs(currentYaw - yaw);

            if (diff >= 4 && diff <= 356) {
                float aa;

                if (diff <= speed.getValue()) {
                    aa = diff * 0.9F;
                } else {
                    aa = (float) (speed.getValue() - Math.random() * 0.5F);
                }

                float finalSpeed = aa * Math.max(timer.getTimeElapsed(), 1) * 0.01F;

                if (diff <= 180) {
                    if (currentYaw > yaw) {
                        mc.thePlayer.rotationYaw -= finalSpeed;
                    } else {
                        mc.thePlayer.rotationYaw += finalSpeed;
                    }
                } else {
                    if (currentYaw > yaw) {
                        mc.thePlayer.rotationYaw += finalSpeed;
                    } else {
                        mc.thePlayer.rotationYaw -= finalSpeed;
                    }
                }
            }
        }

        rotations.updateRotations(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

        mc.thePlayer.rotationYaw = rotations.getYaw();
        mc.thePlayer.rotationPitch = rotations.getPitch();

        timer.reset();
    }

    public EntityPlayer findTarget() {
        ArrayList<EntityPlayer> entities = new ArrayList<>();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                EntityPlayer player = (EntityPlayer) entity;

                if (player.getDistanceToEntity(mc.thePlayer) <= range.getValue() && canAttackEntity(player))
                    entities.add(player);
            }
        }

        if (!entities.isEmpty()) {
            switch (filter.getValue()) {
                case "Range":
                    entities.sort(Comparator.comparingDouble(entity -> entity.getDistanceToEntity(mc.thePlayer)));
                    break;
                case "Health":
                    entities.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                    break;
            }

            return entities.get(0);
        }

        return null;
    }

    private boolean canAttackEntity(EntityPlayer player) {
        return true;
    }
}
