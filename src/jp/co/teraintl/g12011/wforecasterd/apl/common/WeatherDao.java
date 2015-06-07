
package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class WeatherDao {
	
	/** SQLiteDatabaseインスタンス */
	private SQLiteDatabase db;
	
	/** 共通ユーティリティクラスのインスタンス */
	private CommonUtils com;
	
	/**
	 * コンストラクタ
	 * @param db SQLiteDatabaseインスタンス
	 * @param context アプリケーションコンテキスト
	 */
	public WeatherDao(SQLiteDatabase db, Context context) {
		this.db = db;
		com = CommonUtils.getInstance(context);
	}
	
	/**
	 * 一括して複数のデータを挿入する
	 * @param weatherList 気象予報Beanリスト
	 * @param widgetId ウィジェットID
	 * @param context アプリケーション婚期スト
	 * @return データ挿入件数
	 * @throws IOException 入出力例外
	 */
	public long insertBundle(List<WeatherBean> weatherList, LocationBean monitoringPointBean
			, Context context) throws IOException {
		
		ResourceBundle bundle = com.getGlobalResourceBundle();
		Properties prop = com.getAssetsXmlResourceProperty(
				context, bundle.getString("assets_sql_t_weather_history"));
		SQLiteStatement stmt = db.compileStatement(prop.getProperty(CommonUtils.SQL_PROP_INS_OR_REPLACE));
		
		WeatherBean bean = null;
		int widgetId = monitoringPointBean.getWidgetId();
		int index = 0;
		int insertRowCount = 0;
		long now = System.currentTimeMillis();
		String stringDate = CommonUtils.SQLITE_DATE_FORMAT.format(now);
		String announceDate = weatherList.get(0).getAnnounceDate();
		
		try {
			
			db.beginTransaction();
			
			// 天気予報取得履歴テーブルへデータ挿入
			for (int i = 0; i < weatherList.size(); i++) {
				index = 0;
				bean = weatherList.get(i);
				
				stmt.bindString(++index, announceDate);									// announce_date
				stmt.bindString(++index, bean.getStringDate());										// weather_date
				stmt.bindString(++index, Integer.toString(bean.getDayOfWeek()));					// weather_day_of_week
				stmt.bindString(++index, Integer.toString(widgetId));								// widget_id
				stmt.bindString(++index, bean.getWeather());										// weather_string
				stmt.bindString(++index, Integer.toString(bean.getWeatherIconId()));				// weather_icon_id
				stmt.bindString(++index, (bean.getHighestTemperature()
						!= null? bean.getHighestTemperature().toString(): ""));						// highest_temperature
				stmt.bindString(++index, (bean.getLowestTemperature()
						!= null? bean.getLowestTemperature().toString(): ""));						// lowest_temperature
				stmt.bindString(++index, convertChanceOfRain(bean.getChanceOfRain()));				// chance_of_rain
				stmt.bindString(++index, bean.getWindDirection());									// wind_direction
				stmt.bindString(++index, Integer.toString(bean.getWindSpeed()));					// wind_speed
				stmt.bindString(++index, monitoringPointBean.getCountryNameCode());					// bind comment_country_id
				// 海外の気象予報の場合、県名はnull なので分岐する
				stmt.bindString(++index
						, (monitoringPointBean.getCountryNameCode().equals("JP")? monitoringPointBean.getAdministrativeAreaName(): ""));	// bind comment_administrative_area_id
				stmt.bindString(++index, stringDate);												// update_date
				stmt.bindString(++index, stringDate);												// registration_date
				
				stmt.executeInsert();
				
				insertRowCount++;
			}
			
			db.setTransactionSuccessful();
		} finally {
			if (stmt != null)
				stmt.close();
			if (db.inTransaction()) {
				db.endTransaction();
			}
		}
		
		// 全国天気解説テーブルへデータ解説を挿入
		bean = weatherList.get(0);
		prop = com.getAssetsXmlResourceProperty(context
				, context.getString(R.string.assets_sql_t_woc_weather_comment));

		db.execSQL(prop.getProperty(CommonUtils.SQL_PROP_INS_OR_REPLACE)
				, new String[]{
					monitoringPointBean.getCountryNameCode()									// bind country_id
					, bean.getWholeOfCommunityWeatherCommentPubDate()							// announce_date
					, bean.getWholeOfCommunityWeatherComment()									// weather_comment
					, stringDate																// update_date
					, stringDate																// registration_date
		});
		
		// 都道府県天気解説テーブルへデータ解説を挿入
		prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_adar_weather_comment));
		db.execSQL(prop.getProperty(CommonUtils.SQL_PROP_INS_OR_REPLACE)
				, new String[] {
					monitoringPointBean.getAdministrativeAreaName()								// bind administrative_area_id
					, bean.getAdministrativeAreaWeatherCommentPubDate()							// announce_date
					, bean.getAdministrativeAreaWeatherComment()								// weather_comment
					, stringDate																// update_date
					, stringDate																// registration_date
		});

		return insertRowCount;
	}
	
	/**
	 * ウィジェットIDを指定して、最近の発表日の気象一覧を取得する。
	 * @param locationBean 地域情報Bean
	 * @param context アプリケーションコンテキスト
	 * @return 天気予報リスト
	 * @throws IOException 入出力例外
	 */
	public List<WeatherBean> selectByWidgetIdAndNearestAnnounceDate(
			LocationBean locationBean, Context context) throws IOException {
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_weather_history));
		Cursor cursor = null;
		List<WeatherBean> list = new ArrayList<WeatherBean>();
		WeatherBean bean = null;
		
		try {
			// ※ 改善。冗長すぎるのでまとめたい
			// 日本国内の気象予報なのか、海外の気象予報なのかで、取得する項目を変える
			if(locationBean.getCountryNameCode().equals("JP")) {
				// 日本国内の場合
				cursor = db.rawQuery(prop.getProperty("sql_select_by_w_id_and_n_a_date"),
						new String[]{
							Integer.toString(locationBean.getWidgetId())
							, locationBean.getCountryNameCode()
							, locationBean.getAdministrativeAreaName()
							, Integer.toString(locationBean.getWidgetId())});
				
				int index = 0;
				int lineCount = 0;
				String tmpStr = null;
				while (cursor.moveToNext()) {
					index = 0;
					bean = new WeatherBean();
					
					bean.setAnnounceDate(cursor.getString(index++));											// announce_date
					bean.setStringDate(cursor.getString(index++));												// weather_date
					bean.setDayOfWeek(cursor.getInt(index++));													// weather_day_of_week
					
					bean.setWeather(cursor.getString(index++));													// weather_string
					bean.setWeatherIconId(cursor.getInt(index++));												// weather_icon_id
					bean.setHighestTemperature(
							(tmpStr = cursor.getString(index++)) == null
								|| tmpStr.equals("")? null: new Integer(tmpStr));								// highest_temperature
					bean.setLowestTemperature(
							(tmpStr = cursor.getString(index++)) == null
								|| tmpStr.equals("")? null: new Integer(tmpStr));								// lowest_temperature
					bean.setChanceOfRain(revertChanceOfRain(cursor.getString(index++)));						// chance_of_rain
					
					bean.setWindDirection(cursor.getString(index++));											// wind_direction
					
					tmpStr = cursor.getString(index++);
					if (tmpStr != null)
						bean.setWindSpeed(Integer.parseInt(tmpStr));											// wind_speed
					
					if (lineCount <= 0) {
						
						// 先頭の気象予報Bean にのみ全国/都道府県の気象解説とその発表日時を入れる
						bean.setWholeOfCommunityWeatherComment(cursor.getString(index++));						// t_woc_weather_comment
						bean.setWholeOfCommunityWeatherCommentPubDate(cursor.getString(index++));				// announce_date
						bean.setAdministrativeAreaWeatherComment(cursor.getString(index++));					// t_adar_weather_comment
						bean.setAdministrativeAreaWeatherCommentPubDate(cursor.getString(index++));				// announce_date
					}
					
					list.add(bean);
					lineCount++;
				}
			} else {
				// 海外の場合
				cursor = db.rawQuery(prop.getProperty("sql_select_foreign_by_w_id_and_n_a_date"),
						new String[]{
							Integer.toString(locationBean.getWidgetId())
							, Integer.toString(locationBean.getWidgetId())});
				
				int index = 0;
				String tmpStr = null;
				while (cursor.moveToNext()) {
					index = 0;
					bean = new WeatherBean();
					
					bean.setAnnounceDate(cursor.getString(index++));											// announce_date
					bean.setStringDate(cursor.getString(index++));												// weather_date
					bean.setDayOfWeek(cursor.getInt(index++));													// weather_day_of_week
					
					bean.setWeather(cursor.getString(index++));													// weather_string
					bean.setWeatherIconId(cursor.getInt(index++));												// weather_icon_id
					bean.setHighestTemperature(
							(tmpStr = cursor.getString(index++)) == null
								|| tmpStr.equals("")? null: new Integer(tmpStr));								// highest_temperature
					bean.setLowestTemperature(
							(tmpStr = cursor.getString(index++)) == null
								|| tmpStr.equals("")? null: new Integer(tmpStr));								// lowest_temperature
					bean.setChanceOfRain(revertChanceOfRain(cursor.getString(index++)));						// chance_of_rain
					
					bean.setWindDirection(cursor.getString(index++));											// wind_direction
					
					tmpStr = cursor.getString(index++);
					if (tmpStr != null)
						bean.setWindSpeed(Integer.parseInt(tmpStr));											// wind_speed
					
					// ※海外の天気予報については全国気象解説と発表日時は使わない
					
					list.add(bean);
				}
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) cursor.close();
		}
		
		return list;
	}
	
	/**
	 * 降水確率データをDB格納用の形式に変換する。<br />
	 * 変換後のデータは「時間帯,降水確率:時間帯,降水確率...」という形式となる
	 * @param chanceOfRain 降水確率Map
	 * @return 降水確率文字列
	 */
	private String convertChanceOfRain(Map<String, Integer> chanceOfRain) {
		
		StringBuilder sb = new StringBuilder();
		int counter = 1;
		
		if (chanceOfRain == null)
			return null;
		
		for (Map.Entry<String, Integer> e : chanceOfRain.entrySet()) {
			sb.append(e.getKey() + "," + e.getValue() + (counter == chanceOfRain.size()? "": ";"));
			counter++;
		}
		
		return sb.toString();
	}
	
	/**
	 * カンマとセミコロン区切りで区切られた時間毎の降水確率を、Mapに変換する。
	 * @param chanceOfRains 降水確率(カンマとセミコロン区切り)
	 * @return 降水確率(Map)
	 */
	private Map<String, Integer> revertChanceOfRain(String chanceOfRains) {
		
		LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
		String[] chanceOfRainsArray = null;
		String[] tmpArray = null;
		chanceOfRainsArray = chanceOfRains.split(";", -1);
		
		for (int i = 0; i < chanceOfRainsArray.length; i++) {
			tmpArray = chanceOfRainsArray[i].split(",", -1);
			
			if (tmpArray.length > 1) {
				map.put(tmpArray[0], Integer.parseInt(tmpArray[1]));
			} else {
				// 取得したデータがカンマ区切りになっていない場合(すなわち不正データもしくはnullの場合)
				return null;
			}
		}
		
		return map;
	}
	
	/**
	 * 指定された日数より古い気象データを削除する。
	 * @param day 日数
	 * @throws IOException 入出力例外 
	 */
	public void deleteWeatherData(long time, Context context) throws IOException {
		
		// SQL取得
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_weather_history));
		
		// SQL問い合わせ
		db.execSQL(prop.getProperty("sql_delete_by_time"),
				new String[]{CommonUtils.ANNOUNCE_DATE_FORMAT.format(new Date(time))}
		);
	}
} 
