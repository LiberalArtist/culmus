/*************************************************************************
 *
 *  $RCSfile: XMeaningImpl.java,v $
 *
 *  $Revision: 1.0 $
 *
 *       change: $Author: Praveen Reddy V $ $Date: 2007/02/14 23:05:45 $
 *  last change: $Author: Maxim Iorsh     $ $Date: 2012/02/18 00:00:00 $
 *
 *  The Contents of this file are made available subject to the terms of
 *  the BSD license.
 *
 * Copyright (c) 2003 by Sun Microsystems, Inc.
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

package org.culmus.ooo.xmeaningimpltd;
 
import com.sun.star.lang.Locale;
 
public class XMeaningImpl implements
    com.sun.star.linguistic2.XMeaning
{
    String      aMeaning;
    String[]    aSynonyms;
 
    public XMeaningImpl ( String aMeaning, String[] aSynonyms)
    {
        this.aMeaning   = aMeaning;
        this.aSynonyms  = aSynonyms;
        
        if (this.aMeaning == null)
        {
            this.aMeaning = new String("");
	}
        
        if (this.aSynonyms == null)
          this.aSynonyms = new String[]{};
    }

    public String getMeaning() throws com.sun.star.uno.RuntimeException
    {
        return aMeaning;
    }
   public String[] querySynonyms() throws com.sun.star.uno.RuntimeException
   {
       return aSynonyms;
   }
};

