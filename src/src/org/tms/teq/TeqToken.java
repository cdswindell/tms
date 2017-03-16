package org.tms.teq;

import org.tms.api.derivables.Token;

/**
 * Marker class to allow cell values to be set correctly from Remote Value and Time Series evaluation
 */
public class TeqToken extends Token 
{
	TeqToken(Token t)
	{
		super(t);
	}
}
