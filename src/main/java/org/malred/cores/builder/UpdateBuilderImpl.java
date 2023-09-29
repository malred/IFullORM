package org.malred.cores.builder;


public interface UpdateBuilderImpl {
    public Builder update(String tbName, String... columns);

    public Builder update(String[] columns);
}
