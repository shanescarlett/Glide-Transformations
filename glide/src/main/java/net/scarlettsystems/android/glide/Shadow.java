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

	public Shadow(Context context)
	{
		mContext = context;
		this.blurRadius = 0;
		this.elevation = 0;
		this.angle = 0;
		this.colour = Color.argb(128,0,0,0);
	}

	public Shadow setBlurRadius(float blurRadius)
	{
		this.blurRadius = blurRadius;
		return this;
	}

	public Shadow setElevation(float elevation)
	{
		this.elevation = elevation;
		return this;
	}

	public Shadow setAngle(float angle)
	{
		this.angle = angle;
		return this;
	}

	public Shadow setDirection(@Direction int d)
	{
		this.angle = getAngle(d);
		return this;
	}

	public Shadow setShadowColour(@ColorInt int colour)
	{
		this.colour = colour;
		return this;
	}

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
