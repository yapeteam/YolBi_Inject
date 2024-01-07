package cn.yapeteam.yolbi.notification;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.font.FontManager;
import cn.yapeteam.yolbi.font.cfont.CFontRenderer;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author yuxiangll
 * @since 2024/1/8 04:59
 * IntelliJ IDEA
 */
public class NotificationManager {
    private final ArrayList<Notification> notificationArrayList;
    private final CFontRenderer fontRenderer;

    public NotificationManager() {
        fontRenderer = YolBi.instance.getFontManager().getJelloRegular18();
        notificationArrayList = new ArrayList<>();
        YolBi.instance.getEventManager().register(this);
    }
    public void clearAll() {
        notificationArrayList.clear();
    }

    public void post(Notification notification){
        notificationArrayList.add(notification);
    }
    public void add(Notification notification){
        post(notification);
    }

    @Listener
    public void onRender(final EventRender2D event){
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int presize = notificationArrayList.size();
        for (int j=0;j<presize;j++) {
            for (int i = 0; i < notificationArrayList.size(); i++) {
                if (notificationArrayList.get(i)!=null) {
                    if (notificationArrayList.get(i).getAnimation().getProgress()>1) {
                        notificationArrayList.remove(i);
                    }
                }
            }
        }

        int index=0;
        for(Notification notification:notificationArrayList){
            switch (notification.getType()){
                case SUCCESS:
                    break;
                case FAILED:
                    break;
                case INFO:
                    break;
                case NORMAL:
                    break;
                case INIT:
                    RenderINIT(notification,index,sr);
                    break;
            }
            index++;
        }
    }


    private void RenderSUCCESS(Notification notification){
        //todo
    }
    private void RenderFAILED(Notification notification){
        //todo
    }
    private void RenderINFO(Notification notification){
        //todo
    }
    private void RenderNORMAL(Notification notification){
        //todo
    }
    private void RenderINIT(Notification notification, int index,ScaledResolution sr){
        //todo
        RenderUtil.drawBloomShadow(10,10,180,20,10,new Color(0,0,0));
//        RenderUtil.drawRect(10,
//                28,
//                10+180*(1-notification.getAnimation().getProgress()),
//                30,new Color(255,255,255).getRGB());
        float thewid = (float) (180*notification.getAnimation().getProgress());
        if (thewid <0) thewid = 0;
        RenderUtil.drawBloomShadow(10,25, thewid,5,10,new Color(255, 255, 255));
        fontRenderer.drawString(notification.getTitle(),15,15,new Color(255,255,255).getRGB());
    }





}
