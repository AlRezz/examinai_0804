package com.examinai.app.web.intern;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examinai.app.domain.task.Submission;
import com.examinai.app.domain.task.SubmissionStatus;
import com.examinai.app.domain.task.Task;
import com.examinai.app.domain.user.UserRepository;
import com.examinai.app.service.InternTaskService;
import com.examinai.app.service.SubmissionLifecycleService;
import com.examinai.app.service.SubmissionLifecycleView;
import com.examinai.app.service.SubmissionService;
import com.examinai.app.service.TaskService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/intern/tasks")
public class InternTaskController {

	private final UserRepository userRepository;
	private final InternTaskService internTaskService;
	private final TaskService taskService;
	private final SubmissionService submissionService;

	private final SubmissionLifecycleService submissionLifecycleService;

	public InternTaskController(UserRepository userRepository, InternTaskService internTaskService,
			TaskService taskService, SubmissionService submissionService, SubmissionLifecycleService submissionLifecycleService) {
		this.userRepository = userRepository;
		this.internTaskService = internTaskService;
		this.taskService = taskService;
		this.submissionService = submissionService;
		this.submissionLifecycleService = submissionLifecycleService;
	}

	public record TaskWithLifecycle(Task task, Submission submission, SubmissionLifecycleView lifecycle) {
	}

	@GetMapping
	public String list(Authentication authentication, Model model) {
		UUID internId = requireUserId(authentication);
		model.addAttribute("taskRows", internTaskService.listAssignedTasksForIntern(internId).stream().map(t -> {
			Submission s = submissionService.findForInternTask(t.getId(), internId);
			return new TaskWithLifecycle(t, s, submissionLifecycleService.viewForIntern(s));
		}).toList());
		return "intern/tasks/list";
	}

	@GetMapping("/{taskId}")
	public String detail(@PathVariable UUID taskId, Authentication authentication, Model model) {
		UUID internId = requireUserId(authentication);
		if (!internTaskService.isAssigned(taskId, internId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		model.addAttribute("task", taskService.requireTask(taskId));
		var existing = submissionService.findForInternTask(taskId, internId);
		model.addAttribute("submission", existing);
		model.addAttribute("submissionLifecycle", submissionLifecycleService.viewForIntern(existing));
		if (!model.containsAttribute("submissionForm")) {
			var form = new SubmissionForm();
			if (existing != null) {
				form.setRepoIdentifier(existing.getRepoIdentifier());
				form.setCommitSha(existing.getCommitSha());
				form.setPathScope(existing.getPathScope());
			}
			model.addAttribute("submissionForm", form);
		}
		return "intern/tasks/detail";
	}

	@PostMapping("/{taskId}/submission")
	public String submit(@PathVariable UUID taskId, Authentication authentication,
			@Valid @ModelAttribute("submissionForm") SubmissionForm form, BindingResult binding, Model model,
			RedirectAttributes redirectAttributes) {
		UUID internId = requireUserId(authentication);
		if (!internTaskService.isAssigned(taskId, internId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		if (binding.hasErrors()) {
			model.addAttribute("task", taskService.requireTask(taskId));
			model.addAttribute("submission", submissionService.findForInternTask(taskId, internId));
			return "intern/tasks/detail";
		}
		try {
			submissionService.upsertCoordinates(taskId, internId, form.getRepoIdentifier(), form.getCommitSha(),
					form.getPathScope(), SubmissionStatus.SUBMITTED);
			redirectAttributes.addFlashAttribute("submissionNotice", "Submission coordinates saved.");
			return "redirect:/intern/tasks/" + taskId;
		}
		catch (IllegalArgumentException ex) {
			binding.reject("submission", ex.getMessage());
			model.addAttribute("task", taskService.requireTask(taskId));
			model.addAttribute("submission", submissionService.findForInternTask(taskId, internId));
			return "intern/tasks/detail";
		}
	}

	private UUID requireUserId(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
			.getId();
	}
}
