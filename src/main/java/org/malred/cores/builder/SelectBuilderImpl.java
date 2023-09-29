package org.malred.cores.builder;

public interface SelectBuilderImpl {
    public Builder select(String tbName, String... columns) ;

    public Builder select(String[] columns) ;

    public Builder select(String tbName) ;

    public Builder select() ;
}
