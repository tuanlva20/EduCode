package com.poly.controller;


import com.poly.DTO.RegisterDataForm;
import com.poly.bean.Account;
import com.poly.bean.Authority;
import com.poly.bean.Role;
import com.poly.dao.AccountDAO;
import com.poly.dao.AuthorityDAO;
import com.poly.dao.RoleDAO;
import com.poly.service.AccountService;
import com.poly.service.MailService;
import com.poly.service.SecuriryService;

import java.net.http.HttpRequest;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("security")
public class SecurityController {



    @Autowired
    SecuriryService userService;

    @Autowired
    MailService mailService;

    @Autowired
    BCryptPasswordEncoder pe;
    @Autowired
    AccountService accountService;

    @Autowired
    AuthorityDAO authorityDAO;
    @Autowired
    RoleDAO roleDAO;

    RegisterDataForm gForm;
    int coderd;

    

    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

    @RequestMapping("/login/form")
    public String formLogin() {
        return "authenticate/login";
    }

    @RequestMapping("/login/success")
    public String success(Model model) {
        model.addAttribute("message", "????ng nh???p th??nh c??ng");
        
        return "forward:/security/login/form";
    }

    @RequestMapping("/login/error")
    public String error(Model model) {
        System.out.println("-----------------------------------");
        model.addAttribute("message", "????ng nh???p kh??ng th??nh c??ng");
        return "forward:/security/login/form";
    }

    @RequestMapping("/logoff/success")
    public String logoffSuccess(Model model) {
        model.addAttribute("message", "????ng xu???t th??nh c??ng");
        return "forward:/security/login/form";
    }

    @RequestMapping("/oauth2/login/success")
    public String loginOAuth2(OAuth2AuthenticationToken oauth2) {
        userService.loginFormOAuth2(oauth2);
        return "forward:/security/login/success";
    }

    @RequestMapping("/access/denied")
    public String accessDenied(@Valid Model model) {
        model.addAttribute("message", "Vui l??ng truy c???p ????? s??? d???ng t??nh n??ng n??y");
        return "forward:/security/login/form";
    }

