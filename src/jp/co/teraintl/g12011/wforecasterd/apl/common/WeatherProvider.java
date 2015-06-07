package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 気象プロバイダ<br />
 * 気象予報データの参照や操作機能を提供する
 * @author tsutomu
 *
 */
public abstract class WeatherProvider {

	/** 共通ユーティリティインスタンス */
	protected CommonUtils com;
	
	/**
	 * コンストラクタ
	 * @param context
	 */
	public WeatherProvider(Context context) {
		com = CommonUtils.getInstance(context);
	}
	
	/**
	 * 数日間の気象予報を取得する
	 * @param widgetId ウィジェットID
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 件名
	 * @param context アプリケーションコンテキスト
	 * @return 気象予報Beanリスト
	 * @throws IOException 入出力例外
	 */
	public List<WeatherBean> getWeeklyWeatherFromDb(
			LocationBean locationBean, Context context) throws IOException {
		
		List<WeatherBean> list = null;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			WeatherDao dao = new WeatherDao(db, context);
			list = dao.selectByWidgetIdAndNearestAnnounceDate(locationBean, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return list;
	}
	
	/**
	 * 気象情報をクリーンアップする。<br />
	 * 第1引数に指定された日数以上古いデータが対象となる
	 * @param parseInt 削除対象となるデータの日数
	 * @return 削除件数
	 * @throws IOException 入出力例外 
	 */
	public long cleanWeatherData(long time, Context context) throws IOException {
		
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherDao dao = new WeatherDao(db, context);
			dao.deleteWeatherData(time, context);
		} finally {
			if (db.isOpen()) db.close();
		}

		return 0;
	}
	
	public abstract List<WeatherBean> getWeathers(
			LocationBean locationBean, Context context) throws Exception;
	
	/**
	 * 取得した気象予報データを履歴として保存する。
	 * 取得した気象情報と同じアナウンス日時の気象情報がDBに登録されていない場合は、レコードを新規に追加し、
	 * 取得した気象情報と同じアナウンス日時の気象情報がDBに登録されている場合は、そのレコードを上書きする。
	 * @param weatherList 気象予報Beanリスト
	 * @param widgetId ウィジェットID
	 * @param context アプリケーションコンテキスト
	 * @return データ挿入件数
	 * @throws IOException 入出力例外
	 */	
	public long storeWeatherDataIfNotExist(
			List<WeatherBean> weatherList, LocationBean monitoringPointBean
			, Context context) throws IOException {
		
		long insertNum = 0;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherDao dao = new WeatherDao(db, context);
			insertNum = dao.insertBundle(weatherList, monitoringPointBean, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return insertNum;
	}
}
