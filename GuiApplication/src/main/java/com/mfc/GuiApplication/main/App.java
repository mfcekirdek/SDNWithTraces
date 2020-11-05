package com.mfc.GuiApplication.main;


/*
 * Hello world!
 */

import com.mfc.GuiApplication.learning.HMM_Controller;
import com.mfc.GuiApplication.mvc.controller.Controller;
import com.mfc.GuiApplication.mvc.model.Model;
import com.mfc.GuiApplication.mvc.view.View;

import javax.swing.*;

import java.util.ArrayList;
import java.util.HashMap;

public class App {
  HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Model model_true = new Model();
        Model model_predicted = new Model();
        Model model_total = new Model();
        Model model_merged = new Model();

        View view = new View();
        Controller controller =
            new Controller(model_true, model_predicted, model_merged, model_total, view);
        controller.control();

        System.err.println("Please wait for HMM Training..");

        HMM_Controller hc = HMM_Controller.getInstance(view);

        Thread hmm_starter_thread = new Thread() {
          public void run() {
            if(hc.getHmmProcess() != null)
              hc.closeProcessAndConnections();
            hc.startHMMProcess();
            System.out.println("The model is ready..");
          }
        };

        hmm_starter_thread.start();

      }
    });
  }

}
