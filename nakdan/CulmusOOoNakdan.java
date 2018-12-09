/*************************************************************************
 *
 *  $RCSfile: HindiThesaurus.java,v $
 *
 *  $Revision: 1.0 $
 *
 *       change: $Author: Praveen Reddy V $ $Date: 2007/02/14 23:05:45 $
 *  last change: $Author: Maxim Iorsh     $ $Date: 2012/02/18 00:00:00 $
 *
 *  The Contents of this file are made available subject to the terms of
 *  the BSD license.
 *
 *  Copyright (c) 2003 by Sun Microsystems, Inc.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of Sun Microsystems, Inc. nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 *  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 *  USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *************************************************************************/


package org.culmus.ooo.nakdan;

import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.UnoRuntime;

import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;

import com.sun.star.linguistic2.XThesaurus;
import com.sun.star.lang.XInitialization;		
import com.sun.star.lang.XServiceInfo;		
import com.sun.star.lang.XServiceDisplayName;		

import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.lang.IllegalArgumentException;

import com.sun.star.linguistic2.XMeaning;
import com.sun.star.lang.Locale;
import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.Type;
import com.sun.star.comp.loader.FactoryHelper;
import org.culmus.ooo.xmeaningimpltd.*;

import java.util.ArrayList;
import java.io.*;
import java.util.*;
import java.lang.*;

import org.ivrix.hspell.*;
import org.culmus.nakdan.*;

public class CulmusOOoNakdan extends ComponentBase implements
        XThesaurus,
        XInitialization,
        XServiceDisplayName,
        XServiceInfo

