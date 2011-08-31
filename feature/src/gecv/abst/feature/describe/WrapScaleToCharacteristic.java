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

package gecv.abst.feature.describe;

import gecv.abst.filter.derivative.ImageGradient;
import gecv.alg.distort.DistortImageOps;
import gecv.alg.feature.orientation.OrientationGradient;
import gecv.alg.interpolate.TypeInterpolate;
import gecv.core.image.GeneralizedImageOps;
import gecv.misc.GecvMiscOps;
import gecv.struct.ImageRectangle;
import gecv.struct.feature.TupleDesc_F64;
import gecv.struct.image.ImageBase;


/**
 * Wrapper for steerable filters.  The input image patch is resized to a characteristic size and then
 * feed into the filter.
 *
 * @author Peter Abeles
 */
public abstract class WrapScaleToCharacteristic <T extends ImageBase, D extends ImageBase>
		implements ExtractFeatureDescription<T>
{
	OrientationGradient<D> orientation;
	ImageGradient<T,D> gradient;

	T image;
	T scaledImage;
	D scaledDerivX;
	D scaledDerivY;

	int steerR;

	public WrapScaleToCharacteristic( int radiusR ,
								   OrientationGradient<D> orientation,
								   ImageGradient<T, D> gradient,
								   Class<T> inputType ,
								   Class<D> derivType ) {
		this.orientation = orientation;
		this.gradient = gradient;

		steerR = radiusR;
		int w = steerR*2+1+2;
		// +2 is for image border when computing the image derivative

		scaledImage = GeneralizedImageOps.createImage(inputType,w,w);
		scaledDerivX = GeneralizedImageOps.createImage(derivType,w,w);
		scaledDerivY = GeneralizedImageOps.createImage(derivType,w,w);
	}

	@Override
	public void setImage(T image) {
		this.image = image;

	}


	@Override
	public TupleDesc_F64 process(int x, int y, double scale, TupleDesc_F64 ret ) {
		// compute the size of the region at this scale
		int r = (int)Math.ceil(scale*3)+1;

		ImageRectangle area = new ImageRectangle(x-r,y-r,x+r+1,y+r+1);
		if( !GecvMiscOps.checkInside(image,area) )
			return null;

		// create a subimage of this region
		T subImage = (T)image.subimage(area.x0,area.y0,area.x1,area.y1);

		DistortImageOps.scale(subImage,scaledImage, TypeInterpolate.BILINEAR);

		// compute the gradient
		gradient.process(scaledImage, scaledDerivX, scaledDerivY);

		// estimate the angle
		orientation.setImage(scaledDerivX,scaledDerivY);
		double angle = orientation.compute(steerR+1,steerR+1);

		return describe(steerR+1,steerR+1,angle,ret);
		// +1 to avoid edge conditions
	}

	protected abstract TupleDesc_F64 describe( int x , int y , double angle , TupleDesc_F64 ret );
}