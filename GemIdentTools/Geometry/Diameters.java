/*
    GemIdent v1.1b
    Interactive Image Segmentation Software via Supervised Statistical Learning
    http://gemident.com
    
    Copyright (C) 2009 Professor Susan Holmes & Adam Kapelner, Stanford University

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details:
    
    http://www.gnu.org/licenses/gpl-2.0.txt

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package GemIdentTools.Geometry;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a static final class which is referenced
 * when "masks" are needed for attribute
 * generation. A "mask" in this case is the locus of all 
 * points from the center (0,0) to a certain other point,
 * as well as to the point on the opposite side of the origin.
 * <p>
 * For instance, a diameter extending to point (-2,1) might be either:
 * <ul>
 * <li>		     0     0     0     0     0
 * <li>		     1     1     0     0     0
 * <li>	 	     0     0     1     0     0
 * <li>		     0     0     0     1     1
 * <li>		     0     0     0     0     0,
 * </ul>
 * or
 * <ul>
 * <li>		     0     0     0     0     0
 * <li>		     1     0     0     0     0
 * <li>	 	     0     1     1     1     0
 * <li>		     0     0     0     0     1
 * <li>		     0     0     0     0     0.
 * </ul>
 *  </p>
 * 
 * @authors Adam Kapelner, Misha Lipatov
 */
public class Diameters {
	
	/** initial maximum radius to which to generate vectors */
	public static final int INIT_MAX_RADIUS=5;

	/** the mapping from a point to the Diameter itself stored as a list of {@link java.awt.Point Point} objects 
	 * Note that HashMap compares objects using their equals() method, which is good as Points with the same
	 * coordinates are evaluated to be the same here. Also note that two points that are each other's 
	 * reflections in the origin will key to the same diameter */
	private static HashMap<Point,ArrayList<Point>> allDiameterSet;	
	
	/** To construct the static object, call Build */
	static {
		Build();
	}
	/** Build the initial vectors for all points within a circle of radius INIT_MAX_RADIUS */
	public static void Build(){
		
		// the size of the hash map is the area of the circle with the initial maximum radius
		allDiameterSet=new HashMap<Point,ArrayList<Point>>((int)Math.ceil(Math.PI*Math.pow(INIT_MAX_RADIUS, 2)) + 1);
		
		// build a zero diameter
		ArrayList<Point> points=new ArrayList<Point>();
		points.add(new Point(0,0));
		allDiameterSet.put(new Point(0,0), points);
		
		//build all other diameters
		for (Point t : Solids.getSolid(INIT_MAX_RADIUS)) {
			GenerateDiameter(t);
		}
	}
	/**
	 * Generate a diameter one of whose endpoints is given
	 * 
	 * @param t			one of the diameter's endpoints
	 * @return			the diameter as a list of coordinates
	 * 
	 * @see Bresenham's Line Algorithm
	 */
	private static ArrayList<Point> GenerateDiameter(Point t) {
		// helper variable to temporarily hold integer values
		int temp;
		// a copy of the coordinates of the original endpoint
		int x1 = t.x, y1 = t.y;
		// first we reflect the endpoint across the origin into the first two quadrants
		// later we will include both the endpoint and its reflection as keys to get the diameter 
		if (((y1<0)&&(x1>=0)) || ((y1<=0)&&(x1<0))) { 
			// if the endpoint is in the third or fourth quadrant,
			// transform it into the first or the second, respectively
			y1 = -y1; 
			x1 = -x1;
		}
		
		// first octant is defined to include y=0, but not y=x,
		// second octant is defined to include y=x, but not x=0, and so on...
		int octant = 1; // octant of the original vector
		
		// the endpoint should already be in the first two quadrants
		// now we determine its octant (one of four) and
		// transform the its coordinates into the first octant
		// (assume the endpoint has nonzero coordinates)
		if ((x1>0) && (y1>0) && (x1<=y1)) {
			octant = 2;
			temp = x1;
			x1 = y1;
			y1 = temp;
		} else if ((x1<=0) && (y1>0) && (Math.abs(x1)<y1)) {
			octant = 3;
			temp = x1;
			x1 = y1;
			y1 = Math.abs(temp);
		} else if ((x1<0) && (y1>0) && (Math.abs(x1)>=y1)) {
			octant = 4;
			x1 = Math.abs(x1);
		} 
		
		// initialize the array of points that will hold the diameter
		ArrayList<Point> points = new ArrayList<Point>();

		// draw the half-diameter in the first octant
		double error = 0;
		double deltaerr = Math.abs(((double)y1)/((double)x1));
		int x = 0, y = 0;
		points.add(new Point(x,y));
		while (x < x1) {
			error = error + deltaerr;
			if (error>=0.5) {
				y++;
				error = error - 1.0;
			}
			x++;
			points.add(new Point(x,y));
		}
		
		// move the half-diameter from the first octant into its original octant
		for (Point point : points){
			x = point.x;
			y = point.y;
			switch (octant) {
			case 2:
				point.move(y, x);
				break;
			case 3:
				point.move(-y, x);
				break;
			case 4:
				point.move(-x, y);
				break;
			}
		}
		
		// add the points in the other half-diameter
		ArrayList<Point> morePoints = new ArrayList<Point>();
		for (Point point : points) {
			if (!((point.x==0)&&(point.y==0))) {
				x = point.x;
				y = point.y;
				morePoints.add(new Point(-x,-y));
			}
		}
		points.addAll(morePoints);
		
		// save
		allDiameterSet.put(new Point(x1,y1),points);
		allDiameterSet.put(new Point(-x1,-y1),points);
		
		return points;
	}
	/**
	 * Return a diameter one of whose endpoints is t. If the diameter is not yet generated, 
	 * the routine will autogenerate the diameter, cache it, and return it
	 * 
	 * @param t		one of the diameter's endpoints
	 * @return		the diameter
	 */
	public static ArrayList<Point> getDiameter(Point t){
		ArrayList<Point> diameter = allDiameterSet.get(t);
		if (diameter == null)
			diameter = GenerateDiameter(t);
		return diameter;
	}
	
}