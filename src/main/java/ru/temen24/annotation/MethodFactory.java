package ru.temen24.annotation;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class MethodFactory {
    private ApplicationContext applicationContext;

    public MethodFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private static Map<String, ClassAndMethodContainer> methodMap;

    public Object process(Object... commands) {
        try {
            return processWithReflection(commands);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to process command " + commands[0]);
        }
    }

    private Object processWithReflection(Object... commands) throws Throwable {
        initializeMethodMap();
        ClassAndMethodContainer container = methodMap.get(commands[0].toString().toLowerCase());
        if (container == null) {
            container = methodMap.get("/default");
            commands = new Object[1];
        }
        Object classInstance = applicationContext.getBean(container.getLocalClass());
        Method method = container.getLocalMethod();
        return invokeMethod(classInstance, method, commands);
    }

    private Object invokeMethod(Object classInstance, Method method, Object[] commands) throws IllegalAccessException, InvocationTargetException {
        if (commands.length == 1) {
            return method.invoke(classInstance, (Object[]) null);
        } else {
            Object[] params = Arrays.copyOfRange(commands, 1, commands.length);
            return method.invoke(classInstance, params);
        }
    }

    private void initializeMethodMap() {
        if (methodMap == null || methodMap.isEmpty()) {
            methodMap = new HashMap<>();
            List<Class<?>> classList = getAllClassesFromClasspath();
            for (Class cls : classList) {
                getAllCommandsFromClass(cls);
            }
        }
    }

    private List<Class<?>> getAllClassesFromClasspath() {
        URL roots = ClassLoader.getSystemResource("");
        File root = new File(roots.getPath());
        return iterateThroughDirectoriesAndGetClasses(root);
    }

    private List<Class<?>> iterateThroughDirectoriesAndGetClasses(File root) {
        List<Class<?>> list = new ArrayList<>();
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                list.addAll(iterateThroughDirectoriesAndGetClasses(file));
            } else {
                if (file.getName().endsWith(".class")) {
                    Class<?> clz = tryToMakeClass(file);
                    list.add(clz);
                }
            }
        }
        return list;
    }

    private Class<?> tryToMakeClass(File file) {
        Class<?> cls = null;
        String name = file.getAbsolutePath();
        String[] packageParts = name.split(Pattern.quote(File.separator));
        StringBuilder className = new StringBuilder();
        className.append(file.getName().replace(".class", ""));
        boolean classNotFound = true;
        int i = 2;
        while (classNotFound) {
            if (i == packageParts.length) {
                classNotFound = false;
            } else {
                className.insert(0, packageParts[packageParts.length - i] + ".");
                try {
                    cls = Class.forName(className.toString());
                    classNotFound = false;
                } catch (ClassNotFoundException e) {
                    i++;
                }
            }
        }
        return cls;
    }

    private void getAllCommandsFromClass(Class cls) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof BotCommand) {
                    methodMap.put(((BotCommand) annotation).commandName(), new ClassAndMethodContainer(cls, method));
                }
            }
        }
    }
}
