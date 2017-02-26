//package com.scheme.chc.lockscreen.utils;
//
//
//import android.graphics.Point;
//
//public class centroidUtil {
//    private static double area(Point[] polyPoints){
//
//        int i, j, n = polyPoints.length;
//        double area = 0;
//
//        for(i = 0;i < n;i++){
//
//            j = (i + 1)%n;
//
//            area += polyPoints[i].x * polyPoints[j].y;
//            area -= polyPoints[j].x * polyPoints[i].y;
//        }
//
//        area /= 2.0;
//        //System.out.printf("\nArea of the polygon = %f units^2" ,area);
//        return(area);
//
//    }
//
//    public static Point centPoly(Point[] polyPoints){
//
//        double cx = 0, cy = 0;
//        double area = area(polyPoints);
//
//        Point res = new Point();
//
//        int i, j, n = polyPoints.length;
//
//        double factor = 0;
//
//        for(i = 0; i < n; i++){
//
//            j = (i + 1)%n;
//
//            factor = (polyPoints[i].x * polyPoints[j].y - polyPoints[j].x * polyPoints[i].y);
//
//            cx += (polyPoints[i].x + polyPoints[j].x)*factor;
//            cy += (polyPoints[i].y + polyPoints[j].y)*factor;
//        }
//
//        area *= 6.0f;
//        factor = 1/area;
//        cx *= factor;
//        cy *= factor;
//        res.set((int) cx, (int)cy);
//        //System.out.printf("\nCenter X-coordinate = %f units & Center Y-coordinate = %f units", cx, cy );
//        return res;
//    }
//}
