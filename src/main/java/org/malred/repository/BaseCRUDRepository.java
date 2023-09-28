package org.malred.repository;

import java.util.List;

public interface BaseCRUDRepository<T> {
    public List<T> findAll();
    public T findById(int id);

    // 假删除,将version字段设置为-1
    public int remove(int id);

    // 真删除,删除字段
    public int delete(int id);

    public int update(T t);

    public int insert(T t);
}
