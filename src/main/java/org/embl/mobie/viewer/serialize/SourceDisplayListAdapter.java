/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.viewer.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.embl.mobie.viewer.display.RegionDisplay;
import org.embl.mobie.viewer.display.ImageDisplay;
import org.embl.mobie.viewer.display.SegmentationDisplay;
import org.embl.mobie.viewer.display.SourceDisplay;
import org.embl.mobie.viewer.transform.SourceTransformer;

import java.lang.reflect.Type;
import java.util.*;

public class SourceDisplayListAdapter implements JsonSerializer< List< SourceDisplay > >, JsonDeserializer< List< SourceDisplay > >
{
	private static Map<String, Class> nameToClass = new TreeMap<>();
	private static Map<String, String> classToName = new TreeMap<>();

	static {
		nameToClass.put("imageDisplay", ImageDisplay.class);
		classToName.put( ImageDisplay.class.getName(), "imageDisplay");
		nameToClass.put("segmentationDisplay", SegmentationDisplay.class);
		classToName.put( SegmentationDisplay.class.getName(), "segmentationDisplay");
		nameToClass.put("regionDisplay", RegionDisplay.class);
		classToName.put( RegionDisplay.class.getName(), "regionDisplay");
	}

	@Override
	public List< SourceDisplay > deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		List list = new ArrayList<SourceTransformer>();
		JsonArray ja = json.getAsJsonArray();

		for (JsonElement je : ja)
		{
			list.add( JsonHelper.createObjectFromJsonValue( context, je, nameToClass ) );
		}

		return list;
	}

	@Override
	public JsonElement serialize( List< SourceDisplay > sourceDisplays, Type type, JsonSerializationContext context ) {
		JsonArray ja = new JsonArray();
		for ( SourceDisplay sourceDisplay: sourceDisplays ) {
			Map< String, SourceDisplay > nameToSourceDisplay = new HashMap<>();
			nameToSourceDisplay.put( classToName.get( sourceDisplay.getClass().getName() ), sourceDisplay );

			if ( sourceDisplay instanceof ImageDisplay ) {
				ja.add( context.serialize( nameToSourceDisplay, new TypeToken< Map< String, ImageDisplay > >() {}.getType() ) );
			} else if ( sourceDisplay instanceof SegmentationDisplay ) {
				ja.add( context.serialize( nameToSourceDisplay , new TypeToken< Map< String, SegmentationDisplay > >() {}.getType() ) );
			} else if ( sourceDisplay instanceof RegionDisplay ) {
				ja.add( context.serialize( nameToSourceDisplay, new TypeToken< Map< String, RegionDisplay > >() {}.getType() ) );
			} else
			{
				throw new UnsupportedOperationException( "Could not serialise SourceDisplay of type: " + sourceDisplay.getClass().toString() );
			}
		}

		return ja;
	}
}
