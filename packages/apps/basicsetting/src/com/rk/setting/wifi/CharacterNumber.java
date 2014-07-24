package com.rk.setting.wifi;

public class CharacterNumber
{
	private int mNumber = 0;
	private String mCharacter = null;
	public CharacterNumber(int number,String string)
	{
		mNumber = number;
		mCharacter = string;
	}

	public int getNumber()
	{
		return mNumber;
	}

	public String getString()
	{
		return mCharacter;
	}
}
