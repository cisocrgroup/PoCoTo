/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.export;

import com.sun.media.jai.codec.FileSeekableStream;
import jav.correctionBackend.util.FilePathUtils;
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
        if (line.size() != adj.size()) {
            throw new Exception("Invalid ocropus: line add lloc differ");
        }
        adjust(line.getBoundingBox());
        Char prev = null;
        for (int i = 0; i < line.size(); ++i) {
            adjust(line.getBoundingBox(), prev, line.get(i), adj.get(i));
            prev = line.get(i);
        }
    }

    private void adjust(BoundingBox line) {
        final int y0 = imageHeight - line.getBottom() - 1;
        final int y1 = imageHeight - line.getTop() - 1;
        line.setTop(y0);
        line.setBottom(y1);
    }

    private void adjust(BoundingBox line, Char prev, Char current, AdjustmentChar adj) throws Exception {
        if (current.getChar() != adj.codepoint) {
            throw new Exception("Invalid ocropus characters differ");
        }
        adjust(line, prev.getBoundingBox(), current.getBoundingBox(), adj);
    }

    private void adjust(BoundingBox line, BoundingBox prev, BoundingBox current, AdjustmentChar adj) {
        current.setLeft(line.getLeft() + adj.adjustment);
        if (prev != null) {
            prev.setRight(current.getLeft() - 1);
        }
        current.setTop(line.getTop());
        current.setBottom(line.getBottom());
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
        Arrays.sort(adjustmentFiles, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });
        Adjustments adjustments = new Adjustments();
        for (File file : adjustmentFiles) {
            adjustments.add(getAdjustmentLine(file));
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
