package jp.co.teraintl.g12011.wforecasterd.apl.common;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * GPSから緯度・経度情報を取得する
 * @author tsutomu
 */
public class GPSHelper implements LocationListener {
	
	/** ログ出力時のタグ */
	private String LOG_TAG = this.getClass().getName();
	/** GoogleAPIキー */
	private final String API_KEY = "08ua-ND01IL64u5U1npvpLysURGzFMqabZHM2ag";
	
	/** 逆ジオコーディングAccuracy、「どこそこ」レベル */
	public static final int ACCURACY_SUCHANDSUCH = 0;
	/** 逆ジオコーディングAccuracy、国レベル */
	public static final int ACCURACY_COUNTRY = 1;
	/** 逆ジオコーディングAccuracy、州・都道府県レベル */
	public static final int ACCURACY_PREFECTURAL = 2;
	/** 逆ジオコーディングAccuracy、カウンティレベル */
	public static final int ACCURACY_COUNTY = 3;
	/** 逆ジオコーディングAccuracy、市町村レベル */
	public static final int ACCURACY_MUNICIPALITY = 4;
	/** 逆ジオコーディングAccuracy、郵便番号レベル */
	public static final int ACCURACY_POSTALCODE = 5;
	/** 逆ジオコーディングAccuracy、道レベル */
	public static final int ACCURACY_ROAD = 6;
	/** 逆ジオコーディングAccuracy、交差点 */
	public static final int ACCURACY_CROSSING = 7;
	/** 逆ジオコーディングAccuracy、街区レベル */
	public static final int ACCURACY_CITYBLOCK = 8;
	/** 逆ジオコーディングAccuracy、建物レベル */
	public static final int ACCURACY_BUILDING = 9;
	
	/** GPSフラグtrue */
	public static final int GPS_FLAG_TRUE = 0;
	/** GPSフラグfalse */
	public static final int GPS_FLAG_FALSE = 1;
	
	/** GPS情報取得マネージャ */
	private LocationManager locationManager;
	/** 緯度 */
	private double latitude;
	/** 経度 */
	private double longitude;
	/** GPSHelper インスタンス */
	private static GPSHelper gPSHelper;
	
	// ログ出力冗長防止用ログカウンタ
	private int logCounter = 0;
	
	/**
	 * GPSHelper コンストラクタ。
	 */
	private GPSHelper(Context context) {
		// ※ 将来対応・もっとうまくハンドリングすること
		latitude = Double.parseDouble(context.getString(R.string.gps_default_latitude));
		longitude = Double.parseDouble(context.getString(R.string.gps_default_longitude));
	}
	
	/**
	 * インスタンスを取得する。
	 * @return GPSHelperインスタンス
	 */
	public static GPSHelper getInstance(Context context) {
		
		if (gPSHelper == null)
			gPSHelper = new GPSHelper(context);
		
		return gPSHelper;
	}
	
	/**
	 * インスタンスを破棄する
	 */
	public static void destroyInstance() {
		if (gPSHelper != null) {
			try {
				gPSHelper.finalize();
				gPSHelper = null;
			} catch (Throwable e) {
				// ignore
			}
		}
	}
	
	/**
	 * 緯度を取得する
	 * @return 緯度
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * 経度を取得する
	 * @return 経度
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * ロケーションマネージャを取得する。
	 * @param locationManager ロケーションマネージャ
	 */
	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
		
		this.locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				0,
				0,
				this);
	}
	
	/**
	 * APIキーを取得する
	 * @return apiキー
	 */
	public String getAPIKey() {
		return API_KEY;
	}

	@Override
	public void onLocationChanged(Location location) {
		// 緯度と経度の取得
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		if (logCounter < 10) {
			logCounter++;
		} else {
			Log.i(LOG_TAG, "onLocationChanged:[LATITUDE=" + latitude + "],[LONGITUDE=" + longitude + "]");
			logCounter = 0;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(LOG_TAG, "onProviderDisabled():GPS機能が無効になりました。");
	}

	@Override
	public void onProviderEnabled(String provider) {	
		Log.i(LOG_TAG, "onProviderDisabled():GPS機能が有効になりました。");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		switch (status) {
		case LocationProvider.AVAILABLE:
			Log.i(LOG_TAG, "Status: AVAILABLE");
			break;
		case LocationProvider.OUT_OF_SERVICE:
			Log.i(LOG_TAG, "Status: OUT_OF_SERVICE");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.i(LOG_TAG, "Status: TEMPORARILY_UNAVAILABLE");
			break;
		}
	}
}
