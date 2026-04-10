package com.examinai.app.web.admin;

import java.util.HashSet;
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

import com.examinai.app.service.UserManagementService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

	private final UserManagementService userManagementService;

	public AdminUserController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("users", userManagementService.listUsers());
		return "admin/users/list";
	}

	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute("createRequest", new CreateUserRequest());
		model.addAttribute("allRoles", userManagementService.allRolesOrdered());
		model.addAttribute("createMode", true);
		return "admin/user-form";
	}

	@PostMapping("/new")
	public String create(@Valid @ModelAttribute("createRequest") CreateUserRequest request, BindingResult binding,
			RedirectAttributes redirectAttributes, Model model) {
		if (binding.hasErrors()) {
			model.addAttribute("allRoles", userManagementService.allRolesOrdered());
			model.addAttribute("createMode", true);
			return "admin/user-form";
		}
		try {
			userManagementService.createUser(request.getEmail(), request.getPassword(),
					new HashSet<>(request.getRoles()));
			redirectAttributes.addFlashAttribute("adminNotice", "User created.");
			return "redirect:/admin/users";
		}
		catch (IllegalArgumentException ex) {
			binding.reject("admin", ex.getMessage());
			model.addAttribute("allRoles", userManagementService.allRolesOrdered());
			model.addAttribute("createMode", true);
			return "admin/user-form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable UUID id, Model model) {
		var data = userManagementService.loadAdminEditForm(id);
		model.addAttribute("userId", data.userId());
		model.addAttribute("email", data.email());
		model.addAttribute("editRequest", data.editRequest());
		model.addAttribute("allRoles", userManagementService.allRolesOrdered());
		model.addAttribute("createMode", false);
		return "admin/user-form";
	}

	@PostMapping("/{id}/edit")
	public String edit(@PathVariable UUID id, @Valid @ModelAttribute("editRequest") EditUserRequest request,
			BindingResult binding, RedirectAttributes redirectAttributes, Model model) {
		if (binding.hasErrors()) {
			var user = userManagementService.getUserById(id);
			model.addAttribute("userId", user.getId());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("allRoles", userManagementService.allRolesOrdered());
			model.addAttribute("createMode", false);
			return "admin/user-form";
		}
		try {
			userManagementService.updateUser(id, request.isEnabled(), new HashSet<>(request.getRoles()),
					request.getNewPassword());
			redirectAttributes.addFlashAttribute("adminNotice", "User updated.");
			return "redirect:/admin/users";
		}
		catch (IllegalArgumentException ex) {
			var user = userManagementService.getUserById(id);
			model.addAttribute("userId", user.getId());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("allRoles", userManagementService.allRolesOrdered());
			model.addAttribute("createMode", false);
			binding.reject("admin", ex.getMessage());
			return "admin/user-form";
		}
	}
}
