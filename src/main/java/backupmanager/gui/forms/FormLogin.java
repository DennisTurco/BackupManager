package backupmanager.gui.forms;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Entities.User;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.LimitDocument;
import backupmanager.Services.LoginService;
import backupmanager.gui.Controllers.EntryUserController;
import backupmanager.gui.menu.DrawerManager;
import backupmanager.gui.system.FormManager;
import net.miginfocom.swing.MigLayout;

public class FormLogin extends CustomForm {

    private final LoginService loginService = new LoginService();
    private final EntryUserController userController = new EntryUserController();

    public FormLogin() {
        build();
    }

    @Override
    protected void init() {
        setLayout(new MigLayout("al center center"));

        if (!loginService.isFirstAccess()) {
            javax.swing.SwingUtilities.invokeLater(this::showMainForm);
            return;
        }

        loadData();
    }


    @Override
    protected void loadData() {
        JPanel panelLogin = new JPanel(new MigLayout());
        JPanel loginContent = new JPanel(
                new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]")
        );

        lbTitle = new JLabel("Login");
        lbDescription = new JLabel("Please enter your data to access the system");

        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +12;");

        loginContent.add(lbTitle);
        loginContent.add(lbDescription);

        txtName = new JTextField();
        txtSurname = new JTextField();
        txtEmail = new JTextField();
        labelName = new JLabel("Name");
        labelSurname = new JLabel("Surname");
        labelEmail = new JLabel("Email");

        JButton cmdLogin = new JButton("Login");

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

        txtName.setDocument(new LimitDocument(20));
        txtSurname.setDocument(new LimitDocument(20));
        txtEmail.setDocument(new LimitDocument(32));

        loginContent.add(labelName, "gapy 25");
        loginContent.add(txtName);

        loginContent.add(labelSurname, "gapy 10");
        loginContent.add(txtSurname);

        loginContent.add(labelEmail, "gapy 10");
        loginContent.add(txtEmail);

        loginContent.add(cmdLogin, "gapy 20");

        panelLogin.add(loginContent);
        add(panelLogin);

        cmdLogin.addActionListener(e -> {

            String name = txtName.getText().trim();
            String surname = txtSurname.getText().trim();
            String email = txtEmail.getText().trim();

            if (!userController.isInputOkAndShowErrorIfNecessary(this, name, surname, email))
                return;

            User user = new User(name, surname, email);
            loginService.createUserAndSendEmail(user);

            javax.swing.SwingUtilities.invokeLater(this::showMainForm);
        });
    }

    private void showMainForm() {
        DrawerManager.getInstance().getDrawer();
        FormManager.login();
    }

    @Override
    public void setTranslations() {
        if (lbTitle == null) {
            return;
        }

        lbTitle.setText(Translations.get(TKey.USER_TITLE));
        lbDescription.setText(Translations.get(TKey.USER_DESCRIPTION));
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.USER_NAME_PLACEHOLDER));
        txtSurname.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.USER_SURNAME_PLACEHOLDER));
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.USER_EMAIL_PLACEHOLDER));
        labelName.setText(Translations.get(TKey.USER_NAME));
        labelSurname.setText(Translations.get(TKey.USER_SURNAME));
        labelEmail.setText(Translations.get(TKey.USER_EMAIL));
    }

    private JTextField txtName;
    private JTextField txtSurname;
    private JTextField txtEmail;
    private JLabel labelName;
    private JLabel labelSurname;
    private JLabel labelEmail;
    private JLabel lbTitle;
    private JLabel lbDescription;
}
