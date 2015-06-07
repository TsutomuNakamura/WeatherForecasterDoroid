package jp.co.teraintl.g12011.wforecasterd.wforecast.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherProvider;

/**
 * 気象予報クラス
 * @author tsutomu
 *
 */
public class WeatherForecaster {
	
	/** 共通ユーティリティクラスのインスタンス */
	private CommonUtils com;
	
	/**
	 * コンストラクタ
	 * @param context アプリケーションコンテキスト
	 */
	public WeatherForecaster(Context context) {
		com = CommonUtils.getInstance(context);
	}
	
	/**
	 * 数日先の気象予報情報一覧を取得する
	 * @param monitoringPoint 気象観測地点
	 * @param context アプリケーションコンテキスト
	 * @return 気象予報リスト
	 */
	public List<WeatherBean> getWeatherReport(
			LocationBean monitoringPoint, Context context) {
		
		// 結果返却用List
		List<WeatherBean> resultList = null;
		WeatherProvider weatherProvider =
				com.getCurrentWeatherProvider(context);
		try {
			// 気象予報の解析と一覧取得
			resultList = weatherProvider.getWeathers(monitoringPoint, context);
			
			// 気象予報の保存
			weatherProvider.storeWeatherDataIfNotExist(resultList, monitoringPoint, context);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultList;
	}
}
