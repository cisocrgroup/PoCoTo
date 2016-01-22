package jav.gui.token.tools;

import com.sun.media.jai.codec.FileSeekableStream;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import org.openide.modules.InstalledFileLocator;

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
public class ImageProcessor {

    private RenderedOp rop;
    private String imageString = null;
//    private GraphicsConfiguration graphicsConfiguration;

    public ImageProcessor() {
    }

    public String getImageString() {
        return this.imageString;
    }

    public void setImageInput(String filename) {

//        graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        FileSeekableStream fss = null;
        try {
            fss = new FileSeekableStream(filename);
        } catch (IOException e) {
            System.exit(0);
        }

        ParameterBlock pb = new ParameterBlock();
        pb.add(fss);
        this.imageString = filename;
        if (filename.endsWith("tif")) {
            rop = JAI.create("tiff", pb);
        } else if (filename.endsWith("jpg") || filename.endsWith("jpeg")) {
            rop = JAI.create("jpeg", pb);
        } else {
            throw new NullPointerException();
        }
    }

    /*
     * @param x,y,width,height
     */
    public BufferedImage getTokenImage(int inx, int iny, int inw, int inh) {
        return rop.getAsBufferedImage(new Rectangle(inx, iny, inw, inh), null);
    }

    public BufferedImage getTokenImage(int inx, int iny, int inw, int inh, double scale) {

        BufferedImage returnimg = null;
//        Raster r = rop.getData();
//        BufferedImage bim = graphicsConfiguration.createCompatibleImage(inw, inh);
//        Raster subr = r.createChild(inx, iny, inw, inh, 0, 0, null);
//        bim.setData(subr);
        try {
            rop.getAsBufferedImage(new Rectangle(inx, iny, inw, inh), null);
            returnimg = scale(rop.getAsBufferedImage(new Rectangle(inx, iny, inw, inh), null), scale);
        } catch (Exception e) {

            FileSeekableStream fss = null;
            try {
                fss = new FileSeekableStream(InstalledFileLocator.getDefault().locate("modules/ext/notavailable.tiff", "jav.gui.token.display", false).getCanonicalPath());
            } catch (IOException ex) {
            }

            ParameterBlock pb = new ParameterBlock();
            pb.add(fss);
            RenderedOp notavailable = JAI.create("tiff", pb);
            returnimg = scale(notavailable.getAsBufferedImage(), scale);
        }

        return returnimg;
//        return bim;
//        return scale( bim, scale);
//        bi = rop.getAsBufferedImage(new Rectangle(inx,iny,inw,inh), null);
//        bi = scale(bi, scale);
//        return bi;
    }

    /*
     * @param image to scale, scale factor
     */
    public BufferedImage scale(BufferedImage in, double s) {

        try {
            float scale = (float) s;

            Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_BICUBIC);
            ParameterBlockJAI block = new ParameterBlockJAI("scale");
            block.setSource(in, 0);
            block.setParameter("xScale", scale);
            block.setParameter("yScale", scale);
            block.setParameter("interpolation", interpolation);

            RenderedOp zoom = JAI.create("scale", block);
            return zoom.getAsBufferedImage();
        } catch (Exception e) {
            return in;
        }
    }
}
