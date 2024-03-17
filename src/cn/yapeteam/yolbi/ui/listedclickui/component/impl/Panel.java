package cn.yapeteam.yolbi.ui.listedclickui.component.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.shader.impl.ShaderRoundedRect;
import cn.yapeteam.yolbi.ui.listedclickui.ImplScreen;
import cn.yapeteam.yolbi.ui.listedclickui.component.AbstractComponent;
import cn.yapeteam.yolbi.ui.listedclickui.component.Limitation;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import lombok.Getter;

import java.awt.*;

/**
 * @author TIMER_err
 */
public class Panel extends AbstractComponent {
    @Getter
    private final ModuleCategory category;
    @Getter
    private final ImplScreen screenIn;

    public Panel(ImplScreen screenIn, ModuleCategory category) {
        super(null);
        this.category = category;
        this.screenIn = screenIn;
    }

    @Override
    public void init() {
        getChildComponents().clear();
        float y = getY() + ImplScreen.panelTopHeight;
        for (Module module : YolBi.instance.getModuleManager().getModulesByCategory(category)) {
            ModuleButton moduleButton = new ModuleButton(this, module);
            moduleButton.setX(getX());
            moduleButton.setY(y);
            moduleButton.setWidth(getWidth());
            moduleButton.setHeight(ImplScreen.moduleHeight);
            getChildComponents().add(moduleButton);
            y += ImplScreen.moduleHeight + ImplScreen.moduleSpacing;
        }
        super.init();
    }

    @Override
    public void update() {
        float y = getY() + ImplScreen.panelTopHeight + scrollCache, ry = getY() + ImplScreen.panelTopHeight + scroll;
        for (AbstractComponent component : getChildComponents())
            if (component instanceof ModuleButton) {
                ModuleButton moduleButton = (ModuleButton) component;
                moduleButton.setX(getX());
                moduleButton.setY(y);
                moduleButton.setRealY(ry);
                y += ImplScreen.moduleHeight + ImplScreen.moduleSpacing + moduleButton.getExtend();
                ry += ImplScreen.moduleHeight + ImplScreen.moduleSpacing + moduleButton.getExtend();
            }
        scrollCache += (scroll - scrollCache) / 5f;

        float height = 0;
        for (AbstractComponent childComponent : getChildComponents()) {
            height += childComponent.getHeight() + ImplScreen.moduleSpacing;
            if (((ModuleButton) childComponent).isExtended()) {
                for (AbstractComponent component : childComponent.getChildComponents()) {
                    if (!(component instanceof ValueButton && !((ValueButton) component).getValue().getVisibility().get()))
                        height += component.getHeight() + ImplScreen.valueSpacing;

                }
                height -= ImplScreen.valueSpacing;
            }
        }
        height -= ImplScreen.moduleSpacing;
        setHeight(ImplScreen.panelTopHeight + Math.min(height, ImplScreen.panelMaxHeight));

        getChildComponents().forEach(AbstractComponent::update);
    }

    @Getter
    private float scroll = 0, scrollCache = 0;

    private final ShaderRoundedRect roundedRect = new ShaderRoundedRect(3, true, true);

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks, Limitation ignored) {
        if (isDragging()) {
            setX(mouseX - dragX);
            setY(mouseY - dragY);
        }
        if (!getChildComponents().isEmpty()) {
            float allExpand = 0;
            for (AbstractComponent component : getChildComponents())
                if (component instanceof ModuleButton)
                    allExpand += ((ModuleButton) component).getExtend();
            if (this.scroll > 0 || -this.scroll > getChildComponents().size() * (ImplScreen.moduleHeight + ImplScreen.moduleSpacing) - ImplScreen.moduleSpacing + allExpand - (getHeight() - ImplScreen.panelTopHeight))
                this.scroll = 0;
            if (isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY)) {
                float scroll = scroll();
                this.scroll += scroll;
                setWheel(0);
            }
            getChildComponents().forEach(AbstractComponent::update);
        }
        RenderUtil.drawBloomShadow(getX(), getY(), getWidth(), getHeight(), 5, new Color(0), false);
        roundedRect.setWidth(getWidth());
        roundedRect.setHeight(getHeight());
        roundedRect.setColor(ImplScreen.MainTheme[0].getRGB());
        roundedRect.setRadius(3);
        roundedRect.render(getX(), getY(), -1);
        if (!getChildComponents().isEmpty())
            RenderUtil.drawRect(getX(), getY() + ImplScreen.panelTopHeight - 0.5f, getX() + getWidth(), getY() + ImplScreen.panelTopHeight, new Color(210, 210, 210, 84).getRGB());
        AbstractFontRenderer font = YolBi.instance.getFontManager().getPingFangBold18();
        font.drawString(
                category.name(),
                getX() + (getWidth() - font.getStringWidth(category.name())) / 2f,
                getY() + (ImplScreen.panelTopHeight - font.getStringHeight(category.name())) / 2f,
                -1
        );
        Limitation limitation = new Limitation(getX(), getY() + ImplScreen.panelTopHeight, getWidth(), getHeight() - ImplScreen.panelTopHeight);
        limitation.start(() -> {
            RenderUtil.drawFastRoundedRect(getX(), getY() + ImplScreen.panelTopHeight, getX() + getWidth(), getY() + getHeight(), 3, -1);
            RenderUtil.drawRect2(getX(), getY() + ImplScreen.panelTopHeight, getWidth(), 3, -1);
        });
        super.drawComponent(mouseX, mouseY, partialTicks, limitation);
        limitation.end();
    }

    private float scroll() {
        float scroll = 0;
        if (getWheel() > 0) scroll = 20;
        if (getWheel() < 0) scroll = -20;
        if (((ModuleButton) getChildComponents().get(getChildComponents().size() - 1)).getRealY() + ImplScreen.moduleHeight + ((ModuleButton) getChildComponents().get(getChildComponents().size() - 1)).getExtend() + scroll < getY() + getHeight())
            scroll = -(((ModuleButton) getChildComponents().get(getChildComponents().size() - 1)).getRealY() + ImplScreen.moduleHeight + ((ModuleButton) getChildComponents().get(getChildComponents().size() - 1)).getExtend() - getY() - getHeight());
        if (((ModuleButton) getChildComponents().get(0)).getRealY() + scroll > getY() + ImplScreen.panelTopHeight)
            scroll = -(getChildComponents().get(0).getY() - (getY() + ImplScreen.panelTopHeight));
        return scroll;
    }

    public boolean isCurrent() {
        return screenIn.getCurrentPanel() == this;
    }

    @Override
    public boolean isHovering(float mouseX, float mouseY) {
        return isHovering(getX(), getY() + ImplScreen.panelTopHeight, getWidth(), getHeight() - ImplScreen.panelTopHeight, mouseX, mouseY);
    }

    public boolean isFullHovering(float mouseX, float mouseY) {
        return isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
    }

    private float dragX = 0, dragY = 0;

    @Override
    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (!isCurrent()) return;
        if (isHovering(getX(), getY(), getWidth(), ImplScreen.panelTopHeight, mouseX, mouseY)) {
            setDragging(true);
            dragX = mouseX - getX();
            dragY = mouseY - getY();
        }
        getChildComponents().forEach(c -> c.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }
}
