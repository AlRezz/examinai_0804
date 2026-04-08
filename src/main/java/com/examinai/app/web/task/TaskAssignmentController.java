package com.examinai.app.web.task;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examinai.app.service.TaskAssignmentService;
import com.examinai.app.service.TaskService;
import com.examinai.app.service.UserManagementService;

@Controller
@RequestMapping("/tasks")
public class TaskAssignmentController {

	private final TaskService taskService;
	private final TaskAssignmentService taskAssignmentService;
	private final UserManagementService userManagementService;

	public TaskAssignmentController(TaskService taskService, TaskAssignmentService taskAssignmentService,
			UserManagementService userManagementService) {
		this.taskService = taskService;
		this.taskAssignmentService = taskAssignmentService;
		this.userManagementService = userManagementService;
	}

	@GetMapping("/{id}/assignments")
	public String assignmentForm(@PathVariable UUID id, Model model) {
		model.addAttribute("task", taskService.requireTask(id));
		model.addAttribute("interns", userManagementService.listInternsOrderedByEmail());
		List<UUID> selected = taskAssignmentService.listForTask(id)
			.stream()
			.map(a -> a.getIntern().getId())
			.collect(Collectors.toList());
		model.addAttribute("selectedInternIds", selected);
		return "tasks/assign";
	}

	@PostMapping("/{id}/assignments")
	public String saveAssignments(@PathVariable UUID id, @RequestParam(required = false) List<UUID> internIds,
			RedirectAttributes redirectAttributes) {
		try {
			taskAssignmentService.replaceAssignmentsForTask(id, internIds != null ? internIds : List.of());
			redirectAttributes.addFlashAttribute("taskNotice", "Assignments updated (replace-all).");
			return "redirect:/tasks";
		}
		catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("assignmentError", ex.getMessage());
			return "redirect:/tasks/" + id + "/assignments";
		}
	}
}
