package org.grits.toolbox.utils.process;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorFromGlycoCT;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.io.simglycan.SugarImporterSimGlycan;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycoCTParser;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;

import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.annotation.sugar.GlycoVisitorNamespaceSimglycanToKegg;


public class GlycoWorkbenchUtil
{
    private MonosaccharideConversion m_residueTranslatorToGlycoCt = null;
    private MonosaccharideConversion m_residueTranslatorFromGlycoCt = null;

    private Sugar m_glycomeDbSugar = null;
    private Glycan m_glycoWorkbenchGlycan = null;

    public GlycoWorkbenchUtil(MonosaccharideConversion a_residueTranslatorToGlycoCt, MonosaccharideConversion a_residueTranslatorFromGlycoCt)
    {
        this.m_residueTranslatorFromGlycoCt = a_residueTranslatorFromGlycoCt;
        this.m_residueTranslatorToGlycoCt = a_residueTranslatorToGlycoCt;
    }
    
    public Sugar getGlycomeDbSugar()
    {
        return this.m_glycomeDbSugar;
    }

    public void setGlycomeDbSugar(Sugar a_glycomeDbSugar)
    {
        this.m_glycomeDbSugar = a_glycomeDbSugar;
    }

    public Glycan getGlycoWorkbenchGlycan()
    {
        return this.m_glycoWorkbenchGlycan;
    }

    public void setGlycoWorkbenchGlycan(Glycan a_glycoWorkbenchGlycan)
    {
        this.m_glycoWorkbenchGlycan = a_glycoWorkbenchGlycan;
    }

    public void parseGWSSequence(String a_sequence) throws Exception
    {
        this.m_glycomeDbSugar = null;
        this.m_glycoWorkbenchGlycan = GlycanExtraInfo.gwbToGlycan(a_sequence);
    }
    
    public void parseSimGlycanSequence(String a_sequence) throws Exception
    {
        this.m_glycomeDbSugar = null;
        this.m_glycoWorkbenchGlycan = null;
        this.m_glycomeDbSugar = this.createGlycoCTsugarFromSimGlycanFormat(a_sequence);
        this.m_glycoWorkbenchGlycan = this.createGwbGlycan(this.m_glycomeDbSugar);
    }

    public void parseGlycoCTCondensedSequence(String a_sequence) throws Exception
    {
        this.m_glycomeDbSugar = null;
        this.m_glycoWorkbenchGlycan = null;
        this.m_glycomeDbSugar = this.createGlycoCTsugarFromGlycoCTCondensedFormat(a_sequence);
        this.m_glycoWorkbenchGlycan = this.createGwbGlycan(this.m_glycomeDbSugar);
    }
    
    private Sugar createGlycoCTsugarFromGlycoCTCondensedFormat(String a_strSequence) throws SugarImporterException, GlycoVisitorException 
    {   
        Sugar t_sugarResult = null;
        // parse sequence in a sugar object
        SugarImporterGlycoCTCondensed t_importer = new SugarImporterGlycoCTCondensed();
        Sugar t_sugar = t_importer.parse(a_strSequence);
        // translate to GlycoCT
        GlycoVisitorToGlycoCT t_objVisitorGlycoCT = new GlycoVisitorToGlycoCT(this.m_residueTranslatorToGlycoCt, GlycanNamescheme.KEGG);
        t_objVisitorGlycoCT.setUseStrict(true);
        t_objVisitorGlycoCT.setUseSubstPosition(true);
        t_objVisitorGlycoCT.setUseFusion(true);
        t_objVisitorGlycoCT.start( t_sugar );
        t_sugarResult = t_objVisitorGlycoCT.getNormalizedSugar();
        return t_sugarResult;
    }
    
    private Sugar createGlycoCTsugarFromSimGlycanFormat(String a_strSequence) throws SugarImporterException, GlycoVisitorException 
    {   
        Sugar t_sugarResult = null;
        // parse sequence in a sugar object
        SugarImporterSimGlycan t_importer = new SugarImporterSimGlycan();
        Sugar t_sugar = t_importer.parse(a_strSequence);
        GlycoVisitorNamespaceSimglycanToKegg t_visNamespace = new GlycoVisitorNamespaceSimglycanToKegg();
        t_visNamespace.start(t_sugar);
        // translate to GlycoCT
        GlycoVisitorToGlycoCT t_objVisitorGlycoCT = new GlycoVisitorToGlycoCT(this.m_residueTranslatorToGlycoCt, GlycanNamescheme.KEGG);
        t_objVisitorGlycoCT.setUseStrict(true);
        t_objVisitorGlycoCT.setUseSubstPosition(true);
        t_objVisitorGlycoCT.setUseFusion(true);
        t_objVisitorGlycoCT.start( t_sugar );
        t_sugarResult = t_objVisitorGlycoCT.getNormalizedSugar();
        return t_sugarResult;
    }

    private Glycan createGwbGlycan(Sugar a_sugar) throws Exception 
    {
        Glycan t_glycanResult = null;
        GlycoVisitorFromGlycoCT t_visFromGlycoCT = new GlycoVisitorFromGlycoCT( this.m_residueTranslatorFromGlycoCt );
        t_visFromGlycoCT.setNameScheme(GlycanNamescheme.GWB);
        t_glycanResult = GlycoCTParser.fromSugar(a_sugar,this.m_residueTranslatorFromGlycoCt,t_visFromGlycoCT,new MassOptions(),true);
        return t_glycanResult;
    }
}