package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.BooleanValue;
import cn.yapeteam.loader.api.module.values.impl.NumberValue;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoop;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.player.EventPostMotion;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.network.DelayedPacket;
import cn.yapeteam.yolbi.utils.player.PendingVelocity;
import cn.yapeteam.yolbi.utils.reflect.ReflectUtil;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@ModuleInfo(name = "Backtrack", category = ModuleCategory.COMBAT)
public class Backtrack extends Module {
    private final NumberValue<Integer> delay = new NumberValue<>("Delay", 500, 100, 2000, 50);
    private final BooleanValue delayPing = new BooleanValue("Delay ping", true);
    private final BooleanValue delayVelocity = new BooleanValue("Delay velocity", delayPing::getValue, true);

    private final CopyOnWriteArrayList<DelayedPacket> delayedPackets = new CopyOnWriteArrayList<>();

    private EntityLivingBase lastTarget;

    private PendingVelocity lastVelocity;
    public Field S14PacketEntity$posX = ReflectUtil.getField(S14PacketEntity.class, "posX"),
            S14PacketEntity$posY = ReflectUtil.getField(S14PacketEntity.class, "posY"),
            S14PacketEntity$posZ = ReflectUtil.getField(S14PacketEntity.class, "posZ"),
            S14PacketEntity$yaw = ReflectUtil.getField(S14PacketEntity.class, "yaw"),
            S14PacketEntity$pitch = ReflectUtil.getField(S14PacketEntity.class, "pitch"),
            NetHandlerPlayClient$clientWorldController = ReflectUtil.getField(NetHandlerPlayClient.class, "clientWorldController");

    public Backtrack() {
        this.addValues(delay, delayPing, delayVelocity);
    }

    private VirtualEntity virtualEntity = null;

    @Listener
    public void onReceive(EventPacketReceive event) {
        try {
            if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5) {
                if (!delayedPackets.isEmpty())
                    delayedPackets.clear();
            }

            EntityLivingBase currentTarget = getCurrentTarget();

            if (currentTarget != lastTarget) {
                clearPackets();
                if (currentTarget != null) {
                    virtualEntity = new VirtualEntity(currentTarget);
                    YolBi.instance.getEventManager().register(virtualEntity);
                }
            }
            if (currentTarget == null) {
                clearPackets();
                YolBi.instance.getEventManager().unregister(virtualEntity);
                virtualEntity = null;
            } else {
                if (event.getPacket() instanceof S14PacketEntity) {
                    S14PacketEntity packet = event.getPacket();

                    if (packet.getEntity(ReflectUtil.getField(NetHandlerPlayClient$clientWorldController, mc.getNetHandler())) == currentTarget) {
                        byte px = ReflectUtil.getField(S14PacketEntity$posX, packet),
                                py = ReflectUtil.getField(S14PacketEntity$posY, packet),
                                pz = ReflectUtil.getField(S14PacketEntity$posZ, packet);

                        int x = currentTarget.serverPosX + px;
                        int y = currentTarget.serverPosY + py;
                        int z = currentTarget.serverPosZ + pz;

                        double posX = (double) x / 32.0D;
                        double posY = (double) y / 32.0D;
                        double posZ = (double) z / 32.0D;

                        event.setCancelled(true);
                        delayedPackets.add(new DelayedPacket(packet));
                        if (virtualEntity != null) virtualEntity.handleVirtualMovement(posX, posY, posZ);
                    }
                } else if (event.getPacket() instanceof S18PacketEntityTeleport) {
                    S18PacketEntityTeleport packet = event.getPacket();

                    if (packet.getEntityId() == currentTarget.getEntityId()) {
                        double serverX = packet.getX();
                        double serverY = packet.getY();
                        double serverZ = packet.getZ();

                        event.setCancelled(true);
                        delayedPackets.add(new DelayedPacket(packet));
                        if (virtualEntity != null) virtualEntity.handleVirtualTeleport(serverX, serverY, serverZ);
                    }
                } else if (event.getPacket() instanceof S32PacketConfirmTransaction || event.getPacket() instanceof S00PacketKeepAlive) {
                    if (!delayedPackets.isEmpty() && delayPing.getValue()) {
                        event.setCancelled(true);
                        delayedPackets.add(new DelayedPacket(event.getPacket()));
                    }
                } else if (event.getPacket() instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity packet = event.getPacket();

                    if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                        if (!delayedPackets.isEmpty() && delayPing.getValue() && delayVelocity.getValue()) {
                            event.setCancelled(true);
                            lastVelocity = new PendingVelocity(packet.getMotionX() / 8000.0, packet.getMotionY() / 8000.0, packet.getMotionZ() / 8000.0);
                        }
                    }
                }
            }

