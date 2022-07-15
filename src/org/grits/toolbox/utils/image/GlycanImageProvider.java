package org.grits.toolbox.utils.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.ResidueDictionary;
import org.eurocarbdb.application.glycanbuilder.ResidueType;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycomedb.residuetranslator.ResidueTranslator;
import org.grits.toolbox.tools.glycanbuilder.core.config.GraphicOptionsSWT;
import org.grits.toolbox.tools.glycanbuilder.core.renderer.awt.SVGUtils;
import org.grits.toolbox.tools.glycanbuilder.core.workspace.BuilderWorkspaceSWT;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.utils.process.GlycoWorkbenchUtil;
import org.grits.toolbox.widgets.tools.IGRITSEventHandler;
import org.grits.toolbox.widgets.tools.IGRITSEventListener;

/**
 * Because Eclipse has a limitation on the number of image handles, we need image management, allowing GRITS to dispose of images that aren't being
 * viewed without disposing of the meta-data (sequence info). This class creates a Stack, where the most recently viewed image is on top
 * and images not visible in some time at the bottom. If the stack gets full, images on the bottom are popped off and the associated AWT and SWT
 * image objects are disposed.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @author Masaaki Matsubara (matsubara@uga.edu)
 *
 */
public class GlycanImageProvider implements IGRITSEventHandler {
	private static final Logger logger = Logger.getLogger(GlycanImageProvider.class);
	public final static String COMBO_SEQUENCE_SEPARATOR = "~|~";
	private final static Integer STACK_LIMIT = 4000;

	private HashMap<String, List<Glycan>> m_glycans = null;
	private Stack<GlycanImageObject> imageStack = null;
	private boolean blockAccess = false;

	private CartoonOptions m_options = null;
	private CartoonOptions m_FragmentOptions = null;
	private BuilderWorkspaceSWT m_bws;
//	private GlycanWorkspace m_gwb;
	private GlycoWorkbenchUtil m_gwbUtil;
	
	// debugging variables
	private int iCreateCnt = 0;
	private HashMap<String, Integer> mCreateCnt = new HashMap<String, Integer>();
	private HashMap<String, Integer> mDisposeCnt = new HashMap<String, Integer>();

	private List<IGRITSEventListener> eventListeners = new ArrayList<>();
	public final static int GLYCAN_BLOCK_RELEASED = 12345;
	
	public GlycanImageProvider() {
		m_glycans = new HashMap<String, List<Glycan>>();
		setGlycanWorkspace();
		imageStack = new Stack<>();
	}

	public synchronized void blockAccess() {
		this.blockAccess = true;
	}

	public synchronized void releaseBlock() {
		this.blockAccess = false;
	}

//	private GlycanWorkspace getGlycanWorkspace() {
//		return this.m_gwb;
//	}
//
	private BuilderWorkspaceSWT getBuilderWorkspaceSWT() {
		return this.m_bws;
	}

	private synchronized HashMap<String, List<Glycan>> getIdToGlycanList() {
		return this.m_glycans;
	}

	public synchronized Stack<GlycanImageObject> getImageStack() {
		return this.imageStack;
	}

	public synchronized void clearCache() {
		if( this.blockAccess )
			return;

		while( ! this.imageStack.isEmpty() ) {
			GlycanImageObject gio = this.imageStack.pop();
			gio.dispose();
		}
		this.m_glycans.clear();
	}

	private synchronized void pushGlycanImageObject( GlycanImageObject gio ) {
		if( this.imageStack.size() == STACK_LIMIT ) {
			GlycanImageObject gioToRemove = this.imageStack.get(STACK_LIMIT - 1);
			this.imageStack.remove(STACK_LIMIT - 1);
			if( ! gio.equals(gioToRemove) ) {
				gioToRemove.dispose();
			}
		}
		this.imageStack.push(gio);
	}

	private synchronized GlycanImageObject getFromStack(GlycanImageObject a_gio) {
		if( this.blockAccess )
			return null;
		int iInx = getImageStack().indexOf(a_gio);
		if( iInx >= 0 ) {
			GlycanImageObject me = (GlycanImageObject) getImageStack().remove(iInx);
			if( ! me.equals(a_gio) ) {
				logger.warn("Incorrect image object removed from stack.");
			}			
			pushGlycanImageObject(me);
			//			a_gio.dispose();
			return me;
		}		
		return null;
	}

