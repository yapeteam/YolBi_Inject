package cn.yapeteam.yolbi.utils.reflect;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.utils.ClassUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class ReflectUtil {
    private static Field EntityRenderer$theShaderGroup, ShaderGroup$listShaders, Minecraft$timer;
    private static Method EntityRenderer$loadShader, Minecraft$clickMouse, Minecraft$rightClickMouse;

    static {
        try {
            Minecraft$clickMouse = Minecraft.class.getDeclaredMethod(Mapper.map("net/minecraft/client/Minecraft", "clickMouse", "()V", Mapper.Type.Method));
            Minecraft$clickMouse.setAccessible(true);

            Minecraft$rightClickMouse = Minecraft.class.getDeclaredMethod(Mapper.map("net/minecraft/client/Minecraft", "rightClickMouse", "()V", Mapper.Type.Method));
            Minecraft$rightClickMouse.setAccessible(true);

            EntityRenderer$theShaderGroup = EntityRenderer.class.getDeclaredField(Mapper.map("net/minecraft/client/renderer/EntityRenderer", "theShaderGroup", null, Mapper.Type.Field));
            EntityRenderer$loadShader = EntityRenderer.class.getDeclaredMethod(Mapper.map("net/minecraft/client/renderer/EntityRenderer", "loadShader", null, Mapper.Type.Method), ResourceLocation.class);
            EntityRenderer$theShaderGroup.setAccessible(true);
            EntityRenderer$loadShader.setAccessible(true);

            ShaderGroup$listShaders = ShaderGroup.class.getDeclaredField(Mapper.map("net/minecraft/client/shader/ShaderGroup", "listShaders", null, Mapper.Type.Field));
            ShaderGroup$listShaders.setAccessible(true);

            Minecraft$timer = Minecraft.class.getDeclaredField(Mapper.map("net/minecraft/client/Minecraft", "timer", null, Mapper.Type.Field));
            Minecraft$timer.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            Logger.exception(e);
        }
    }

    public static void Minecraft$clickMouse(Minecraft minecraft) {
        try {
            Minecraft$clickMouse.invoke(minecraft);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public static void Minecraft$rightClickMouse(Minecraft minecraft) {
        try {
            Minecraft$rightClickMouse.invoke(minecraft);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public static Timer Minecraft$getTimer(Minecraft minecraft) {
        try {
            return (Timer) Minecraft$timer.get(minecraft);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return null;
    }

    public static ShaderGroup GetEntityRenderer$theShaderGroup(EntityRenderer entityRenderer) {
        try {
            return (ShaderGroup) EntityRenderer$theShaderGroup.get(entityRenderer);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return null;
    }

    public static void SetEntityRenderer$theShaderGroup(EntityRenderer entityRenderer, ShaderGroup theShaderGroup) {
        try {
            EntityRenderer$theShaderGroup.set(entityRenderer, theShaderGroup);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }


    public static void EntityRenderer$loadShader(EntityRenderer entityRenderer, ResourceLocation resourceLocationIn) {
        try {
            EntityRenderer$loadShader.invoke(entityRenderer, resourceLocationIn);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public static List<Shader> GetShaderGroup$listShaders(ShaderGroup shaderGroup) {
        try {
            return (List<Shader>) ShaderGroup$listShaders.get(shaderGroup);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return null;
    }

    public static void SetShaderGroup$listShaders(ShaderGroup shaderGroup, List<Shader> listShaders) {
        try {
            ShaderGroup$listShaders.set(shaderGroup, listShaders);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public static final boolean hasOptifine = ClassUtils.getClass("net.optifine.Log") != null;

    public static Field getField(Class<?> clz, String name) {
        try {
            Field field = clz.getDeclaredField(Mapper.mapFieldWithSuper(clz.getName(), name, null));
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Logger.exception(e);
        }
        return null;
    }

    public static <T> T getField(Object obj, String name) {
        try {
            return (T) Objects.requireNonNull(getField(obj.getClass(), name)).get(obj);
        } catch (IllegalAccessException e) {
            Logger.exception(e);
        }
        return null;
    }

    public static <T> T getField(Field field, Object obj) {
        try {
            Object o = field.get(obj);
            if (o != null) return (T) o;
        } catch (IllegalAccessException e) {
            throw new RuntimeException();
        }
        throw new RuntimeException();
    }
}
