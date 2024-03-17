package cn.yapeteam.yolbi.utils.player;

import cn.yapeteam.yolbi.utils.IMinecraft;
import net.minecraft.util.BlockPos;

public class RotationsUtil implements IMinecraft {

    public static float[] getRotationsToPosition(double x, double y, double z) {
        double deltaX = x - mc.thePlayer.posX;
        double deltaY = y - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
        double deltaZ = z - mc.thePlayer.posZ;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(-Math.atan2(deltaX, deltaZ));
        float pitch = (float) Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));

        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsToBlockPos(BlockPos pos) {
        return getRotationsToPosition(pos.getX(), pos.getY(), pos.getZ());
    }
}