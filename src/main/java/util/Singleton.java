package util;

import java.util.function.Supplier;

public class Singleton<T> {
    private final Supplier<T> supplier;
    private T instance = null;

    public Singleton(Supplier<T> instance) {
        this.supplier = instance;
    }

    public T get() {
        if (instance == null) {
            instance = supplier.get();
        }
        return instance;
    }

    public void set(T instance) {
        this.instance = instance;
    }
}
