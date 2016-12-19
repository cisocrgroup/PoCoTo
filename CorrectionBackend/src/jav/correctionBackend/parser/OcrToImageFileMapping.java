package jav.correctionBackend.parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class OcrToImageFileMapping implements Iterable<OcrToImageFileMapping.Mapping> {

    private final File ocrdir, imagedir;
    private final FilenameFilter filter;
    private final ArrayList<Mapping> mappings;
    private static final String[] IMAGE_FILE_EXTENSION = {
        ".tif", ".tiff", ".jpg", ".jpeg", ".png",
        ".bin.tif", ".bin.tiff", ".bin.jpg", ".bin.jpeg", ".bin.png"
    };

    public OcrToImageFileMapping(
            File imagedir,
            File ocrdir,
            FilenameFilter filter
    ) throws IOException {
        this.ocrdir = ocrdir;
        this.imagedir = imagedir;
        this.filter = filter;
        mappings = new ArrayList<>();
        gatherMappings();
        sortMappings();
    }

    public final class Mapping {

        public final File ocrfile, imagefile;

        public Mapping(File imagefile, File ocrfile) {
            this.ocrfile = ocrfile;
            this.imagefile = imagefile;
        }
    }

    public Mapping get(int i) {
        return mappings.get(i);
    }

    @Override
    public Iterator<Mapping> iterator() {
        return mappings.iterator();
    }

    public int size() {
        return mappings.size();
    }

    private void gatherMappings() throws IOException {
        for (String ocrfile : gatherOcrFiles()) {
            mappings.add(makeMapping(new File(ocrdir, ocrfile)));
        }
    }

    private Mapping makeMapping(File ocrfile) throws IOException {
        String baseName = getBaseName(ocrfile);
        for (String ext : IMAGE_FILE_EXTENSION) {
            File imagefile = new File(imagedir, baseName + ext);
            // Log.info(this, "searching for image %s", imagefile.getName());
            if (imagefile.exists()) {
                return new Mapping(imagefile, ocrfile);
            }
        }
        throw new IOException(ocrfile + ": missing image file");
    }

    private String[] gatherOcrFiles() {
        return ocrdir.list(filter);
    }

    private static String getBaseName(File file) {
        String filename = file.getName();
        return filename.substring(0, filename.lastIndexOf("."));
    }

    private void sortMappings() {
        Collections.sort(mappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping a, Mapping b) {
                return a.ocrfile.compareTo(b.ocrfile);
            }
        });
    }
}
