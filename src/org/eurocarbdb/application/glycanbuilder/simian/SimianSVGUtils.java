package org.eurocarbdb.application.glycanbuilder.simian;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;

import org.eurocarbdb.application.glycanbuilder.BBoxManager;
import org.eurocarbdb.application.glycanbuilder.DefaultPaintable;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.PositionManager;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


public class SimianSVGUtils {

	static public byte[] getTranscodedSVG(GlycanRenderer gr, Collection<Glycan> structures, boolean show_masses, boolean show_redend, org.apache.batik.transcoder.Transcoder transcoder) {
		if( structures == null )
			structures = new Vector<Glycan>();

		try {
			// compute size
			PositionManager posManager = new PositionManager();
			BBoxManager bboxManager = new BBoxManager();
			Rectangle all_bbox = gr.computeBoundingBoxes(structures,show_masses,show_redend,posManager,bboxManager);    
			Dimension all_dim = gr.computeSize(all_bbox);       

			// prepare g2d
			org.apache.batik.svggen.SVGGraphics2D g2d = prepareGraphics(all_dim);    

			// fix EPS bug (flip vertically)
			//            if( transcoder!=null && transcoder instanceof org.apache.fop.render.ps.EPSTranscoder ) {
			//            g2d.scale(1,-1);
			//            g2d.translate(0,-all_dim.height);
			//            }

			// paint
			for( Glycan s : structures ) 
				gr.paint(new DefaultPaintable(g2d),s,null,null,show_masses,show_redend,posManager,bboxManager);

			// transcode
			return transcode(g2d,all_dim,transcoder);
		} 
		catch(Exception e) {        
//			LogUtils.report(e);
			return null;
		}
	}    
	static private byte[] transcode(org.apache.batik.svggen.SVGGraphics2D g2d, Dimension all_dim,org.apache.batik.transcoder.Transcoder transcoder) throws Exception {

		// Stream out SVG to a string          
		StringWriter out = new StringWriter();
		g2d.stream(out, true);        
		String svg = out.toString();

		// 
		if( transcoder==null ) 
			return svg.getBytes();

		// set transcoder dimensions
		transcoder.addTranscodingHint(org.apache.batik.transcoder.image.ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE, true);
//		transcoder.addTranscodingHint(org.apache.batik.transcoder.image.ImageTranscoder.KEY_BACKGROUND_COLOR, Color.green);        
		transcoder.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(0.3528f));
		transcoder.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_MAX_WIDTH,new Float(all_dim.width));
		transcoder.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_MAX_HEIGHT,new Float(all_dim.height));
		transcoder.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH,new Float(all_dim.width));
		transcoder.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT,new Float(all_dim.height));
		transcoder.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_AOI,new Rectangle(0,0,all_dim.width,all_dim.height));

		// transcode
		StringReader in = new StringReader(svg);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);

		org.apache.batik.transcoder.TranscoderInput input = new org.apache.batik.transcoder.TranscoderInput(in);
		org.apache.batik.transcoder.TranscoderOutput output = new org.apache.batik.transcoder.TranscoderOutput(bos);  
		transcoder.transcode(input, output);
		//fos.close();
		return baos.toByteArray();
	}
	static private org.apache.batik.svggen.SVGGraphics2D prepareGraphics(Dimension all_dim) {

		// Create an instance of the SVG Generator
		DOMImplementation domImpl = org.apache.batik.dom.GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);       
		org.apache.batik.svggen.SVGGraphics2D g2d = new org.apache.batik.svggen.SVGGraphics2D(document);

		// compute scale factor to fit 400x400 (otherwise it does not display)
		/*
		double sf = Math.min(600./all_dim.width,600./all_dim.height);   
		    if( sf<1. )
		        g2d.scale(sf,sf);
		    else
		        sf = 1.;
		 */
		double sf = 2.0;
		all_dim.width = (int)(all_dim.width*sf);
		    all_dim.height = (int)(all_dim.height*sf);

		
//		g2d.setBackground(Color.white);
		g2d.setSVGCanvasSize(all_dim);

		return g2d;
	}

}
