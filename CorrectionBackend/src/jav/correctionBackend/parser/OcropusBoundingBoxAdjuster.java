/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.correctionBackend.parser;

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
            throw new Exception(
                    String.format(
                            "Invalid ocropus directory %s: number of lines in hOCR differ from number of llocs files",
                            locDir
                    )
            );
        }
        for (int i = 0; i < lines.size(); ++i) {
            adjust(lines.get(i), adjs.get(i));
        }
    }

    private void adjust(Line line, AdjustmentLine adjLine) throws Exception {
        adjustHorizontal(line.getBoundingBox());
        setRightAdjustments(adjLine, line.getBoundingBox().getRight());
        final int base = line.getBoundingBox().getLeft();
        AdjustmentChar adjChar = null;

        for (int i = 0; i < line.size(); ++i) {
            Char current = line.get(i);
            current.getBoundingBox().setTop(line.getBoundingBox().getTop());
            current.getBoundingBox().setBottom(line.getBoundingBox().getBottom());
            int iadjust = adjChar == null ? 0 : adjChar.iadjust;
            adjChar = findAdjustmentChar(iadjust, current, adjLine);
            current.getBoundingBox().setRight(base + adjChar.rightadj);
            current.getBoundingBox().setLeft(base + adjChar.leftadj);
        }
    }

    private AdjustmentChar findAdjustmentChar(int i, Char current, AdjustmentLine adjLine)
            throws Exception {
        for (; i < adjLine.size(); ++i) {
            AdjustmentChar adjChar = adjLine.get(i);
            if ((adjChar.codepoint == current.getChar())
                    || (Character.isWhitespace(adjChar.codepoint)
                    && Character.isWhitespace(current.getChar()))) {
                adjChar.iadjust = i + 1;
                AdjustmentChar prev = i == 0 ? null : adjLine.get(i - 1);
                AdjustmentChar next = (i + 1) < adjLine.size()
                        ? adjLine.get(i + 1) : null;
                return doAdjustChar(prev, adjChar, next);
            }
        }
        throw new Exception(
                String.format(
                        "Could not find `%s`",
                        new String(Character.toChars(current.getChar()))
                )
        );
    }

    private AdjustmentChar doAdjustChar(AdjustmentChar p, AdjustmentChar c, AdjustmentChar n) {
        assert (c != null);
        if (p != null && Character.isWhitespace(p.codepoint)) {
            int w = p.rightadj - p.leftadj;
            if (w > 0) {
                c.leftadj -= w / 2;
            }
            //c.leftadj -= 10;
        }

        if (n != null && Character.isWhitespace(n.codepoint)) {
            int w = n.rightadj - n.leftadj;
            if (w > 0) {
                c.rightadj += w / 2;
            }
            // c.rightadj += 10;
        }
        return c;
    }

    private void adjustHorizontal(BoundingBox bb) {
        final int y0 = imageHeight - bb.getBottom() - 1;
        final int y1 = imageHeight - bb.getTop() - 1;
        bb.setTop(y0);
        bb.setBottom(y1);
    }

    private void setRightAdjustments(AdjustmentLine line, int r) {
        final int n = line.size();
        for (int i = 0; i < n; ++i) {
            if ((i + 1) < n) {
                line.get(i).rightadj = line.get(i + 1).leftadj;
            } else {
                line.get(i).rightadj = r;
            }
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
        //Log.info(this, "locDir %s", locDir);
    }

    private void setImageFile(File image) throws Exception {
        // Log.debug(this, "setImageFile: %s", image);
        if (image == null || image.getName().isEmpty()) {
            // ignore if file name is empty (when exporting)
            imageHeight = 0;
            return;
        }
        FileSeekableStream fss = new FileSeekableStream(image);
        ParameterBlock pb = new ParameterBlock();
        pb.add(fss);
        if (image.getName().endsWith("tiff")
                || image.getName().endsWith("tif")) {
            imageHeight = JAI.create("tiff", pb).getHeight();
        } else if (image.getName().endsWith("jpeg")
                || image.getName().endsWith("jpg")) {
            imageHeight = JAI.create("jpeg", pb).getHeight();
        } else if (image.getName().endsWith("png")) {
            imageHeight = JAI.create("png", pb).getHeight();
        } else {
            throw new Exception("Invalid image");
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
        AdjustmentLine adjustmentLine;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file)
                )
        )) {
            adjustmentLine = new AdjustmentLine(file);
            String line;
            while ((line = reader.readLine()) != null) {
                adjustmentLine.add(AdjustmentChar.fromString(line));
            }
        }
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

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (AdjustmentChar c : this) {
                builder.appendCodePoint(c.codepoint);
            }
            return builder.toString();
        }
    }

    private static class AdjustmentChar {

        private final int codepoint;
        private int leftadj, rightadj, iadjust;

        public AdjustmentChar(int codepoint, int adj) {
            this.leftadj = adj;
            this.codepoint = codepoint;
            this.rightadj = 0;
            this.iadjust = 0;
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
