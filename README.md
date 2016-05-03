# image2footprint
image2footprint is a utility for converting images into gEDA PCB compatible footprint artwork.

image2footprint is licensed GPL2, or at your option, GPL3 or higher.

image2footprint is a simple utility that uses the java ImageIO class to load .jpg or .png files and convert them into gEDA PCB compatible footprints for placement on PCB layouts.

The user can specify a pixel pitch (in microns), which is equal to the spacing between dots in the rendered silk screen image, in millimetres. The default is 0.5mm, which is equivalent to 500,000 nanometres.

The user can also specify a final image size (yet to be implemented) in terms of height and width, specified in mm, and the provided image will be scaled to this final size on the final footprint. The image dimensions should ideally be integer multiples, m x n, of the pixel pitch, so that the resulting image will be m x n pixels in size.

Transparent layers or any other features which are not to appear on the final footprint should be made black with a suitable graphics editor, i.e. the GIMP.

The converter assumes a white silkscreen on a dark soldermask background. For each pixel in the source image, the luminosity of the pixel is used to generate a silkscreen dot, the area of which is scaled in proportion to the source image pixel's luminosity.

The user can specify a minimum silk dot size; the default is 8mil, which is equivalent to 203 nanometres.

Specifying a minimum pixel size (in centimils) is equivalent to specifying a threshold luminosity, below which silk dots are not generated. This may be of use when converting a photograph of an existing PCB into a footprint, upon which trackwork and features can be snapped to grid to duplicate the PCB.

The user can add corner points to the top left and bottom right corners with the -cp command line flag, to assist with placement of the footprint on layouts of a defined size and with a pre-determined alignment.

The utility exports three versions of the footprint:

- one which simply averages R,G,B values to produce a grey level,

- another which calculates luminosity based on sRGB colour weights:

  - rY = 0.212655;
  - gY = 0.715158;
  - bY = 0.072187;

- and another (yet to be implemented) which applies gamma correction before determining luminosity.

  - see http://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color

The luminosity value ( 0...n ) is then used to determine the dot size on the silk screen:

  thickness = diameter = sqrt((luminosity/n)* pitch^2)

The user can select which footprint looks best.

Obviously, the appearance of the manufactured PCB will depend on the capabilities of the PCB manufacturer, and their in house conversion of the Gerber data supplied to them.

TODO - Kicad compatible export

Usage: 

 install a jdk for your OS

 javac image2footprint.java

 java image2footprint picture.png -mp 600 -pp 400 -cp

	 -mp XXX	specify minimum pixel size(centimils)

	 -pp YYY	specify pixel pitch (microns)

	 -cp		add corner points to footprint
