package dev.scx.app.util.cache;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/// 默认缓存实现 (永久储存, 达到最大容量后移除之前的元素)
///
/// @param <K>
/// @param <V>
/// @author scx567888
/// @version 0.0.1
public class Cache<K, V> implements ICache<K, V> {

    private final Map<K, V> map;

    public Cache(int maxSize) {
        this(maxSize, false);
    }

    public Cache(int maxSize, boolean useSynchronized) {
        this(maxSize, useSynchronized, false);
    }

    /// @param maxSize         最大容量
    /// @param useSynchronized 是否使用同步
    /// @param accessOrder     使用 LRU 策略
    public Cache(int maxSize, boolean useSynchronized, boolean accessOrder) {
        if (useSynchronized) {
            this.map = Collections.synchronizedMap(new FixedSizeLinkedHashMap<>(maxSize, 16, 0.75f, accessOrder));
        } else {
            this.map = new FixedSizeLinkedHashMap<>(maxSize, 16, 0.75f, accessOrder);
        }
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(K key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

}
