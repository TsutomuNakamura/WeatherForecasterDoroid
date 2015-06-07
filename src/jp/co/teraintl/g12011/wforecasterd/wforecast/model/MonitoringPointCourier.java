package jp.co.teraintl.g12011.wforecasterd.wforecast.model;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.MonitoringPointProvider;

/**
 * 気象観測地点取得クラス
 * @author tsutomu
 *
 */
public class MonitoringPointCourier {
	
	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName(); 
	
	/**
	 * 最寄りの気象観測地点を取得する
	 * @param context アプリケーションコンテキスト
	 * @param currentLocation 現在地情報が格納された地域情報Bean
	 * @return 最寄り気象観測地点
	 */
	public LocationBean getNearestMonitoringPoint(Context context, LocationBean currentLocation) {
		
		List<LocationBean> list = null;
		MonitoringPointProvider provider = new MonitoringPointProvider(context);
		LocationBean resultBean = null;
		
		if(currentLocation.getGpsFlag() == true && currentLocation.getCountryNameCode().equals("JP")) {
			// GPS フラグがtrue　且つ日本国内のみ最寄り地点を集計する
			// それ以外については、最寄り地点は算出する必要は無い
			try {
				list = provider.getCandidateForMonitoringPoint(context, currentLocation);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// 現在の場所から、気象観測地点候補の中の最も近い気象観測地点を求める
			int interimIndex = -1;
			double interimClosestRange = Double.MAX_VALUE;
			double currentRange = 0;
			
			for (int i = 0; i < list.size(); i++) {
				if ( (currentRange = getDistanceBetweenThe2Points(currentLocation, list.get(i)))
						< interimClosestRange) {
					
					// 気象観測地点候補地のインデックスと距離を更新する
					interimIndex = i;
					interimClosestRange = currentRange;
				}
			}
			
			resultBean = list.get(interimIndex);

			// WidgetId とGpsフラグを設定して最寄り地点情報を返す
			resultBean.setWidgetId(currentLocation.getWidgetId());
			resultBean.setGpsFlag(currentLocation.getGpsFlag());
			
		} else {
			// GPS 利用フラグがfalse であったり海外の気象予報の場合は、
			// 最寄観測地点を計測する必要は無い
			resultBean = currentLocation;
		}
		
		//　観測地点の決定情報をログに出力
		Log.i(LOG_TAG, "最寄り観測地点[id=" + resultBean.getId() + "],[latitude=" + resultBean.getLatitude() + "],[longitude="
				+ resultBean.getLongitude() + "],[countryNameCode=" + resultBean.getCountryNameCode() + "],[countryName="
				+ resultBean.getCountryName() + "],[regionName=" + resultBean.getRegionName() + "],[administrativeAreaName="
				+ resultBean.getAdministrativeAreaName() + "],[localityName=" + resultBean.getLocalityName() + "],[vLocalityName="
				+ resultBean.getVLocalityName() + "],[rssUrl=" + resultBean.getRssUrl() + "]");
				
		return resultBean;
	}
	
	/**
	 * 現在地と気象観測地点候補の間の距離を取得する。
	 * 距離は便宜を図って地球を平面と仮定して三角関数を使用して求めることとする。
	 * @param currentLocation 現在地
	 * @param monitoringPoint 観測地点
	 * @return 現在地から観測地点までの距離
	 */
	private double getDistanceBetweenThe2Points(
			LocationBean currentLocation, LocationBean monitoringPoint) {
		
		// x軸の長さとy軸の長さを求める
		double xAxis = currentLocation.getLatitude() - monitoringPoint.getLatitude();
		double yAxis = currentLocation.getLongitude() - monitoringPoint.getLongitude();
		
		if (xAxis < 0)
			xAxis *= -1;
		if (yAxis < 0)
			yAxis *= -1;
		
		// 三平方の定理の公式を使用して、2点間の距離を求める
		return Math.sqrt(Math.pow(xAxis, 2) + Math.pow(yAxis, 2));
	}
}
