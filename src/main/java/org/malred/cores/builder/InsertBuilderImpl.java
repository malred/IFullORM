package org.malred.cores.builder;

public interface InsertBuilderImpl {
    public Builder insert(String[] params);

    public Builder insert(String tbName, String... params);
}
