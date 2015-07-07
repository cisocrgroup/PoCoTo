/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.gui.main;

import jav.gui.events.MessageCenter;
import jav.gui.events.saved.SavedEvent;
import jav.logging.log4j.Log;
import java.io.File;
import java.io.IOException;
import static java.lang.Thread.sleep;
import javax.swing.JFrame;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.windows.WindowManager;

/**
 *
 * @author finkf
 */
public class BackgroundSaver extends Saver implements Runnable {
    private static final int SLEEP_TIME_MILLIS = 60 * 10 * 1000;
    @Override
    public void run() {
        while (true) {
            try {
                sleepAndThenSave();
            } catch (InterruptedException|IOException e) {
                Log.error(this, "could not sleepAndThenSave: " + e.getMessage());
            }
        }
            
    }
    private void sleepAndThenSave() throws InterruptedException, IOException {
        sleep(SLEEP_TIME_MILLIS);
        String projectdir = MainController.findInstance()
                .getDocumentProperties()
                .getProperty("databasepath");
        
        if (projectdir == null)
            return;
        File backupFile = new File(projectdir, "backup.ocrzip");
        Log.info(
                this, 
                "slept for %d seconds; saving to %s",
                SLEEP_TIME_MILLIS / 1000,
                backupFile.getCanonicalPath()
        );
        save();
        Log.info(this, "done saving");
    }
    
    @Override
    public void save() throws IOException {
        // do *not* discard edits
        MessageCenter.getInstance().fireSavedEvent(new SavedEvent(this));
    }
    
}
