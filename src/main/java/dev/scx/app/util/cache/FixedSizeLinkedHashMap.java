package dev.scx.app.util.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/// 固定大小的 LinkedHashMap
///
/// @param <K> a
/// @param <V> a
/// @author scx567888
/// @version 0.0.1
public final class FixedSizeLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    /// 最大容量
    private final int maxSize;

    public FixedSizeLinkedHashMap(int maxSize, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.maxSize = maxSize;
    }

    public FixedSizeLinkedHashMap(int maxSize, int initialCapacity) {
        super(initialCapacity);
        this.maxSize = maxSize;
    }

    public FixedSizeLinkedHashMap(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    public FixedSizeLinkedHashMap(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

}
