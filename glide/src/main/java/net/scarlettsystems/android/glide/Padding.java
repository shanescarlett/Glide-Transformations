package net.scarlettsystems.android.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * Padding.java
 * This transformation adds padding intrinsically to the bitmap.
 * This is used to add a coloured border to the image, or create
 * transparent padding to prevent clipping when drawing shadows.
 *
 * @author Shane Scarlett
 * @version 1.0.0
 */
@SuppressWarnings("unused, WeakerAccess")
public class Padding extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.Padding";
	private static final byte[] ID_BYTES = ID.getBytes();
	private int paddingLeft, paddingRight, paddingTop, paddingBottom;
	private int colour = Color.argb(0,0,0,0);

	/**
	 * Default constructor.
	 * The padding is transparent by default.
	 *
	 * @param  padding  thickness of padding in pixels
	 * @return      returns self
	 */
	public Padding(int padding)
	{
		paddingLeft = padding;
		paddingRight = padding;
		paddingTop = padding;
		paddingBottom = padding;
	}

	/**
	 * Constructor.
	 * The padding is transparent and zero by default.
	 *
	 * @return      returns self
	 */
	public Padding()
	{
		paddingLeft = 0;
		paddingRight = 0;
		paddingTop = 0;
		paddingBottom = 0;
	}

	public Padding setPadding(int left, int right, int top, int bottom)
	{
		paddingLeft = left;
		paddingRight = right;
		paddingTop = top;
		paddingBottom = bottom;
		return this;
	}

	/**
	 * Sets the colour of the padding.
	 * The padding is transparent by default.
	 *
	 * @param  colour  the colour as a @ColorInt
	 * @return      returns self
	 */
	public Padding setColour(@ColorInt int colour)
	{
		this.colour = colour;
		return this;
	}

	/**
	 * Sets the colour of the padding by resource.
	 * The padding is transparent by default.
	 *
	 * @param  res  the colour as a @ColorRes
	 * @return      returns self
	 */
	public Padding setColourRes(@ColorRes int res, Context con)
	{
		if(Build.VERSION.SDK_INT < 23)
		{
			this.colour = con.getResources().getColor(res);
		}
		else
		{
			this.colour = con.getResources().getColor(res, null);
		}
		return this;
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight)
	{
		//Size Image
		int paddedWidth = Math.max(0, source.getWidth() - (paddingLeft + paddingRight));
		int paddedHeight = Math.max(0, source.getHeight() - (paddingTop + paddingBottom));
		Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		Rect bitmapBounds = new Rect(paddingLeft, paddingTop, paddedWidth + paddingLeft, paddedHeight + paddingTop);
		//Create Image Paint
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		//Draw to Canvas
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(colour);
		canvas.drawBitmap(source, null, bitmapBounds, paint);
		return bitmap;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Padding)
		{
			Padding other = (Padding) object;
			return paddingLeft == other.paddingLeft
					&& paddingRight == other.paddingRight
					&& paddingTop == other.paddingTop
					&& paddingBottom == other.paddingBottom
					&& colour == other.colour;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(paddingLeft,
						Util.hashCode(paddingRight,
								Util.hashCode(paddingTop,
										Util.hashCode(paddingBottom,
												Util.hashCode(colour))))));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		messages.add(ID_BYTES);
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(paddingLeft).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(paddingRight).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(paddingTop).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(paddingBottom).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(colour).array());

		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}