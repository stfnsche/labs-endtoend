package org.openengsb.labs.endtoend.testcontext;

public class TestContextID {
    private final String id;

    public TestContextID(String contextName, String osName, String osArch) {
        String tmp = osName + "." + osArch;
        if (null != contextName && contextName.length() > 0) {
            tmp = contextName + "." + tmp;
        }
        this.id = tmp;
    }

    public TestContextID(String osName, String osArch) {
        this.id = osName + "." + osArch;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestContextID other = (TestContextID) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