{
    public String[] synms=new String[10];

    public CulmusOOoNakdan()
    {
        
    }

    private boolean IsEqual( Locale aLoc1, Locale aLoc2 )
    {
        return aLoc1.Language.equals( aLoc2.Language ) &&
               aLoc1.Country .equals( aLoc2.Country )  &&
               aLoc1.Variant .equals( aLoc2.Variant );  
    }

    // __________ interface methods __________
    
    
    //*****************
    //XSupportedLocales
    //*****************
    public Locale[] getLocales()
        throws com.sun.star.uno.RuntimeException
    {
        Locale aLocales[] = 
        {
            new Locale( "he", "IL", "" )
        };

        return aLocales;
    }
    
    public boolean hasLocale( Locale aLocale ) 
        throws com.sun.star.uno.RuntimeException
    {
        boolean bRes = false;
        if (IsEqual( aLocale, new Locale( "he", "IL", "" ) ))
            bRes = true;
        return bRes;        
    }
    
    //**********
    //XThesaurus
    //**********
    public XMeaning[] queryMeanings( 
            String aTerm, Locale aLocale,
            PropertyValue[] aProperties )
        throws com.sun.star.lang.IllegalArgumentException,
               com.sun.star.uno.RuntimeException
    {
        if (IsEqual( aLocale, new Locale() ) || aTerm.length() == 0)
            return null;
        
        if (!hasLocale( aLocale ))
            return null;

	try
	{
	    String error = TestInstall.TestEnvironment();

	    if (error != null)
		return new XMeaning[] {new XMeaningImpl(error, null)};
	}
        catch (java.lang.Throwable e)
        {
	    return new XMeaning[]{new JavaExceptionMeaning(e)};
        }

	try
	{
	    Vector<CulmusNakdan.Entry> entries = CulmusNakdan.queryMeanings(aTerm);

	    // Traverse results, group by meaning.
            TreeMap<String, TreeSet<String>> aMap = new TreeMap();

	    for (CulmusNakdan.Entry entry : entries)
	    {
		HSplit split = entry.key.split;
		HLingData ld = entry.key.ld;

		if (entry.values != null)
		{
		    for (CulmusNakdan.Value value : entry.values)
		    {
                        LexicalItem item = value.item;

		        String catCode;
 		        switch (item.category)
		        {
		            case Noun:		catCode = H.AYIN; break;
		            case Adjective:	catCode = H.TAV; break;
		            case Verb:		catCode = H.PE; break;
		            case Adposition:	catCode = H.MEM+". "+H.YOD+H.HET+H.SAMECH; break;
		            case Numeral:	catCode = H.MEM+H.SAMECH+H.PE+H.RESH; break;
		            default:
			    {
                                switch (ld.category)
                                {
		                    case Noun:		catCode = H.AYIN; break;
	                            case Adjective:	catCode = H.TAV; break;
		                    case Verb:		catCode = H.PE; break;
		                    default:            catCode = "?";
                                }
                            }
		        }

			String meaning = "";
                        if (split.prefix.length() > 0)
                            meaning += split.prefix + "+";
                        meaning += split.baseword + "[" + catCode + "]: " + item.description;

                        String valueDesc = "";
                        if (ld.category == HLingData.Category.Uncategorized)
                            valueDesc = item.describe();
                        else
                            valueDesc = ld.getDesc();

			if (!aMap.containsKey(meaning))
			    aMap.put(meaning, new TreeSet());

			if (!value.menukad.equals(""))
			{
			    aMap.get(meaning).add(value.menukad + " (" + valueDesc + ")");
			}
		    }
		}
	    }

	    ArrayList<XMeaning> aList = new ArrayList();
	    for (Map.Entry<String, TreeSet<String>> entry : aMap.entrySet())
	    {
		aList.add(new XMeaningImpl(entry.getKey(), entry.getValue().toArray(new String[]{})));
	    }

            XMeaning[] aRes = aList.toArray(new XMeaning[]{});

	    return aRes;
	}
	catch (java.lang.Throwable e)
	{
	    e.printStackTrace();
	    return new XMeaning[]{new JavaExceptionMeaning(e)};
	}
    }   //-------------END OF queryMeanings()------------
    
    //********************
    // XServiceDisplayName
    //********************
    public String getServiceDisplayName( Locale aLocale ) 
        throws com.sun.star.uno.RuntimeException
    {
	return "Nakdan by Culmus";                                                    
    }

    //****************
    // XInitialization
    //****************
    public void initialize( Object[] aArguments ) 
        throws com.sun.star.uno.Exception,
               com.sun.star.uno.RuntimeException
    {

    }

    //*************
    // XServiceInfo
    //*************
    public boolean supportsService( String aServiceName )
        throws com.sun.star.uno.RuntimeException
    {
        String[] aServices = getSupportedServiceNames_Static();
        int i, nLength = aServices.length;
        boolean bResult = false;

        for( i = 0; !bResult && i < nLength; ++i )
            bResult = aServiceName.equals( aServices[ i ] );

        return bResult;
    }

    public String getImplementationName()
        throws com.sun.star.uno.RuntimeException
    {
        return _aSvcImplName;
    }
        
    public String[] getSupportedServiceNames()
        throws com.sun.star.uno.RuntimeException
    {
        return getSupportedServiceNames_Static();
    }
    
    // __________ static things __________

    public static String _aSvcImplName = "Culmus Nakdan";
    
    public static String[] getSupportedServiceNames_Static()
    {
        String[] aResult = { "com.sun.star.linguistic2.Thesaurus" };
        return aResult;
    }


    /**
     * Returns a factory for crdeating the service.
     * This method is called by the <code>JavaLoader</code>
     * <p>
     * @return  returns a <code>XSingleServiceFactory</code> for creating the component
     * @param   implName     the name of the implementation for which a service is desired
     * @param   multiFactory the service manager to be used if needed
     * @param   regKey       the registryKey
     * @see                  com.sun.star.comp.loader.JavaLoader
     */
    public static XSingleServiceFactory __getServiceFactory(
        String aImplName,
        XMultiServiceFactory xMultiFactory,
        com.sun.star.registry.XRegistryKey xRegKey )
    {
        XSingleServiceFactory xSingleServiceFactory = null;
        if( aImplName.equals( _aSvcImplName ) )
        {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(
                    CulmusOOoNakdan.class, _aSvcImplName,
                    xMultiFactory, xRegKey );
        }
        return xSingleServiceFactory;
    }

    public static boolean __writeRegistryServiceInfo( 
            com.sun.star.registry.XRegistryKey xRegKey )
    {
        boolean bResult = true;
        String[] aServices = getSupportedServiceNames_Static();
        int i, nLength = aServices.length;
        for( i = 0; i < nLength; ++i )
        {
            bResult = bResult && FactoryHelper.writeRegistryServiceInfo(
                _aSvcImplName, aServices[i], xRegKey );
        }
        return bResult;
    }
}

