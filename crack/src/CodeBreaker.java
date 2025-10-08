import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.StatusWindow;
import client.view.WorklistItem;
import client.view.ProgressItem;

import network.Sniffer;
import network.sniffer.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

import java.math.BigInteger;
import javax.swing.*;
import client.view.WorklistItem;
import client.view.ProgressItem;
import rsa.Factorizer;
import rsa.ProgressTracker;
import java.awt.FlowLayout;
import java.awt.BorderLayout;



public class CodeBreaker implements SnifferCallback {

    private final JPanel workList;
    private final JPanel progressList;
    
    private final JProgressBar mainProgressBar;

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    

    // -----------------------------------------------------------------------
    
    private CodeBreaker() {
        StatusWindow w  = new StatusWindow();

        workList        = w.getWorkList();
        progressList    = w.getProgressList();
        mainProgressBar = w.getProgressBar();
    }
    
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {

        /*
         * Most Swing operations (such as creating view elements) must be performed in
         * the Swing EDT (Event Dispatch Thread).
         * 
         * That's what SwingUtilities.invokeLater is for.
         */

        SwingUtilities.invokeLater(() -> {
            CodeBreaker codeBreaker = new CodeBreaker();
            new Sniffer(codeBreaker).start();
        });

        
    }

    // -----------------------------------------------------------------------

    /** Called by a Sniffer thread when an encrypted message is obtained. */
    @Override
    public void onMessageIntercepted(String message, BigInteger n) {
        

       

        WorklistItem item = new WorklistItem(n, message);
        

        SwingUtilities.invokeLater(() -> {
            JButton b = new JButton("Break");
            JButton c = new JButton("Cancel");
            JButton r = new JButton("Remove");
            
            workList.add(item);
            item.add(b);
            workList.revalidate();
            b.addActionListener(e -> {
                workList.remove(item);

                ProgressItem progressItem = new ProgressItem(n, message);

                
                progressList.add(progressItem);
                mainProgressBar.setMaximum(mainProgressBar.getMaximum() + 1_000_000);
                
                
                
                progressItem.revalidate();

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.add(c);
                progressItem.add(buttonPanel, BorderLayout.SOUTH);

                r.addActionListener(e3 -> {
                    
                    System.out.println("Remove1: Main: " + mainProgressBar.getValue() + " / " + mainProgressBar.getMaximum());
                    SwingUtilities.invokeLater(() -> {
                        progressList.remove(progressItem);
                        // Justera värdet proportionellt när vi tar bort en färdig uppgift
                        int currentValue = mainProgressBar.getValue();
                        int currentMax = mainProgressBar.getMaximum();
                        mainProgressBar.setMaximum(currentMax - 1_000_000);
                        // Justera värdet proportionellt för att behålla samma andel
                        if (currentMax > 0) {
                            int newValue = Math.max(0, currentValue - 1_000_000);
                            mainProgressBar.setValue(newValue);
                        }
                    });

                    
                    progressList.revalidate();
                    progressList.repaint();
                    System.out.println("Remove 2: Main: " + mainProgressBar.getValue() + " / " + mainProgressBar.getMaximum());
                });

                pool.submit(() -> {
                    try {
                        Factorizer.crack(message, n, new ProgressTracker(){
                            @Override
                            public void onProgress(int ppmDelta) {
                                //System.out.println("Progress update: " + ppmDelta);
                                 System.out.println("Main: " + mainProgressBar.getValue() + " / " + mainProgressBar.getMaximum());
                                JProgressBar bar = progressItem.getProgressBar();
                                int newValue = Math.min(bar.getValue()+ ppmDelta, bar.getMaximum());
                                bar.setValue(newValue);
                                if(bar.getValue()<= bar.getMaximum()){
                                    updateMainProgressBar(ppmDelta);
                                }
                               
                                if(bar.getValue() >= 1_000_000){
                                    buttonPanel.remove(c);
                                    buttonPanel.add(r);
                                    buttonPanel.revalidate();
                                }
                            }
                        });
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
            });
        });
        
    }
    private void updateMainProgressBar(int delta) {
        SwingUtilities.invokeLater(() -> {
            
            int newValue = mainProgressBar.getValue() + delta;   
            mainProgressBar.setValue(Math.min(newValue, mainProgressBar.getMaximum()));
        });
    }

}

