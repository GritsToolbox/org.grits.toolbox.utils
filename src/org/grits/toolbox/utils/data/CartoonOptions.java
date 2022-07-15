package org.grits.toolbox.utils.data;

public class CartoonOptions {
    private String sImageLayout = null;
    private String sImageStyle = null;
    private Double dImageScaleFactor = 1.0D;
    private Integer iImageOrientation = null;
    private boolean bShowInfo = true;
    private boolean bShowMasses = false;
    private boolean bShowRedEnd = true;
    
    public CartoonOptions( String _sImageLayout, String _sImageStyle, Double _dImageScaleFactor, 
    		Integer _iImageOrientation, boolean _bShowInfo, boolean _bShowMasses, boolean _bShowRedEnd ) {
		this.sImageLayout = _sImageLayout;
		this.sImageStyle = _sImageStyle;
		this.dImageScaleFactor = _dImageScaleFactor;
		this.iImageOrientation = _iImageOrientation;
		this.bShowInfo = _bShowInfo;
		this.bShowMasses = _bShowMasses;
		this.bShowRedEnd = _bShowRedEnd;
	}
    
    public boolean isShowInfo() {
		return bShowInfo;
	}
    public void setShowInfo(boolean bShowInfo) {
		this.bShowInfo = bShowInfo;
	}
    
    public boolean isShowMasses() {
		return bShowMasses;
	}
    public void setShowMasses(boolean bShowMasses) {
		this.bShowMasses = bShowMasses;
	}
    
    public boolean isShowRedEnd() {
		return bShowRedEnd;
	}
    public void setShowRedEnd(boolean bShowRedEnd) {
		this.bShowRedEnd = bShowRedEnd;
	}
    
    public Integer getImageOrientation() {
		return iImageOrientation;
	}
    
    public void setImageOrientation(Integer iImageOrientation) {
		this.iImageOrientation = iImageOrientation;
	}
    
    public String getImageLayout()
    {
        return sImageLayout;
    }

    public void setImageLayout(String a_imageLayout)
    {
        sImageLayout = a_imageLayout;
    }

    public String getImageStyle()
    {
        return sImageStyle;
    }

    public void setImageStyle(String a_imageStyle)
    {
        sImageStyle = a_imageStyle;
    }
    
    public void setImageScaleFactor(Double a_imageScaleFactor) {
		this.dImageScaleFactor = a_imageScaleFactor;
	}
    
    public Double getImageScaleFactor() {
		return dImageScaleFactor;
	}

}