    @RequestMapping("/register/form")
    public String registerForm(Model model, @ModelAttribute("userData") RegisterDataForm userData) {
        model.addAttribute("userData", userData);
        return "authenticate/register";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        webDataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping(value = "/register/form")
    public String PostRegister(@ModelAttribute("userData") RegisterDataForm userData,
     BindingResult bindingResult,Model model, RedirectAttributes redirect) {

        String parternEmail = "^[A-Za-z0-9_.]{6,32}@([a-zA-Z0-9]{2,12})(.[a-zA-Z]{2,12})+$";

        if(userData.getUsername() ==null){
                model.addAttribute("errorUsername","Vui l??ng nh???p Username");
                return "authenticate/register";
        }else if(accountService.findByUsername(userData.getUsername())!=null){
            model.addAttribute("errorUsername","T??n t??i kho???n ???? t???n t???i");
            return "authenticate/register";
        }
        else if(userData.getUsername().trim().length() <= 4 &&
            userData.getUsername().trim().length() >= 20){
                model.addAttribute("errorUsername","Nh???p Username ??t nh???t t??? 4 ?????n 20 k?? t???");
                return "authenticate/register";
        }else if(userData.getEmail()==null){
                model.addAttribute("errorEmail","Vui l??ng nh???p Email");
                return "authenticate/register";
        }else if(!userData.getEmail().matches(parternEmail)){
                model.addAttribute("errorEmail","Nh???p Email kh??ng ch??nh x??c");
                return "authenticate/register";
        }else if(userData.getPassword()==null||userData.getPassword().trim().length() < 8){
                model.addAttribute("errorPassword", "M???t kh???u ??t nh???t ph???i 8 k?? t???");
                return "authenticate/register";
        }else if(userData.getConfirm()==null||!userData.getConfirm().equals(userData.getPassword())){
                model.addAttribute("errorConfirm", "Nh???p l???i m???t kh???u kh??ng ch??nh x??c");
                return "authenticate/register";
        }
            coderd = ( int )(Math.random()*(900000+1)+100000);
            mailService.sendMailRegister(userData.getEmail(),coderd);
            gForm = userData;
        return "redirect:/security/register/mail";
    }

    @RequestMapping("/register/mail")
    public String registercheckMail(Model model) {

        return "email/registerMail";
    }
    @PostMapping("/register/mail")
    public String registercheckMail(Model model,HttpServletRequest request) {
        String code = request.getParameter("code");
        String coderandom = String.valueOf(coderd);
        if(!code.equals(coderandom)){
            model.addAttribute("message","Nh???p m?? x??c nh???n kh??ng ch??nh x??c");
            return "email/registerMail";
        }else{
            Account account = new Account();
            BeanUtils.copyProperties(gForm, account);
            account.setPassword(pe.encode(account.getPassword()));
            accountService.create(account);
            Authority authority = new Authority();
            authority.setAccount(account);
            Role role = roleDAO.findById("1").get();
            authority.setRole(role);
            authorityDAO.save(authority);
            return "authenticate/login";
        }
    }

    @RequestMapping("/forgotpassword")
    public String forgotPass(Model model) {
        return "authenticate/forgotpassword";
    }
    @PostMapping("/forgotpassword")
    public String layma(Model model,HttpServletRequest request,RedirectAttributes riderect ){
        String username = request.getParameter("username");
        Account account = accountService.findByUsername(username);
        if(account == null){
            model.addAttribute("message", "T??i kho???n kh??ng ch??nh x??c");
            return "authenticate/forgotpassword";
        }else{
            coderd = ( int )(Math.random()*(900000+1)+100000);
            mailService.sendMailRegister(account.getEmail(),coderd);
            // model.addAttribute("user", username);
            riderect.addAttribute("username", username);
            return "redirect:/security/forgot/mail";
        }
        
    }

    @GetMapping("/forgot/mail")
    public String forgotinputMail(Model model,HttpServletRequest request,RedirectAttributes redirect){
        String username = request.getParameter("username");
        System.out.println(username);
        model.addAttribute("username", username);
        return "authenticate/forgotMail";
    }



    @PostMapping("/forgot/mail")
    public String forgotMail(Model model,HttpServletRequest request,RedirectAttributes riderect) {
        String code = request.getParameter("code");
        String coderandom = String.valueOf(coderd);
        String username = request.getParameter("username");
        System.out.println(username+"lo");
        if(!code.equals(coderandom)){
            model.addAttribute("message","Nh???p m?? x??c nh???n kh??ng ch??nh x??c");
            return "authenticate/forgotMail";
        }else{
            // Account account = accountService.findByUsername(username);
            // System.out.println(username+"lo");
            // account.setPassword(username);
            riderect.addAttribute("username", username);
            return "redirect:/security/forgotConfirm/formPass";
        }
    }

    @RequestMapping("/forgotConfirm/formPass")
    public String formPassForgot(Model model,HttpServletRequest request,RedirectAttributes redirect){
        String username = request.getParameter("username");
        System.out.println(username);
        model.addAttribute("username", username);
        return "authenticate/forgotPassConfirm";
    }

    @PostMapping("/forgotConfirm/formPass")
    public String postFormPassForgot(Model model,HttpServletRequest request){
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");
        if(password==null || password.length() < 8){
            model.addAttribute("message", "M???t kh???u ??t nh???t ph???i 8 k?? t???");
            return "authenticate/forgotPassConfirm";
        }else if(confirm==null|| !confirm.equals(password)){
            model.addAttribute("message", "Nh???p l???i m???t kh???u kh??ng ch??nh x??c");
            return "authenticate/forgotPassConfirm";
        }else{
            Account account = accountService.findByUsername(username);
            System.out.println(username+"lo");
            account.setPassword(pe.encode(password));
            accountService.update(account);
            return "authenticate/login";
        }
        
    }

}
