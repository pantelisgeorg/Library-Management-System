package com.library.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Self-service profile: any logged-in user (ADMIN or MEMBER) can change
 * their own email and password. Username and role stay admin-controlled.
 */
@Controller
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/edit")
    @Transactional
    public String editProfile(Principal principal,
                              @RequestParam String email,
                              @RequestParam String currentPassword,
                              @RequestParam(required = false) String newPassword,
                              RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Any profile change must be confirmed with the current password
        if (currentPassword == null || currentPassword.isBlank()
                || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/profile";
        }

        // Email change: must stay unique and belong to this account
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            User other = userRepository.findByEmail(email).orElse(null);
            if (other != null && !other.getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Email \"" + email + "\" already exists");
                return "redirect:/profile";
            }
            user.setEmail(email);
        }

        // Optional password change
        boolean passwordChanged = false;
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            passwordChanged = true;
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message",
                passwordChanged ? "Profile updated (password changed)" : "Profile updated");
        return "redirect:/profile";
    }
}