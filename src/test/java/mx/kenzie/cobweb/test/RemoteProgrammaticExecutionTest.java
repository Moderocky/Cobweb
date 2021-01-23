package mx.kenzie.cobweb.test;

import mx.kenzie.cobweb.Registry;
import org.junit.Test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.util.regex.Pattern;

public class RemoteProgrammaticExecutionTest {
    
    @Test
    public void test() throws Throwable {
        final Registry registry = Registry.acquireLocal();
        assert registry != null;
        assert registry.export("bob", new Bob()) != null;
        final Alice stub = registry.retrieve("bob");
        assert stub instanceof Alice;
        assert !(stub instanceof Bob);
        assert stub instanceof Remote;
        stub.voidMethod();
        assert stub.intMethod() == 62;
        stub.methodWithParams(6, "hello");
        ServerError error = null;
        try {
            stub.methodWithParams(7, "hello");
        } catch (ServerError e) {
            error = e;
        }
        assert error != null;
        Class<?> cls = stub.complexMethod("hello", Pattern.compile("helle?o"));
        assert cls == String.class;
        assert registry.emptyBindings() > 0;
    }
    
    private interface Alice extends Remote {
        
        void voidMethod() throws RemoteException;
        
        int intMethod() throws RemoteException;
        
        void methodWithParams(final int one, final String two) throws RemoteException;
        
        Class<? extends String> complexMethod(final String string, final Pattern pattern) throws RemoteException;
        
    }
    
    private static class Bob implements Alice {
        @Override
        public void voidMethod() {
            final int i = 1;
            assert i == 1;
        }
        
        @Override
        public int intMethod() {
            return 62;
        }
        
        @Override
        public void methodWithParams(int one, String two) {
            assert one == 6;
            assert two.equals("hello");
        }
        
        @Override
        public Class<? extends String> complexMethod(String string, Pattern pattern) {
            assert pattern != null;
            assert pattern.matcher(string).matches();
            return string.getClass();
        }
    }
    
}
