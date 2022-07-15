package org.grits.toolbox.utils.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.examples.AddDimensionedImage;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.swt.widgets.Display;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.tools.glycanbuilder.core.config.GraphicOptionsSWT;
import org.grits.toolbox.tools.glycanbuilder.core.workspace.BuilderWorkspaceSWT;
import org.grits.toolbox.utils.image.SimianImageConverter;

public class ExcelWriterHelper extends AddDimensionedImage {

	private static BuilderWorkspaceSWT bws;

	static {
		bws = new BuilderWorkspaceSWT(Display.getDefault());
		bws.setNotation(GraphicOptionsSWT.NOTATION_SNFG);
	}

	public BufferedImage createGlycanImage(String a_sequence, String a_displayStyle, boolean a_showMasses,
			boolean a_showRedEnd, Double a_imageScalingFactor)
			throws Exception {
		List<String> lSequences = new ArrayList<>();
		lSequences.add(a_sequence);
		return this.createGlycanImage(lSequences, a_displayStyle, a_showMasses, a_showRedEnd, a_imageScalingFactor);
	}

	public BufferedImage createGlycanImage(List<String> a_sequences, String a_displayStyle, boolean a_showMasses,
			boolean a_showRedEnd, Double a_imageScalingFactor) throws Exception {
		if (a_displayStyle != null)
			bws.setDisplay(a_displayStyle);
		// GWB has a own scaling - here set to 1 to not use it. The reason is
		// that GWB tries to be smart and for 75% image size removes the
		// linkages. Which is in this case unwanted.
		double dScaleTo = a_imageScalingFactor < 1.0 ? 2.0 : a_imageScalingFactor;
		double dScaleFrom = a_imageScalingFactor < 1.0 ? a_imageScalingFactor / 2.0 : a_imageScalingFactor;

		List<Glycan> lGlycans = new ArrayList<>();
		for (String sequence : a_sequences) {
			if (sequence == null)
				continue;
			lGlycans.add(Glycan.fromString(sequence));
		}
		BufferedImage img = bws.getGlycanRendererAWT().getImage(lGlycans, true, a_showMasses, a_showRedEnd, dScaleTo);
		// Now we do our own scaling
		if (a_imageScalingFactor < 1.0D) {
			int width = (int) (img.getWidth() * dScaleFrom);
			int height = (int) (img.getHeight() * dScaleFrom);
			java.awt.Image newImage = img.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
			BufferedImage newBufferedImage = SimianImageConverter.convert(newImage);
			newImage.flush();
			return newBufferedImage;
		}
		return img;
	}

	/**
	 * 
	 * Writes the given image object into a cell in the given workbook and sheet at
	 * the given cell identified by row and column indexes
	 * 
	 * @see {@link org.apache.poi.ss.examples.AddDimensionedImage.addImageToSheet()}
	 * @param a_workbook Excel workbook
	 * @param a_sheet    sheet to use
	 * @param a_iRowNum  row number for the cell
	 * @param a_iColNum  column number for the cell
	 * @param a_img      image to add to the given cell
	 * @param a_imgs     array of images to put the newly generated excel picture
	 *                   into (to be used for resizing the images later)
	 */
	public void writeCellImage(Workbook a_workbook, Sheet a_sheet, int a_iRowNum, int a_iColNum, BufferedImage a_img,
			List<Picture> a_imgs) throws Exception {

		if (a_iColNum < 0 || a_img == null)
			return;

		Drawing drawing = a_sheet.createDrawingPatriarch();
		double imageWidthMM = ConvertImageUnits
				.widthUnits2Millimetres(ConvertImageUnits.pixel2WidthUnits(a_img.getWidth()));
		double imageHeightMM = ConvertImageUnits
				.widthUnits2Millimetres(ConvertImageUnits.pixel2WidthUnits(a_img.getHeight()));

		// Set +3 each on Height and Width for margin (bottom and left) of pictures
		ClientAnchorDetail colClientAnchorDetail = this.fitImageToColumns(a_sheet, a_iColNum, imageWidthMM + 3,
				AddDimensionedImage.EXPAND_ROW_AND_COLUMN);
		ClientAnchorDetail rowClientAnchorDetail = this.fitImageToRows(a_sheet, a_iRowNum, imageHeightMM + 3,
				AddDimensionedImage.EXPAND_ROW_AND_COLUMN);

		ClientAnchor anchor = a_workbook.getCreationHelper().createClientAnchor();

		// Set 10 for margin (top and right) of pictures
		anchor.setDx1(10);
		anchor.setDy1(10);
		anchor.setDx2(colClientAnchorDetail.getInset());
		anchor.setDy2(rowClientAnchorDetail.getInset());
		anchor.setCol1(colClientAnchorDetail.getFromIndex());
		anchor.setRow1(rowClientAnchorDetail.getFromIndex());
		anchor.setCol2(colClientAnchorDetail.getToIndex());
		anchor.setRow2(rowClientAnchorDetail.getToIndex());

		anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(a_img, "png", bos);

		int index = a_sheet.getWorkbook().addPicture(bos.toByteArray(), Workbook.PICTURE_TYPE_PNG);
		a_imgs.add(drawing.createPicture(anchor, index));
	}

}