	public synchronized GlycanImageObject getImage(String a_imageId)
	{
		if( this.blockAccess )
			return null;
		if( ! this.m_glycans.containsKey(a_imageId) ) 
			return null;
		GlycanImageObject newGio = new GlycanImageObject(a_imageId);
		GlycanImageObject prevGio = getFromStack(newGio);
		if( prevGio == null ) {
			return newGio;
		}
		newGio.dispose();
		return prevGio;
	}

	public synchronized int getStackSize() {
		if( this.blockAccess )
			return -2;
		return imageStack.size();
	}

	public synchronized int getCacheSize() {
		return m_glycans.size();
	}

	public synchronized CartoonOptions getCartoonOptions() {
		return this.m_options;
	}

	public synchronized void setCartoonOptions(CartoonOptions a_options) {
		this.m_options = a_options;
//		m_gwb.setNotation(m_options.getImageLayout());
//		m_gwb.setDisplay(m_options.getImageStyle()); 
//		m_gwb.getGraphicOptions().ORIENTATION = a_options.getImageOrientation();
		//        m_gwb.getGraphicOptions().SHOW_INFO = a_options.isShowInfo();
		m_bws.setNotation(m_options.getImageLayout());
		m_bws.setDisplay(m_options.getImageStyle());
		m_bws.getGraphicOptions().ORIENTATION = a_options.getImageOrientation();
	}

//	public static GlycanWorkspace getNewGlycanWorkspace() {
//		GlycanWorkspace t_gwb = new GlycanWorkspace(null,false,new GlycanRendererAWT());
//		t_gwb.getGraphicOptions().ORIENTATION = GraphicOptions.RL; 
//		t_gwb.getGraphicOptions().SHOW_INFO = true; 
//		t_gwb.getGraphicOptions().SHOW_MASSES = false; 
//		t_gwb.getGraphicOptions().SHOW_REDEND = true;
//		return t_gwb;
//	}

	public static BuilderWorkspaceSWT getNewBuilderWorkspaceSWT() {
		BuilderWorkspaceSWT t_bw = new BuilderWorkspaceSWT(SimianImageConverter.getCurrentDisplay());
		t_bw.getGraphicOptions().ORIENTATION = GraphicOptionsSWT.RL; 
		t_bw.getGraphicOptions().SHOW_INFO = true; 
		t_bw.getGraphicOptions().SHOW_MASSES = false; 
		t_bw.getGraphicOptions().SHOW_REDEND = true;
		return t_bw;
	}

