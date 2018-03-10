package net.scarlettsystems.android.glide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.IntDef;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * Flips (reflects) the image in the specified direction.
 *
 * @author Shane Scarlett
 * @version 1.0.0
 */
public class Flip extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.Flip";
	private static final byte[] ID_BYTES = ID.getBytes();
	private float xScale, yScale;

	/**
	 * Denotes that the annotated element represents a flip direction
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef
			({
					Direction.HORIZONTAL,
					Direction.VERTICAL,
					Direction.BOTH
			})
	public @interface Direction
	{
		int HORIZONTAL = 0;
		int VERTICAL = 1;
		int BOTH = 2;
	}

	/**
	 * Returns transformation that flips the image in the specified direction.
	 *
	 * @param d flip direction
	 */
	public Flip(@Direction int d)
	{
		switch(d)
		{
			case Direction.HORIZONTAL:
				xScale = -1;
				yScale = 1;
				break;
			case Direction.VERTICAL:
				xScale = 1;
				yScale = -1;
				break;
			case Direction.BOTH:
				xScale = -1;
				yScale = -1;
				break;
			default:
				throw new IllegalArgumentException("Invalid direction parameter.");
		}
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight)
	{
		Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.scale(xScale, yScale, source.getWidth()/2, source.getHeight()/2);
		canvas.drawBitmap(source, 0, 0, null);
		return bitmap;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Greyscale)
		{
			return true;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(xScale,
						Util.hashCode(yScale)));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		messages.add(ID_BYTES);
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(xScale).array());
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(yScale).array());

		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}
