package com.mfc.GuiApplication.mvc.controller;

import com.mfc.GuiApplication.entity.CellTower;

public interface TraceModeWorker {
    void work(CellTower cell_true, CellTower cell_predicted);
}
