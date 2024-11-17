package com.mycompany.autobackupprogram.Dialogs;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.mycompany.autobackupprogram.Entities.Preferences;
import com.mycompany.autobackupprogram.Enums.ConfigKey;
import com.mycompany.autobackupprogram.Enums.LanguagesEnum;
import com.mycompany.autobackupprogram.Enums.ThemesEnum;
import com.mycompany.autobackupprogram.Enums.TranslationLoaderEnum;
import com.mycompany.autobackupprogram.Enums.TranslationLoaderEnum.TranslationCategory;
import com.mycompany.autobackupprogram.Enums.TranslationLoaderEnum.TranslationKey;
import com.mycompany.autobackupprogram.Managers.ThemeManager;

import static com.mycompany.autobackupprogram.GUI.BackupManagerGUI.OpenExceptionMessage;
import java.awt.Image;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.simple.parser.ParseException;

public class PreferencesDialog extends javax.swing.JDialog {

    private boolean isApply = false; 

    public PreferencesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        // logo application
        Image icon = new ImageIcon(this.getClass().getResource(ConfigKey.LOGO_IMG.getValue())).getImage();
        this.setIconImage(icon); 
        
        ThemeManager.updateThemeDialog(this);
        setLanguages();
        setThemes();
        setTranslations();
    }
    
    public void changeTheme() {
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languagesComboBox = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        themesComboBox = new javax.swing.JComboBox<>();
        applyBtn = new javax.swing.JButton();
        closeBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");
        setAlwaysOnTop(true);
        setResizable(false);

        languagesComboBox.setToolTipText("");

        jLabel1.setText("Language");

        jLabel2.setText("Theme");

        themesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                themesComboBoxActionPerformed(evt);
            }
        });

        applyBtn.setText("Apply");
        applyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyBtnActionPerformed(evt);
            }
        });

        closeBtn.setText("Close");
        closeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(223, Short.MAX_VALUE)
                        .addComponent(applyBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(closeBtn))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                            .addComponent(themesComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(languagesComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languagesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(themesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyBtn)
                    .addComponent(closeBtn))
                .addGap(14, 14, 14))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void themesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_themesComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_themesComboBoxActionPerformed

    private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyBtnActionPerformed
        isApply = true;
        try {
            // translactions
            Preferences.setLanguage((String) languagesComboBox.getSelectedItem());
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
            setTranslations();

            // theme
            Preferences.setTheme((String) themesComboBox.getSelectedItem());
            ThemeManager.updateThemeDialog(this);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }  
    }//GEN-LAST:event_applyBtnActionPerformed

    private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeBtnActionPerformed
    
    private void setLanguages() {
        languagesComboBox.addItem(LanguagesEnum.ENG.getLanguageName());
        languagesComboBox.addItem(LanguagesEnum.ITA.getLanguageName());
        
        languagesComboBox.setSelectedItem(Preferences.getLanguage().getLanguageName());
    }
    
    private void setThemes() {
        themesComboBox.addItem(ThemesEnum.INTELLIJ.getThemeName());
        themesComboBox.addItem(ThemesEnum.DRACULA.getThemeName());
        themesComboBox.addItem(ThemesEnum.CARBON.getThemeName());
        themesComboBox.addItem(ThemesEnum.ARC_ORAGE.getThemeName());
        themesComboBox.addItem(ThemesEnum.ARC_DARK_ORANGE.getThemeName());
        themesComboBox.addItem(ThemesEnum.CYAN_LIGHT.getThemeName());
        themesComboBox.addItem(ThemesEnum.NORD.getThemeName());
        themesComboBox.addItem(ThemesEnum.HIGH_CONTRAST.getThemeName());
        themesComboBox.addItem(ThemesEnum.SOLARIZED_DARK.getThemeName());
        themesComboBox.addItem(ThemesEnum.SOLARIZED_LIGHT.getThemeName());

        themesComboBox.setSelectedItem(Preferences.getTheme().getThemeName());
    }
    
    private void setTranslations() {
        setTitle(TranslationCategory.PREFERENCES_DIALOG.getTranslation(TranslationKey.PREFERENCES_TITLE));
        applyBtn.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.APPLY_BUTTON));
        closeBtn.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.CLOSE_BUTTON));
        jLabel1.setText(TranslationCategory.PREFERENCES_DIALOG.getTranslation(TranslationKey.LANGUAGE));
        jLabel2.setText(TranslationCategory.PREFERENCES_DIALOG.getTranslation(TranslationKey.THEME));
    }
    
    public boolean isApply() {
        return isApply;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyBtn;
    private javax.swing.JButton closeBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox<String> languagesComboBox;
    private javax.swing.JComboBox<String> themesComboBox;
    // End of variables declaration//GEN-END:variables
}
