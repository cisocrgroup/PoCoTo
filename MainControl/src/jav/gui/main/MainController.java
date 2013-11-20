package jav.gui.main;

import cis.profiler.client.ProfilerWebServiceCallbackHandler;
import cis.profiler.client.ProfilerWebServiceStub;
import cis.profiler.client.ProfilerWebServiceStub.AbortProfilingRequest;
import cis.profiler.client.ProfilerWebServiceStub.AbortProfilingRequestType;
import cis.profiler.client.ProfilerWebServiceStub.AbortProfilingResponse;
import cis.profiler.client.ProfilerWebServiceStub.AbortProfilingResponseType;
import cis.profiler.client.ProfilerWebServiceStub.AttachmentType;
import cis.profiler.client.ProfilerWebServiceStub.Base64Binary;
import cis.profiler.client.ProfilerWebServiceStub.GetProfileRequest;
import cis.profiler.client.ProfilerWebServiceStub.GetProfileRequestType;
import cis.profiler.client.ProfilerWebServiceStub.GetProfileResponse;
import cis.profiler.client.ProfilerWebServiceStub.GetProfileResponseType;
import cis.profiler.client.ProfilerWebServiceStub.GetProfilingStatusRequest;
import cis.profiler.client.ProfilerWebServiceStub.GetProfilingStatusRequestType;
import cis.profiler.client.ProfilerWebServiceStub.GetProfilingStatusResponse;
import cis.profiler.client.ProfilerWebServiceStub.GetProfilingStatusResponseType;
import cis.profiler.client.ProfilerWebServiceStub.SimpleEnrichRequest;
import cis.profiler.client.ProfilerWebServiceStub.SimpleEnrichRequestType;
import cis.profiler.client.ProfilerWebServiceStub.SimpleEnrichResponse;
import cis.profiler.client.ProfilerWebServiceStub.SimpleEnrichResponseType;
import jav.correctionBackend.*;
import jav.gui.cookies.CorrectionSystemReadyCookie;
import jav.gui.cookies.DocumentLoadedCookie;
import jav.gui.cookies.ProfilerIDCookie;
import jav.gui.dialogs.CustomErrorDialog;
import jav.gui.events.MessageCenter;
import jav.gui.events.cancel.CancelEvent;
import jav.gui.events.concordance.ConcordanceEvent;
import jav.gui.events.concordance.ConcordanceType;
import jav.gui.events.documentChanged.DocumentChangedEvent;
import jav.gui.events.saved.SavedEvent;
import jav.gui.events.saved.SavedEventSlot;
import jav.gui.events.tokenStatus.*;
import jav.gui.filter.AbstractTokenFilter;
import jav.gui.main.undoredo.MyUndoableEdit;
import java.awt.Component;
import java.awt.Cursor;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.awt.UndoRedo;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und
 * Sprachverarbeitung, University of Munich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * This file is part of the ocr-postcorrection tool developed by the IMPACT
 * working group at the Centrum für Informations- und Sprachverarbeitung,
 * University of Munich. For further information and contacts visit
 * http://ocr.cis.uni-muenchen.de/
 *
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class MainController implements Lookup.Provider, TokenStatusEventSlot, SavedEventSlot {

    private static final Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
    private static final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private InstanceContent content;
    private Lookup lookup;
    private static MainController instance;
    private CorrectionSystem cs = null;
    private Saver saver;
    private SaveAsWrap saveAs;
    private Preferences node;
    private AbstractEditorViewTopComponent lastFocusedTopComponent = null;
    private CorrectionSystemReadyCookie csrcookie;
    private DocumentLoadedCookie docloadcookie;
    private String baseDir;
    private boolean logging;
    private Logger logger;
    private FileAppender appender;
    private boolean docOpened = false;
    private Document globalDocument = null;
    private Properties docproperties;
    private String profiler_user_id = null;
    private ProfilerIDCookie profileridcookie = null;
    private boolean done;
    private ProfilerWebServiceStub stub = null;
    private File tempFile;
    private UndoRedo.Manager manager = null;
    
    static {
        instance = new MainController();
    }

    public static MainController findInstance() {
        return instance;
    }

    public MainController() {

        try {
            System.loadLibrary("mlib_jai");
        } catch( UnsatisfiedLinkError e ) {
            e.printStackTrace();
        }
        content = new InstanceContent();
        lookup = new AbstractLookup(content);
        saver = new Saver();
        saveAs = new SaveAsWrap();
        manager = new UndoRedo.Manager();
        manager.setLimit(-1);
        
//        timestamper = new Timestamper();

        try {
            // create an instance of the ProfilerStub
            stub = new ProfilerWebServiceStub("http://diener.cis.uni-muenchen.de:8080/axis2/services/ProfilerWebService");
            stub._getServiceClient().getOptions().setManageSession(true);
            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(3600000);
        } catch (AxisFault ex) {
            stub = null;
        }

        if (System.getProperty("user.dir").endsWith("trunk/netbeans")) {
            baseDir = "../";
        } else {
            try {
                File f = new File(System.getProperty("user.dir"));
                f.createNewFile();
                baseDir = f.getCanonicalPath();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        node = NbPreferences.forModule(this.getClass());
        logging = node.getBoolean("logging", true);
        if (logging) {
            logger = Logger.getLogger("Logger");
            try {
                appender = new FileAppender(new PatternLayout("%d{dd-MM-yyyy HH:mm:ss} - %m%n"), baseDir + File.separator + "log.txt", true);
                logger.setLevel(Level.ALL);
                logger.addAppender(appender);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        docproperties = new Properties();
        csrcookie = new CorrectionSystemReadyCookie();
        docloadcookie = new DocumentLoadedCookie();
        cs = new CorrectionSystem();
        content.add(csrcookie);
        this.refreshID();

        MessageCenter.getInstance().addTokenStatusEventListener(this);
        MessageCenter.getInstance().addSavedEventListener(this);
    }

    public String getProfilerUserID() {
        return this.profiler_user_id;
    }

    public void addToLog(String msg) {
        logger.log(Level.INFO, msg);
    }

    public UndoRedo getUndoRedo() {
        if( globalDocument != null) {
            return globalDocument.getUndoRedoManager();
        } else {
            return manager;
        }
    }

    public Saver getSaver() {
        return saver;
    }

    public SaveAsWrap getSaveAsWrap() {
        return saveAs;
    }

    public void shutDown() {
        if (this.docOpened) {
            try {
                globalDocument.cleanupDatabase();
            } finally {
                cs.closeDocument();
            }
        }
    }

    public void createDocumentNoAnalysis(final String inputDirPath, final FileType t, String encoding, String propertiespath, String projectpath, String projectname) {
        ProgressRunnable<Document> r = new DocumentCreator(inputDirPath, t, encoding, propertiespath, projectpath, projectname);
        Document d = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"), true);
        globalDocument = d;
        globalDocument.setUndoRedoManager(manager);

        if (d != null) {
            content.add(saveAs);
            content.add(csrcookie);
            content.add(docloadcookie);
            this.refreshID();
            MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, d));
        } else {
            content.add(csrcookie);
        }
    }

    public void createDocumentNoAnalysis(final String inputDirPath, final String imgDirPath, final FileType t, String encoding, String propertiespath, String projectpath, String projectname) {
        ProgressRunnable<Document> r = new DocumentCreator(inputDirPath, imgDirPath, t, encoding, propertiespath, projectpath, projectname);
        Document d = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"), true);

        content = new InstanceContent();
        lookup = new AbstractLookup(content);

        if (d != null) {
            globalDocument = d;
            globalDocument.setUndoRedoManager(manager);
            content.add(saveAs);
            content.add(csrcookie);
            content.add(docloadcookie);
            this.refreshID();
            MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, d));
        } else {
            content.add(csrcookie);
        }
    }

    public void createDocumentAnalysis(String xmlDirName, String imgDirName, FileType inputType, String encoding, String propertiespath, String projectpath, String projectname, String configuration) {
        ProgressRunnable<Document> r = new DocumentCreator(xmlDirName, imgDirName, inputType, encoding, propertiespath, projectpath, projectname);
        Document d = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"), true);

        content = new InstanceContent();
        lookup = new AbstractLookup(content);

        if (d != null) {
            globalDocument = d;
            globalDocument.setUndoRedoManager(manager);
            content.add(saveAs);
            content.add(csrcookie);
            content.add(docloadcookie);
            this.refreshID();
            this.simpleProfileDocument(configuration);
            MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, d));
        } else {
            content.add(csrcookie);
        }
    }

    public void createDocumentAnalysis(String xmlDirName, FileType inputType, String encoding, String propertiespath, String projectpath, String projectname, String configuration) {
        ProgressRunnable<Document> r = new DocumentCreator(xmlDirName, inputType, encoding, propertiespath, projectpath, projectname);
        Document d = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"), true);

        if (d != null) {
            globalDocument = d;
            globalDocument.setUndoRedoManager(manager);
            content.add(saveAs);
            content.add(csrcookie);
            content.add(docloadcookie);
            this.refreshID();
            this.simpleProfileDocument(configuration);
            MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, d));
        } else {
            content.add(csrcookie);
        }
    }

    public void loadDocument(final String doc) {
        ProgressRunnable<Document> r = new DocumentLoader(doc);
        Document d = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"), true);

        content = new InstanceContent();
        lookup = new AbstractLookup(content);
        
        this.refreshID();

        if (d != null) {
            // TODO implement error handling because undoredo only has content at this point if tool crashed without proper shutdown
            globalDocument = d;
            globalDocument.setUndoRedoManager(manager);            
            globalDocument.truncateUndoRedo();
            content.add(saveAs);
            content.add(csrcookie);
            content.add(docloadcookie);
            this.refreshID();
            MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, globalDocument));
        } else {
            content.add(csrcookie);
        }
    }

    @Deprecated
    public void importOCRCXMLasNewProject(String ocrcxmlp, String imgdirp, String propp, String projp, String projname, String profilename) {
        ProgressRunnable<Document> r = new OCRCXMLasNewProjectImporter(ocrcxmlp, imgdirp, propp, projp, projname, profilename);
        Document d = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"), true);

        content = new InstanceContent();
        lookup = new AbstractLookup(content);

        if (d != null) {
            globalDocument = d;
            globalDocument.setUndoRedoManager(manager);
            content.add(saveAs);
            content.add(csrcookie);
            content.add(docloadcookie);
            this.refreshID();
            MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, d));
        } else {
            content.add(csrcookie);
        }
    }

    @Deprecated
    public void simpleProfileDocument(String configuration) {
        try {
            ProgressRunnable<Integer> r = new DocumentSimpleProfiler(configuration);
            Integer retval = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"), true);
            if (retval != null) {
                if (retval == 0) {
                    MainController.findInstance().getSaver().save();
                } else {
                    System.out.println("ERRROR" + retval);
                    new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("profile_error"));
                }
            }
        } catch (IOException ex) {
            System.err.println("SimpleEnrichError");
            Exceptions.printStackTrace(ex);
        }

    }

    public void profileDocument(String configuration) {
        try {
            ProgressRunnable<Integer> r = new DocumentProfiler(configuration);
            Integer retval = ProgressUtils.showProgressDialogAndRun(r, java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"), true);
            if (retval != null) {
                if (retval == 0) {
//                    MainController.findInstance().getSaver().save();
                    MainController.findInstance().reloadDocument();
                } else {
                    System.out.println("ERRROR" + retval);
                    new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/dialogs/Bundle").getString("profile_error"));
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public CorrectionSystem getCorrectionSystem() {
        return cs;
    }

    public void correctTokenByString(final int tokenID, final String cand) {
        try {
            final int undo_redo = globalDocument.getUndoRedoIndex();
            globalDocument.correctTokenByString(tokenID, cand);
            if( globalDocument.getUndoRedoManager() == null) {
                System.out.println("NULL MANAGER");
            }
            globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.CORRECTED, undo_redo)));
            MessageCenter.getInstance().fireTokenStatusEvent(new CorrectedEvent(this, tokenID, TokenStatusType.CORRECTED, cand, true));
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void correctTokenByCandidate(int tokenID, Candidate cand) {
        this.correctTokenByString(tokenID, cand.getSuggestion());
    }

    public void correctTokensByString(HashMap<Integer, String> art) {
        try {
            int undo_redo = globalDocument.getUndoRedoIndex();
            if (globalDocument.correctTokensByString(art)) {
                Iterator<Integer> iter = art.keySet().iterator();
                while (iter.hasNext()) {
                    int tokenId = iter.next();
                    MessageCenter.getInstance().fireTokenStatusEvent(new CorrectedEvent(this, tokenId, TokenStatusType.CORRECTED, art.get(tokenId), true));
                }
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.MULTICORRECTED, undo_redo)));
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void applyFilter(AbstractTokenFilter fc) {
        ArrayList<Token> filterResult = fc.applyFilter(cs.getDocument().tokenIterator());
        MessageCenter.getInstance().fireConcordanceEvent(new ConcordanceEvent(this, ConcordanceType.DIVERSE, filterResult, fc.getName()));
    }

    public void mergeRightward(final int tokenID, int numTok) {
        try {
            MainController.changeCursorWaitStatus(true);
            int undo_redo = globalDocument.getUndoRedoIndex();
            ArrayList<Integer> retval = globalDocument.mergeRightward(tokenID, numTok);
            if (retval.size() > 0) {
                MessageCenter.getInstance().fireTokenStatusEvent(new MergeEvent(this, tokenID, TokenStatusType.MERGED_RIGHT, retval));
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.MERGE, undo_redo)));
            }
            MainController.changeCursorWaitStatus(false);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void mergeRightward(final int tokenID) {
        try {
            MainController.changeCursorWaitStatus(true);
            int undo_redo = globalDocument.getUndoRedoIndex();
            ArrayList<Integer> retval = globalDocument.mergeRightward(tokenID);
            if (retval.size() > 0) {
                MessageCenter.getInstance().fireTokenStatusEvent(new MergeEvent(this, tokenID, TokenStatusType.MERGED_RIGHT, retval));
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.MERGE, undo_redo)));
            }
            MainController.changeCursorWaitStatus(false);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Page getPage(int index) {
        return this.globalDocument.getPage(index);
    }

    public void removeEdit(int editid) {
        globalDocument.removeEdit(editid);
    }

    public void setCorrected(int index, boolean b) {
        try {
            int undo_redo = globalDocument.getUndoRedoIndex();
            if (globalDocument.setCorrected(index, b)) {
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.SETCORRECTED, undo_redo)));
                MessageCenter.getInstance().fireTokenStatusEvent(new SetCorrectedEvent(this, index, TokenStatusType.SETCORRECTED, b));
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void setCorrected(ArrayList<Integer> art, boolean b) {
        try {
            int undo_redo = globalDocument.getUndoRedoIndex();
            if (globalDocument.setCorrected(art, b)) {
                for (Integer indexInDocument : art) {
                    MessageCenter.getInstance().fireTokenStatusEvent(new SetCorrectedEvent(this, indexInDocument, TokenStatusType.SETCORRECTED, b));
                }
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.MULTISETCORRECTED, undo_redo)));
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void deleteToken(int index) {
        try {
            MessageCenter.getInstance().fireCancelEvent(new CancelEvent(this));

            MainController.changeCursorWaitStatus(true);
            int undo_redo = globalDocument.getUndoRedoIndex();
            ArrayList<Integer> retval = this.globalDocument.deleteToken(index);
            if (retval.size() > 0) {
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.DELETE, undo_redo)));
                MessageCenter.getInstance().fireTokenStatusEvent(new DeleteEvent(this, index, TokenStatusType.DELETE, retval));
            }
            MainController.changeCursorWaitStatus(false);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }        
    }

    public void deleteToken(int begin, int afterend) {
        try {
            MessageCenter.getInstance().fireCancelEvent(new CancelEvent(this));

            MainController.changeCursorWaitStatus(true);
            int undo_redo = globalDocument.getUndoRedoIndex();
            ArrayList<Integer> retval = this.globalDocument.deleteToken(begin, afterend);
            if (retval.size() > 0) {
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.DELETE, undo_redo)));
                MessageCenter.getInstance().fireTokenStatusEvent(new DeleteEvent(this, begin, TokenStatusType.DELETE, retval));
            }
            MainController.changeCursorWaitStatus(false);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean getDocOpen() {
        return this.docOpened;
    }

    public HashMap<String, OCRErrorInfo> computeErrorFreqList() {
        return globalDocument.computeErrorFreqList();
    }

    public void setLastFocusedTopComponent(AbstractEditorViewTopComponent tc) {
        if (this.lastFocusedTopComponent != null) {
            content.remove(this.lastFocusedTopComponent.getGlobalActions());
        }
        this.lastFocusedTopComponent = tc;
        content.add(this.lastFocusedTopComponent.getGlobalActions());
    }

    public TopComponent getLastFocusedTopComponent() {
        return this.lastFocusedTopComponent;
    }

    public String getLastFocusedTCName() {
        return this.lastFocusedTopComponent.getClass().getSimpleName();
    }

    public void undoAll() {
        MainController.changeCursorWaitStatus(true);
        globalDocument.undoAll();
        this.discardEdits();
        MainController.changeCursorWaitStatus(false);
    }

    public void discardEdits() {
        globalDocument.truncateUndoRedo();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public void addToLookup(Object o) {
        content.add(o);
    }

    public void removeFromLookup(Object o) {
        content.remove(o);
    }

    public boolean hasUnsavedChanges() {
        Saver result = lookup.lookup(Saver.class);
        if (result != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void dispatchEvent(TokenStatusEvent e) {
        Saver result = lookup.lookup(Saver.class);
        if (result == null) {
            content.add(getSaver());
        }
    }

    @Override
    public void dispatchEvent(SavedEvent e) {
        content.remove(getSaver());
    }

    public void startNoDisturbWork(AbstractEditorViewTopComponent tc) {
        content.remove(tc.getGlobalActions());
    }

    public void endNoDisturbWork(AbstractEditorViewTopComponent tc) {
        content.add(tc.getGlobalActions());
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setLogging(boolean b) {
        this.logging = b;
    }

    public boolean getLogging() {
        return this.logging;
    }

    public void customizeScrollPane(JScrollPane jsp) {

        jsp.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);

        JScrollBar vertical = jsp.getVerticalScrollBar();
        InputMap im = vertical.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("PAGE_UP"), "negativeBlockIncrement");
        im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "positiveBlockIncrement");

        im.put(KeyStroke.getKeyStroke("END"), "maxScroll");
        im.put(KeyStroke.getKeyStroke("HOME"), "minScroll");
    }

    public Document getDocument() {
        return this.globalDocument;
    }

    public Properties getDocumentProperties() {
        return this.docproperties;
    }

    public ProfilerWebServiceStub getProfilerWebServiceStub() {
        return stub;
    }

    public void reloadDocument() {
        MessageCenter.getInstance().fireDocumentChangedEvent(new DocumentChangedEvent(this, globalDocument));
    }

    public void refreshID() {
        this.profiler_user_id = node.get("profiler_user_id", "");
        if (stub != null) {
            if (this.profiler_user_id.equals("")) {
                if (profileridcookie != null) {
                    content.remove(profileridcookie);
                    profileridcookie = null;
                }
            } else {
                if (profileridcookie != null) {
                    content.remove(profileridcookie);
                    profileridcookie = null;
                }
                profileridcookie = new ProfilerIDCookie();
                content.add(profileridcookie);
            }
        }
    }

    public void updatePattern(Pattern p) {
        globalDocument.updatePattern(p);
    }

    public void updatePatternOccurrence(PatternOccurrence po) {
        globalDocument.updatePatternOccurrence(po);
    }

    public ArrayList<Pattern> getPatternList() {
        ArrayList<Pattern> retval = new ArrayList<>();
        Iterator<Pattern> iterp = globalDocument.patternIterator();
        while (iterp.hasNext()) {
            Pattern p = iterp.next();
            Iterator<PatternOccurrence> iterpo = globalDocument.patternOccurrenceIterator(p.getPatternID());
            while (iterpo.hasNext()) {
                p.addOccurence(iterpo.next(), false);
            }
            retval.add(p);
        }
        return retval;
    }

    public static void changeCursorWaitStatus(final boolean isWaiting) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                try {
                    JFrame mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();
                    Component glassPane = mainFrame.getGlassPane();
                    if (isWaiting) {
                        glassPane.setVisible(true);
                        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    } else {
                        glassPane.setVisible(false);
                        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                } catch (Exception e) {
                    // probably not worth handling 
                }
            }
        });
    }

    public void setDefaultProperties() {
        docproperties.setProperty("mainFontSize", "20");
        docproperties.setProperty("mainImageScale", "0.5");
        docproperties.setProperty("completeImageScale", "0.4");
        docproperties.setProperty("candidateFontSize", "13");
        docproperties.setProperty("concImageScale", "0.5");
        docproperties.setProperty("concFontSize", "20");
        docproperties.setProperty("ocrerrorFontSize", "13");
        docproperties.setProperty("profilerFontSize", "13");
        docproperties.setProperty("profiled", "false");
        docproperties.setProperty("configuration", "");
        docproperties.setProperty("hasImages", "");
    }

    public void undo(MyEditType edittype, int editid) {
        System.out.println("UNDO " + edittype.toString() + " " + editid);
        MainController.changeCursorWaitStatus(true);
        if (edittype.equals(MyEditType.SETCORRECTED)) {
            SetCorrectedUndoRedoInformation scu = (SetCorrectedUndoRedoInformation) globalDocument.undo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new SetCorrectedEvent(this, scu.getTokenID(), TokenStatusType.SETCORRECTED, scu.getSetTo()));

        } else if (edittype.equals(MyEditType.CORRECTED)) {
            CorrectedUndoRedoInformation cui = (CorrectedUndoRedoInformation) globalDocument.undo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new CorrectedEvent(this, cui.getTokenID(), TokenStatusType.CORRECTED, cui.getReplacement(), cui.getSetTo()));

        } else if (edittype.equals(MyEditType.MULTICORRECTED)) {
            MultiCorrectedUndoRedoInformation mcui = (MultiCorrectedUndoRedoInformation) globalDocument.undo(editid);
            Iterator<CorrectedUndoRedoInformation> iter = mcui.getUndoRedoInformations().iterator();
            while (iter.hasNext()) {
                CorrectedUndoRedoInformation cui = iter.next();
                MessageCenter.getInstance().fireTokenStatusEvent(new CorrectedEvent(this, cui.getTokenID(), TokenStatusType.CORRECTED, cui.getReplacement(), cui.getSetTo()));
            }

        } else if (edittype.equals(MyEditType.MULTISETCORRECTED)) {
            MultiSetCorrectedUndoRedoInformation mcui = (MultiSetCorrectedUndoRedoInformation) globalDocument.undo(editid);
            Iterator<SetCorrectedUndoRedoInformation> iter = mcui.getUndoRedoInformations().iterator();
            while (iter.hasNext()) {
                SetCorrectedUndoRedoInformation scu = iter.next();
                MessageCenter.getInstance().fireTokenStatusEvent(new SetCorrectedEvent(this, scu.getTokenID(), TokenStatusType.SETCORRECTED, scu.getSetTo()));
            }
        } else if (edittype.equals(MyEditType.MERGE)) {
            MergeUndoRedoInformation muri = (MergeUndoRedoInformation) globalDocument.undo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new SplitEvent(this, muri.getTokenID(), TokenStatusType.SPLIT, muri.getAffectedTokens()));

        } else if (edittype.equals(MyEditType.SPLIT)) {
            SplitUndoRedoInformation muri = (SplitUndoRedoInformation) globalDocument.undo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new MergeEvent(this, muri.getTokenID(), TokenStatusType.MERGED_RIGHT, muri.getAffectedTokens()));

        } else if (edittype.equals(MyEditType.DELETE)) {
            DeleteUndoRedoInformation duri = (DeleteUndoRedoInformation) globalDocument.undo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new InsertEvent(this, duri.getTokenID(), TokenStatusType.INSERT, duri.getAffectedTokens()));
        }

        if (!globalDocument.getUndoRedoManager().canUndo()) {
            content.remove(saver);
        }
        MainController.changeCursorWaitStatus(false);
    }

    public void redo(MyEditType edittype, int editid) {

        MainController.changeCursorWaitStatus(true);
        System.out.println("REDO " + edittype.toString() + " " + editid);
        if (edittype.equals(MyEditType.SETCORRECTED)) {
            SetCorrectedUndoRedoInformation scu = (SetCorrectedUndoRedoInformation) globalDocument.redo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new SetCorrectedEvent(this, scu.getTokenID(), TokenStatusType.SETCORRECTED, scu.getSetTo()));

        } else if (edittype.equals(MyEditType.CORRECTED)) {
            CorrectedUndoRedoInformation cui = (CorrectedUndoRedoInformation) globalDocument.redo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new CorrectedEvent(this, cui.getTokenID(), TokenStatusType.CORRECTED, cui.getReplacement(), cui.getSetTo()));

        } else if (edittype.equals(MyEditType.MULTICORRECTED)) {
            MultiCorrectedUndoRedoInformation mcui = (MultiCorrectedUndoRedoInformation) globalDocument.redo(editid);
            Iterator<CorrectedUndoRedoInformation> iter = mcui.getUndoRedoInformations().iterator();
            while (iter.hasNext()) {
                CorrectedUndoRedoInformation cui = iter.next();
                MessageCenter.getInstance().fireTokenStatusEvent(new CorrectedEvent(this, cui.getTokenID(), TokenStatusType.CORRECTED, cui.getReplacement(), cui.getSetTo()));
            }

        } else if (edittype.equals(MyEditType.MULTISETCORRECTED)) {
            MultiSetCorrectedUndoRedoInformation mcui = (MultiSetCorrectedUndoRedoInformation) globalDocument.redo(editid);
            Iterator<SetCorrectedUndoRedoInformation> iter = mcui.getUndoRedoInformations().iterator();
            while (iter.hasNext()) {
                SetCorrectedUndoRedoInformation scu = iter.next();
                MessageCenter.getInstance().fireTokenStatusEvent(new SetCorrectedEvent(this, scu.getTokenID(), TokenStatusType.SETCORRECTED, scu.getSetTo()));
            }

        } else if (edittype.equals(MyEditType.MERGE)) {
            MergeUndoRedoInformation muri = (MergeUndoRedoInformation) globalDocument.redo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new MergeEvent(this, muri.getTokenID(), TokenStatusType.MERGED_RIGHT, muri.getAffectedTokens()));

        } else if (edittype.equals(MyEditType.SPLIT)) {
            SplitUndoRedoInformation suri = (SplitUndoRedoInformation) globalDocument.redo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new SplitEvent(this, suri.getTokenID(), TokenStatusType.SPLIT, suri.getAffectedTokens()));

        } else if (edittype.equals(MyEditType.DELETE)) {
            DeleteUndoRedoInformation duri = (DeleteUndoRedoInformation) globalDocument.redo(editid);
            MessageCenter.getInstance().fireTokenStatusEvent(new DeleteEvent(this, duri.getTokenID(), TokenStatusType.DELETE, duri.getAffectedTokens()));
        }
        MainController.changeCursorWaitStatus(false);
    }

    public void checkPrint() {
        Iterator<Page> pageit = globalDocument.pageIterator();
        while (pageit.hasNext()) {
            Page p = pageit.next();
            System.out.println("Page Nr=" + p.getIndex() + " start=" + p.getStartIndex() + " end=" + p.getEndIndex());
        }
        System.out.println("\n\n");
        Iterator<Token> tokenit = globalDocument.allTokenIterator();
        while (tokenit.hasNext()) {
            Token t = tokenit.next();
            TokenImageInfoBox b = t.getTokenImageInfoBox();
            if (b != null) {
                System.out.println("Token: " + t.getID() + " " + t.getIndexInDocument() + " " + t.getWDisplay() + " " + t.isCorrected() + " " + t.isNormal() + " " + t.isSuspicious() + " " + b.getCoordinateLeft() + " " + b.getCoordinateRight());
            } else {
                System.out.println("Token: " + t.getID() + " " + t.getIndexInDocument() + " " + t.getWDisplay() + " " + t.isCorrected() + " " + t.isNormal() + " " + t.isSuspicious());
            }
        }
    }

    public void splitToken(int tokenIndex, String editString) {
        try {
            MainController.changeCursorWaitStatus(true);
            int undo_redo = globalDocument.getUndoRedoIndex();
            ArrayList<Integer> retval = globalDocument.splitToken(tokenIndex, editString);
            if (retval.size() > 0) {
                MessageCenter.getInstance().fireTokenStatusEvent(new SplitEvent(this, tokenIndex, TokenStatusType.SPLIT, retval));
                globalDocument.getUndoRedoManager().undoableEditHappened(new UndoableEditEvent(this, new MyUndoableEdit(MyEditType.SPLIT, undo_redo)));
            }
            MainController.changeCursorWaitStatus(false);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class DocumentCreator implements ProgressRunnable<Document> {

        private final String doc;
        private final FileType t;
        private final String enc;
        private final String img;
        private String propertiespath;
        private String projectpath;
        private String projectname;

        public DocumentCreator(String doc, FileType t, String e, String propp, String projp, String projn) {
            this.doc = doc;
            this.img = "";
            this.enc = e;
            this.t = t;
            this.propertiespath = propp;
            this.projectpath = projp;
            this.propertiespath = projn;
        }

        public DocumentCreator(String doc, String img, FileType t, String e, String propp, String projp, String projn) {
            this.doc = doc;
            this.img = img;
            this.enc = e;
            this.t = t;
            this.propertiespath = propp;
            this.projectpath = projp;
            this.projectname = projn;
        }

        @Override
        public Document run(ProgressHandle ph) {
            Document document = null;
            if( globalDocument != null) {
                ph.progress("erasing UndoRedo");
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("doc_create"));
                globalDocument.getUndoRedoManager().discardAllEdits();
            }
            ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"));
            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"));

            docproperties.setProperty("imgbasepath", img);
            docproperties.setProperty("original_encoding", enc);
            docproperties.setProperty("databasepath", projectpath + File.separator + projectname);
            setDefaultProperties();

            cs.newDocumentFromXML(projectpath + File.separator + projectname, doc, img, t, enc, ph);
            document = cs.getDocument();

            if (document != null) {

                docOpened = true;

                if (!img.equals("")) {
                    document.setBaseImagePath(img);
                    boolean hasimg = document.checkImageFiles();
                    if (!hasimg) {
                        new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("img_error"));
                        document.setHasImages(false);
                    } else {
                        document.setHasImages(true);
                    }
                } else {
                    document.setHasImages(false);
                }

                FileOutputStream tempout;
                try {
                    tempout = new FileOutputStream(propertiespath);
                    docproperties.store(tempout, "");
                    tempout.close();
                } catch (FileNotFoundException ex) {
                    ph.progress("Error");
                    ph.setDisplayName("Error");
                    new CustomErrorDialog().showDialog("Error while creating the document!\n" + ex.getLocalizedMessage());
                    document = null;
                    return document;
                } catch (IOException ex) {
                    ph.progress("Error");
                    ph.setDisplayName("Error");
                    new CustomErrorDialog().showDialog("Error while creating the document!\n" + ex.getLocalizedMessage());
                    document = null;
                    return document;
                }

                MainController.findInstance().getCorrectionSystem().getDocument().setProjectFilename(propertiespath);
                MRUFilesOptions opts = MRUFilesOptions.getInstance();
                opts.addFile(propertiespath);
            }
            return document;
        }
    }

    private class DocumentLoader implements ProgressRunnable<Document> {

        private final String doc;

        public DocumentLoader(String projectarchive) {
            this.doc = projectarchive;
        }

        @Override
        public Document run(ProgressHandle ph) {
            Document document = null;

            FileInputStream fis;
            try {
                fis = new FileInputStream(new File(doc));
                docproperties.load(fis);
                fis.close();
            } catch (FileNotFoundException ex) {
                ph.progress("Error");
                ph.setDisplayName("Error");
                new CustomErrorDialog().showDialog("Error while loading the document!\n" + ex.getLocalizedMessage());
                new CustomErrorDialog().showDialog(ex.getMessage());
                document = null;
                return document;
            } catch (IOException ex) {
                ph.progress("Error");
                ph.setDisplayName("Error");
                new CustomErrorDialog().showDialog("Error while loading the document!\n" + ex.getLocalizedMessage());
                new CustomErrorDialog().showDialog(ex.getMessage());
                document = null;
                return document;
            }

            if( globalDocument != null) {
                ph.progress("erasing UndoRedo");
                ph.setDisplayName("erasing UndoRedo");
                globalDocument.getUndoRedoManager().discardAllEdits();
            }
            ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"));
            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"));

            cs.openDocument(docproperties.getProperty("databasepath"));
            document = cs.getDocument();

            if (document != null) {

                docOpened = true;
                document.setBaseImagePath(docproperties.getProperty("imgbasepath", ""));
                boolean hasimg = document.checkImageFiles();
                if (!hasimg) {
                    new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("img_error"));
                    document.setHasImages(false);
                } else {
                    document.setHasImages(true);
                }
            } else {
                document.setHasImages(false);
            }

            MainController.findInstance().getCorrectionSystem().getDocument().setProjectFilename(doc);
            MRUFilesOptions opts = MRUFilesOptions.getInstance();
            opts.addFile(doc);

            return document;
        }
    }

    private class DocumentProfiler implements ProgressRunnable<Integer>, Cancellable {

        private String configuration;
        private int totalBytesRead;
        private long totalBytesToRead;
        private ProgressHandle ph;
        private String status;
        private int retval;

        public DocumentProfiler(String c) {
            this.configuration = c;
        }

        @Override
        public Integer run(ProgressHandle p) {
            this.ph = p;
            try {
                retval = -1;

                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"));

                GetProfileRequest gpr = new GetProfileRequest();
                GetProfileRequestType gprt = new GetProfileRequestType();
                gprt.setConfiguration(this.configuration);
                gprt.setUserid(MainController.findInstance().getProfilerUserID());

                AttachmentType docoutatt = new AttachmentType();
                Base64Binary docoutbin = new Base64Binary();

                tempFile = File.createTempFile("document", ".ocrcxml");
                tempFile.deleteOnExit();

                globalDocument.exportAsDocXML(tempFile.getCanonicalPath(), false);

                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"));

                FileDataSource docoutds = new FileDataSource(tempFile.getCanonicalPath());
                DataHandler docoutdh = new DataHandler(docoutds);
                docoutbin.setBase64Binary(docoutdh);
                docoutatt.setBinaryData(docoutbin);
                gprt.setDoc_in(docoutatt);
                gprt.setDoc_in_type("DOCXML");
                gprt.setDoc_in_size(tempFile.length());

                gpr.setGetProfileRequest(gprt);

                done = false;

                ProfilerWebServiceCallbackHandler handler = new ProfilerWebServiceCallbackHandler() {
                    @Override
                    public void receiveResultgetProfile(GetProfileResponse gpres) {
                        GetProfileResponseType gprest = gpres.getGetProfileResponse();

                        try {
                            DataHandler dh_doc = gprest.getDoc_out().getBinaryData().getBase64Binary();
                            FileOutputStream doc_out = new FileOutputStream(tempFile.getCanonicalPath());
                            InputStream doc_in = dh_doc.getInputStream();

                            totalBytesToRead = gprest.getDoc_out_size();

                            byte[] buffer = new byte[8192];
                            int bytesRead = 0;

                            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("downloading_doc"));

                            while ((bytesRead = doc_in.read(buffer, 0, 8192)) != -1) {
                                totalBytesRead += bytesRead;
                                doc_out.write(buffer, 0, bytesRead);
                                ph.progress(totalBytesRead + " / " + totalBytesToRead);
                            }

                            doc_in.close();
                            doc_out.flush();
                            doc_out.close();

                            globalDocument.clearCandidates();
                            new OCRXMLImporter().importCandidates(globalDocument, tempFile.getCanonicalPath());

                            tempFile = File.createTempFile("profile", ".xml");
                            tempFile.deleteOnExit();

                            DataHandler dh_prof = gprest.getProfile_out().getBinaryData().getBase64Binary();
                            FileOutputStream prof_out = new FileOutputStream(tempFile.getCanonicalPath());
                            InputStream prof_in = dh_prof.getInputStream();

                            totalBytesToRead = gprest.getProfile_out_size();
                            totalBytesRead = 0;

                            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("downloading_profile"));

                            while ((bytesRead = prof_in.read(buffer, 0, 8192)) != -1) {
                                totalBytesRead += bytesRead;
                                prof_out.write(buffer, 0, bytesRead);
                                ph.progress(totalBytesRead + " / " + totalBytesToRead);
                            }

                            prof_in.close();
                            prof_out.flush();
                            prof_out.close();

                            globalDocument.clearPatterns();
                            new OCRXMLImporter().importProfile(globalDocument, tempFile.getCanonicalPath());

                            retval = 0;
                            done = true;
                        } catch (FileNotFoundException ex) {
                            retval = -1;
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            retval = -1;
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    @Override
                    public void receiveErrorgetProfile(Exception e) {
                        AbortProfilingRequest apr = new AbortProfilingRequest();
                        AbortProfilingRequestType aprt = new AbortProfilingRequestType();

                        aprt.setUserid(MainController.findInstance().getProfilerUserID());
                        apr.setAbortProfilingRequest(aprt);
                        try {
                            AbortProfilingResponse aps = MainController.findInstance().getProfilerWebServiceStub().abortProfiling(apr);
                            AbortProfilingResponseType apst = aps.getAbortProfilingResponse();

                        } catch (RemoteException ex) {
                            Exceptions.printStackTrace(ex);
                            new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                        }
                        retval = -1;
                        System.out.println("ReceiveError " + e.getMessage());
                    }
                };

                MainController.findInstance().getProfilerWebServiceStub().startgetProfile(gpr, handler);

                while (!done) {
                    try {
                        Thread.sleep(3000);
                        GetProfilingStatusRequest grpq = new GetProfilingStatusRequest();
                        GetProfilingStatusRequestType grpqt = new GetProfilingStatusRequestType();
                        grpqt.setUserid(MainController.findInstance().getProfilerUserID());

                        grpq.setGetProfilingStatusRequest(grpqt);
                        GetProfilingStatusResponse gprs = MainController.findInstance().getProfilerWebServiceStub().getProfilingStatus(grpq);
                        GetProfilingStatusResponseType gprst = gprs.getGetProfilingStatusResponse();

                        status = gprst.getStatus();
                        ph.setDisplayName(gprst.getStatus());
                        ph.progress(gprst.getAdditional());
                    } catch (InterruptedException ex) {
                        retval = -1;
                    }
                }
                return retval;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public boolean cancel() {
            if (status.equals("profiling")) {
                AbortProfilingRequest apr = new AbortProfilingRequest();
                AbortProfilingRequestType aprt = new AbortProfilingRequestType();

                aprt.setUserid(MainController.findInstance().getProfilerUserID());
                apr.setAbortProfilingRequest(aprt);
                try {
                    AbortProfilingResponse aps = MainController.findInstance().getProfilerWebServiceStub().abortProfiling(apr);
                    AbortProfilingResponseType apst = aps.getAbortProfilingResponse();

                    if (apst.getMessage().equals("destroyed")) {
                        return true;
                    } else {
                        new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                        return false;
                    }
                } catch (RemoteException ex) {
                    Exceptions.printStackTrace(ex);
                    new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                    return false;
                }
            } else {
                new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                return false;
            }
        }
    }

    private class DocumentSimpleProfiler implements ProgressRunnable<Integer>, Cancellable {

        private String configuration;
        private int totalBytesRead;
        private long totalBytesToRead;
        private ProgressHandle ph;
        private String status;
        private int retval;

        public DocumentSimpleProfiler(String c) {
            this.configuration = c;
        }

        @Override
        public Integer run(ProgressHandle p) {
            this.ph = p;
            try {
                retval = -1;

                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("exporting"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("exporting"));

                SimpleEnrichRequest gpr = new SimpleEnrichRequest();
                SimpleEnrichRequestType gprt = new SimpleEnrichRequestType();
                gprt.setConfiguration(this.configuration);

                AttachmentType docoutatt = new AttachmentType();
                Base64Binary docoutbin = new Base64Binary();

                tempFile = File.createTempFile("document", ".ocrcxml");
                tempFile.deleteOnExit();

                globalDocument.exportAsDocXML(tempFile.getCanonicalPath(), false);

                ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"));
                ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("profiling"));

                FileDataSource docoutds = new FileDataSource(tempFile.getCanonicalPath());
                DataHandler docoutdh = new DataHandler(docoutds);
                docoutbin.setBase64Binary(docoutdh);
                docoutatt.setBinaryData(docoutbin);
                gprt.setDoc_in(docoutatt);
                gprt.setDoc_in_size(tempFile.length());

                gpr.setSimpleEnrichRequest(gprt);

                done = false;

                ProfilerWebServiceCallbackHandler handler = new ProfilerWebServiceCallbackHandler() {
                    @Override
                    public void receiveResultsimpleEnrich(SimpleEnrichResponse gpres) {
                        SimpleEnrichResponseType gprest = gpres.getSimpleEnrichResponse();

                        try {
                            DataHandler dh_doc = gprest.getDoc_out().getBinaryData().getBase64Binary();
                            FileOutputStream doc_out = new FileOutputStream(tempFile.getCanonicalPath());
                            InputStream doc_in = dh_doc.getInputStream();

                            totalBytesToRead = gprest.getDoc_out_size();

                            byte[] buffer = new byte[8192];
                            int bytesRead = 0;

                            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("downloading_doc"));

                            while ((bytesRead = doc_in.read(buffer, 0, 8192)) != -1) {
                                totalBytesRead += bytesRead;
                                doc_out.write(buffer, 0, bytesRead);
                                ph.progress(totalBytesRead + " / " + totalBytesToRead);
                            }

                            doc_in.close();
                            doc_out.flush();
                            doc_out.close();

                            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("importing_doc"));
                            new OCRXMLImporter().simpleUpdateDocument(globalDocument, tempFile.getCanonicalPath());
                            retval = 0;
                            done = true;
                        } catch (FileNotFoundException ex) {
                            retval = -1;
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            retval = -1;
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    @Override
                    public void receiveErrorsimpleEnrich(Exception e) {
                        AbortProfilingRequest apr = new AbortProfilingRequest();
                        AbortProfilingRequestType aprt = new AbortProfilingRequestType();

                        aprt.setUserid(MainController.findInstance().getProfilerUserID());
                        apr.setAbortProfilingRequest(aprt);
                        try {
                            AbortProfilingResponse aps = MainController.findInstance().getProfilerWebServiceStub().abortProfiling(apr);
                            AbortProfilingResponseType apst = aps.getAbortProfilingResponse();
                        } catch (RemoteException ex) {
                            Exceptions.printStackTrace(ex);
                            new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                        }
                        retval = -1;
                        System.out.println("ReceiveError " + e.getMessage());
                    }
                };

                MainController.findInstance().getProfilerWebServiceStub().startsimpleEnrich(gpr, handler);

                while (!done) {
                    try {
                        Thread.sleep(3000);
                        GetProfilingStatusRequest grpq = new GetProfilingStatusRequest();
                        GetProfilingStatusRequestType grpqt = new GetProfilingStatusRequestType();
                        grpqt.setUserid("simpleEnrich");

                        grpq.setGetProfilingStatusRequest(grpqt);
                        GetProfilingStatusResponse gprs = MainController.findInstance().getProfilerWebServiceStub().getProfilingStatus(grpq);
                        GetProfilingStatusResponseType gprst = gprs.getGetProfilingStatusResponse();

                        status = gprst.getStatus();
                        ph.setDisplayName(gprst.getStatus());
                        ph.progress(gprst.getAdditional());
                    } catch (InterruptedException ex) {
                        retval = -1;
                    }
                }
                return retval;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public boolean cancel() {
            if (status.equals("profiling")) {
                AbortProfilingRequest apr = new AbortProfilingRequest();
                AbortProfilingRequestType aprt = new AbortProfilingRequestType();

                aprt.setUserid(MainController.findInstance().getProfilerUserID());
                apr.setAbortProfilingRequest(aprt);
                try {
                    AbortProfilingResponse aps = MainController.findInstance().getProfilerWebServiceStub().abortProfiling(apr);
                    AbortProfilingResponseType apst = aps.getAbortProfilingResponse();

                    if (apst.getMessage().equals("destroyed")) {
                        return true;
                    } else {
                        new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                        return false;
                    }
                } catch (RemoteException ex) {
                    Exceptions.printStackTrace(ex);
                    new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                    return false;
                }
            } else {
                new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("not_possible"));
                return false;
            }
        }
    }

    private class OCRCXMLasNewProjectImporter implements ProgressRunnable<Document> {

        private String doc;
        private String img = "";
        private String propertiespath;
        private String projectpath;
        private String projectname;
        private String profilename;

        public OCRCXMLasNewProjectImporter(String doc, String imgdir, String propp, String projp, String projn, String profn) {
            this.doc = doc;
            this.img = imgdir;
            this.propertiespath = propp;
            this.projectpath = projp;
            this.projectname = projn;
            this.profilename = profn;
        }

        public OCRCXMLasNewProjectImporter(String doc, String propp, String projp, String projn, String profn) {
            this.doc = doc;
            this.propertiespath = propp;
            this.projectpath = projp;
            this.projectname = projn;
            this.profilename = profn;
        }

        @Override
        public Document run(ProgressHandle ph) {
            Document document = null;
            ph.progress("erasing UndoRedo");
            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("doc_create"));
            globalDocument.getUndoRedoManager().discardAllEdits();
            ph.progress(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"));
            ph.setDisplayName(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("opening"));

            docproperties.setProperty("imgbasepath", img);
            docproperties.setProperty("databasepath", projectpath + File.separator + projectname);
            setDefaultProperties();

            cs.newDocumentFromOCRCXML(projectpath + File.separator + projectname, doc, img, ph);
            document = cs.getDocument();

            if (profilename != null) {
                cs.importProfile(document, profilename);
            }

            if (document != null) {

                docOpened = true;

                document.setBaseImagePath(img);
                if (img != null) {
                    boolean hasimg = document.checkImageFiles();
                    if (!hasimg) {
                        new CustomErrorDialog().showDialog(java.util.ResourceBundle.getBundle("jav/gui/main/Bundle").getString("img_error"));
                        document.setHasImages(false);
                    } else {
                        document.setHasImages(true);
                    }
                } else {
                    document.setHasImages(false);
                }
                docproperties.setProperty("hasImages", "" + document.getHasImages());

                FileOutputStream tempout;
                try {
                    tempout = new FileOutputStream(propertiespath);
                    docproperties.store(tempout, "");
                    tempout.close();
                } catch (FileNotFoundException ex) {
                    ph.progress("Error");
                    ph.setDisplayName("Error");
                    new CustomErrorDialog().showDialog("Error while creating the document!\n" + ex.getLocalizedMessage());
                    document = null;
                    return document;
                } catch (IOException ex) {
                    ph.progress("Error");
                    ph.setDisplayName("Error");
                    new CustomErrorDialog().showDialog("Error while creating the document!\n" + ex.getLocalizedMessage());
                    document = null;
                    return document;
                }

                MainController.findInstance().getCorrectionSystem().getDocument().setProjectFilename(propertiespath);
                MRUFilesOptions opts = MRUFilesOptions.getInstance();
                opts.addFile(propertiespath);
            }
            return document;
        }
    }
}