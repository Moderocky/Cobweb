module mx.kenzie.cobweb {
    requires static org.jetbrains.annotations;
    requires transitive java.rmi;
    uses java.rmi.Remote;
    opens mx.kenzie.cobweb;
}
