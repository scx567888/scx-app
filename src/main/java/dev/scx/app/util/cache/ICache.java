package dev.scx.app.util.cache;

import java.util.function.Function;

/// 缓存接口
///
/// @author scx567888
/// @version 0.0.1
public interface ICache<K, V> {

    V get(K key);

    V put(K key, V value);

    V remove(K key);

    void clear();

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

}
