package backupmanager.frames;

import backupmanager.Controllers.GuiController;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.frames.Controllers.BackupProgressController;


public class BackupProgressGUI extends javax.swing.JDialog {

    private final BackupProgressController progressController;

    public BackupProgressGUI(String initialPath, String destinationPath) {
        initComponents();

        progressController = new BackupProgressController();

        this.setIconImage(GuiController.getIcon(this.getClass()));

        initialPathLabel.setText(initialPath);
        destinationPathLabel.setText(destinationPath);

        closeButton.setEnabled(false);

        setTranslations();
   }

    public void updateProgressBar(int value, String fileProcessed, int filesCopiedSoFar, int totalFilesCount) {
        // editing the percentage
        progressBar.setValue(value);
        percentageLabel.setText(value + " %");

        // editing the current file zipped
        fileZippedLabel.setText(fileProcessed);

        // edit the title with counts
        setTitle(TranslationCategory.PROGRESS_BACKUP_FRAME.getTranslation(TranslationKey.PROGRESS_BACKUP_TITLE) + " - " + filesCopiedSoFar + "/" + totalFilesCount);

        if (value == 100) {
            loadingMessageLabel.setText(TranslationCategory.PROGRESS_BACKUP_FRAME.getTranslation(TranslationKey.STATUS_COMPLETED));
            closeButton.setEnabled(true);
            CancelButton.setEnabled(false);
            fileZippedLabel.setText("");
            this.setAlwaysOnTop(true);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        loadingMessageLabel = new javax.swing.JLabel();
        percentageLabel = new javax.swing.JLabel();
        CancelButton = new javax.swing.JButton();
        initialPathLabel = new javax.swing.JLabel();
        destinationPathLabel = new javax.swing.JLabel();
        fileZippedLabel = new javax.swing.JLabel();

        jLabel4.setText("jLabel4");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Backup in progress");
        setMaximumSize(new java.awt.Dimension(430, 175));
        setMinimumSize(new java.awt.Dimension(430, 175));
        setResizable(false);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        loadingMessageLabel.setText("loading...");

        percentageLabel.setText("0%");

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        initialPathLabel.setText("path1");
        initialPathLabel.setMaximumSize(new java.awt.Dimension(415, 16));
        initialPathLabel.setMinimumSize(new java.awt.Dimension(415, 16));
        initialPathLabel.setPreferredSize(new java.awt.Dimension(415, 16));

        destinationPathLabel.setText("path2");
        destinationPathLabel.setMaximumSize(new java.awt.Dimension(415, 16));
        destinationPathLabel.setMinimumSize(new java.awt.Dimension(415, 16));
        destinationPathLabel.setPreferredSize(new java.awt.Dimension(415, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 260, Short.MAX_VALUE)
                        .addComponent(CancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileZippedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                                    .addComponent(loadingMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(percentageLabel))
                            .addComponent(initialPathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(destinationPathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(initialPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(destinationPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(fileZippedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadingMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(percentageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeButton)
                    .addComponent(CancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        progressController.handleCancelButtonRequest(this);
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void setTranslations() {
        setTitle(TranslationCategory.PROGRESS_BACKUP_FRAME.getTranslation(TranslationKey.PROGRESS_BACKUP_TITLE));
        CancelButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.CANCEL_BUTTON));
        closeButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.CLOSE_BUTTON));
        loadingMessageLabel.setText(TranslationCategory.PROGRESS_BACKUP_FRAME.getTranslation(TranslationKey.STATUS_LOADING));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel destinationPathLabel;
    private javax.swing.JLabel fileZippedLabel;
    private javax.swing.JLabel initialPathLabel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel loadingMessageLabel;
    private javax.swing.JLabel percentageLabel;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
