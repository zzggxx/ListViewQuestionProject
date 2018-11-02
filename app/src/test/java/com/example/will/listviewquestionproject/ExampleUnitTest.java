package com.example.will.listviewquestionproject;

import org.junit.Test;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

//        强引用
        /*是指创建一个对象并把这个对象赋给一个引用变量。
        强引用有引用变量指向时永远不会被垃圾回收。即使内存不足的时候。*/

//        软引用
        /*软引用的对象当系统内存充足时和强引用没有太多区别，但内存不足时会回收软引用的对象*/
        SoftReference<Personer>[] softReferences = new SoftReference[10];
        for (int i = 0; i < softReferences.length; i++) {
            softReferences[i] = new SoftReference<Personer>(new Personer("name", 1));
        }

//        弱引用
        /*弱引用具有很强的不确定性。因为垃圾回收每次都会回收弱引用的对象*/
        WeakReference<Personer>[] weakReferences = new WeakReference[10];
        for (int i = 0; i < weakReferences.length; i++) {
            weakReferences[i] = new WeakReference<Personer>(new Personer("name", 1));
        }

//        虚引用


    }
}