package com.examinai.app.web.task;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examinai.app.service.TaskService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("tasks", taskService.listAllOrderedByDueDate());
		return "tasks/list";
	}

	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute("taskForm", new TaskForm());
		model.addAttribute("taskId", null);
		return "tasks/form";
	}

	@PostMapping("/new")
	public String create(@Valid @ModelAttribute("taskForm") TaskForm form, BindingResult binding, Model model,
			RedirectAttributes redirectAttributes) {
		if (binding.hasErrors()) {
			return "tasks/form";
		}
		taskService.create(form.getTitle(), form.getDescription(), form.getDueDate());
		redirectAttributes.addFlashAttribute("taskNotice", "Task created.");
		return "redirect:/tasks";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable UUID id, Model model) {
		var task = taskService.requireTask(id);
		var form = new TaskForm();
		form.setTitle(task.getTitle());
		form.setDescription(task.getDescription());
		form.setDueDate(task.getDueDate());
		model.addAttribute("taskForm", form);
		model.addAttribute("taskId", id);
		return "tasks/form";
	}

	@PostMapping("/{id}/edit")
	public String update(@PathVariable UUID id, @Valid @ModelAttribute("taskForm") TaskForm form, BindingResult binding,
			Model model, RedirectAttributes redirectAttributes) {
		if (binding.hasErrors()) {
			model.addAttribute("taskId", id);
			return "tasks/form";
		}
		taskService.update(id, form.getTitle(), form.getDescription(), form.getDueDate());
		redirectAttributes.addFlashAttribute("taskNotice", "Task saved.");
		return "redirect:/tasks";
	}
}
