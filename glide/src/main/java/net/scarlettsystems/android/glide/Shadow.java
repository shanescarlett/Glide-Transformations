package net.scarlettsystems.android.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
 * Shadow.java
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
public class Shadow extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.Shadow";
	private static final byte[] ID_BYTES = ID.getBytes();
	private Context mContext;
	private float blurRadius, elevation, angle;
	private int colour;
	@Retention(RetentionPolicy.SOURCE)
	@IntDef
	({
		Direction.EAST,
		Direction.NORTHEAST,
		Direction.NORTH,
		Direction.NORTHWEST,
		Direction.WEST,
		Direction.SOUTHWEST,
		Direction.SOUTH,
		Direction.SOUTHEAST
	})
	public @interface Direction
	{
		int EAST = 0;
		int NORTHEAST = 1;
		int NORTH = 2;
		int NORTHWEST = 3;
		int WEST = 4;
		int SOUTHWEST = 5;
		int SOUTH = 6;
		int SOUTHEAST = 7;
	}

	/**
	 * Default constructor.
	 * The shadow is set at 0 elevation and 0 blur, with black colour at 50%
	 * opacity, by default.
	 *
	 * @param  context  current context
	 */
	public Shadow(Context context)
	{
		mContext = context;
		this.blurRadius = 0;
		this.elevation = 0;
		this.angle = 0;
		this.colour = Color.argb(128,0,0,0);
	}

	/**
	 * Sets the blur radius of the shadow.
	 * It is advised to pad the image by at least this amount plus the
	 * elevation to prevent clipping of the shadow.
	 *
	 * @param  blurRadius  elevation in pixels
	 * @return      returns self
	 */
	public Shadow setBlurRadius(float blurRadius)
	{
		this.blurRadius = blurRadius;
		return this;
	}

	/**
	 * Sets the elevation, or how much the shadow is offset from the image.
	 * It is advised to pad the image by at least this amount plus the
	 * blur radius to prevent clipping of the shadow.
	 *
	 * @param  elevation  elevation in pixels
	 * @return      returns self
	 */
	public Shadow setElevation(float elevation)
	{
		this.elevation = elevation;
		return this;
	}

	/**
	 * Sets the angle in which the shadow is offset from the image.
	 * Zero degrees indicates due west, and angles progress counter-clockwise.
	 * Angles larger than 360° or smaller than 0° simply indicate wraps around the circle.
	 *
	 * @param  angle  the angle in degrees
	 * @return      returns self
	 */
	public Shadow setAngle(float angle)
	{
		this.angle = angle;
		return this;
	}

	/**
	 * Sets the cardinal direction in which the shadow is offset from the image.
	 *
	 * @param d the cardinal direction as a @Direction
	 * @return returns self
	 */
	public Shadow setDirection(@Direction int d)
	{
		this.angle = getAngle(d);
		return this;
	}

	/**
	 * Sets the shadow's colour.
	 * Shadow is drawn black with 50% opacity by default.
	 *
	 * @param colour the colour as a @ColorInt
	 * @return returns self
	 */
	public Shadow setShadowColour(@ColorInt int colour)
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
	public Shadow setShadowColourRes(@ColorRes int res)
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

	private float getAngle(@Direction int d)
	{
		switch(d)
		{
			case Direction.EAST:
				return 0;
			case Direction.NORTHEAST:
				return 45;
			case Direction.NORTH:
				return 90;
			case Direction.NORTHWEST:
				return 135;
			case Direction.WEST:
				return 180;
			case Direction.SOUTHWEST:
				return 225;
			case Direction.SOUTH:
				return 270;
			case Direction.SOUTHEAST:
				return 315;
			default:
				throw new IllegalArgumentException("Invalid Direction");
		}
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight)
	{
		Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		Bitmap shadow = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		//Calculate Shadow Offset
		float shadowX = elevation * (float)Math.cos(Math.toRadians(angle));
		float shadowY = -(elevation * (float)Math.sin(Math.toRadians(angle)));

		//Create Shadow Paint
		Paint shadowPaint = new Paint();
		shadowPaint.setAntiAlias(true);
		shadowPaint.setColorFilter(new PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN));
		//Render Shadow
		final RenderScript rs = RenderScript.create(mContext);
		final Allocation input = Allocation.createFromBitmap( rs, source, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT );
		final Allocation output = Allocation.createTyped( rs, input.getType() );
		final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create( rs, Element.U8_4( rs ) );
		script.setRadius(blurRadius);
		script.setInput( input );
		script.forEach( output );
		output.copyTo( shadow );
		//Draw to Canvas
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		canvas.drawBitmap(shadow, shadowX, shadowY, shadowPaint);
		canvas.drawBitmap(source, 0, 0, null);
		//Output
		shadow.recycle();
		return bitmap;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Shadow)
		{
			Shadow other = (Shadow) object;
			return blurRadius == other.blurRadius
					&& elevation == other.elevation
					&& angle == other.angle
					&& colour == other.colour;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(blurRadius,
				Util.hashCode(elevation,
				Util.hashCode(angle,
				Util.hashCode(colour)))));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		messages.add(ID_BYTES);
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(blurRadius).array());
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(elevation).array());
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(angle).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(colour).array());

		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}
