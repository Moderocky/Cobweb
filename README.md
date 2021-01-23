Cobweb
=====
### Opus #2

A library for extremely simple distributed programming.

Access methods and objects from completely separate Java processes running in an external JVM
- even on an entirely different physical machine.

Cobweb is designed to easily connect parts of a distributed Java program together, in one big spider-web.

This is built on the work of the Remote Method Interface, a little-known Java 1 API for making remote procedure calls.
I must give particular credit to `DOI: 10.1109/40.591654`, the research paper that inspired me to make this.

As the RMI is such an ignored feature, Cobweb exists to make it more easily accessible and useful,
using helper methods, generics and other, newer features of Java.

Features:
* Export objects to be accessed by external JVMs.
* Call methods directly from another Java process.
* Automatic retrieval and management of local registries.

Sample Intentions:
* Create fully integrated remote backends without needing to touch sockets or transfer protocol.
* Export APIs locally that can be accessed without dependencies.
* Export global attachment points that your other programs can link to.
* Transfer data easily without manual serialisation or connections.
* Easily link client processes with a centralised backend.
* Sequester parts of a program to prevent cascade failures.
* Split up a program to spread the resource cost over multiple machines.

### Maven Information
```xml
<repository>
    <id>pan-repo</id>
    <name>Pandaemonium Repository</name>
    <url>https://gitlab.com/api/v4/projects/18568066/packages/maven</url>
</repository>
``` 

```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>cobweb</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Examples

#### Simple instance access

Create a remote interface. (Goes on remote and origin.)

```java
private interface Alice extends Remote {
    
}
```

Create an implementation. (Goes on origin only.)
```java
private static class Bob implements Alice {

}
```

Export an instance of the implementation from the origin.
```java 
final Alice stub = registry.export("bob", new Bob());
```

Import the instance by proxy on the remote.
```java 
final Alice stub = registry.retrieve("bob");
```

#### Calling remote code

With the following class structure:
```java
interface Alice extends Remote { // Proxy interface
    
    int intMethod()
        throws RemoteException; // Important for passing errors properly
    
    Class<?> complexMethod(final String string) // Non-primitive classes may be passed
        throws RemoteException; // All methods must throw RemoteException

}

class Bob implements Alice { // Origin instance class
    
    @Override
    public int intMethod() {
        return 62; // return primitives
    }
    
    @Override
    public Class<?> complexMethod(final String string) { // pass params
        assert string != null; // you can throw errors here :)
        return string.getClass(); // return complex types
    }
}
```

You can export your instance from the origin as so:
```java 
final Registry registry = Registry.acquireLocal();
assert registry != null;
assert registry.export("bob", new Bob()) != null;
```
...and then acquire it remotely with:
```java 
final Alice stub = registry.retrieve("bob");
assert stub.intMethod() == 62;
Class<?> cls = stub.complexMethod("hello");
assert cls == String.class;
```

Remember that in most cases, the parameters are transmitted to origin,
and the method code is executed on origin, with the result transmitted back to remote.

### Explanation

While it would take a full forty pages to explain distributed computing in Java, and I am not qualified to do so,
I do provide a brief insight into how Cobweb and the RMI work.

Remote method invocation relies on a "registry" system that is partly separate from Java's ordinary processes.
It is built from a system of socket servers and receivers, which can be used to transmit data internally or externally.

When objects are exported and retrieved by remote JVMs, they are 'marshalled' (serialised) and their structure is transmitted in two parts:
a skeleton class, and a stub class. A `_Skel` and `_Stub` class are generated at runtime as proxy accessors for the instance.

When a method is called on the remote, it marshals the parameters and transmits them back to the origin to run the actual method,
and then returns the result in the same way.

As an approach, this has some weaknesses.
To begin with, large amounts of data may need to be sent for heavily wrapped parameters, and a lot of skeleton classes might be
recursively generated in order to preserve behaviour.
While this means you can execute a remote method as you would any other, it also means the JVM is doing more work than necessary.

From reading the original creator, Ann Wollrath's research, it seems that there was an intention to expand on this and create
a better and more complex system.

For a better explanation, please refer to `DOI: 10.1109/40.591654`.

### Intentions

Twenty-five years have now passed since the RMI's creation, and there seems to be very little difference between JDK 15's implementation
and the original, from Java 1.1.

I fully intend to take up the torch and see if I can expand on the original work making use of runtime-created method accessors,
bytecode alterations and possibly even transmitting compiled code from the Graal AOT compiler, in order to reduce the
need for transference of unnecessary or trivial data.

I may also be able to analyse method behaviour and strip out unnecessary parts of transferred parameters.
Using a combination of Java's Unsafe and some internal tricks of the JVM, it would be entirely possible to create whittled-down
versions of parameter objects that contain only the necessary content, and none of the unused fields.

It might also be possible to trim out any side effects, or find better, faster methods of serialising the objects.

Now I am sure I cannot match the work of Sun Microsystems on this front, but I believe improvements can be made.

It is my intention for Cobweb to eventually be able to outperform the RMI on its own, rather than simply being a hand-holding
wrapper for it.
