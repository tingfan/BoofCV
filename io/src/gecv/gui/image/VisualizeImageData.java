/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.gui.image;

import gecv.abst.misc.GenericPixelMath;
import gecv.alg.misc.PixelMath;
import gecv.core.image.ConvertBufferedImage;
import gecv.struct.image.ImageBase;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageInteger;

import java.awt.image.BufferedImage;

/**
 * Renders different primitive image types into a BufferedImage for visualization purposes.
 *
 * @author Peter Abeles
 */
public class VisualizeImageData {

	public static BufferedImage standard( ImageBase<?> src , BufferedImage dst )
	{
		if( ImageInteger.class.isAssignableFrom(src.getClass()) ) {
			ImageInteger srcInt = (ImageInteger)src;

			double max = GenericPixelMath.maxAbs(srcInt);
			return colorizeSign(srcInt,dst,(int)max);
		} else if( ImageFloat32.class.isAssignableFrom(src.getClass()) ) {
			float max = PixelMath.maxAbs((ImageFloat32)src);
			return grayMagnitude((ImageFloat32)src,dst,max);
		}

		return null;
	}

	/**
	 * 
	 * @param src
	 * @param dst
	 * @param maxValue
	 * @return
	 */
	public static BufferedImage colorizeSign( ImageInteger src, BufferedImage dst, int maxValue ) {
		dst = ConvertBufferedImage.checkInputs(src, dst);

		if( !src.isSigned() )
			throw new IllegalArgumentException("Can only convert signed images.");

		if( maxValue == 0 )
			return dst;

		for( int y = 0; y < src.height; y++ ) {
			for( int x = 0; x < src.width; x++ ) {
				int v = src.get(x,y);

				int rgb;
				if( v > 0 ) {
					rgb = (255*v/maxValue) << 16;
				} else {
					rgb = -255*v/maxValue;
				}
				dst.setRGB(x,y,rgb);
			}
		}

		return dst;
	}

	public static BufferedImage grayUnsigned( ImageInteger src, BufferedImage dst, int maxValue )
	{
		dst = ConvertBufferedImage.checkInputs(src, dst);

		if( src.isSigned() )
			throw new IllegalArgumentException("Can only convert unsigned images.");

		for( int y = 0; y < src.height; y++ ) {
			for( int x = 0; x < src.width; x++ ) {
				int v = src.get(x,y);

				int rgb = 255 *v / maxValue;

				dst.setRGB(x,y,rgb << 16 | rgb << 8 | rgb  );
			}
		}

		return dst;
	}

	public static BufferedImage grayMagnitude( ImageInteger src, BufferedImage dst, int maxValue )
	{
		dst = ConvertBufferedImage.checkInputs(src, dst);

		for( int y = 0; y < src.height; y++ ) {
			for( int x = 0; x < src.width; x++ ) {
				int v = Math.abs(src.get(x,y));

				int rgb = 255 *v / maxValue;

				dst.setRGB(x,y,rgb << 16 | rgb << 8 | rgb  );
			}
		}

		return dst;
	}

	public static BufferedImage colorizeSign( ImageFloat32 src, BufferedImage dst, float maxValue ) {
		dst = ConvertBufferedImage.checkInputs(src, dst);

		for( int y = 0; y < src.height; y++ ) {
			for( int x = 0; x < src.width; x++ ) {
				float v = src.get(x,y);

				int rgb;
				if( v > 0 ) {
					rgb = (int)(255*v/maxValue) << 16;
				} else {
					rgb = (int)(-255*v/maxValue);
				}
				dst.setRGB(x,y,rgb);
			}
		}

		return dst;
	}

	public static BufferedImage grayMagnitude( ImageFloat32 src, BufferedImage dst, float maxValue )
	{
		dst = ConvertBufferedImage.checkInputs(src, dst);

		for( int y = 0; y < src.height; y++ ) {
			for( int x = 0; x < src.width; x++ ) {
				float v = Math.abs(src.get(x,y));

				int rgb = (int)(255 *v / maxValue);

				dst.setRGB(x,y,rgb << 16 | rgb << 8 | rgb  );
			}
		}

		return dst;
	}
}
