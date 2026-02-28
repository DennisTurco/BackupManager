package backupmanager.frames;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.menu.MyDrawerBuilder;
import backupmanager.system.Form;
import backupmanager.system.FormManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class Login extends Form {
    public Login() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));
        createLogin();
    }

    private void createLogin() {
        JPanel panelLogin = new JPanel(new MigLayout());

        JPanel loginContent = new JPanel(new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]"));

        JLabel lbTitle = new JLabel("Login");
        JLabel lbDescription = new JLabel("Please enter your data to access the system");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +12;");

        loginContent.add(lbTitle);
        loginContent.add(lbDescription);

        JTextField txtName = new JTextField();
        JTextField txtSurname = new JTextField();
        JTextField txtEmail = new JTextField();
        JButton cmdLogin = new JButton("Login") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };

        // style
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your name");
        txtSurname.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your surname");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your email");

        panelLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]border:5,5,5,5,shade($Panel.background,10%),,20;" +
                "[dark]border:5,5,5,5,tint($Panel.background,5%),,20;" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,2%);");

        loginContent.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");

        txtName.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtSurname.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtEmail.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");

        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");

        loginContent.add(new JLabel("Name"), "gapy 25");
        loginContent.add(txtName);

        loginContent.add(new JLabel("Surname"), "gapy 10");
        loginContent.add(txtSurname);

        loginContent.add(new JLabel("Email"), "gapy 10");
        loginContent.add(txtEmail);
        loginContent.add(cmdLogin, "gapy 20");

        panelLogin.add(loginContent);
        add(panelLogin);

        // event
        cmdLogin.addActionListener(e -> {
            MyDrawerBuilder.getInstance().initHeader();
            FormManager.login();
        });
    }
}
