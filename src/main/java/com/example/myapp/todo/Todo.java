package com.example.myapp.todo;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name="Todos")
@NamedQueries({
	@NamedQuery(name="findAllTodos", query="SELECT m FROM Todo m"),
	@NamedQuery(name="findTodoById", query="SELECT m FROM Todo m WHERE m.id = :id")
})
public class Todo {
	@Id
	//データベースのAUTO_INCREMENTと連携
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String task;
	private LocalDate date;
	private boolean completed;
	
	public Todo() {}
	
	public Todo(Long id, String task, LocalDate date,boolean completed) {
		this.id = id;
		this.task = task;
		this.date = date;
		this.completed = completed;
	}

	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}
	public String getTask() {return task;}
	public void setTask(String task) {this.task = task;}
	public LocalDate getDate() {return date;}
	public void setDate(LocalDate date) {this.date = date;}
	public boolean getCompleted() {return completed;}
	public void setCompleted(boolean completed) {this.completed = completed;}
}
