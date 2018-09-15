//
// image2footprint.java v0.1
//
// Copyright (C) 2016 Erich S. Heinzle, a1039181@gmail.com

//    see LICENSE-gpl-v2.txt for software license
//    see README.txt
//    
//    This program is free software; you can redistribute it and/or
//    modify it under the terms of the GNU General Public License
//    as published by the Free Software Foundation; either version 2
//    of the License, or (at your option) any later version.
//    
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//    
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
//    MA  02110-1301, USA.
//    
//    image2footprint
//    Copyright (C) 2016 Erich S. Heinzle a1039181@gmail.com

import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.IOException;

public class image2footprint { 

  static PrintWriter footprint;
  static String FPHeader1 = "Element[\"\" \"";
  static String FPHeader2 = "\" \"\" \"\" 0 0 0 25000 0 100 \"\"]\n(";
  static String FPFooter = ")";
  static String filename = "";

  static int red = 0;
  static int green = 0;
  static int blue = 0;
  static Color pixel = null;
  static double luminosity = 0;

  static int width = 0;
  static int height = 0;
  static boolean addCornerPoints = false;

  static boolean wouldSirLikeSomeCrackedPepper = false;

  static long scaledPixelSize = 0;
  static long x = 0;
  static long y = 0;
  static long minimumPixelSize = 400; // centimils

  static long dotPitchMicrons = 125; //  = 0.125mm dot pitch
  static long dotPitchNM = dotPitchMicrons*1000;
  static long dotPitchDecimils = dotPitchNM/254;
  static long dotAreaNmSq = (long)(dotPitchNM*dotPitchNM/4);
  static long maximalDotFactor = dotPitchDecimils*dotPitchDecimils/765;

  static int pinNum = 0;

  public static void main(String [] args) throws IOException {

    if (args.length > 0) {
        filename = args[0];
        if (filename == null || "-h".equals(filename)) {
          printHelp();
          System.exit(0);
        }
    }

    for (int index = 1; index < args.length; index++) {
      if (args[index].equals("-pp")
          && (++index < args.length)) {
        dotPitchMicrons = Integer.parseInt(args[index]);
      } else if (args[index].equals("-mp")
                 && (++index < args.length)) {
        minimumPixelSize = Integer.parseInt(args[index]);
      } else if (args[index].equals("-cp")) {
        addCornerPoints = true;
      } else if (args[index].equals("-wslscp")) {
         wouldSirLikeSomeCrackedPepper = true;
      } else if (args[index].equals("-h")) {
        printHelp();
        System.exit(0);
      }
    }
      
    BufferedImage graphic = null;
    File input = new File(filename);

    try {
      graphic = ImageIO.read(input);
    } catch(Exception e) {
      System.out.println("Could not read " + filename + ".");
      System.out.println(e);
      System.exit(0);
    }

    width = graphic.getWidth();
    height = graphic.getHeight();
    System.out.println("Image height (pixels): " + height);
    System.out.println("Image width (pixels): " + width);
    
    try {
      generateAverageGreyscaleFP(graphic);
      generateSRGBGreyscale(graphic);
    } catch (Exception e) {
      System.out.println("There was a problem converting " + filename);
      System.out.println(e);
    }

  }

  private static void generateAverageGreyscaleFP(BufferedImage picture) throws IOException {

    if (picture != null) {
      PrintWriter footprint
          = new PrintWriter(filename + "_averagedRGB.fp");
      footprint.println(FPHeader1 + filename + FPHeader2);

      pinNum = 0;
      for (int w = 0; w < width; w++) {
        x = w*dotPitchNM + dotPitchNM/2; // make it nm
        y = dotPitchNM/2;
        for (int h = 0; h < height; h++) {
          y += dotPitchNM; // make it nm
          pixel = new Color(picture.getRGB(w, h));
          red = pixel.getRed();
          green = pixel.getGreen();
          blue = pixel.getBlue();
          luminosity = ((red + green + blue));
          scaledPixelSize =
              (long)(Math.sqrt(luminosity*maximalDotFactor));
          if (wouldSirLikeSomeCrackedPepper && luminosity == 765) {
            footprint.println(gedaPin(x,y));
          } else if (scaledPixelSize > minimumPixelSize) {
            footprint.println(gedaSilkPixel(x,y,scaledPixelSize));
          }
        }
      }
      if (addCornerPoints) {
        footprint.println("# Corner points");
        footprint.println(addOppositeCorners());
      }
      footprint.println(")");
      footprint.close();
    }
  }

