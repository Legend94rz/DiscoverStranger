package org.helloworld.JigsawGame;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by qf on 2015/5/2.
 */
class PictureCut {
    private static ArrayList<PicItem> picList = new ArrayList<PicItem>() ;

    public static ArrayList<PicItem> GetCut(Bitmap bitmap,int row,int col)
    {
        picList.clear() ;
        int h = bitmap.getHeight() / row  ;
        int w = bitmap.getWidth()  / col  ;
        int cnt = 0 ;
        for (int i=0; i<row ; ++i)
            for (int j=0; j<col; ++j)
            {
                picList.add(new PicItem(Bitmap.createBitmap(bitmap,j*w,i*h,w,h),cnt++)) ;
            }
        return picList ;
    }
}