            lastTarget = currentTarget;
        } catch (Throwable e) {
            Logger.exception(e);
        }
    }

    @Listener
    private void onRender(EventRender3D e) {
        if (virtualEntity != null)
            RenderUtil.drawEntityBox(
                    virtualEntity.getEntityBoundingBox(), virtualEntity.cacheX, virtualEntity.cacheY, virtualEntity.cacheZ, virtualEntity.cacheX, virtualEntity.cacheY, virtualEntity.cacheZ,
                    new Color(-1), true, true, 1, e.getPartialTicks()
            );
    }

    @Listener
    public void onPostMotion(EventPostMotion event) {
        updatePackets();
    }

    public EntityLivingBase getCurrentTarget() {
        if (mc.theWorld == null) return null;
        List<Entity> entityList = new ArrayList<>(mc.theWorld.loadedEntityList);
        entityList = entityList.stream().filter(entity ->
                entity != mc.thePlayer &&
                        entity instanceof EntityLivingBase &&
                        entity.getDistanceToEntity(mc.thePlayer) <= 6
        ).sorted(
                Comparator.comparingInt(entity -> (int) (entity.getDistanceToEntity(mc.thePlayer) * 100))
        ).collect(Collectors.toList());
        if (!entityList.isEmpty()) return (EntityLivingBase) entityList.get(0);
        return null;
    }

    public void updatePackets() {
        if (!delayedPackets.isEmpty()) {
            for (int i = 0; i < delayedPackets.size(); i++) {
                DelayedPacket p = delayedPackets.get(i);
                if (p.getTimer().getTimeElapsed() >= delay.getValue()) {
                    handlePacket(p.getPacket());
                    if (lastVelocity != null) {
                        mc.thePlayer.motionX = lastVelocity.getX();
                        mc.thePlayer.motionY = lastVelocity.getY();
                        mc.thePlayer.motionZ = lastVelocity.getZ();
                        lastVelocity = null;
                    }
                    delayedPackets.remove(i);
                    i--;
                }
            }
        }
    }

    public void clearPackets() {
        if (lastVelocity != null) {
            mc.thePlayer.motionX = lastVelocity.getX();
            mc.thePlayer.motionY = lastVelocity.getY();
            mc.thePlayer.motionZ = lastVelocity.getZ();
            lastVelocity = null;
        }

        if (!delayedPackets.isEmpty()) {
            for (DelayedPacket p : delayedPackets)
                handlePacket(p.getPacket());
            delayedPackets.clear();
        }
    }

    public void handlePacket(Packet<INetHandlerPlayClient> packet) {
        if (packet instanceof S14PacketEntity) {
            handleEntityMovement((S14PacketEntity) packet);
        } else if (packet instanceof S18PacketEntityTeleport) {
            handleEntityTeleport((S18PacketEntityTeleport) packet);
        } else if (packet instanceof S32PacketConfirmTransaction) {
            handleConfirmTransaction((S32PacketConfirmTransaction) packet);
        } else if (packet instanceof S00PacketKeepAlive) {
            mc.getNetHandler().handleKeepAlive((S00PacketKeepAlive) packet);
        }
    }

    public void handleEntityMovement(S14PacketEntity packetIn) {
        Entity entity = packetIn.getEntity(ReflectUtil.getField(NetHandlerPlayClient$clientWorldController, mc.getNetHandler()));

        if (entity != null) {
            byte posX = ReflectUtil.getField(S14PacketEntity$posX, packetIn),
                    posY = ReflectUtil.getField(S14PacketEntity$posY, packetIn),
                    posZ = ReflectUtil.getField(S14PacketEntity$posZ, packetIn);
            entity.serverPosX += posX;
            entity.serverPosY += posY;
            entity.serverPosZ += posZ;
            byte yaw = ReflectUtil.getField(S14PacketEntity$yaw, packetIn);
            byte pitch = ReflectUtil.getField(S14PacketEntity$pitch, packetIn);
            double d0 = (double) entity.serverPosX / 32.0D;
            double d1 = (double) entity.serverPosY / 32.0D;
            double d2 = (double) entity.serverPosZ / 32.0D;
            float f = packetIn.func_149060_h() ? (float) (yaw * 360) / 256.0F : entity.rotationYaw;
            float f1 = packetIn.func_149060_h() ? (float) (pitch * 360) / 256.0F : entity.rotationPitch;
            entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, false);
            entity.onGround = packetIn.getOnGround();
        }
    }

    public void handleEntityTeleport(S18PacketEntityTeleport packetIn) {
        Entity entity = ReflectUtil.<WorldClient>getField(NetHandlerPlayClient$clientWorldController, mc.getNetHandler()).getEntityByID(packetIn.getEntityId());

        if (entity != null) {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            double d0 = (double) entity.serverPosX / 32.0D;
            double d1 = (double) entity.serverPosY / 32.0D;
            double d2 = (double) entity.serverPosZ / 32.0D;
            float f = (float) (packetIn.getYaw() * 360) / 256.0F;
            float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;

            if (Math.abs(entity.posX - d0) < 0.03125D && Math.abs(entity.posY - d1) < 0.015625D && Math.abs(entity.posZ - d2) < 0.03125D) {
                entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, f, f1, 3, true);
            } else {
                entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, true);
            }

            entity.onGround = packetIn.getOnGround();
        }
    }

    public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn) {
        Container container = null;
        EntityPlayer entityplayer = mc.thePlayer;

        if (packetIn.getWindowId() == 0) {
            container = entityplayer.inventoryContainer;
        } else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
            container = entityplayer.openContainer;
        }

        if (container != null && !packetIn.func_148888_e()) {
            mc.getNetHandler().addToSendQueue(new C0FPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
        }
    }

    private static class VirtualEntity {
        private double posX, posY, posZ;
        private double cacheX, cacheY, cacheZ;
        private final float width, height;

        public VirtualEntity(Entity entity) {
            cacheX = this.posX = entity.posX;
            cacheY = this.posY = entity.posY;
            cacheZ = this.posZ = entity.posZ;
            this.width = entity.width;
            this.height = entity.height;
        }

        public void handleVirtualMovement(double posX, double posY, double posZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }

        public void handleVirtualTeleport(double serverPosX, double serverPosY, double serverPosZ) {
            double d0 = serverPosX / 32.0D;
            double d1 = serverPosY / 32.0D;
            double d2 = serverPosZ / 32.0D;

            if (!(Math.abs(posX - d0) < 0.03125D && Math.abs(posY - d1) < 0.015625D && Math.abs(posZ - d2) < 0.03125D)) {
                posX = d0;
                posY = d1;
                posZ = d2;
            }
        }

        @Listener
        private void onUpdate(EventLoop e) {
            cacheX += (posX - cacheX) * 0.2;
            cacheY += (posY - cacheY) * 0.2;
            cacheZ += (posZ - cacheZ) * 0.2;
        }

        public AxisAlignedBB getEntityBoundingBox() {
            float f = this.width / 2.0F;
            return new AxisAlignedBB(cacheX - (double) f, cacheY, cacheZ - (double) f, cacheX + (double) f, cacheY + (double) this.height, cacheZ + (double) f);
        }
    }

    public boolean isDelaying() {
        return this.isEnabled() && !delayedPackets.isEmpty();
    }
}
