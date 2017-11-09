package application;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;

public class FaceRecognizer {

	LBPHFaceRecognizer faceRecognizer;
	public File root;
	MatVector images;
	Mat labels;

	public void init() {
		// mention the directory the faces has been saved
		String trainingDir = "./faces";

		root = new File(trainingDir);

		FilenameFilter imgFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			}
		};

		File[] imageFiles = root.listFiles(imgFilter);

		this.images = new MatVector(imageFiles.length);

		this.labels = new Mat(imageFiles.length, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.createBuffer();

		int counter = 0;
		// reading face images from the folder

		for (File image : imageFiles) {
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

			// extracting unique face code from the face image names
			// this unique face will be used to fetch all other information from
			// the database
			int label = Integer.parseInt(image.getName().split("\\-")[0]);

			images.put(counter, img);

			labelsBuf.put(counter, label);

			counter++;
		}

		// face training
		this.faceRecognizer = createLBPHFaceRecognizer();
		this.faceRecognizer.train(images, labels);

	}

	public int recognize(IplImage faceData) {

		Mat faces = cvarrToMat(faceData);

		cvtColor(faces, faces, CV_BGR2GRAY);

		IntPointer label = new IntPointer(1);
		DoublePointer confidence = new DoublePointer(0);
		this.faceRecognizer.predict(faces, label, confidence);

		int predictedLabel = label.get(0);

		return predictedLabel;

	}
}
