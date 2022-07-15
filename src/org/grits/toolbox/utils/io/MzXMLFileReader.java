package org.grits.toolbox.utils.io;

import java.io.IOException;

import org.systemsbiology.jrap.grits.stax.MSXMLParser;
import org.systemsbiology.jrap.grits.stax.Scan;
import org.systemsbiology.jrap.grits.stax.ScanHeader;

public class MzXMLFileReader 
{
    private MSXMLParser m_parser;

    public MzXMLFileReader(String a_fileName) throws IOException 
    {
        this.m_parser = new MSXMLParser(a_fileName);
        int t_info = this.m_parser.getScanCount();
        if ( t_info == 0)
        {
            throw new IOException("File is not a valid mzXML file.");
        }
    }

    public MzXMLFileReader(String a_fileName, boolean _bIsSequential) throws IOException 
    {
        this.m_parser = new MSXMLParser(a_fileName, _bIsSequential);
        int t_info = this.m_parser.getScanCount();
        if ( t_info == 0)
        {
            throw new IOException("File is not a valid mzXML file.");
        }
    }


    public double[][] parse(int a_scan) 
    {
        Scan t_scan = m_parser.rap(a_scan);
        if( t_scan == null ) 
        	return null;
        double[][] t_massList = t_scan.getMassIntensityList();
        return t_massList;
    }
    
    public Scan getScan(int a_scan) {
    	Scan t_scan = m_parser.rap(a_scan);
    	return t_scan;
    }

    public Scan getFirstMS1Scan() {
    	Scan firstScan = null;
    	if ( m_parser != null ) {
    		for( int i = 1; firstScan == null && i < m_parser.getMaxScanNumber() + 1; i++ ) {
    			ScanHeader header = m_parser.rapHeader(i);
    			if ( header != null && header.getMsLevel() == 1 ) {
    				Scan s = m_parser.rap(i);
    				if( s.getMassIntensityList() != null && s.getMassIntensityList()[0].length > 0 ) {
    					firstScan = m_parser.rap(i);
    					return firstScan;
    				}
    			}
    		}
    	}
    	return null;
    }
}