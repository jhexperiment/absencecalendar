package com.jhexperiment.java.absence_calendar;

import java.lang.reflect.Type;
import java.util.BitSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Class to prevent Json library from trying to serialize BitSets
 * @author jhxmonkey
 *
 */
public class BitSetSerializer implements JsonSerializer<BitSet> {
	@Override
	public JsonElement serialize(BitSet src, Type arg1, JsonSerializationContext arg2) {
		return null; //I dont care about it !!!
	}
}
