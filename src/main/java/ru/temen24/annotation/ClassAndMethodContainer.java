package ru.temen24.annotation;


import java.lang.reflect.Method;

class ClassAndMethodContainer {

    private Class localClass;
    private Method localMethod;

    public ClassAndMethodContainer(Class localClass, Method localMethod) {
        this.localClass = localClass;
        this.localMethod = localMethod;
    }

    public ClassAndMethodContainer() {
    }

    public Class getLocalClass() {
        return localClass;
    }

    public void setLocalClass(Class localClass) {
        this.localClass = localClass;
    }

    public Method getLocalMethod() {
        return localMethod;
    }

    public void setLocalMethod(Method localMethod) {
        this.localMethod = localMethod;
    }
}
