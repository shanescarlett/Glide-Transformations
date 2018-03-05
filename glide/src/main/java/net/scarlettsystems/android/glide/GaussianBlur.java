package net.scarlettsystems.android.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;


public class GaussianBlur extends BitmapTransformation
{
	private static final String ID = "net.scarlettsystems.android.transformations.glide.SoftShadow";
	private static final byte[] ID_BYTES = ID.getBytes();
	private Context mContext;
	private float blurRadius;
	private static final float RENDERSCRIPT_MAX_BLUR_RADIUS = 25.0f;

	public GaussianBlur(Context context, float blurRadius)
	{
		mContext = context;
		this.blurRadius = blurRadius;
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight)
	{
		if(blurRadius <= RENDERSCRIPT_MAX_BLUR_RADIUS)
		{
			blur(source, blurRadius);
		}
		else
		{
			float scaleFactor = (RENDERSCRIPT_MAX_BLUR_RADIUS / blurRadius);
			int scaledWidth = Math.max(1, Math.round((float) source.getWidth() * scaleFactor));
			int scaledHeight = Math.max(1, Math.round((float) source.getHeight() * scaleFactor));
			Bitmap scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
			blur(scaled, RENDERSCRIPT_MAX_BLUR_RADIUS);
			Canvas canvas = new Canvas(source);
			Rect bitmapBounds = new Rect(0, 0, source.getWidth(), source.getHeight());
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			canvas.drawBitmap(scaled, null, bitmapBounds, paint);
		}
		return source;

	}

	private void blur(Bitmap bitmap, float radius)
	{
		final RenderScript rs = RenderScript.create(mContext);
		final Allocation input = Allocation.createFromBitmap( rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT );
		final Allocation output = Allocation.createTyped( rs, input.getType() );
		final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create( rs, Element.U8_4( rs ) );
		script.setRadius( radius );
		script.setInput( input );
		script.forEach( output );
		output.copyTo( bitmap );
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof GaussianBlur)
		{
			GaussianBlur other = (GaussianBlur) object;
			return blurRadius == other.blurRadius;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(ID.hashCode(),
				Util.hashCode(blurRadius));
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest)
	{
		ArrayList<byte[]> messages = new ArrayList<>();

		messages.add(ID_BYTES);
		messages.add(ByteBuffer.allocate(Float.SIZE/Byte.SIZE).putFloat(blurRadius).array());

		for(int c = 0; c < messages.size(); c++)
		{
			messageDigest.update(messages.get(c));
		}
	}
}