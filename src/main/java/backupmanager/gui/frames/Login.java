package backupmanager.gui.frames;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Entities.User;
import backupmanager.Services.LoginService;
import backupmanager.gui.menu.MyDrawerBuilder;
import backupmanager.gui.system.Form;
import backupmanager.gui.system.FormManager;
import net.miginfocom.swing.MigLayout;

public class Login extends Form {

    private final LoginService loginService = new LoginService();

    public Login() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));

        if (!loginService.isFirstAccess()) {
            javax.swing.SwingUtilities.invokeLater(this::showMainForm);
            return;
        }

        createLogin();
    }

    private void createLogin() {

        JPanel panelLogin = new JPanel(new MigLayout());
        JPanel loginContent = new JPanel(
                new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]")
        );

        JLabel lbTitle = new JLabel("Login");
        JLabel lbDescription = new JLabel("Please enter your data to access the system");

        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +12;");

        loginContent.add(lbTitle);
        loginContent.add(lbDescription);

        JTextField txtName = new JTextField();
        JTextField txtSurname = new JTextField();
        JTextField txtEmail = new JTextField();

        JButton cmdLogin = new JButton("Login");

        // Placeholder
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your name");
        txtSurname.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your surname");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your email");

        // Style
        panelLogin.putClientProperty(FlatClientProperties.STYLE,
                "[light]border:5,5,5,5,shade($Panel.background,10%),,20;" +
                "[dark]border:5,5,5,5,tint($Panel.background,5%),,20;" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,2%);"
        );

        loginContent.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        String fieldStyle = "margin:4,10,4,10;arc:12;";

        txtName.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        txtSurname.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        txtEmail.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        cmdLogin.putClientProperty(FlatClientProperties.STYLE, fieldStyle);

        loginContent.add(new JLabel("Name"), "gapy 25");
        loginContent.add(txtName);

        loginContent.add(new JLabel("Surname"), "gapy 10");
        loginContent.add(txtSurname);

        loginContent.add(new JLabel("Email"), "gapy 10");
        loginContent.add(txtEmail);

        loginContent.add(cmdLogin, "gapy 20");

        panelLogin.add(loginContent);
        add(panelLogin);

        cmdLogin.addActionListener(e -> {

            String name = txtName.getText().trim();
            String surname = txtSurname.getText().trim();
            String email = txtEmail.getText().trim();

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty()) {
                return;
            }

            User user = new User(name, surname, email);
            loginService.createNewUser(user);

            javax.swing.SwingUtilities.invokeLater(this::showMainForm);
        });
    }

    private void showMainForm() {
        MyDrawerBuilder.getInstance().initHeader();
        FormManager.login();
    }
}
