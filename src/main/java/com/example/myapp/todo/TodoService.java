package com.example.myapp.todo;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class TodoService implements MyDAO<Todo>{
	@PersistenceContext
	private EntityManager manager;
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Todo> findAll(){
		List<Todo> list = null;
		Query query = manager.createNamedQuery("findAllTodos");
		list = query.getResultList();
		return list;
	}
	@Override
	public Todo find(Long id) {
		Todo entity = manager.find(Todo.class, id);
		
		// 名前付きクエリを指定してクエリのオブジェクトを作る
		//TypedQuery<Todo> query = manager.createNamedQuery("findTodoById", Todo.class);
		// プレースホルダ（:id）に値をセットする
		//query.setParameter("id", id);
		// 実行して1件取得する
		//Todo entity = query.getSingleResult();

		return entity;
	}

	@Override
	@Transactional
	public boolean insert(Todo entity) {
		manager.persist(entity);

		return true;
	}

	@Override
	@Transactional
	public boolean update(Todo entity) {
		manager.merge(entity);

		return true;
	}

	@Override
	@Transactional
	public boolean delete(Todo entity) {
		manager.remove(entity);

		return true;
	}
}
