package jav.gui.main;

import jav.gui.events.MessageCenter;
import jav.gui.events.saved.SavedEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.windows.WindowManager;

/**
 *Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:

 *Redistributions of source code must retain the above copyright
 *notice, this list of conditions and the following disclaimer.
 *Redistributions in binary form must reproduce the above copyright
 *notice, this list of conditions and the following disclaimer in the
 *documentation and/or other materials provided with the distribution.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This file is part of the ocr-postcorrection tool developed
 * by the IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 * For further information and contacts visit http://ocr.cis.uni-muenchen.de/
 * 
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class Saver implements SaveCookie {

    private final int BUFSIZ = 4096;

    @Override
    public void save() throws IOException {
        
        MainController.findInstance().discardEdits();
        MessageCenter.getInstance().fireSavedEvent(new SavedEvent(this));
//
//        ProgressRunnable<Integer> r = new ProgressRunnable<Integer>() {
//
//            @Override
//            public Integer run(ProgressHandle ph) {
//
//                try {
//                    ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("saving"));
//                    ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("saving"));
//                    //no project filename set
//                    if (MainController.findInstance().getCorrectionSystem().getDocument().getProjectFilename() == null) {
//                        MainController.findInstance().createNewDocTempFile();
//                        saveAs();
//                    } else {
//                        MainController.findInstance().discardEdits();
//                        MainController.findInstance().getDocument().saveas(MainController.findInstance().getTempDocFilename());
//                        File f = new File(MainController.findInstance().getCorrectionSystem().getProjectFilename());
//                        f.delete();
//                        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(MainController.findInstance().getCorrectionSystem().getProjectFilename()));
//                        out.setLevel(Deflater.DEFAULT_COMPRESSION);
//                        FileInputStream in = new FileInputStream(MainController.findInstance().getTempDocFilename());
//                        out.putNextEntry(new ZipEntry("document.ocrcxml"));
//                        int len;
//                        byte inbuf[] = new byte[BUFSIZ];
//                        while ((len = in.read(inbuf, 0, BUFSIZ)) != -1) {
//                            out.write(inbuf, 0, len);
//                        }
//                        out.closeEntry();
//                        in.close();
//                        if (MainController.findInstance().getTempProfileFilename() != null) {
//                            in = new FileInputStream(MainController.findInstance().getTempProfileFilename());
//                            out.putNextEntry(new ZipEntry("profile.xml"));
//                            while ((len = in.read(inbuf, 0, BUFSIZ)) != -1) {
//                                out.write(inbuf, 0, len);
//                            }
//                            in.close();
//                            out.closeEntry();
//                        }
//                        
//                        if( MainController.findInstance().getTempPropertiesFilename() == null) {
//                            MainController.findInstance().createNewPropTempFile();
//                        }
//                        
//                        FileOutputStream tempout = new FileOutputStream(MainController.findInstance().getTempPropertiesFilename());
//                        MainController.findInstance().getDocumentProperties().store(tempout, "");
//                        tempout.close();
//                        in = new FileInputStream(MainController.findInstance().getTempPropertiesFilename());
//                        out.putNextEntry(new ZipEntry("properties.ini"));
//                        while ((len = in.read(inbuf, 0, BUFSIZ)) != -1) {
//                            out.write(inbuf, 0, len);
//                        }
//                        in.close();
//                        out.closeEntry();                                                   
//                        out.close();
//                    }
//
//                    MessageCenter.getInstance().fireSavedEvent(new SavedEvent(this));
//                    MRUFilesOptions opts = MRUFilesOptions.getInstance();
//                    opts.addFile(MainController.findInstance().getCorrectionSystem().getProjectFilename());
//                    return 0;
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                    return -1;
//                }
//            }
//        };
//        ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("saving"), false);
    }

    public void saveAs() throws IOException {
        JFileChooser jfc = new JFileChooser();
//        AbstractButton button = SwingUtils.getDescendantOfType(AbstractButton.class, jfc, "Icon", UIManager.getIcon("FileChooser.detailsViewIcon"));
//        button.doClick();

        jfc.setDialogTitle(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("ocrproj_file"));
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".ocrczip") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("ocrproj_file");
            }
        });

        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();

            if (!file.getName().endsWith(".ocrczip")) {
                String newName = file.getName() + ".ocrczip";
                file = new File(file.getParent(), newName);
            }
            
            this.saveAs(file.getCanonicalPath(), true);
        }
    }

    public void saveAs(FileObject fo, String string) throws IOException {

        String path = fo.getPath();
        if (!string.endsWith(".ocrczip")) {
            string = string + ".ocrczip";
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        
        this.saveAs(path + string, true);
    }

    public void saveAs(String filename, final boolean changeProjName) throws IOException {
        
        final File filetoSave = new File(filename);

        MainController.findInstance().discardEdits();
        ProgressRunnable<Integer> r = new ProgressRunnable<Integer>() {

            @Override
            public Integer run(ProgressHandle ph) {
                return 0;
//                try {
//                    ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("saving"));
//                    ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("saving"));
//                    MainController.findInstance().discardEdits();
////                    MainController.findInstance().getDocument().saveas(MainController.findInstance().getTempDocFilename());
//                    if( changeProjName == true) {
////                        MainController.findInstance().getCorrectionSystem().setProjectFilename(filetoSave.getCanonicalPath());
//                    }
//                    if (filetoSave.exists()) {
//                        filetoSave.delete();
//                    }
//                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filetoSave.getCanonicalPath()));
//                    out.setLevel(Deflater.DEFAULT_COMPRESSION);
//                    FileInputStream in = new FileInputStream(MainController.findInstance().getTempDocFilename());
//                    out.putNextEntry(new ZipEntry("document.ocrcxml"));
//                    int len;
//                    byte inbuf[] = new byte[BUFSIZ];
//                    while ((len = in.read(inbuf, 0, BUFSIZ)) != -1) {
//                        out.write(inbuf, 0, len);
//                    }
//                    out.closeEntry();
//                    in.close();
//                    if (MainController.findInstance().getTempProfileFilename() != null) {
//                        in = new FileInputStream(MainController.findInstance().getTempProfileFilename());
//                        out.putNextEntry(new ZipEntry("profile.xml"));
//                        while ((len = in.read(inbuf, 0, BUFSIZ)) != -1) {
//                            out.write(inbuf, 0, len);
//                        }
//                        in.close();
//                        out.closeEntry();
//                    }
//                    
//                    if( MainController.findInstance().getTempPropertiesFilename() == null) {
//                        MainController.findInstance().createNewPropTempFile();
//                    }
//                    
//                    FileOutputStream tempout = new FileOutputStream(MainController.findInstance().getTempPropertiesFilename());
//                    MainController.findInstance().getDocumentProperties().store(tempout, "");
//                    tempout.close();
//                    in = new FileInputStream(MainController.findInstance().getTempPropertiesFilename());
//                    out.putNextEntry(new ZipEntry("properties.ini"));
//                    while ((len = in.read(inbuf, 0, BUFSIZ)) != -1) {
//                        out.write(inbuf, 0, len);
//                    }
//                    in.close();
//                    out.closeEntry();                    
//                    out.close();
//                    MessageCenter.getInstance().fireSavedEvent(new SavedEvent(this));
//                    if( changeProjName == true) {
//                        MRUFilesOptions opts = MRUFilesOptions.getInstance();
//                        opts.addFile(filetoSave.getCanonicalPath());
//                    }
//
//                    return 0;
//                } catch (Exception ex) {
//                    Exceptions.printStackTrace(ex);
//                    return -1;
//                }
            }
        };
        ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("saving"), false);
        JFrame f = (JFrame) WindowManager.getDefault().getMainWindow();
        f.setTitle(filetoSave.getCanonicalPath());
    }
}