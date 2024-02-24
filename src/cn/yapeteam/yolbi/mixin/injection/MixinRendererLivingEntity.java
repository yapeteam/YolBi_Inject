package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Overwrite;
import cn.yapeteam.loader.mixin.annotations.Shadow;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.render.EventRotationsRender;
import cn.yapeteam.yolbi.utils.reflect.ReflectUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.EmissiveTextures;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import org.apache.logging.log4j.Logger;

@Mixin(RendererLivingEntity.class)
@SuppressWarnings("DataFlowIssue")
public class MixinRendererLivingEntity<T extends EntityLivingBase> extends Render<T> {
    @Shadow
    private static final Logger logger = null;
    @Shadow
    public static final boolean animateModelLiving = false;
    @Shadow
    protected boolean renderOutlines = false;
    @Shadow
    public ModelBase mainModel;
    @Shadow
    public EntityLivingBase renderEntity;
    @Shadow
    public float renderLimbSwing;
    @Shadow
    public float renderLimbSwingAmount;
    @Shadow
    public float renderAgeInTicks;
    @Shadow
    public float renderHeadYaw;
    @Shadow
    public float renderHeadPitch;
    @Shadow
    public float renderScaleFactor;
    @Shadow
    public float renderPartialTicks;
    @Shadow
    private boolean renderModelPushMatrix;

    protected MixinRendererLivingEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Shadow
    protected float getSwingProgress(T livingBase, float partialTickTime) {
        return 0;
    }

    @Shadow
    protected float interpolateRotation(float par1, float par2, float par3) {
        return 0;
    }

    @Shadow
    protected void renderLivingAt(T entityLivingBaseIn, double x, double y, double z) {
    }

    @Shadow
    protected float handleRotationFloat(T livingBase, float partialTicks) {
        return 0;
    }

    @Shadow
    protected void rotateCorpse(T bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
    }

    @Shadow
    protected void preRenderCallback(T entitylivingbaseIn, float partialTickTime) {
    }

    @Shadow
    protected boolean setScoreTeamColor(T entityLivingBaseIn) {
        return false;
    }

    @Shadow
    protected void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
    }

    @Shadow
    protected void unsetScoreTeamColor() {
    }

    @Shadow
    protected boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks) {
        return false;
    }

    @Shadow
    protected void unsetBrightness() {
    }

    @Shadow
    protected void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_) {
    }

    @Overwrite(method = "doRender", desc = "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (ReflectUtil.hasOptifine && !Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, entity, this, x, y, z)) {
            if (animateModelLiving) {
                entity.limbSwingAmount = 1.0F;
            }

            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
            this.mainModel.isRiding = entity.isRiding();

            if (Reflector.ForgeEntity_shouldRiderSit.exists()) {
                this.mainModel.isRiding = entity.isRiding() && entity.ridingEntity != null && Reflector.callBoolean(entity.ridingEntity, Reflector.ForgeEntity_shouldRiderSit);
            }

            this.mainModel.isChild = entity.isChild();

            try {
                EventRotationsRender event = new EventRotationsRender(this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks), this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, partialTicks);

                if (entity == renderManager.livingPlayer) {
                    YolBi.instance.getEventManager().post(event);
                }

                float f = event.getBodyYaw();
                float f1 = event.getYaw();

                float f2 = f1 - f;

                if (this.mainModel.isRiding && entity.ridingEntity instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase) entity.ridingEntity;
                    f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    f2 = f1 - f;
                    float f3 = MathHelper.wrapAngleTo180_float(f2);

                    if (f3 < -85.0F) {
                        f3 = -85.0F;
                    }

                    if (f3 >= 85.0F) {
                        f3 = 85.0F;
                    }

                    f = f1 - f3;

                    if (f3 * f3 > 2500.0F) {
                        f += f3 * 0.2F;
                    }

                    f2 = f1 - f;
                }

                float f7 = event.getPitch();
                this.renderLivingAt(entity, x, y, z);
                float f8 = this.handleRotationFloat(entity, partialTicks);
                this.rotateCorpse(entity, f8, f, partialTicks);
                GlStateManager.enableRescaleNormal();
                GlStateManager.scale(-1.0F, -1.0F, 1.0F);
                this.preRenderCallback(entity, partialTicks);
                float f4 = 0.0625F;
                GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
                float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

                if (entity.isChild()) {
                    f6 *= 3.0F;
                }

                if (f5 > 1.0F) {
                    f5 = 1.0F;
                }

                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
                this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625F, entity);

                if (CustomEntityModels.isActive()) {
                    this.renderEntity = entity;
                    this.renderLimbSwing = f6;
                    this.renderLimbSwingAmount = f5;
                    this.renderAgeInTicks = f8;
                    this.renderHeadYaw = f2;
                    this.renderHeadPitch = f7;
                    this.renderScaleFactor = f4;
                    this.renderPartialTicks = partialTicks;
                }

                if (this.renderOutlines) {
                    boolean flag1 = this.setScoreTeamColor(entity);
                    this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                    if (flag1) {
                        this.unsetScoreTeamColor();
                    }
                } else {
                    boolean flag = this.setDoRenderBrightness(entity, partialTicks);

                    if (EmissiveTextures.isActive()) {
                        EmissiveTextures.beginRender();
                    }

                    if (this.renderModelPushMatrix) {
                        GlStateManager.pushMatrix();
                    }

                    this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                    if (this.renderModelPushMatrix) {
                        GlStateManager.popMatrix();
                    }

                    if (EmissiveTextures.isActive()) {
                        if (EmissiveTextures.hasEmissive()) {
                            this.renderModelPushMatrix = true;
                            EmissiveTextures.beginRenderEmissive();
                            GlStateManager.pushMatrix();
                            this.renderModel(entity, f6, f5, f8, f2, f7, f4);
                            GlStateManager.popMatrix();
                            EmissiveTextures.endRenderEmissive();
                        }

                        EmissiveTextures.endRender();
                    }

                    if (flag) {
                        this.unsetBrightness();
                    }

                    GlStateManager.depthMask(true);

                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator()) {
                        this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);
                    }
                }

                if (CustomEntityModels.isActive()) {
                    this.renderEntity = null;
                }

                GlStateManager.disableRescaleNormal();
            } catch (Exception exception) {
                logger.error("Couldn't render entity", exception);
            }

            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();

            if (!this.renderOutlines) {
                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }

            if (Reflector.RenderLivingEvent_Post_Constructor.exists()) {
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, entity, this, x, y, z);
            }
        } else {
            //todo for vanilla
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(T t) {
        return null;
    }
}
