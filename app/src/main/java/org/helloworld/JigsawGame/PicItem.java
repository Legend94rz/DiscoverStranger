package org.helloworld.JigsawGame;

import android.graphics.Bitmap;

/**
 * Created by qf on 2015/5/2.
 */
class PicItem implements Comparable<PicItem>
{
	private Bitmap bitmap;
	private int number;
	private float value;

	public void SetNumber(int n)
	{
		number = n;
	}

	public int GetNumber()
	{
		return number;
	}

	public void SetBitmap(Bitmap b)
	{
		bitmap = b;
	}

	public Bitmap GetBitmap()
	{
		return bitmap;
	}

	public void setValue(float value)
	{
		this.value = value;
	}

	public float getValue()
	{
		return this.value;
	}

	public PicItem()
	{
	}

	public PicItem(Bitmap bitmap, int number)
	{
		this.bitmap = bitmap;
		this.number = number;
	}

	@Override
	public int compareTo(PicItem o)
	{
		if (this.value == o.getValue()) return 0;
		if (this.value < o.getValue()) return -1;
		return 1;
	}
}
