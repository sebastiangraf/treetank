package org.treetank.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.treetank.revisioning.IRevisioning;

public class ClassLoaderTest {

    public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException,
        InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> revClazz = Class.forName("org.treetank.revisioning.Differential");
        Constructor<?> cons = revClazz.getConstructors()[0];
        IRevisioning obj = (IRevisioning)cons.newInstance(4);
        System.out.println(obj);
    }
}
