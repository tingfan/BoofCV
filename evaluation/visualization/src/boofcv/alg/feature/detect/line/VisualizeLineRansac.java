/*
 * Copyright (c) 2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
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

package boofcv.alg.feature.detect.line;


import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.alg.feature.detect.edge.GGradientToEdgeFeatures;
import boofcv.alg.feature.detect.line.gridline.Edgel;
import boofcv.alg.feature.detect.line.gridline.GridLineModelDistance;
import boofcv.alg.feature.detect.line.gridline.GridLineModelFitter;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.numerics.fitting.modelset.ModelMatcher;
import boofcv.numerics.fitting.modelset.ransac.SimpleInlierRansac;
import boofcv.struct.feature.MatrixOfList;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt8;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.line.LinePolar2D_F32;
import georegression.struct.line.LineSegment2D_F32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Computes the Hough Polar transform and displays some of its steps and the detected lines
 *
 * @author Peter Abeles
 */
public class VisualizeLineRansac<I extends ImageBase , D extends ImageBase> {

	Class<I> imageType;
	Class<D> derivType;

	public VisualizeLineRansac(Class<I> imageType, Class<D> derivType) {
		this.imageType = imageType;
		this.derivType = derivType;
	}

	public void process( BufferedImage image ) {
		int regionSize = 40;

		I input = GeneralizedImageOps.createImage(imageType,image.getWidth(),image.getHeight());
		D derivX = GeneralizedImageOps.createImage(derivType,image.getWidth(),image.getHeight());
		D derivY = GeneralizedImageOps.createImage(derivType,image.getWidth(),image.getHeight());
		ImageFloat32 edgeIntensity =  new ImageFloat32(input.width,input.height);
		ImageFloat32 suppressed =  new ImageFloat32(input.width,input.height);
		ImageFloat32 orientation =  new ImageFloat32(input.width,input.height);
		ImageSInt8 direction = new ImageSInt8(input.width,input.height);
		ImageUInt8 detected = new ImageUInt8(input.width,input.height);

		GridLineModelDistance distance = new GridLineModelDistance((float)(Math.PI*0.75));
		GridLineModelFitter fitter = new GridLineModelFitter((float)(Math.PI*0.75));

		ModelMatcher<LinePolar2D_F32, Edgel> matcher =
				new SimpleInlierRansac<LinePolar2D_F32,Edgel>(123123,fitter,distance,25,2,2*regionSize/3,1000,1);

		ImageGradient<I,D> gradient = FactoryDerivative.sobel(imageType, derivType);

		System.out.println("Image width "+input.width+" height "+input.height);

		ConvertBufferedImage.convertFrom(image, input, imageType);
		gradient.process(input, derivX, derivY);
		GGradientToEdgeFeatures.intensityAbs(derivX, derivY, edgeIntensity);

		// non-max suppression on the lines
//		GGradientToEdgeFeatures.direction(derivX,derivY,orientation);
//		GradientToEdgeFeatures.discretizeDirection4(orientation,direction);
//		GradientToEdgeFeatures.nonMaxSuppression4(edgeIntensity,direction,suppressed);

		GThresholdImageOps.threshold(edgeIntensity,detected,30,false);

		GridRansacLineDetector alg = new GridRansacLineDetector(40,10,matcher);

		alg.process((ImageFloat32) derivX, (ImageFloat32) derivY, detected);

		MatrixOfList<LineSegment2D_F32> gridLine = alg.getFoundLines();

		ConnectLinesGrid connect = new ConnectLinesGrid(Math.PI*0.01,1,8);
//		connect.process(gridLine);
//		LineImageOps.pruneClutteredGrids(gridLine,3);
		List<LineSegment2D_F32> found = gridLine.createSingleList();
//		LineImageOps.mergeSimilar(found,(float)(Math.PI*0.05),2f);
//		LineImageOps.pruneSmall(found,40);

		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLineSegments(found);
		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));

//		BufferedImage renderedTran = VisualizeImageData.grayMagnitude(alg.getTransform().getTransform(),null,-1);
//		BufferedImage renderedBinary = VisualizeBinaryData.renderBinary(alg.getBinary(), null);

//		ShowImages.showWindow(renderedBinary,"Detected Edges");
//		ShowImages.showWindow(renderedTran,"Parameter Space");
		ShowImages.showWindow(gui,"Detected Lines");
	}

	public static void main( String args[] ) {
		VisualizeLineRansac<ImageFloat32,ImageFloat32> app =
				new VisualizeLineRansac<ImageFloat32,ImageFloat32>(ImageFloat32.class,ImageFloat32.class);

//		app.process(UtilImageIO.loadImage("data/simple_objects.jpg"));
//		app.process(UtilImageIO.loadImage("data/shapes01.png"));
		app.process(UtilImageIO.loadImage("data/lines_indoors.jpg"));
//		app.process(UtilImageIO.loadImage("data/outdoors01.jpg"));
	}
}
