package com.ofg.infrastructure.discovery.util

import com.google.common.base.Function
import com.google.common.base.Predicate
import spock.lang.Specification

class CollectionUtilsSpec extends Specification {

    def 'should return a value if exists in collection'() {
        given:
            List collection = [1, 2, 3]
        expect:
            2 == CollectionUtils.find(collection, { Integer input -> return input == 2 } as Predicate)
    }

    def 'should return null if value does not exist in collection'() {
        given:
            List collection = [1, 2, 3]
        expect:
            !CollectionUtils.find(collection, { Integer input -> return input == 4 } as Predicate)
    }

    def 'should convert a collection to a set'() {
        given:
            List collection = [1, 2, 3, 3]
        expect:
            [1,2,3] as Set == CollectionUtils.toSet(collection)
    }

    def 'should flatten a collection'() {
        given:
            List collection = [1, [2], [[3, 3]]]
        expect:
            [1, 2, 3, 3] == CollectionUtils.flatten(collection, Integer)
    }

    def 'should convert a set to a map with an identity function'() {
        given:
            Set collection = [1, 2, 3]  as Set
        when:
            Map<String, Integer> map = CollectionUtils.toMap(collection, { Integer input ->
                return "${input}a".toString()
            } as Function)
        then:
            ['1a':1, '2a':2, '3a': 3] == map
    }

    @SuppressWarnings("unchecked")
    def 'should convert a set to a map with a provided value function'() {
        given:
            Set collection = [1, 2, 3]  as Set
        when:
            Map map = CollectionUtils.toMap(
                    collection,
                    { Integer input -> "${input}a".toString() },
                    { Integer input -> return input + 1 })
        then:
            ['1a':2, '2a':3, '3a': 4] == map
    }

}
