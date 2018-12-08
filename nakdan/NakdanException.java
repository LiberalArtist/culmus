// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

class NakdanException extends Exception
{
    public enum ErrorCode
    {
	UnsupportedPartOfSpeech, UnsupportedKinuiGuf, WiktionaryMissingEntry, WiktionaryMissingForm,
	UnmatchedLingData
    }

    ErrorCode errorCode;

    public NakdanException(ErrorCode code)
    {
	errorCode = code;
    }
    
    public ErrorCode getErrorCode()
    {
	return errorCode;
    }
}
  
