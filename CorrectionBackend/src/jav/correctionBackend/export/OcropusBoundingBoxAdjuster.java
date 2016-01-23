/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import com.sun.media.jai.codec.FileSeekableStream;
import jav.correctionBackend.util.FilePathUtils;
import jav.logging.log4j.Log;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.media.jai.JAI;

/**
 *
 * @author flo
 */
public class OcropusBoundingBoxAdjuster {

    private File locDir;
    private int imageHeight;
    private final Page page;

    public OcropusBoundingBoxAdjuster(Page page) throws Exception {
        this.page = page;
        setOcrFile(page.getOcrFile());
        setImageFile(page.getImageFile());
    }

    public Page adjust() throws Exception {
        ArrayList<Line> lines = new ArrayList<>();
        for (Paragraph p : page) {
            lines.addAll(p);
        }
        adjust(lines);
        return page;
    }

    private void adjust(ArrayList<Line> lines) throws Exception {
        Adjustments adjs = getAdjustments();
        if (lines.size() != adjs.size()) {
            throw new Exception("Invalid ocropus: lines and llocs differ");
        }
        for (int i = 0; i < lines.size(); ++i) {
            adjust(lines.get(i), adjs.get(i));
        }
    }

    private void adjust(Line line, AdjustmentLine adj) throws Exception {
        adjustHorizontal(line.getBoundingBox());
        BoundingBox prev = null;
        for (int i = 0, j = -1; i < line.size(); ++i) {
            Char c = line.get(i);
            final int oldj = j;
            j = adj.findNext(j, c);
            if (j != -1) {
                adjust(line.getBoundingBox(), prev, c.getBoundingBox(), adj.get(j));
            } else {
                Log.info(this, "skipping `%s`", new String(Character.toChars(c.getChar())));
                adjust(line.getBoundingBox(), prev, c.getBoundingBox(), null);
                j = oldj;
            }
            prev = c.getBoundingBox();
        }
    }

    private void adjustHorizontal(BoundingBox bb) {
        final int y0 = imageHeight - bb.getBottom() - 1;
        final int y1 = imageHeight - bb.getTop() - 1;
        bb.setTop(y0);
        bb.setBottom(y1);
    }

    private void adjust(BoundingBox line, BoundingBox prev, BoundingBox current, AdjustmentChar adj) throws Exception {
        if (adj != null) {
            current.setLeft(line.getLeft() + adj.adjustment);
        } else if (prev != null) {
            current.setLeft(prev.getRight() + 1);
        } else {
            current.setLeft(line.getLeft());
        }
        current.setTop(line.getTop());
        current.setBottom(line.getBottom());
        if (prev != null) {
            prev.setRight(current.getLeft() - 1);
        }
    }

    private void setOcrFile(File ocr) throws Exception {
        String name = FilePathUtils.removeAllExtensionsFromFileName(ocr);
        if (name.isEmpty()
                || ocr.getParentFile() == null
                || ocr.getParentFile().getParentFile() == null) {
            throw new Exception("Invalid ocropus input file: " + ocr.getCanonicalPath());
        }
        File pp = ocr.getParentFile().getParentFile();
        String book = ocr.getParentFile().getName().replaceFirst("hocr", "book");
        locDir = new File(new File(pp, book), name);
        Log.info(this, "locDir %s", locDir);
    }

    private void setImageFile(File image) throws Exception {
        FileSeekableStream fss = new FileSeekableStream(image);
        ParameterBlock pb = new ParameterBlock();
        pb.add(fss);
        if (image.getName().endsWith("tiff")
                || image.getName().endsWith("tif")) {
            imageHeight = JAI.create("tiff", pb).getHeight();
        } else if (image.getName().endsWith("jpeg")
                || image.getName().endsWith("jpg")) {
            imageHeight = JAI.create("jpeg", pb).getHeight();
        } else {
            throw new Exception("Invalid ocropus image file: " + image.getCanonicalPath());
        }
    }

    private Adjustments getAdjustments() throws Exception {
        File adjustmentFiles[] = locDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".llocs");
            }
        });
        Adjustments adjustments = new Adjustments();
        if (adjustmentFiles != null) { // if the directory does not exist (empty file)
            Arrays.sort(adjustmentFiles, new Comparator<File>() {
                @Override
                public int compare(File a, File b) {
                    return a.getName().compareTo(b.getName());
                }
            });
            for (File file : adjustmentFiles) {
                adjustments.add(getAdjustmentLine(file));
            }
        }
        return adjustments;
    }

    private AdjustmentLine getAdjustmentLine(File file) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file)
                )
        );
        AdjustmentLine adjustmentLine = new AdjustmentLine(file);
        String line;
        while ((line = reader.readLine()) != null) {
            adjustmentLine.add(AdjustmentChar.fromString(line));
        }
        reader.close();
        return adjustmentLine;
    }

    private static class Adjustments extends ArrayList<AdjustmentLine> {

    }

    private static class AdjustmentLine extends ArrayList<AdjustmentChar> {

        private final File file;

        public AdjustmentLine(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public int findNext(int i, Char c) {
            for (int j = i + 1; j < size(); ++j) {
                if (get(j).codepoint == c.getChar()) {
                    return j;
                }
            }
            return -1;
        }
    }

    private static class AdjustmentChar {

        private final int adjustment, codepoint;

        public AdjustmentChar(int codepoint, int adjustment) {
            this.adjustment = adjustment;
            this.codepoint = codepoint;
        }

        public static AdjustmentChar fromString(String str) throws Exception {
            final int i = str.indexOf('\t');
            if (str.isEmpty() || i == -1) {
                throw new Exception("Invalid ocropus llocs adjustment: " + str);
            }
            final int cp = str.codePointAt(0);
            final int adj = (int) Double.parseDouble(str.substring(i + 1));
            return new AdjustmentChar(cp, adj);
        }
    }
}
