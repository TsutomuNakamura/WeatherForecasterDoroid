package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 過去の取得した地域プロバイダ<br />
 * DBの地域取得履歴テーブルから、過去の取得した地域を提供するクラス。
 * この処理は排他処理ロックが取得された上で呼ばれることを想定。
 * @author tsutomu
 */
public class WeatherLocationProvider {
	
	/** ログ出力用タグ */
	private final String LOG_TAG = this.getClass().getName();

	/** 共通ユーティリティクラスのインスタンス */
	CommonUtils com;
	
	/**
	 * コンストラクタ
	 * @param context アプリケーションコンテキスト
	 */
	public WeatherLocationProvider(Context context) {
		com = CommonUtils.getInstance(context);
	}
	
	/**
	 * 現在地取得履歴から過去の現在地データを取得する。即刻取得対象は
	 * @return locationBean オブジェクト
	 */
	public LocationBean getHistoryLocation(int widgetId, Context context) {

		SQLiteDatabase db = null;
		LocationBean locationBean = new LocationBean();

		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			
			// まず、対象のウィジェットが海外なのか海外なのか調べる
			if(dao.selectCountryNameCodeByWidgetId(widgetId, context).getCountryNameCode().equals("JP")) {

				// ウィジェットが日本の天気であるならば、都道府県名も取得する形で地域情報Bean を取得する
				locationBean = dao.selectByWidgetId(widgetId, context);

				if (locationBean != null) {
					Log.i(LOG_TAG, "地域取得履歴から取得:[WidgetId="
							+ locationBean.getWidgetId() + "][JsonId=" + locationBean.getJsonId() + "],[GPSFlag="
							+ locationBean.getGpsFlag() + "],[Latitude=" + locationBean.getLatitude() + "],[Longitude="
							+ locationBean.getLongitude() + "Accuracy=" + locationBean.getAccuracy() + "],[CountryNameCode="
							+ locationBean.getCountryNameCode() + "],[CountryName=" + locationBean.getCountryName() + "],[AdministrativeAreaName="
							+ locationBean.getAdministrativeAreaName() + "],[SubAdministrativeAreaName=" + locationBean.getSubAdministrativeAreaName() + "],[LocalityName="
							+ locationBean.getLocalityName() + "],[UpdateDate=" + locationBean.getUpdateDate() + "],[RegistrationDate" + locationBean.getRegistrationDate() + "]");
				} else {
					Log.w(LOG_TAG, "地域情報が取得できませんでした[LocationBean=null]");
				}
			} else {
				// ウィジェットが海外の天気であるならば、海外用の検索条件で地域情報Bean を取得する
				locationBean = dao.selectByWidgetIdForForeignCountry(widgetId, context);

				if (locationBean != null) {
					Log.i(LOG_TAG, "地域取得履歴から取得:[WidgetId="
							+ locationBean.getWidgetId() + "][JsonId=" + locationBean.getJsonId() + "],[GPSFlag="
							+ locationBean.getGpsFlag() + "],[Latitude=" + locationBean.getLatitude() + "],[Longitude="
							+ locationBean.getLongitude() + "Accuracy=" + locationBean.getAccuracy() + "],[CountryNameCode="
							+ locationBean.getCountryNameCode() + "],[CountryName=" + locationBean.getCountryName() + "],[LocalityName="
							+ locationBean.getLocalityName() + "],[UpdateDate=" + locationBean.getUpdateDate() + "],[RegistrationDate" + locationBean.getRegistrationDate() + "]");
				} else {
					Log.w(LOG_TAG, "地域情報が取得できませんでした[LocationBean=null]");
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (db.isOpen()) db.close();
		}

		Log.i(LOG_TAG, "getHistoryLocation finish");
		return locationBean;
	}
	
	/**
	 * ウィジェット取得地域履歴一覧テーブルを更新する
	 * @param locationBean locationBean オブジェクト
	 * @return 更新されたカラム数
	 * @throws SQLException SQL例外
	 * @throws IOException 入出力例外
	 */
	public int updateWeatherLocation(
			Context context, LocationBean locationBean) throws SQLException, IOException {
		
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);

			// ウィジェットIDを指定して挿入処理
			dao.insertOrReplace(locationBean, context);
		} finally {
			if (db.isOpen())
				db.close();
		}
		return 1;
	}
	
	/**
	 * 取得履歴としてデータを1件挿入する
	 * @param locationBean 地域情報Bean
	 * @param context アプリケーションコンテキスト
	 * @return 挿入件数
	 * @throws IOException 入出力例外
	 */
	public long insertWeatherLocation(
			LocationBean locationBean, Context context)throws IOException {
		
		SQLiteDatabase db = null;
		long ret = 0;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			
			ret = dao.insertOrReplace(locationBean, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return ret;
	}

	/**
	 * ウィジェット取得地域履歴一覧テーブルのデータを削除する
	 * @param widgetIds ウィジェットID配列。nullを指定した場合は全件削除
	 * @return 削除件数
	 * @throws IOException 入出力例外
	 */
	public long deleteWeatherLocation(int[] widgetIds, Context context) throws IOException {
		
		long ret = 0;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			ret = new WeatherLocationDao(db, context)
					.deleteByWidgetIds(widgetIds, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return ret;
	}
	
	/**
	 * ウィジェット取得地域履歴一覧テーブルのデータを全て削除する
	 * @param context アプリケーションコンテキスト
	 * @throws IOException 入出力例外
	 */
	public void deleteWeatherLocationAll(Context context) throws IOException {
		deleteWeatherLocation(null, context);
	}

	/**
	 * 都道府県名一覧を取得する
	 * @param countryNameCode 国名コード
	 * @param context アプリケーションコンテキスト
	 * @return 都道府県名一覧Bean
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> getAdministrativeAreaNameList(String countryNameCode,
			Context context) throws IOException {
		
		List<LocationBean> list = null;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			list = dao.selectAdministrativeAreaNameList(countryNameCode, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return list;
	}

	/**
	 * 都道府県名をキーに市区町村名一覧を取得する
	 * @param administrativeAreaName 都道府県名
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報Bean
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> getLocalityNameList(String countryNameCode
			, String administrativeAreaName, Context context) throws IOException {
		
		List<LocationBean> list = null;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			list = dao.selectLocalityNameList(countryNameCode, administrativeAreaName, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return list;
	}

	/**
	 * 国名コードをキーに海外都市名一覧を取得する
	 * @param administrativeAreaName 都道府県名
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報Bean
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> getForeignLocalityNameList(
			String countryNameCode
			, Context context) throws IOException {
		
		List<LocationBean> list = null;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			list = dao.selectForeignLocalityNameList(countryNameCode, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return list;
	}
	
	/**
	 * ウィジェットの地域情報を設定する
	 * @param widgets ウィジェットリスト
	 * @param context アプリケーションコンテキスト
	 * @return 更新件数
	 * @throws IOException 入出力例外
	 */
	public long setOrUpdateWidgetsLocale(
			List<LocationBean> widgets, Context context) throws IOException {
		
		long ret = 0;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			
			// ※将来改善 Statement を使用して複数更新を高速化する
			for (LocationBean widget: widgets) {
				ret += dao.insertOrReplace(widget, context);
			}

		} finally {
			if (db.isOpen()) db.close();
		}
		
		return ret;
	}

	/**
	 * GPSフラグを設定する
	 * @param widgetId ウィジェットID
	 * @param gpsFlag GPSフラグ
	 * @param context アプリケーションコンテキスト
	 * @return 更新カラム数
	 */
	public int setGpsFlag(int widgetId, boolean gpsFlag, Context context) {
		
		int ret = 0;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			ret = dao.setGpsFlag(widgetId, gpsFlag, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return ret;
	}

	/**
	 * デフォルトの地域が設定されたウィジェットのリストを作成する
	 * @param appWidgets ウィジェットID配列
	 * @return ウィジェットリスト
	 */
	public List<LocationBean> createDefaultWidgetList(int[] appWidgets, Context context) {
		
		List<LocationBean> widgets = new ArrayList<LocationBean>();
		
		for (int i = 0; i < appWidgets.length; i++) {
			widgets.add(WeatherLocationDao.getDefaultLocationData(appWidgets[i], true, context));
		}
		
		return widgets;
	}

	/**
	 * ウィジェットをすべて取得する
	 * @param context アプリケーションコンテキスト
	 * @return ウィジェットリスト
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> getAllWidgets(Context context) throws IOException {
		
		SQLiteDatabase db = null;
		List<LocationBean> jpRet = null;
		List<LocationBean> foreignRet = null;
		
		// ※ 改善(これでは処理効率が悪い)
		// 今のところ、日本のウィジェットと海外のウィジェットを同じ方法で抽出できない。
		// 日本のウィジェットは国名と県名をもとに、都道府県マスタテーブル(m_p_administrative_area)等を
		// 連結して1つの整合性のとれたデータを取得するが、海外の地名については
		// 国名と都市名という形式で取得しているので、それができない(そもそも県名という概念がない)。
		// そのため、日本のウィジェットと海外のウィジェットは今のところ別々の手順で取得するようにしている。
		
		//　日本のウィジェットと海外のウィジェットを取得する
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			jpRet = dao.selectAllForLocationBeanFormat(context);
			foreignRet = dao.selectAllForForeignLocationBeanFormat(context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		// 日本のウィジェットと海外のウィジェットを結合する
		jpRet.addAll(foreignRet);

		return jpRet;
	}

	/**
	 * ウィジェット取得地域履歴一覧テーブルのウィジェットの地域情報を更新し、
	 * ウィジェットの更新回数を0に設定する
	 * @param widgetId ウィジェットID
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 都道府県名
	 * @param localityName 市区町村名
	 * @param context アプリケーションコンテキスト
	 * @throws IOException 入出力例外
	 */
	public void resetWidgetLocationData(int widgetId, String countryNameCode,
			String administrativeAreaName, String localityName, Context context) throws IOException {
		
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			dao.updateWidgetLocation(
					widgetId, countryNameCode, administrativeAreaName, localityName, context);
		} finally {
			if (db.isOpen()) db.close();
		}
	}
	
	/**
	 * 海外のウィジェット取得地域履歴一覧テーブルのウィジェットの地域情報を更新し、
	 * ウィジェットの更新回数を0に設定する。
	 * 引数に都道府県名は与えない。
	 * @param widgetId ウィジェットID
	 * @param countryNameCode 国名コード
	 * @param localityName 市区町村名
	 * @param context アプリケーションコンテキスト
	 * @throws IOException 入出力例外
	 */
	public void resetWidgetForeignLocationData(int widgetId, String countryNameCode
			, String localityName, Context context) throws IOException {
		
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			dao.updateWidgetForeignLocation(
					widgetId, countryNameCode, localityName, context);
		} finally {
			if (db.isOpen()) db.close();
		}
	}
	
	/**
	 * 海外の国名リストを取得する
	 * @param context アプリケーションコンテキスト
	 * @return 都道府県名一覧Bean(countryNameCode に国名のみが入っている形式)
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> getForeignCountryNameList(Context context) throws IOException {
		
		List<LocationBean> list = null;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			WeatherLocationDao dao = new WeatherLocationDao(db, context);
			list = dao.selectForeignCountryNameList("JP", context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return list;
	}

}
