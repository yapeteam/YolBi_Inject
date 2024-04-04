package cn.yapeteam.loader.api.module.values;

public interface ChangedCallback<T> {
    T run(T oldV, T newV);
}
