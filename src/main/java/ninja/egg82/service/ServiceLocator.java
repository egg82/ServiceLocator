package ninja.egg82.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceLocator {
    private static ConcurrentMap<Class<?>, Optional<?>> services = new ConcurrentHashMap<>();

    private ServiceLocator() {}

    public static void register(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        register(clazz, true);
    }

    public static void register(Class<?> clazz, boolean lazy) throws InstantiationException, IllegalAccessException {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null.");
        }

        register(clazz, lazy ? null : clazz.newInstance());
    }

    public static void register(Object service) {
        if (service == null) {
            throw new IllegalArgumentException("service cannot be null.");
        }
        if (service instanceof Class) {
            throw new IllegalArgumentException("service must not be a Class object.");
        }

        register(service.getClass(), service);
    }

    private static void register(Class<?> clazz, Object initialized) {
        Set<Class<?>> interfaces = getInterfaces(clazz);
        Set<Class<?>> classes = getSuperClasses(clazz);

        Optional<?> value = Optional.ofNullable(initialized);

        services.put(clazz, value);
        for (Class<?> i : interfaces) {
            services.put(i, value);
        }
        for (Class<?> c : classes) {
            services.put(c, value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<? extends T> remove(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null.");
        }

        Set<T> retVal = new HashSet<>();

        services.entrySet().removeIf(kvp -> {
            if (kvp.getKey().equals(clazz) || clazz.isAssignableFrom(kvp.getKey())) {
                Set<Class<?>> removedClasses = new HashSet<>();
                removedClasses.addAll(getInterfaces(kvp.getKey()));
                removedClasses.addAll(getSuperClasses(kvp.getKey()));
                services.keySet().removeIf(removedClasses::contains);

                if (kvp.getValue().isPresent()) {
                    retVal.add((T) kvp.getValue().get());
                }
                return true;
            }
            return false;
        });

        return retVal;
    }

    private static Set<Class<?>> getInterfaces(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<>();
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> i : interfaces) {
            classes.add(i);
            classes.addAll(getInterfaces(i));
        }
        return classes;
    }

    private static Set<Class<?>> getSuperClasses(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<>();
        Class<?> c = clazz;
        while ((c = c.getSuperclass()) != null) {
            classes.add(c);
        }
        return classes;
    }

    public static <T> T get(Class<T> clazz) throws InstantiationException, IllegalAccessException, ServiceNotFoundException {
        Optional<T> retVal = getOptional(clazz);
        if (!retVal.isPresent()) {
            throw new ServiceNotFoundException(clazz);
        }
        return retVal.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptional(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        Optional<T> retVal = null;
        // The try/catch/throws are dirty hacks to get around the fact that you can't throw a checked exception in a lambda
        try {
            retVal = (Optional<T>) services.computeIfPresent(clazz, (k, v) -> {
                if (!v.isPresent()) {
                    try {
                        return Optional.ofNullable(k.newInstance());
                    } catch (InstantiationException|IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                return v;
            });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof InstantiationException) {
                throw (InstantiationException) ex.getCause();
            }
            if (ex.getCause() instanceof IllegalAccessException) {
                throw (IllegalAccessException) ex.getCause();
            }
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
        }
        return (retVal == null) ? Optional.empty() : retVal;
    }

    public static boolean contains(Class<?> clazz) {
        return clazz != null && services.containsKey(clazz);
    }

    public static boolean isInitialized(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        Optional<?> retVal = services.get(clazz);
        return retVal != null && retVal.isPresent();
    }
}