  private static void generateSRGBGreyscale(BufferedImage picture)
    throws IOException{
    if (picture != null) {
      PrintWriter footprint
          = new PrintWriter(filename + "_sRGB.fp");
      footprint.println(FPHeader1 + filename + FPHeader2);

      pinNum = 0;
      for (int w = 0; w < width; w++) {
        x = w*dotPitchNM + dotPitchNM/2; // make it nm
        y = dotPitchNM/2;
        for (int h = 0; h < height; h++) {
          y += dotPitchNM; // make it nm
          pixel = new Color(picture.getRGB(w, h));
          red = pixel.getRed();
          green = pixel.getGreen();
          blue = pixel.getBlue();
          luminosity = 3*(0.212655 * red
                          + 0.715158 * green
                          + 0.072187 * blue);
          scaledPixelSize =
              (long)(Math.sqrt(luminosity*maximalDotFactor));
          if (wouldSirLikeSomeCrackedPepper && luminosity == 765) {
            footprint.println(gedaPin(x,y));
          } else if (scaledPixelSize > minimumPixelSize) {
            footprint.println(gedaSilkPixel(x,y,scaledPixelSize));
          }
        }
      }
      if (addCornerPoints) {
        footprint.println("# Corner points");
        footprint.println(addOppositeCorners());
      }
      footprint.println(")");
      footprint.close();
    }
  }

  private static String addOppositeCorners() {
    return gedaSilkPixel(0,0,1000)
        + "\n"
        + gedaSilkPixel((width)*dotPitchNM,
                      (height)*dotPitchNM,
                      1000);
  } 

  private static String gedaSilkPixel(long xCoord,
                                    long yCoord,
                                    long thickness) {
    return "ElementLine[" +
        xCoord/254 + " " +  // convert nm to centimils
        yCoord/254 + " " +
        xCoord/254 + " " +
        yCoord/254 + " " +
        thickness + // in centimils
        "]";
  }

  private static String gedaPin(long xCoord,
                                long yCoord) {
    return "Pin[" +
        xCoord/254 + " " +  // convert nm to centimils
        yCoord/254 + " " +
        8000 + " " +
        1000 + " " +
        1000 + " " +
        3500 + " " +
        "\"pin\"" + " \"" +
        ++pinNum + "\" " +
        "\"\"" + 
        "]";
  }


  /*
Pin[X Y Thickness Clearance Mask Drill Name Number SFlags]
Name        Type     Values Description
---------------------------------------
X, Y        Integer  ?      Position (Center)
Thickness   Integer  ?      Diameter of copper annulus (pad)
Clearance   Integer  ?      (Half) separation to surrounding copper
Mask        Integer  ?      Diameter of solder mask relief (opening)
Drill       Integer  ?      Diameter of the hole
Name        String   ?      (Arbitrary) name of this pin
Number      String   ?      Number of the pin, used to attach nets
SFlags      String   ?      Symbolic flags
  */

  private static void printHelp() {
    System.out.println("\nUsage: \n\n"
                       + " java image2footprint picture.png\n\n"
                       + "\t -mp XXX"
                       + "\tspecify minimum pixel size"
                       + "(centimils)\n\n"
                       + "\t -pp YYY"
                       + "\tspecify pixel pitch (microns)\n\n"
                       + "\t -cp"
                       + "\t\tadd corner points to footprint\n\n"
                       + "\t -wslscp"
                       + "\t\"Would Sir Like Some Cracked Pepper?\" Sprinkle pins on any pure white pixels.\n");
  }
 
}
