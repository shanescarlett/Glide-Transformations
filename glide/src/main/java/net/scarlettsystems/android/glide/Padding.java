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
public class Padding extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.Padding";
	private static final byte[] ID_BYTES = ID.getBytes();
	private Context mContext;
	private int padding;
	private int colour = Color.argb(0,0,0,0);

	/**
	 * Default constructor.
	 * The padding is transparent by default.
	 *
	 * @param  context  current context
	 * @param  padding  thickness of padding in pixels
	 * @return      returns self
	 */
	public Padding(Context context, int padding)
	{
		mContext = context;
		this.padding = padding;
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
	public Padding setColourRes(@ColorRes int res)
	{
		if(Build.VERSION.SDK_INT < 23)
		{
			this.colour = mContext.getResources().getColor(res);
		}
		else
		{
			this.colour = mContext.getResources().getColor(res, null);
		}
		return this;
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight)
	{
		//Size Image
		int paddedWidth = Math.max(0, source.getWidth() - (padding * 2));
		int paddedHeight = Math.max(0, source.getHeight() - (padding * 2));
		Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		Rect bitmapBounds = new Rect(padding, padding, paddedWidth + padding, paddedHeight + padding);
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
			return padding == other.padding
					&& colour == other.colour;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(padding,
						Util.hashCode(colour)));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		messages.add(ID_BYTES);
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(padding).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(colour).array());

		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}