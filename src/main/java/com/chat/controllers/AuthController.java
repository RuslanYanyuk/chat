package com.chat.controllers;

import com.chat.models.User;
import com.chat.services.SecurityService;
import com.chat.services.UserService;
import com.chat.utils.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Ruslan Yaniuk
 * @date September 2017
 */
@Controller
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    UserValidator userValidator;

    @Autowired
    SecurityService securityService;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String getRegistrationPage(@RequestParam(value = "error", required = false) String error, Model model) {
        model.addAttribute("userForm", new User());
        if (error != null) {
            model.addAttribute("error", "Your username or password is invalid.");
        }
        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerUser(@ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
        userValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "redirect:/registration?error";
        }
        userService.add(userForm);
        securityService.autoLogin(userForm.getUsername(), userForm.getPassword());
        return "redirect:/";
    }
}