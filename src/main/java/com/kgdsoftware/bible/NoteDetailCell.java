/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kgdsoftware.bible;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author henriwarren
 */
public class NoteDetailCell extends javax.swing.JPanel {
    public static SimpleDateFormat sDF = new SimpleDateFormat("MMM dd.yyy HH:mm");
    /**
     * Creates new form NoteDetailCell
     */

    public NoteDetailCell() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        idLabel = new javax.swing.JLabel();
        verseIdLabel = new javax.swing.JLabel();
        remoteTimeLabel = new javax.swing.JLabel();
        localTimeLabel = new javax.swing.JLabel();
        verseLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        checkedLabel = new javax.swing.JLabel();
        changedLabel = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        setBackground(new java.awt.Color(255, 255, 255));

        idLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        idLabel.setText("id");

        verseIdLabel.setBackground(new java.awt.Color(255, 255, 255));
        verseIdLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        verseIdLabel.setText("verseId");

        remoteTimeLabel.setBackground(new java.awt.Color(255, 51, 51));
        remoteTimeLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        remoteTimeLabel.setText("remoteTime");

        localTimeLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        localTimeLabel.setText("localTime");

        verseLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        verseLabel.setText("verse");

        jLabel2.setText("jLabel2");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        checkedLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        checkedLabel.setText("jLabel3");

        changedLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        changedLabel.setText("jLabel4");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(idLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(verseLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(remoteTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addComponent(localTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(verseIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(changedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idLabel)
                    .addComponent(verseLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verseIdLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remoteTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkedLabel)
                    .addComponent(changedLabel))
                .addGap(0, 0, 0)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void setId(long id) {
        idLabel.setText("id: " + id);
    }
    public void setLocalTime(long time) {
        localTimeLabel.setText("L " + sDF.format(new Date(time)));
    }
    public void setRemoteTime(long time) {
        remoteTimeLabel.setText("R " + sDF.format(new Date(time*1000)));
    }
    public void setVerse(int bookId, int chapter, int verse) {
        verseLabel.setText("(" + bookId + ") " + chapter + ":" + verse);
    }
    public void setVerseId(int verseId) {
        verseIdLabel.setText("VerseId: " + verseId);
    }
    public void setChecked(boolean checked) {
        checkedLabel.setText(checked ? "checked" : "not checked");
    }
    public void setChanged(boolean changed) {
        changedLabel.setText(changed ? "changed" : "not changed");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel changedLabel;
    private javax.swing.JLabel checkedLabel;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel localTimeLabel;
    private javax.swing.JLabel remoteTimeLabel;
    private javax.swing.JLabel verseIdLabel;
    private javax.swing.JLabel verseLabel;
    // End of variables declaration//GEN-END:variables
}