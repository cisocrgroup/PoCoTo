/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import jav.correctionBackend.SpreadIndexDocument;
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
    ) {
        this.fileType = fileType;
        this.imagedir = imagedir;
        this.ocrdir = ocrdir;
        this.ph = ph;
        this.documentBuilder = new SpreadIndexDocumentBuilder(jdbc);
    }

    public SpreadIndexDocument parse() throws IOException {
        OcrToImageFileMapping mappings = new OcrToImageFileMapping(
                imagedir,
                ocrdir,
                fileType.getFilenameFilter()
        );
        documentBuilder.init();
        int i = 0;
        final int n = mappings.size();
        for (OcrToImageFileMapping.Mapping mapping : mappings) {
            progress(mapping, ++i, n);
            PageParser pageParser = fileType.getPageParser();
            documentBuilder.append(
                    pageParser.parse(mapping.ocrfile),
                    mapping.imagefile,
                    mapping.ocrfile
            );
        }
        return documentBuilder.build();
    }

    private void progress(OcrToImageFileMapping.Mapping m, int i, int n) {
        ph.progress(
                String.format("parsing %s [%s] (%d/%d)",
                        m.ocrfile,
                        m.imagefile,
                        ++i,
                        n
                )
        );
    }
}
