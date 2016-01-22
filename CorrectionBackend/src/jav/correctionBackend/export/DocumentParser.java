/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.FileType;
import jav.correctionBackend.SpreadIndexDocument;
import jav.logging.log4j.Log;
import java.io.File;
import java.io.IOException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author finkf
 */
public class DocumentParser {

    private final FileType fileType;
    private final File imagedir, ocrdir;
    private final ProgressHandle ph;
    private final DocumentBuilder documentBuilder;

    public DocumentParser(
            File imagedir,
            File ocrdir,
            FileType fileType,
            ProgressHandle ph,
            JdbcConnectionPool jdbc
    ) throws IOException {
        this.fileType = fileType;
        this.imagedir = imagedir;
        this.ocrdir = ocrdir;
        this.ph = ph;
        this.documentBuilder = new SpreadIndexDocumentBuilder(jdbc);
        if (!imagedir.isDirectory()) {
            throw new IOException("Not a directory: " + imagedir.getName());
        }
        if (!ocrdir.isDirectory()) {
            throw new IOException("Not a directory: " + ocrdir.getName());
        }
    }

    public SpreadIndexDocument parse() throws IOException, Exception {
        OcrToImageFileMapping mappings = new OcrToImageFileMapping(
                imagedir,
                ocrdir,
                fileType.getFilenameFilter()
        );
        documentBuilder.init();
        int i = 0;
        final int n = mappings.length();
        for (OcrToImageFileMapping.Mapping mapping : mappings) {
            progress(mapping, ++i, n);
            PageParser pageParser = fileType.getPageParser();
            pageParser.setImageFile(mapping.imagefile);
            documentBuilder.append(pageParser.parse(mapping.ocrfile));
        }
        return documentBuilder.build();
    }

    private void progress(OcrToImageFileMapping.Mapping m, int i, int n) {
        String msg = String.format("reading %s [%s] (%d/%d)",
                m.ocrfile.getName(),
                m.imagefile.getName(),
                i,
                n
        );
        ph.progress(msg);
        Log.info(this, msg);
    }
}
