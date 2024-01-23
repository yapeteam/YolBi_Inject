package cn.yapeteam.yolbi.utils.animation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EasingAnimation {
    private Easing easing;
    private long beginTime;
    private long duration;
    private double startValue, currentValue, lastTarget = Double.NaN;

    public EasingAnimation(Easing easing, long duration, double startValue) {
        this.easing = easing;
        this.duration = duration;
        this.startValue = this.currentValue = startValue;
        this.beginTime = System.currentTimeMillis();
    }

    public void resetTime() {
        beginTime = System.currentTimeMillis();
    }

    public double getValue(double targetValue) {
        if (!Double.isNaN(lastTarget) && targetValue != lastTarget) {
            startValue = lastTarget;
            resetTime();
        }
        lastTarget = targetValue;
        double progress = getProgress();
        currentValue = currentValue < targetValue ?
                startValue + (targetValue - startValue) * progress :
                startValue - (startValue - targetValue) * progress;
        if (isFinished()) currentValue = targetValue;
        return currentValue;
    }


    public double getProgress() {
        return easing.getFunction().apply((double) (System.currentTimeMillis() - beginTime) / duration);
    }

    public boolean isFinished() {
        return System.currentTimeMillis() - duration >= beginTime;
    }
}
