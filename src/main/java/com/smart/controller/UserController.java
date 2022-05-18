package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Messages;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;

	// method to add common data
	@ModelAttribute
	public void addCommomdata(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME : " + userName);
		// get the user using userName(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER : " + user);
		model.addAttribute("user", user);
	}

	// user dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}

	// open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form handler
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			// Processing and uploading file
			if (file.isEmpty()) {
				// if the file is empty then message will display here..
				System.out.println("FILE IS EMPTY!!!");
			} else {
				// upload the file to folder and update the name to contact..
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("IMAGE UPLOADED SUCCESSFULLY!!!");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Contact added to database..");
			System.out.println("DATA " + contact);
			// success message...
			session.setAttribute("message", new Messages("Contact Added Successfully!!!", "success"));
		} catch (Exception e) {
			System.out.println("ERROR : " + e.getMessage());
			e.printStackTrace();
			// error message..
			session.setAttribute("message", new Messages("Something went wrong!! try again!!!", "danger"));
		}
		return "normal/add_contact_form";
	}
}
