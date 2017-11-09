package application;
import org.bytedeco.javacpp.*;

import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;




public class OCR
{
    public  String init() throws FontFormatException
    {
    	try {
    	     GraphicsEnvironment ge = 
    	         GraphicsEnvironment.getLocalGraphicsEnvironment();
    	     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("f2.ttf")));
    	} catch (IOException e) {
    	     //Handle exception
    	}
    	
       BytePointer outText;
       
       TessBaseAPI api=new TessBaseAPI();
  
   
  
       
        //to use tesseract api,at first you have to install tesseract with desired language training data on your system.After That you have to mention 
       //the installation folder.
       if(api.Init("C:/tessdata", "eng") != 0)
       {
    	   System.out.println("could not initialize tesseract");
    	   System.exit(1);
    	   
       }
      
       //
       
       //For Bengali Language
       /*    if(api.Init("C:/tesseract-ocr/tessdata", "ben") != 0)
       {
    	   System.out.println("could not initialize tesseract");
    	   System.exit(1);
    	   
       }*/
       
       //read an image from default location for ocr output
       PIX image=pixRead("ocr_test.png");
       if(image==null)
       {
    	   System.err.println("Could not opened the image or Image not found ");
    	   
       }
       
       api.SetImage(image);
       
       outText=api.GetUTF8Text();
       
       
       String output= outText.getString();
       
       api.End();
       outText.deallocate();
       pixDestroy(image);
       
       return output;
    }
}
