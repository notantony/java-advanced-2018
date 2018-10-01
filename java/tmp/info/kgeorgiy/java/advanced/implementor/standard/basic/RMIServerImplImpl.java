package info.kgeorgiy.java.advanced.implementor.standard.basic;

public class RMIServerImplImpl extends info.kgeorgiy.java.advanced.implementor.standard.basic.RMIServerImpl {
    public RMIServerImplImpl(java.util.Map arg0) {
        super(arg0);
    }

    protected void export() throws java.io.IOException {
        return;
    }

    protected javax.management.remote.rmi.RMIConnection makeClient(java.lang.String arg0, javax.security.auth.Subject arg1) throws java.io.IOException {
        return null;
    }

    public java.rmi.Remote toStub() throws java.io.IOException {
        return null;
    }

    protected void closeClient(javax.management.remote.rmi.RMIConnection arg0) throws java.io.IOException {
        return;
    }

    protected java.lang.String getProtocol() {
        return null;
    }

    protected void closeServer() throws java.io.IOException {
        return;
    }

}