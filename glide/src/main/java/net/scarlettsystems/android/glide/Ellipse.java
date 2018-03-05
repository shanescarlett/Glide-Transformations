package net.scarlettsystems.android.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;


public class Ellipse extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.Ellipse";
	private static final byte[] ID_BYTES = ID.getBytes();
	private Context mContext;
	private float xDiameter, yDiameter, angle;
	private boolean isFraction, isCircle;
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

	public Ellipse(Context context)
	{
		mContext = context;
		this.isFraction = true;
		this.xDiameter = 1f;
		this.yDiameter = 1f;
		this.angle = 0;
		this.colour = Color.argb(0,0,0,0);
	}

	public Ellipse setCircleSize(int size)
	{
		this.isCircle = true;
		this.isFraction = false;
		this.xDiameter = Math.max(0, size);
		this.yDiameter = this.xDiameter;
		return this;
	}

	public Ellipse setCircleSizeFraction(float fraction)
	{
		this.isCircle = true;
		this.isFraction = true;
		this.xDiameter = Math.max(0f, Math.min(1f, fraction));
		this.yDiameter = this.xDiameter;
		return this;
	}

	public Ellipse setSize(int x, int y)
	{
		this.isCircle = false;
		this.isFraction = false;
		this.xDiameter = Math.max(0, xDiameter);
		this.yDiameter = Math.max(0, yDiameter);
		return this;
	}

	public Ellipse setSizeFraction(float x, float y)
	{
		this.isCircle = false;
		this.isFraction = true;
		this.xDiameter = Math.max(0f, Math.min(1f, x));
		this.yDiameter = Math.max(0f, Math.min(1f, y));
		return this;
	}

	public Ellipse setAngle(int angle)
	{
		this.angle = angle;
		return this;
	}

	public Ellipse setColour(@ColorInt int colour)
	{
		this.colour = colour;
		return this;
	}

	public Ellipse setColourRes(@ColorRes int res)
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
		int pivotX = source.getWidth() / 2;
		int pivotY = source.getHeight() / 2;
		resolveDiameters(source.getWidth(), source.getHeight());

		float ellipseLeft = (source.getWidth() - xDiameter) / 2;
		float ellipseTop = (source.getHeight() - yDiameter) / 2;
		float ellipseRight = ellipseLeft + xDiameter;
		float ellipseBottom = ellipseTop + yDiameter;
		RectF ellipseBounds = new RectF(ellipseLeft, ellipseTop, ellipseRight, ellipseBottom);

		Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		Rect bitmapBounds = new Rect(0,0, source.getWidth(), source.getHeight());
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		Canvas canvas = new Canvas(bitmap);
		canvas.rotate(-angle, pivotX, pivotY);
		canvas.drawOval(ellipseBounds, paint);
		canvas.rotate(angle, pivotX, pivotY);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, null, bitmapBounds, paint);
		canvas.drawColor(colour, PorterDuff.Mode.DST_OVER);
		return bitmap;
	}

	private void resolveDiameters(int width, int height)
	{
		if(!isFraction){return;}

		if(isCircle)
		{
			int dia = Math.min(width, height);
			xDiameter *= dia;
			yDiameter *= dia;
		}
		else
		{
			xDiameter *= width;
			yDiameter *= height;
		}
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Ellipse)
		{
			Ellipse other = (Ellipse) object;
			return xDiameter == other.xDiameter
					&& yDiameter == other.xDiameter
					&& angle == other.angle
					&& colour == other.colour
					&& isCircle == other.isCircle;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(xDiameter,
						Util.hashCode(yDiameter,
								Util.hashCode(angle,
										Util.hashCode(colour,
												Util.hashCode(isCircle))))));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		char boolCircle;
		if(isCircle){boolCircle = 't';}else{boolCircle = 'f';}

		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(xDiameter).array());
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(yDiameter).array());
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(angle).array());
		messages.add(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(colour).array());
		messages.add(ByteBuffer.allocate(Character.SIZE/Byte.SIZE).putChar(boolCircle).array());

		messageDigest.update(ID_BYTES);
		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}