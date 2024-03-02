package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Overwrite;
import cn.yapeteam.loader.mixin.annotations.Shadow;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.player.EventJump;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * @author yuxiangll
 * @since 2024/1/7 21:39
 * IntelliJ IDEA
 */
@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase extends EntityLivingBase {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Shadow
    public float getJumpUpwardsMotion() {
        return 0.42F;
    }

    @Shadow
    public boolean isPotionActive(Potion potionIn) {
        return false;
    }

    @Shadow
    public PotionEffect getActivePotionEffect(Potion potionIn) {
        return null;
    }

    @Override
    public ItemStack getHeldItem() {
        return null;
    }

    @Override
    public ItemStack getEquipmentInSlot(int i) {
        return null;
    }

    @Override
    public ItemStack getCurrentArmor(int i) {
        return null;
    }

    @Override
    public void setCurrentItemOrArmor(int i, ItemStack itemStack) {
    }

    @Override
    public ItemStack[] getInventory() {
        return new ItemStack[0];
    }

    @Shadow
    public float rotationYaw;

    @Shadow
    public boolean isSprinting() {
        return false;
    }

    @Shadow
    public double motionX;
    @Shadow
    public double motionY;
    @Shadow
    public double motionZ;
    @Shadow
    public boolean isAirBorne;

    @Overwrite(
            method = "jump",
            desc = "()V"
    )
    protected void jump() {
        double jumpY = this.getJumpUpwardsMotion();

        if (this.isPotionActive(Potion.jump)) {
            jumpY += (float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }

        EventJump event = new EventJump(jumpY, this.rotationYaw, this.isSprinting());
        //noinspection ConstantValue
        if ((EntityLivingBase) this == Minecraft.getMinecraft().thePlayer)
            YolBi.instance.getEventManager().post(event);

        this.motionY = event.getMotionY();

        if (event.isBoosting()) {
            float f = event.getYaw() * 0.017453292F;
            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }

        this.isAirBorne = true;
    }
}