	public void setGlycanWorkspace() {
//		if( m_gwb == null ) {
//			m_gwb = GlycanImageProvider.getNewGlycanWorkspace();
//		}
		if( m_bws == null ) {
			m_bws = GlycanImageProvider.getNewBuilderWorkspaceSWT();
		}
		if( m_gwbUtil == null ) {
			Config t_objConf = new Config();
			MonosaccharideConversion t_msdb = new MonosaccharideConverter(t_objConf);
			try {
				m_gwbUtil = new GlycoWorkbenchUtil(new ResidueTranslator(), t_msdb);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private synchronized boolean add(String a_imageId, Glycan a_glycan) throws Exception
	{
		List<Glycan> lGlycanList = null;
		if( this.m_glycans.containsKey(a_imageId) ) {
			lGlycanList = this.m_glycans.get(a_imageId);
		} else {
			lGlycanList = new ArrayList<>();
			this.m_glycans.put(a_imageId, lGlycanList);
		}
		if( lGlycanList.contains(a_glycan) ) 
			return false;

		lGlycanList.add(a_glycan);
		return true;
	}

	public synchronized void addMergeImageToProviderWithReducingEnd( String combinedGlycoCTSequence, String reducingEnd ) throws ImageCreationException {
		//logger.debug("Sequence: " + combinedGlycoCTSequence);
		try {
			if( m_glycans.get(combinedGlycoCTSequence) != null ) {
				return;
			}
			int iInx1 = combinedGlycoCTSequence.indexOf(COMBO_SEQUENCE_SEPARATOR);
			int rInx1 = reducingEnd.indexOf(COMBO_SEQUENCE_SEPARATOR);
			if (iInx1 < 0) {
				m_gwbUtil.parseGlycoCTCondensedSequence(combinedGlycoCTSequence);
				Glycan glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
				glycan.setReducingEndType(ResidueType.createOtherResidue(reducingEnd, -1.0));
				add(combinedGlycoCTSequence, glycan);
			} else {
				String sRemaining = combinedGlycoCTSequence;
				String reducindEndRemaining = reducingEnd;
				do {
					String sSeq = iInx1 > 0 ? sRemaining.substring(0, iInx1) : sRemaining;
					String rSeq = rInx1 > 0 ? reducindEndRemaining.substring(0, rInx1) : reducindEndRemaining;
					m_gwbUtil.parseGlycoCTCondensedSequence(sSeq);
					Glycan glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
					if (rSeq == null || rSeq.isEmpty()) {
						logger.error("Reducing End is not available for sequence " + combinedGlycoCTSequence);
						glycan.setReducingEndType(ResidueDictionary.findResidueType("freeEnd"));
					}
					else
						glycan.setReducingEndType(ResidueType.createOtherResidue(rSeq, -1.0));
					add(combinedGlycoCTSequence+reducingEnd, glycan);
					sRemaining = iInx1 > 0 ? sRemaining.substring(iInx1	+ COMBO_SEQUENCE_SEPARATOR.length()) : null;
					reducindEndRemaining = rInx1 > 0? reducindEndRemaining.substring(rInx1 + COMBO_SEQUENCE_SEPARATOR.length() ): null;
					iInx1 = sRemaining != null ? sRemaining.indexOf(COMBO_SEQUENCE_SEPARATOR) : -1;
					rInx1 = reducindEndRemaining != null ? reducindEndRemaining.indexOf(COMBO_SEQUENCE_SEPARATOR) : -1;
				} while (sRemaining != null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ImageCreationException(e);
		}
	}

	public synchronized void addMergeImageToProvider( String _sCombinedSequence, String _sCombinedGlycanID ) throws ImageCreationException {
		try {
			if( m_glycans.get(_sCombinedSequence) != null ) {
				return;
			}
			int iInx1 = _sCombinedSequence.indexOf(COMBO_SEQUENCE_SEPARATOR);
			if (iInx1 < 0) {
				m_gwbUtil.parseGWSSequence(_sCombinedSequence);
				Glycan glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
				add(_sCombinedSequence, glycan);
			} else {
				String sRemaining = _sCombinedSequence;
				do {
					String sSeq = iInx1 > 0 ? sRemaining.substring(0, iInx1) : sRemaining;
					m_gwbUtil.parseGWSSequence(sSeq);
					Glycan glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
					add(_sCombinedSequence, glycan);
					sRemaining = iInx1 > 0 ? sRemaining.substring(iInx1	+ COMBO_SEQUENCE_SEPARATOR.length()) : null;
					iInx1 = sRemaining != null ? sRemaining.indexOf(COMBO_SEQUENCE_SEPARATOR) : -1;
				} while (sRemaining != null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ImageCreationException(e);
		}
	}

	public synchronized void addImageToProviderWithReducingEnd( String glycoCtSequence, String reducingEnd ) throws ImageCreationException {
		//logger.debug("Sequence: " + glycoCtSequence);
		try {
			if( m_glycans.get(glycoCtSequence) != null ) {
				return;
			}
			m_gwbUtil.parseGlycoCTCondensedSequence(glycoCtSequence.trim());
			if (m_gwbUtil.getGlycoWorkbenchGlycan() != null )
			{
				// GWB sequence
				Glycan t_glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
				if (reducingEnd == null || reducingEnd.isEmpty()) {
					logger.error("Reducing End is not available for sequence " + glycoCtSequence);
					t_glycan.setReducingEndType(ResidueDictionary.findResidueType("freeEnd"));
				}
				else
					t_glycan.setReducingEndType(ResidueType.createOtherResidue(reducingEnd, -1.0));
				List<Glycan> lPhony = new ArrayList<>();
				lPhony.add(t_glycan);
				m_glycans.put(glycoCtSequence+reducingEnd, lPhony);
			}	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ImageCreationException(e);
		}
	}

	public synchronized void addImageToProvider( String _sSequence, String _sGlycanID ) throws ImageCreationException {
		try {
			if( m_glycans.get(_sSequence) != null ) {
				return;
			}
			m_gwbUtil.parseGWSSequence(_sSequence);
			if (m_gwbUtil.getGlycoWorkbenchGlycan() != null )
			{
				// GWB sequence
				Glycan t_glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
				List<Glycan> lPhony = new ArrayList<>();
				lPhony.add(t_glycan);
				m_glycans.put(_sSequence, lPhony);
			}	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ImageCreationException(e);
		}
	}

	/*public static BufferedImage createCombinedImage(List<BufferedImage> a_image)  {
		try {
			BufferedImage t_result = SimianImageConverterOld.createImage(findMaxWidth(a_image), totalHeight(a_image));
			int t_positionY = 0;
			for (BufferedImage t_bufferedImage : a_image)
			{
				addImage(t_bufferedImage, t_positionY, t_result);
				t_positionY += t_bufferedImage.getHeight();
			}
			return t_result;
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private static Integer totalHeight(List<BufferedImage> a_image)
	{
		int t_total = 0;
		for (BufferedImage t_bufferedImage : a_image)
		{
			t_total += t_bufferedImage.getHeight();
		}
		return t_total;
	}

	private static Integer findMaxWidth(List<BufferedImage> a_image)
	{
		int t_max = 0;
		for (BufferedImage t_bufferedImage : a_image)
		{
			if ( t_bufferedImage.getWidth() > t_max)
			{
				t_max = t_bufferedImage.getWidth();
			}
		}
		return t_max;
	}

	private static void addImage(BufferedImage a_bufferedImage, int a_positionY, BufferedImage a_image)
	{
		a_image.createGraphics().drawImage(a_bufferedImage, 0, a_positionY , null);
	}*/

	@Override
	public void addEventListener(IGRITSEventListener arg0) {
		if( ! eventListeners.contains(arg0) ) {
			eventListeners.add(arg0);
		}			
	}

	@Override
	public List<IGRITSEventListener> getEventListeners() {
		return eventListeners;
	}

	@Override
	public void notifyListeners(int arg0) {
		for( IGRITSEventListener listener : eventListeners ) {
			listener.handleEvent(arg0);
		}
	}

	@Override
	public void removeEventListener(IGRITSEventListener arg0) {
		if( eventListeners.contains(arg0) ) {
			eventListeners.remove(arg0);
		}			
	}
	
	public class GlycanImageObject {
		private Image swtImage;
		private BufferedImage awtBufferedImage;
		private String sImageId = null;
		private boolean bIsOriginalSize;
		private double dScaleValX;
		private double dScaleValY;
		private Integer iHashCode = null;
		private boolean bBlock = false;

		public GlycanImageObject( String sImageId ) {
			this.sImageId = sImageId;
		}

		public boolean isOriginalSize() {
			return bIsOriginalSize;
		}

		public void setIsOriginalSize(boolean bIsOriginalSize) {
			this.bIsOriginalSize = bIsOriginalSize;
		}

		@Override
		public int hashCode() {
			if( iHashCode != null ) 
				return iHashCode.intValue();
			iHashCode = new Integer(sImageId.hashCode());
			return hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if( ! (obj instanceof GlycanImageObject) )
				return false;
			return ( (GlycanImageObject) obj).sImageId.equals(this.sImageId);
		}

		@Override
		public String toString() {
			return "Image: "+ sImageId + ", original size: " + bIsOriginalSize;
		}

		public String getImageId() {
			return sImageId;
		}

		public void blockAccess() {
			this.bBlock = true;
		}

		public void releaseBlock() {
			this.bBlock = false;
		}

		private void setScaledImage( double dScaleValX, double dScaleValY ) {
			double dScaleVal1 = dScaleValX / (double) getAwtBufferedImage().getWidth();
			double dScaleVal2 = dScaleValY / (double) getAwtBufferedImage().getHeight();
			double dScaleVal = dScaleVal1 < dScaleVal2 ? dScaleVal1 : dScaleVal2;

			int width = (int) ( (double) getAwtBufferedImage().getWidth() * dScaleVal );
			int height = (int) ( (double) getAwtBufferedImage().getHeight() * dScaleVal );
			java.awt.Image newImage = getAwtBufferedImage().getScaledInstance( width, height, BufferedImage.SCALE_AREA_AVERAGING );	
			dispose();
			this.awtBufferedImage = SimianImageConverter.convert(newImage);
			this.swtImage = SimianImageConverter.convert(awtBufferedImage);
			bIsOriginalSize = false;
			newImage.flush();
		}

		private void loadImageObjects() {
			List<Glycan> lGlycans = getIdToGlycanList().get(sImageId);
			if( lGlycans == null )
				return;
			BufferedImage bImage = null;
			boolean bShowRedEnd = sImageId.contains("cleavage") ? true : getCartoonOptions().isShowRedEnd();
			bImage = SimianImageConverter.createCartoon(lGlycans, getBuilderWorkspaceSWT(),
					getCartoonOptions().getImageScaleFactor(),
					getCartoonOptions().isShowMasses(),
					bShowRedEnd,
					true, BufferedImage.SCALE_AREA_AVERAGING);
//			if( lGlycans.size() == 1 ) {
//				bImage = SimianImageConverter.createCartoon(lGlycans.get(0), getGlycanWorkspace(), 
//						getCartoonOptions().getImageScaleFactor(),
//						getCartoonOptions().isShowMasses(),
//						bShowRedEnd,
//						true, BufferedImage.SCALE_AREA_AVERAGING);
//			} else {
//				List<BufferedImage> alList = new ArrayList<BufferedImage>();	
//				for( Glycan glycan : lGlycans ) {
//					BufferedImage newImg = SimianImageConverter.createCartoon(glycan, getGlycanWorkspace(), 
//							getCartoonOptions().getImageScaleFactor(),
//							getCartoonOptions().isShowMasses(),
//							bShowRedEnd,
//							true, BufferedImage.SCALE_AREA_AVERAGING);
//					alList.add(newImg);
//				}
//				bImage = GlycanImageProvider.createCombinedImage(alList);
//			}
			this.awtBufferedImage = bImage;
			this.swtImage = SimianImageConverter.convert(awtBufferedImage);
			bIsOriginalSize = true;
			iCreateCnt++;
//			if( (iCreateCnt % 100) == 0 )
//				logger.debug("Creating image, current count: " + iCreateCnt);
			int iCnt = 0;
			if( mCreateCnt.containsKey(sImageId) ) {
				iCnt= mCreateCnt.get(sImageId);				
			}
			iCnt++;
			mCreateCnt.put(sImageId, iCnt);
		}

		public boolean isDisposed() {
			if( this.swtImage.isDisposed() ) {
				return true;
			}
			return this.swtImage == null && this.awtBufferedImage == null;
		}

		public void dispose() {
			if( this.bBlock ) {
				return;
			}
			if( this.swtImage != null ) {
				this.swtImage.dispose();
				this.swtImage = null;
			}
			if( this.awtBufferedImage != null ) {
				this.awtBufferedImage.flush();
				this.awtBufferedImage = null;
			}
			iCreateCnt--;
//			if( (iCreateCnt % 100) == 0 )
//				logger.debug("Creating image, current count: " + iCreateCnt);
			int iCnt = 0;
			if( mDisposeCnt.containsKey(sImageId) ) {
				iCnt= mDisposeCnt.get(sImageId);				
			}
			iCnt++;
			mDisposeCnt.put(sImageId, iCnt);			
		}

		public BufferedImage getScaledAwtBufferedImage(double dScaleValX, double dScaleValY) {
			if( blockAccess ) {
				return null;
			}
			if( this.awtBufferedImage != null && ! isOriginalSize() && 
					dScaleValX == this.dScaleValX &&
					dScaleValY == this.dScaleValY ) {
				//				pushGlycanImageObject(this);
				if( getFromStack(this) == null )
					pushGlycanImageObject(this);
				return this.awtBufferedImage;
			}			
			setScaledImage(dScaleValX, dScaleValY);
			return awtBufferedImage;
		}

		public BufferedImage getAwtBufferedImage() {
			if( blockAccess ) {
				return null;
			}
			GlycanImageObject gio = getFromStack(this);
			if( gio != null && gio.awtBufferedImage != null && gio.isOriginalSize() ) {
				return gio.awtBufferedImage;
			}
			if( this.awtBufferedImage == null || ! isOriginalSize()) {
				loadImageObjects();
			}
			//			if( getFromStack(this) == null )
			pushGlycanImageObject(this);
			return awtBufferedImage;
		}

		public Image getSwtImage() {
			if( blockAccess ) {
				return null;
			}
			GlycanImageObject gio = getFromStack(this);
			if( gio != null && gio.swtImage != null && gio.isOriginalSize() ) {
				return gio.swtImage;
			}
			//			if( iCnt > 1 ) {
			//				int iDCnt = 0;
			//				if ( mDisposeCnt.containsKey(sImageId) ) {
			//					iDCnt = mDisposeCnt.get(sImageId);
			//				}
			//				logger.debug("Image already created. Create cnt: " + iCnt + ", dispose count: " + iDCnt);
			//			}
			if( this.swtImage == null || ! isOriginalSize()) {
				loadImageObjects();
			}
			//			if( getFromStack(this) == null )
			pushGlycanImageObject(this);
			return swtImage;
		}

		public void exportSVG(File file) throws FileNotFoundException, Exception {
			List<Glycan> lGlycans = getIdToGlycanList().get(sImageId);
			if( lGlycans == null )
				return;
			boolean bShowRedEnd = sImageId.contains("cleavage") ? true : getCartoonOptions().isShowRedEnd();
			// export into SVG
			SVGUtils.export(new FileOutputStream(file), getBuilderWorkspaceSWT().getGlycanRendererAWT(),
					lGlycans, getCartoonOptions().isShowMasses(), bShowRedEnd, 
					getCartoonOptions().getImageScaleFactor(), "svg");
		}
	}
}
