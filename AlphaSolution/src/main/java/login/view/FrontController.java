package login.view;

import login.dataAccess.DataFacade;
import login.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;

@Controller
public class FrontController {

    //use case controller (GRASP Controller)
    private LoginController loginController = new LoginController(new DataFacade());
    private ProjectController projectController = new ProjectController(new DataFacade());
    private TaskController taskController = new TaskController(new DataFacade());


    @GetMapping("/register-login")
    public String getHome() {
        return "index";
    }


    @PostMapping("/login-homepage")
    public String loginUser(WebRequest request, Model model) throws LoginSampleException, SQLException {
        //Retrieve values from HTML form via WebRequest
        String email = request.getParameter("email");
        String pwd = request.getParameter("password");
        User user = loginController.login(email, pwd);
        setSessionInfo(request, user);
        //Adds a list of projects using attribute "Project"
        model.addAttribute("Project",projectController.showProjectList(user));
        return "home";
    }

    @GetMapping("/home")
    public String home(WebRequest request, Model model) throws SQLException {
        User user = (User) request.getAttribute("user",WebRequest.SCOPE_SESSION);
        model.addAttribute("Project",projectController.showProjectList(user));

        return "home";
    }

    @PostMapping("/register-homepage")
    public String createUser(WebRequest request) throws LoginSampleException, DuplicateException {
        //Retrieve values from HTML form via WebRequest
        String email = request.getParameter("email");
        String password1 = request.getParameter("password1");
        String password2 = request.getParameter("password2");

        //Checks if the email exist in the database. Throws exception if email exist
        loginController.duplicateEmail(email);
        // If passwords match, work + data is delegated to logic controller
        if (password1.equals(password2)) {
            User user = loginController.createUser(email, password1);
            setSessionInfo(request, user);

            return "home";

        } else { // If passwords don't match, an exception is thrown
            throw new LoginSampleException("The two passwords did not match");
        }


    }

    @PostMapping("/create-project")
    public String createProject(WebRequest request) throws SQLException {
        String name = request.getParameter("project-name");
        User user = (User) request.getAttribute("user",WebRequest.SCOPE_SESSION);
        //Creates a project
        projectController.createProject(name,user);
        return "redirect:/home";
    }

    @GetMapping("/create-project1")
    public String createProject1()  {

        return "create-project";
    }

    @GetMapping("/{id}/create-task")
    public String createTask1(@PathVariable("id") int projectId, Model model)  {
        model.addAttribute("projectId", projectId);

        return "create-task";
    }


    @PostMapping("/{id}/create-task")
    public String createTask(@PathVariable("id") int projectId, WebRequest request) throws SQLException {
        String name = request.getParameter("task-name");
        int time = Integer.parseInt(request.getParameter("task-time"));
        int price = Integer.parseInt(request.getParameter("task-price"));
        //Creates a task
        taskController.createTask(name,time,price,projectId);

        return "redirect:/home";
    }



    @GetMapping("/{id}/delete-project")
    public String deleteProject1(@PathVariable("id") int projectId, Model model) throws SQLException {
        model.addAttribute("projectId", projectId);
        //We made these method because you cant delete a project with tasks in it.
        // Therefore you have to delete the tasks first before you can delete a project.
        taskController.deleteTask(projectId);
        projectController.deleteProject(projectId);

        return "redirect:/home";
    }

    @GetMapping("/{id}/edit-project")
    public String editProject1(@PathVariable("id") int projectId, Model model) throws SQLException {
        model.addAttribute("projectId", projectId);
        //The method finds a project name and inserts it into the attribute Project which now can be shown in the text field
        model.addAttribute("Project", projectController.findProject(projectId));

        return "edit";
    }

    @PostMapping("/{id}/edit-project")
    public String editProject(@PathVariable("id") int projectId, WebRequest request) throws SQLException {
        String name = request.getParameter("edit-project-name");
        //The method updates the project name in the database
        projectController.editProject(name,projectId);

        return "redirect:/home";
    }



    @GetMapping("/{id}/details")
    public String showDetails1(@PathVariable("id") int projectId, Model model) throws SQLException {
        model.addAttribute("projectId", projectId);
        //The three methods is used and added to the attributes to show all the details about the tasks
        model.addAttribute("details", taskController.showAll(projectId));
        model.addAttribute("calculatetime", taskController.calcTime(projectId));
        model.addAttribute("calculateprice", taskController.calcPrice(projectId));


        return "details";
    }



    private void setSessionInfo(WebRequest request, User user) {
        // Place user info on session
        request.setAttribute("user", user, WebRequest.SCOPE_SESSION);
    }


    @ExceptionHandler(Exception.class)
    public String anotherError(Model model, Exception exception) {
        model.addAttribute("message",exception.getMessage());
        return "exceptionPage";
    }
}