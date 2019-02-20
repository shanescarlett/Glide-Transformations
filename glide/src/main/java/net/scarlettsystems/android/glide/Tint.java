package net.scarlettsystems.android.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * This transformation applies a shadow intrinsically to the bitmap.
 * This is useful for images with complex shapes where Android does
 * not support elevation shadows. The colour of the shadow, its blur
 * radius, and offset from the image can all be configured.
 * <p>
 * Images should be padded with transparent pixels by at least the
 * blur radius plus the elevation in order for the drawn shadow to
 * display properly without clipping. See: Padding
 *
 * @author Shane Scarlett
 * @version 1.0.0
 * @see Padding
 */
@SuppressWarnings("unused, WeakerAccess")
public class Tint extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.Tint";
	private static final byte[] ID_BYTES = ID.getBytes();
	private Context mContext;
	private PorterDuff.Mode tintMode;
	private int colour;
	private static final float RENDERSCRIPT_MAX_BLUR_RADIUS = 25.0f;

	@IntDef({EAST, NORTHEAST, NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH, SOUTHEAST})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Direction {}

	public static final int EAST = 0;
	public static final int NORTHEAST = 1;
	public static final int NORTH = 2;
	public static final int NORTHWEST = 3;
	public static final int WEST = 4;
	public static final int SOUTHWEST = 5;
	public static final int SOUTH = 6;
	public static final int SOUTHEAST = 7;

	/**
	 * Default constructor.
	 * The shadow is set at 0 elevation and 0 blur, with black colour at 50%
	 * opacity, by default.
	 *
	 * @param  context  current context
	 */
	public Tint(Context context)
	{
		mContext = context;
		this.tintMode = PorterDuff.Mode.SRC_IN;
		this.colour = Color.argb(128,0,0,0);
	}

	/**
	 * Sets the tint mode to be used
	 *
	 * @param  mode  PorterDuff tint mode
	 * @return      returns self
	 */
	public Tint setTintMode(PorterDuff.Mode mode)
	{
		this.tintMode = mode;
		return this;
	}

	/**
	 * Sets the shadow's colour.
	 * Shadow is drawn black with 50% opacity by default.
	 *
	 * @param colour the colour as a @ColorInt
	 * @return returns self
	 */
	public Tint setTintColour(@ColorInt int colour)
	{
		this.colour = colour;
		return this;
	}

	/**
	 * Sets the shadow's colour by colour resource.
	 * Shadow is drawn black with 50% opacity by default.
	 *
	 * @param  res  the colour resource as a @ColorRes
	 * @return      returns self
	 */
	public Tint setTintColourRes(@ColorRes int res)
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
		Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		//Draw to Canvas
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(source,0, 0, null);
		canvas.drawColor(colour, tintMode);

		return bitmap;
	}

	private void blur(Bitmap bitmap, Bitmap copyTo, float radius)
	{
		final RenderScript rs = RenderScript.create(mContext);
		final Allocation input = Allocation.createFromBitmap( rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT );
		final Allocation output = Allocation.createTyped( rs, input.getType() );
		final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create( rs, Element.U8_4( rs ) );
		script.setRadius( radius );
		script.setInput( input );
		script.forEach( output );
		output.copyTo( copyTo );
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Tint)
		{
			Tint other = (Tint) object;
			return tintMode == other.tintMode
					&& colour == other.colour;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(tintMode,
						Util.hashCode(colour)));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		messages.add(ID_BYTES);
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(colour).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(tintMode.hashCode()).array());

		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}
