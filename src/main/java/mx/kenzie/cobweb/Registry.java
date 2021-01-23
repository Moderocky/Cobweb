package mx.kenzie.cobweb;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Registry {
    
    protected final int port;
    protected final InetAddress host;
    protected final java.rmi.registry.Registry registry;
    
    protected Registry() throws IOException {
        this.port = java.rmi.registry.Registry.REGISTRY_PORT;
        this.host = InetAddress.getLocalHost();
        System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
        this.registry = LocateRegistry
            .createRegistry(port);
    }
    
    protected Registry(boolean create) throws IOException {
        this(java.rmi.registry.Registry.REGISTRY_PORT, InetAddress.getLocalHost(), create);
    }
    
    protected Registry(int port, InetAddress host, boolean create) throws IOException {
        this(port, host, create
            ? LocateRegistry.createRegistry(port)
            : LocateRegistry.getRegistry(host.getHostAddress(), port));
    }
    
    protected Registry(int port, InetAddress host, java.rmi.registry.Registry registry) throws IOException {
        this.port = port;
        this.host = host;
        System.setProperty("java.rmi.server.hostname", host.getHostAddress());
        this.registry = registry;
    }
    
    protected Registry(int port, boolean create) throws IOException {
        this(port, InetAddress.getLocalHost(), create);
    }
    
    protected Registry(InetAddress host, boolean create) throws IOException {
        this(java.rmi.registry.Registry.REGISTRY_PORT, host, create);
    }
    
    /**
     * This will acquire or create the default local registry.
     *
     * @return the default local registry
     */
    public static Registry acquireLocal() {
        try {
            return create();
        } catch (Exception ex) {
            return getLocal();
        }
    }
    
    /**
     * Creates a local registry on the local hostname, with the
     * common registry port (1099).
     * <p>
     * This will be accessible locally.
     *
     * @return the new registry.
     */
    public static Registry create() {
        try {
            return new Registry();
        } catch (IOException e) {
            throw new RuntimeException("ERROR/Unable to create local registry.", e);
        }
    }
    
    /**
     * Retrieves the local registry, or null if none exists.
     *
     * @return the local registry otherwise null
     */
    public static @Nullable Registry getLocal() {
        try {
            return new Registry(false);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * This will acquire or create the local registry
     * at the given port.
     *
     * @return the local registry
     */
    public static Registry acquireLocal(int port) {
        try {
            return create(port);
        } catch (Exception ex) {
            return getLocal(port);
        }
    }
    
    /**
     * Creates a registry on the given hostname, with the
     * supplied port.
     * <p>
     * This also sets the system property
     * 'java.rmi.server.hostname' to the given address - this
     * may be unnecessary, but can help to resolve conflict issues in
     * some cases.
     * Note that this means running a second registry would override
     * this.
     *
     * @param port the registry port
     * @return the new registry.
     */
    public static Registry create(int port) {
        try {
            return new Registry(port, InetAddress.getLocalHost(), true);
        } catch (IOException e) {
            throw new RuntimeException("ERROR/Unable to create registry.", e);
        }
    }
    
    /**
     * Retrieves the local registry on the given port, or null if none exists.
     *
     * @param port the registry port
     * @return the local registry otherwise null
     */
    public static @Nullable Registry getLocal(int port) {
        try {
            return new Registry(false);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Retrieves the registry from the given hostname, with the
     * supplied port.
     *
     * @param port the registry port
     * @param host the registry hostname - can be local
     * @return the registry otherwise null
     */
    public static @Nullable Registry getRemote(InetAddress host, int port) {
        try {
            return new Registry(port, host, false);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Retrieves the registry from the given hostname, with the
     * default port.
     *
     * @param host the registry hostname - can be local
     * @return the registry otherwise null
     */
    public static @Nullable Registry getRemote(InetAddress host) {
        try {
            return new Registry(host, false);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Provides the registry port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Provides the registry hostname.
     *
     * @return the host
     */
    public @NotNull InetAddress getHost() {
        return host;
    }
    
    /**
     * Exports an object on the given port, then binds it to this
     * registry with the provided name as an identifier.
     * <p>
     * Note: the stub parameter (T) must be an interface, as this
     * will be proxied.
     *
     * @param name   the binding name
     * @param object the exported object
     * @param <T>    the stub interface type
     * @param <Q>    the object type
     * @return the stub proxy
     * @throws RemoteException       if export fails
     * @throws AlreadyBoundException if name is already bound
     */
    public <T extends Remote, Q extends T> T export(String name, Q object) throws
        RemoteException,
        AlreadyBoundException {
        return export(name, object, 0);
    }
    
    /**
     * Exports an object on the given port, then binds it to this
     * registry with the provided name as an identifier.
     * <p>
     * Note: the stub parameter (T) must be an interface, as this
     * will be proxied.
     *
     * @param name   the binding name
     * @param object the exported object
     * @param port   the port to export this on
     * @param <T>    the stub interface type
     * @param <Q>    the object type
     * @return the stub proxy
     * @throws RemoteException       if export fails
     * @throws AlreadyBoundException if name is already bound
     */
    @SuppressWarnings("unchecked")
    public <T extends Remote, Q extends T> T export(String name, Q object, int port) throws
        RemoteException,
        AlreadyBoundException {
        System.setProperty("java.rmi.server.hostname", host.getHostAddress());
        T stub = (T) UnicastRemoteObject
            .exportObject(object, port);
        registry.bind(name, stub);
        return stub;
    }
    
    /**
     * Binds a pre-exported stub in the registry.
     *
     * @param name the binding name
     * @param stub the exported object
     * @param <T>  the stub interface type
     * @return true if successful, false if already bound
     * @throws RemoteException if remote communication with the registry failed
     */
    public <T extends Remote> boolean bind(String name, T stub) throws RemoteException {
        try {
            registry.bind(name, stub);
            return true;
        } catch (AlreadyBoundException ex) {
            return false;
        }
    }
    
    /**
     * Retrieve a stub from the registry.
     *
     * @param name         the bound name
     * @param defaultValue the value to return if null
     * @param <T>          the stub type interface
     * @return the remote stub, or default
     * @throws RemoteException if communication with the registry failed
     */
    public <T extends Remote> T retrieve(String name, T defaultValue) throws RemoteException {
        final T stub = retrieve(name);
        if (stub == null) return defaultValue;
        return stub;
    }
    
    /**
     * Retrieve a stub from the registry.
     *
     * @param name the bound name
     * @param <T>  the stub type interface
     * @return the remote stub, or null if unbound
     * @throws RemoteException if communication with the registry failed
     */
    @SuppressWarnings("unchecked")
    public <T extends Remote> @Nullable T retrieve(String name) throws RemoteException {
        try {
            return (T) registry
                .lookup(name);
        } catch (NotBoundException ex) {
            return null;
        }
    }
    
    /**
     * Returns true if the given name is bound in the registry.
     *
     * @param name the name
     * @return true if bound, otherwise false
     * @throws RemoteException if communication with the registry failed
     */
    public boolean isBound(String name) throws RemoteException {
        final String[] list = registry.list();
        for (String s : list) {
            if (s.equals(name)) return true;
        }
        return false;
    }
    
    /**
     * Fetches all of the binding names from the registry.
     *
     * @return the binding names
     * @throws RemoteException if communication with the registry failed
     */
    public String[] getBindings() throws RemoteException {
        return registry.list();
    }
    
    /**
     * Fetches all of the binding names from the registry.
     *
     * @return the binding names
     * @throws RemoteException if communication with the registry failed
     */
    public int emptyBindings() throws RemoteException {
        synchronized (registry) {
            final String[] contents = registry.list();
            final int count = contents.length;
            for (final String string : contents) {
                unbind(string);
            }
            return count;
        }
    }
    
    /**
     * Unbinds a stub from the registry.
     *
     * @param name the binding name
     * @return true if successful, false if not bound
     * @throws RemoteException if remote communication with the registry failed
     */
    public boolean unbind(String name) throws RemoteException {
        try {
            registry.unbind(name);
            return true;
        } catch (NotBoundException ex) {
            return false;
        }
    }
    
}
