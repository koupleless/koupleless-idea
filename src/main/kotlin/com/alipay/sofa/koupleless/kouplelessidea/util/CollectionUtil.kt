package com.alipay.sofa.koupleless.kouplelessidea.util


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/6/30 14:01
 */
object CollectionUtil {
    fun <Q,T> addOrPutList(map:MutableMap<Q,MutableList<T>>, key:Q, value:T){
        if(map.containsKey(key)){
            map[key]!!.add(value)
        }else{
            map[key] = mutableListOf(value)
        }
    }

    fun <Q,T> addOrPutSet(map:MutableMap<Q,MutableSet<T>>, key:Q, value:T){
        if(map.containsKey(key)){
            map[key]!!.add(value)
        }else{
            map[key] = mutableSetOf(value)
        }
    }

    fun <Q,K,V> addOrPutMap(map:MutableMap<Q,MutableMap<K,V>>, key:Q, subKey:K,value:V){
        if(map.containsKey(key)){
            map[key]!![subKey] = value
        }else{
            map[key] = mutableMapOf(Pair(subKey,value))
        }
    }
}
