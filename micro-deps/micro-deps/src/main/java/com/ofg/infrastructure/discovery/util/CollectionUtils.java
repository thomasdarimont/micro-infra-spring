package com.ofg.infrastructure.discovery.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {

    public static <T> T find(Collection<T> collection, Predicate<T> whatToFind) {
        for (Iterator<T> iter = collection.iterator(); iter.hasNext(); ) {
            T item = iter.next();
            if (whatToFind.apply(item)) {
                return item;
            }
        }
        return null;
    }

    public static <F, T, V> Map<F, T> toMap(Collection<V> collection, Function<V, F> keyFunction, Function<V, T> valueFunction) {
        Collection<F> keys = Collections2.transform(collection, keyFunction);
        Collection<T> values = Collections2.transform(collection, valueFunction);
        LinkedList<T> valuesList = new LinkedList<>(values);
        return combineCollectionsIntoMap(keys, valuesList);
    }

    private static <F, T> Map<F, T> combineCollectionsIntoMap(Collection<F> keys, LinkedList<T> valuesList) {
        Map<F, T> map = Maps.newHashMap();
        for(F key : keys) {
            map.put(key, valuesList.pop());
        }
        return map;
    }

    public static <F, T> Map<F, T> toMap(Collection<T> collection, Function<T, F> keyFunction) {
        return toMap(collection, keyFunction, new Function<T, T>() {
            @Override
            public T apply(T input) {
                return input;
            }
        });
    }

    public static <T> Set<T> toSet(Collection<T> collection) {
        Set<T> answer = new HashSet<T>(collection.size());
        answer.addAll(collection);
        return answer;
    }

    public static <T> List<T> flatten(Collection<?> list, Class<T> type) {
        List<Object> retVal = new ArrayList<Object>();
        flatten(list, retVal);
        return (List<T>) retVal;
    }

    private static void flatten(Collection<?> fromTreeList, Collection<Object> toFlatList) {
        for (Object item : fromTreeList) {
            if (item instanceof Collection<?>) {
                flatten((Collection<?>) item, toFlatList);
            } else {
                toFlatList.add(item);
            }
        }
    }
}