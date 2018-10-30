package ninja.egg82.service;

public class ServiceNotFoundException extends Exception {
    private final Class<?> clazz;

    public ServiceNotFoundException(Class<?> clazz) {
        super();
        this.clazz = clazz;
    }

    public Class<?> getServiceClass() { return clazz; }
}
