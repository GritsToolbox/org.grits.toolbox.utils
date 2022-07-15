/******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/


package org.grits.toolbox.utils.image;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.tools.glycanbuilder.core.workspace.BuilderWorkspaceSWT;
import org.jat.generation.internal.helpers.ImageConverter;

public class SimianImageConverter extends ImageConverter {

	public static BufferedImage convert( java.awt.Image srcImage) {
		if ( srcImage instanceof BufferedImage ) 
			return (BufferedImage) srcImage;
		
		BufferedImage bimage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(srcImage, 0, 0, null);
		bGr.dispose();
		return bimage;
	}
	
	public static BufferedImage createCartoon( Glycan glycan, BuilderWorkspaceSWT bws, double dScaleFactor, boolean bShowMasses, boolean bShowRedEnd,
			boolean bNoScaleLessThanOne, int iScaleType ) {
		List<Glycan> lGlycans = new ArrayList<>();
		lGlycans.add(glycan);
		return createCartoon(lGlycans, bws, dScaleFactor, bShowMasses, bShowRedEnd, bNoScaleLessThanOne, iScaleType);
	}

	public static BufferedImage createCartoon( List<Glycan> glycan, BuilderWorkspaceSWT bws, double dScaleFactor, boolean bShowMasses, boolean bShowRedEnd,
			boolean bNoScaleLessThanOne, int iScaleType ) {
		double dScaleTo = bNoScaleLessThanOne && dScaleFactor < 1.0 ? 2.0 : dScaleFactor;
		double dScaleFrom = bNoScaleLessThanOne && dScaleFactor < 1.0 ? dScaleFactor / 2.0 : dScaleFactor;
		
		BufferedImage img = createCartoonAWT( glycan, bws, dScaleTo, bShowMasses, bShowRedEnd );
		if ( bNoScaleLessThanOne && dScaleFactor < 1.0 ) {
			int width = (int) ( (double) img.getWidth() * dScaleFrom );
		    int height = (int) ( (double) img.getHeight() * dScaleFrom );
			java.awt.Image newImage = img.getScaledInstance( width, height, iScaleType );
			BufferedImage newBufferedImage = SimianImageConverter.convert(newImage);
			newImage.flush();
			return newBufferedImage;
		} 
		
		return img;
	}

	public static Image createCartoonSWT( List<Glycan> glycan, BuilderWorkspaceSWT bws, double dScaleFactor, boolean bShowMasses, boolean bShowRedEnd ) {
		try {
			Image img = bws.getGlycanRenderer().getImage(glycan, 
					true, 
					bShowMasses, 
					bShowRedEnd, 
					dScaleFactor);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage createCartoonAWT( List<Glycan> glycan, BuilderWorkspaceSWT bws, double dScaleFactor, boolean bShowMasses, boolean bShowRedEnd ) {
		try {
			BufferedImage img = bws.getGlycanRendererAWT().getImage(glycan, 
					true, 
					bShowMasses, 
					bShowRedEnd, 
					dScaleFactor);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
