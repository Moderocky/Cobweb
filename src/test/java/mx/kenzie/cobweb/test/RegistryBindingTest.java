package mx.kenzie.cobweb.test;

import mx.kenzie.cobweb.Registry;
import org.junit.Test;

import java.rmi.Remote;

public class RegistryBindingTest {
    
    @Test
    public void bindLocalDefault() throws Throwable {
        final Registry registry = Registry.acquireLocal();
        assert registry != null;
        testRegistry(registry);
        assert registry.unbind("bob");
    }
    
    private void testRegistry(final Registry registry) throws Throwable {
        assert registry != null;
        final Alice stub = registry.export("bob", new Bob());
        assert stub != null;
        assert registry.isBound("bob");
        assert registry.retrieve("bob") != null;
        final Alice fetch = registry.retrieve("bob");
        assert fetch != null;
        final String[] bindings = registry.getBindings();
        assert bindings != null;
        assert bindings.length == 1;
        assert bindings[0].equals("bob");
    }
    
    @Test
    public void bindLocalWithPort() throws Throwable {
        final Registry registry = Registry.acquireLocal(10882);
        assert registry != null;
        testRegistry(registry);
        assert registry.unbind("bob");
    }
    
    @Test
    public void connectDefault() throws Throwable {
        final Registry registry = Registry.acquireLocal();
        assert registry != null;
        testRegistry(registry);
        final Alice stub = registry.retrieve("bob");
        assert stub != null;
        assert registry.export("bean", new Bob()) != null;
        final Alice bean = registry.retrieve("bean");
        assert !(bean instanceof Bob);
        assert bean != null;
        final Alice blob = registry.retrieve("blob", new Bob());
        assert blob instanceof Bob;
        assert registry.unbind("bob");
        assert registry.unbind("bean");
        assert !registry.unbind("blob");
    }
    
    @Test
    public void connectWithPort() throws Throwable {
        final Registry registry = Registry.acquireLocal(10882);
        assert registry != null;
        testRegistry(registry);
        final Alice stub = registry.retrieve("bob");
        assert stub != null;
        assert registry.export("bean", new Bob()) != null;
        final Alice bean = registry.retrieve("bean");
        assert !(bean instanceof Bob);
        assert bean != null;
        final Alice blob = registry.retrieve("blob", new Bob());
        assert blob instanceof Bob;
        assert registry.unbind("bob");
        assert registry.unbind("bean");
        assert !registry.unbind("blob");
    }
    
    @Test
    public void emptyBindings() throws Throwable {
        final Registry registry = Registry.acquireLocal();
        assert registry != null;
        testRegistry(registry);
        assert registry.export("a", new Bob()) != null;
        assert registry.export("b", new Bob()) != null;
        assert registry.export("c", new Bob()) != null;
        assert registry.getBindings().length == 4;
        assert registry.emptyBindings() == 4;
        assert registry.getBindings().length == 0;
    }
    
    private interface Alice extends Remote {
    
    }
    
    private static class Bob implements Alice {
    
    }
    
}
