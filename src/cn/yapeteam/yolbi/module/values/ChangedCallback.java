package cn.yapeteam.yolbi.module.values;

public interface ChangedCallback<T> {
    T run(T oldV, T newV);
}
