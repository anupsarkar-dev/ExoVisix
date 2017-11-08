package application;

import javax.swing.JFrame;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.CvSize;
import org.bytedeco.javacpp.opencv_core.CvSlice;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.*;

import javafx.scene.image.ImageView;
import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class SquareDetector {

	Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	IplImage frame;
	ImageView frameShow;
	public Exception exception = null;
	public IplImage grabbedImage = null, grayImage = null, smallImage = null;
	public CanvasFrame canvass = new CanvasFrame("Webcam");

	// use default camera
	public OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);;

	public void setFrameShow(ImageView frameShow) {
		this.frameShow = frameShow;
	}

	public void setFrame(IplImage frame) {
		this.frame = frame;
	}

	int thresh = 50;
	IplImage img = null;
	IplImage img0 = null;
	CvMemStorage storage = null;
	CvMemStorage storage2 = null;
	String wndname = "Square Detection Demo";

	// Java spesific

	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

	// helper function:
	// finds a cosine of angle between vectors
	// from pt0->pt1 and from pt0->pt2
	double angle(CvPoint pt1, CvPoint pt2, CvPoint pt0) {
		double dx1 = pt1.x() - pt0.x();
		double dy1 = pt1.y() - pt0.y();
		double dx2 = pt2.x() - pt0.x();
		double dy2 = pt2.y() - pt0.y();

		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}

	// returns sequence of squares detected on the image.
	// the sequence is stored in the specified memory storage
	CvSeq findSquares4(IplImage img, CvMemStorage storage) {
		// Java translation: moved into loop
		// CvSeq contours = new CvSeq();
		int i, c, l, N = 11;
		CvSize sz = cvSize(img.width() & -2, img.height() & -2);
		IplImage timg = cvCloneImage(img); // make a copy of input image
		IplImage gray = cvCreateImage(sz, 8, 1);
		IplImage pyr = cvCreateImage(cvSize(sz.width() / 2, sz.height() / 2), 8, 3);
		IplImage tgray = null;
		// Java translation: moved into loop
		// CvSeq result = null;
		// double s = 0.0, t = 0.0;

		// create empty sequence that will contain points -
		// 4 points per square (the square's vertices)
		CvSeq squares = cvCreateSeq(0, Loader.sizeof(CvSeq.class), Loader.sizeof(CvPoint.class), storage);

		// select the maximum ROI in the image
		// with the width and height divisible by 2
		cvSetImageROI(timg, cvRect(0, 0, sz.width(), sz.height()));

		// down-scale and upscale the image to filter out the noise
		cvPyrDown(timg, pyr, 7);
		cvPyrUp(pyr, timg, 7);
		tgray = cvCreateImage(sz, 8, 1);

		// find squares in every color plane of the image
		for (c = 0; c < 3; c++) {
			// extract the c-th color plane
			cvSetImageCOI(timg, c + 1);
			cvCopy(timg, tgray);

			// try several threshold levels
			for (l = 0; l < N; l++) {
				// hack: use Canny instead of zero threshold level.
				// Canny helps to catch squares with gradient shading
				if (l == 0) {
					// apply Canny. Take the upper threshold from slider
					// and set the lower to 0 (which forces edges merging)
					cvCanny(tgray, gray, 0, thresh, 5);
					// dilate canny output to remove potential
					// holes between edge segments
					cvDilate(gray, gray, null, 1);
				} else {
					// apply threshold if l!=0:
					// tgray(x,y) = gray(x,y) < (l+1)*255/N ? 255 : 0
					cvThreshold(tgray, gray, (l + 1) * 255 / N, 255, CV_THRESH_BINARY);
				}

				// find contours and store them all as a list
				// Java translation: moved into the loop
				CvSeq contours = new CvSeq();
				cvFindContours(gray, storage, contours, Loader.sizeof(CvContour.class), CV_RETR_LIST,
						CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

				// test each contour
				while (contours != null && !contours.isNull()) {
					// approximate contour with accuracy proportional
					// to the contour perimeter
					// Java translation: moved into the loop
					CvSeq result = cvApproxPoly(contours, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP,
							cvContourPerimeter(contours) * 0.02, 0);
					// square contours should have 4 vertices after
					// approximation
					// relatively large area (to filter out noisy contours)
					// and be convex.
					// Note: absolute value of an area is used because
					// area may be positive or negative - in accordance with the
					// contour orientation
					if (result.total() == 4 && Math.abs(cvContourArea(result, CV_WHOLE_SEQ, 0)) > 1000
							&& cvCheckContourConvexity(result) != 0) {

						// Java translation: moved into loop
						double s = 0.0, t = 0.0;

						for (i = 0; i < 5; i++) {
							// find minimum angle between joint
							// edges (maximum of cosine)
							if (i >= 2) {
								// Java translation:
								// Comment from the HoughLines.java sample code:
								// " Based on JavaCPP, the equivalent of the C
								// code:
								// CvPoint* line =
								// (CvPoint*)cvGetSeqElem(lines,i);
								// CvPoint first=line[0];
								// CvPoint second=line[1];
								// is:
								// Pointer line = cvGetSeqElem(lines, i);
								// CvPoint first = new
								// CvPoint(line).position(0);
								// CvPoint second = new
								// CvPoint(line).position(1);
								// "
								// ... so after some trial and error this seem
								// to work
								// t = fabs(angle(
								// (CvPoint*)cvGetSeqElem( result, i ),
								// (CvPoint*)cvGetSeqElem( result, i-2 ),
								// (CvPoint*)cvGetSeqElem( result, i-1 )));
								t = Math.abs(angle(new CvPoint(cvGetSeqElem(result, i)),
										new CvPoint(cvGetSeqElem(result, i - 2)),
										new CvPoint(cvGetSeqElem(result, i - 1))));
								s = s > t ? s : t;
							}
						}

						// if cosines of all angles are small
						// (all angles are ~90 degree) then write quandrange
						// vertices to resultant sequence
						if (s < 0.3)
							for (i = 0; i < 4; i++) {
								cvSeqPush(squares, cvGetSeqElem(result, i));
							}
					}

					// take the next contour
					contours = contours.h_next();
				}
			}
		}

		// release all the temporary images
		cvReleaseImage(gray);
		cvReleaseImage(pyr);
		cvReleaseImage(tgray);
		cvReleaseImage(timg);

		return squares;
	}

	// the function draws all the squares in the image
	void drawSquares(IplImage img, CvSeq squares) {

		// Java translation: Here the code is somewhat different from the C
		// version.
		// I was unable to get straight forward CvPoint[] arrays
		// working with "reader" and the "CV_READ_SEQ_ELEM".

		// CvSeqReader reader = new CvSeqReader();

		IplImage cpy = cvCloneImage(img);
		int i = 0;

		// Used by attempt 3
		// Create a "super"-slice, consisting of the entire sequence of squares
		CvSlice slice = new CvSlice(squares);

		// initialize reader of the sequence
		// cvStartReadSeq(squares, reader, 0);

		// read 4 sequence elements at a time (all vertices of a square)
		for (i = 0; i < squares.total(); i += 4) {
			CvPoint rect = new CvPoint(4);
			IntPointer count = new IntPointer(1).put(4);
			// get the 4 corner slice from the "super"-slice
			cvCvtSeqToArray(squares, rect, slice.start_index(i).end_index(i + 4));

			cvPolyLine(cpy, rect.position(0), count, 1, 1, CV_RGB(0, 255, 0), 3, CV_AA, 0);

			// Frame frame =converter.convert(cpy);
			// canvass.showImage(converter.convert(cpy));

			/*
			 * BufferedImage image = paintConverter.getBufferedImage(frame, 2.2
			 * );
			 * 
			 * 
			 * WritableImage display = SwingFXUtils.toFXImage(image, null);
			 * 
			 * 
			 * frameShow.setImage(display);
			 */

			// return frame;
			// canvas.showImage(converter.convert(cpy));

		}
		canvass.showImage(converter.convert(cpy));

		cvReleaseImage(cpy);
	}

	public void loop() {
		// canvass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		storage2 = CvMemStorage.create();

		try {

			storage = cvCreateMemStorage(0);

			try {
				grabber.start();
			} catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// get framerate
			double frameRate = grabber.getFrameRate();
			long wait = (long) (1000 / (frameRate == 0 ? 10 : frameRate));

			// keep capturing
			while (true) {
				Thread.sleep(wait);
				grabbedImage = converter.convert(grabber.grab());

				// drawSquares(grabbedImage, findSquares4(grabbedImage,
				// storage2));

				// canvas.showImage(converter.convert(grabbedImage));
				// show grabbed image

				cvClearMemStorage(storage2);
				cvClearMemStorage(storage);

				if (grabbedImage != null) {

					// canvas.showImage(converter.convert(grabbedImage));

					drawSquares(grabbedImage, findSquares4(grabbedImage, storage));
				}

			}

			// show stack trace
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
