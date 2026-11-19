package com.example.myapp.todo;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TodoController02 {

	@Autowired
	private TodoService todoService;
	
	//全件表示
	@GetMapping("/todoList02")
	public String todoList01(Model m) {
	    // データベースから全件取得してモデルに追加
	    //m.addAttribute("todos", todoService.findAll());
		
		// 全件取得：一度変数で受ける
		List<Todo> alltodos = todoService.findAll();
		m.addAttribute("todos", alltodos);
		
		//全件数
		m.addAttribute("totalCount", alltodos.size());
		//残りタスク数
		long undoneCount = countUndone(alltodos);
		m.addAttribute("undoneCount", undoneCount);
		
	    m.addAttribute("today", LocalDate.now());
	    
		return "todo/todoList02";
	}
	
	//登録
	@PostMapping("/insert02")
    public String insertTodo(
            @RequestParam("TASK") String task,
            @RequestParam("DATE") LocalDate date
            ) {
		Todo t = new Todo();
		t.setId(null);
		t.setTask(task);
		t.setDate(date);
		t.setCompleted(false);
		
		// データベースに登録する処理を追加
        todoService.insert(t);
        
        return "redirect:/todoList02";
    }
	
	//削除
	@PostMapping("/delete02")
	public String deleteTodo(@RequestParam("id") Long id) {
		// 1. IDを元に、今ある find(id) メソッドを使ってエンティティを取得する
		Todo t = todoService.find(id);
		// 2. データが存在すれば、今ある delete(entity) メソッドに渡して削除する
		if(t != null) {
			todoService.delete(t);
		}
		return "redirect:/todoList02";
	}
	
	//変更（タスク名）
	@PostMapping("/update_task02")
    public String updateTask(
    		@RequestParam(name = "ID", required = false) Long id,
            @RequestParam(name = "TASK", required = false) String task,
            RedirectAttributes errorMsg
    		) {
		//未選択時にエラー表示
		if(id == null) {
			errorMsg.addFlashAttribute("errorMsg", "変更するタスクが選択されていません。");
			return "redirect:/todoList02";
		}
		if(task == null || task.isBlank()) {
			errorMsg.addFlashAttribute("errorMsg", "変更するタスク名が入力されていません。");
			return "redirect:/todoList02";
		}
		
		//選択されたIDをもとに、データベースからTodoエンティティを取得する
		Todo t = todoService.find(id);
		//取得したエンティティのタスクを新しく書き換える
		t.setTask(task);
		//データベースを更新する
		todoService.update(t);
        
        return "redirect:/todoList02";
	}
	
	//変更（期限日）
	@PostMapping("/update_date02")
    public String updateDate(
    		@RequestParam(name = "ID", required = false) Long id,
            @RequestParam(name = "DATE", required = false) LocalDate date,
            RedirectAttributes errorMsg
            ) {
		//未選択時にエラー表示
		if(id == null) {
			errorMsg.addFlashAttribute("errorMsg", "変更するタスクが選択されていません。");
			return "redirect:/todoList02";
		}
		if(date == null) {
			errorMsg.addFlashAttribute("errorMsg", "変更するタスクの期限日が入力されていません。");
			return "redirect:/todoList02";
		}
		
		//選択されたIDをもとに、データベースからTodoエンティティを取得する
		Todo t = todoService.find(id);
		//取得したエンティティのタスクを新しく書き換える
		t.setDate(date);
		//データベースを更新する
		todoService.update(t);
        
        return "redirect:/todoList02";
	}
	
	//チェック（完了か未完了か）
	@PostMapping("/update_check02/{id}")
	public String updateCheck(
			@PathVariable("id") Long id
			){
		Todo t = todoService.find(id);
		// データが存在する場合のみ処理を実行する
		if(t != null) {
			if (t.getCompleted() == false) {
				t.setCompleted(true);
			}else {
				t.setCompleted(false);
			}
			todoService.update(t);
		}
		return "redirect:/todoList02";
	}
	
	//抽出
	@PostMapping("/filter")
	public String filterTodo(
			@RequestParam(name = "FILTER", required = false) String filterType,
			Model m) {
		LocalDate today = LocalDate.now();
		List<Todo> alltodos = todoService.findAll();
		List<Todo> filtertodos = new ArrayList<>();
		
		if(filterType == null || filterType.equals("解除")) {
			filtertodos = alltodos;
		}else {
			for(Todo todo : alltodos) {
				LocalDate todoDate = todo.getDate();
				if(filterType.equals("期限前") && todoDate.isAfter(today)) {
					filtertodos.add(todo);
				}else if(filterType.equals("今日まで") && todoDate.equals(today)) {
					filtertodos.add(todo);
				}else if(filterType.equals("オーバー") && todoDate.isBefore(today)) {
					filtertodos.add(todo);
				}
			}
		}
		
	    m.addAttribute("todos", filtertodos);
	    m.addAttribute("today", today);
		//全件数
		m.addAttribute("totalCount", alltodos.size());
		//残りタスク数
		long undoneCount = countUndone(alltodos);
		m.addAttribute("undoneCount", undoneCount);
	    
		//抽出した件数
		m.addAttribute("filterCount", filtertodos.size());
		
		return "todo/todoList02";
	}
	
	//並び替え
	@PostMapping("/sort")
	public String sortTodo(
			@RequestParam(name = "SORT", required = false) String sortType,
			Model m) {
		LocalDate today = LocalDate.now();
		// 一度データベースから全件取得して変数に入れる
		List<Todo> alltodos = todoService.findAll();
		if(alltodos == null) {
			return "todo/todoList02";
		}
		// その変数を使って新しいリストのコピーを作る
		List<Todo> sorttodos = new ArrayList<>(alltodos);
		//上記2行を1行にまとめる場合
		//List<Todo> sorttodos = new ArrayList<>(todoService.findAll());
		
		//nullが入っている変数に対して.equals("～")を呼び出すとNullPointerExceptionというエラー発生
		if(sortType != null) {
			if(sortType.equals("昇順")) {
				//Todoオブジェクトの中から getDate（日付）を取り出して、それを基準に順番を比較
				sorttodos.sort(Comparator.comparing(Todo::getDate));
			}else if(sortType.equals("降順")) {
				sorttodos.sort(Comparator.comparing(Todo::getDate).reversed());
			}
		}
		
		m.addAttribute("todos", sorttodos);
	    m.addAttribute("today", today);
	    
		//全件数
		m.addAttribute("totalCount", alltodos.size());
		//残りタスク数
		long undoneCount = countUndone(alltodos);
		m.addAttribute("undoneCount", undoneCount);
	    
		return "todo/todoList02";
	}
	
	//CSV出力（全件）
	@GetMapping("/export/csv")
	public void exportCSV(HttpServletResponse response) throws IOException{
		//ファイル名と文字コードの設定
		LocalDate today = LocalDate.now();
		String csvName = "todoList_" + today + ".csv";
		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + csvName + "\"");
		//データベースから全件取得
		List<Todo> todos = todoService.findAll();
		//CSVテキスト書き出し
		try(PrintWriter w = response.getWriter()){
			
			// ★【テスト用】わざとIOExceptionを発生させる
	        //if (true) {throw new java.io.IOException("テスト用の強制エラーです");}
			
			//1行目は文字コードをUTF-8に指定
			w.write("\uFEFF");
			w.println("ID,タスク名,期限日,完了状態");
			for(Todo todo : todos) {
				w.print(todo.getId() + ",");
				w.print("\"" + todo.getTask()+ "\",");
				w.print(todo.getDate() + ",");
				w.println(todo.getCompleted() ? "完了" : "未完了");
			}
		}catch (Exception e) {
			throw new RuntimeException("CSV書き込み：エラー", e);
		}
    }
	
	//TEXT出力（全件）
	@GetMapping("/export/txt")
	public void exportTXT(HttpServletResponse response) throws IOException{
		//ファイル名と文字コードの設定
		LocalDate today = LocalDate.now();
		String txtName = "todoList_" + today + ".txt";
		response.setContentType("text/plain; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + txtName + "\"");
		//データベースから全件取得
		List<Todo> todos = todoService.findAll();
		//テキスト書き出し
		try(PrintWriter w = response.getWriter()){
			//1行目は文字コードをUTF-8に指定
			w.write("\uFEFF");
			w.println("----------------------------------------");
			for(Todo todo : todos) {
				w.println("・ID: " + todo.getId());
				w.println("・タスク名: " + todo.getTask());
				w.println("・期限日: " + todo.getDate());
				w.print("・完了状態: ");
				w.println(todo.getCompleted() ? "完了" : "未完了");
				w.println("----------------------------------------");
			}
		}catch (Exception e) {
			throw new RuntimeException("TEXT書き込み：エラー", e);
		}
    }
	
	//メソッド：残りタスク数カウント
	private long countUndone(List<Todo> todos) {
		long undoneCount = 0;
		for(Todo todo : todos) {
			if(todo.getCompleted() == false) {
				undoneCount++;
			}
		}
		return undoneCount;
	}
	
}