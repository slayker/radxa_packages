package com.rk.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.os.Build;
import android.os.SystemProperties;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import com.rk.setting.ScreenInformation;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class Version extends Activity
{
	private static final String PRODUCT_VERSION= SystemProperties.get("ro.rksdk.version","rockchip");
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_CLEARABLE_FLAGS);
        setContentView(R.layout.version);

		createTitle();
		createContextTitle();
		createContext();
    }

	private void createContextTitle()
	{
		float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
		TextView modeTitle = (TextView)findViewById(R.id.mode_title);
		modeTitle.setTextSize(size);

		TextView android = (TextView)findViewById(R.id.android_title);
		android.setTextSize(size);

		TextView kernel = (TextView)findViewById(R.id.kernel_title);
		kernel.setTextSize(size);

		TextView build = (TextView)findViewById(R.id.build_title);
		build.setTextSize(size);
	}
	
	private void createContext()
	{
		float size = ScreenInformation.mScreenWidth/40f*ScreenInformation.mDpiRatio;
		
		TextView mode = (TextView)findViewById(R.id.mode_number);
		mode.setText(Build.MODEL);
		mode.setTextSize(size);

		TextView android = (TextView)findViewById(R.id.android_version);
		android.setText(Build.VERSION.RELEASE);
		android.setTextSize(size);
		
		TextView kernel = (TextView)findViewById(R.id.kernel_version);
		kernel.setText(getFormattedKernelVersion());
		kernel.setTextSize(size);

		TextView build = (TextView)findViewById(R.id.build_number);
		build.setText(PRODUCT_VERSION + "\n" + Build.DISPLAY);
		build.setTextSize(size);
	}
	
	private void createTitle()
	{
		ImageView image = (ImageView)findViewById(R.id.title_image);
		Bitmap resize = bitMapScale(R.drawable.version);
		image.setScaleType(ImageView.ScaleType.CENTER);
		image.setImageBitmap(resize);

		TextView title = (TextView)findViewById(R.id.title_text);
		title.setTextSize(ScreenInformation.mScreenWidth/25f*ScreenInformation.mDpiRatio);
	}

	private Bitmap bitMapScale(int id)
	{
		Bitmap map = BitmapFactory.decodeResource(this.getResources(),id);
		float scale = ScreenInformation.mScreenWidth/1280f*ScreenInformation.mDpiRatio;
		int width = (int)((float)map.getWidth()*scale);
		int height = (int)((float)map.getHeight()*scale);

 		Bitmap resize = Bitmap.createScaledBitmap(map, width, height, true);
		return resize;
	}
	
	private String getFormattedKernelVersion() {
		String procVersionStr;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/version"), 256);
			try {
				procVersionStr = reader.readLine();
			} finally {
				reader.close();
			}

			final String PROC_VERSION_REGEX = "\\w+\\s+" + /* ignore: Linux */
			"\\w+\\s+" + /* ignore: version */
			"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
			"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
														 * group 2:
														 * (xxxxxx@xxxxx
														 * .constant)
														 */
			"\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
			"([^\\s]+)\\s+" + /* group 3: #26 */
			"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
			"(.+)"; /* group 4: date */

			Pattern p = Pattern.compile(PROC_VERSION_REGEX);
			Matcher m = p.matcher(procVersionStr);

			if (!m.matches()) {
				return "Unavailable";
			} else if (m.groupCount() < 4) {
				return "Unavailable";
			} else {
				return (new StringBuilder(m.group(1)).append("\n")
						.append(m.group(2)).append(" ").append(m.group(3))
						.append("\n").append(m.group(4))).toString();
			}
		} catch (IOException e) {
			return "Unavailable";
		}
	}
}
