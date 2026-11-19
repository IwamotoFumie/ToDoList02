package com.example.myapp.todo;

import java.io.Serializable;
import java.util.List;

public interface MyDAO<E> extends Serializable {
	public List<E> findAll();
	public E find(Long id);
	public boolean insert(E entity);
	public boolean update(E entity);
	public boolean delete(E entity);
}
