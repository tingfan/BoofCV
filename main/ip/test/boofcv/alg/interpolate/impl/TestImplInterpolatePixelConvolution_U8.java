/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.interpolate.impl;

import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.struct.convolve.KernelContinuous1D_F32;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Peter Abeles
 */
public class TestImplInterpolatePixelConvolution_U8 
		extends GeneralInterpolationPixelChecks<ImageUInt8>
{
	public TestImplInterpolatePixelConvolution_U8() {
		exceptionOutside = false;
	}

	@Override
	protected ImageUInt8 createImage(int width, int height) {
		return new ImageUInt8(width, height);
	}

	@Override
	protected InterpolatePixel<ImageUInt8> wrap(ImageUInt8 image, int minValue, int maxValue) {
		InterpolatePixel<ImageUInt8> ret = new ImplInterpolatePixelConvolution_U8(new Dummy(),0,255);
		ret.setImage(image);

		return ret;
	}

	@Override
	protected float compute(ImageUInt8 img, float x, float y) {
		int xx = (int)x;
		int yy = (int)y;

		int x0 = xx - 2;
		int x1 = xx + 2;
		int y0 = yy - 2;
		int y1 = yy + 2;

		if( x0 < 0 ) x0 = 0;
		if( x1 >= img.width ) x1 = img.width-1;
		if( y0 < 0 ) y0 = 0;
		if( y1 >= img.height ) y1 = img.height-1;

		float total = 0;
		for( int i = y0; i <= y1; i++ ) {
			for( int j = x0; j <= x1; j++ ) {
				total += img.get(j,i);
			}
		}

		total /= (1+x1-x0)*(1+y1-y0);

		return total;
	}

	private static class Dummy extends KernelContinuous1D_F32
	{

		private Dummy() {
			super(5);
		}

		@Override
		public boolean isInteger() {
			return false;
		}

		@Override
		public float compute(float x) {
			return 1.0f/5.0f;
		}
	}
}
