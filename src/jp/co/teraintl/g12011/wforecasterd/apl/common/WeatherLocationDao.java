package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 現在地取得履歴データにアクセスするオブジェクト。<br />
 * 本オブジェクトでアクセスの対象となるデータは現在地取得履歴テーブル。<br /><br />
 * @author tsutomu
 */
public class WeatherLocationDao {
	
	/** ウィジェットID */
	private final String COLUMN_WIDGET_ID = "widget_id";
	/** JSON ID */
	private final String COLUMN_JSON_ID = "json_id";
	/** 更新回数 */
	private final String COLUMN_UPDATE_COUNT = "update_count";
	/** GPS取得フラグ */
	private final String COLUMN_GPS_FLAG = "gps_flag";
	/** 緯度 */
	private final String COLUMN_LATITUDE = "latitude";
	/** 経度 */
	private final String COLUMN_LONGITUDE = "longitude";
	/** 正確さ */
	private final String COLUMN_ACCURACY = "accuracy";
	/** 国名コード */
	private final String COLUMN_COUNTRY_NAME_CODE = "country_name_code";
	/** 国名 */
	private final String COLUMN_COUNTRY_NAME = "country_name";
	/** 都道府県名 */
	private final String COLUMN_ADMINISTRATIVE_AREA_NAME = "administrative_area_name";
	/** 郡などの集落名 */
	private final String COLUMN_SUB_ADMINISTRATIVE_AREA_NAME = "sub_administrative_area_name";
	/** 地区名 */
	private final String COLUMN_LOCALITY_NAME = "locality_name";
	/** 更新日 */
	private final String COLUMN_UPDATE_DATE = "update_date";
	/** 新規作成日 */
	private final String COLUMN_REGISTRATION_DATE = "registration_date";
	
	/** SQLiteDatabase インスタンス */
	private SQLiteDatabase db;
	
	/** 共通ユーティリティクラスのインスタンス */
	private CommonUtils com;
	
	/** ResourceBundle インスタンス */
	private ResourceBundle bundle;
	
	/**
	 * コンストラクタ
	 * @param db databaseインスタンス
	 */
	public WeatherLocationDao(SQLiteDatabase db, Context context) {
		this.db = db;
		com = CommonUtils.getInstance(context);
		bundle = com.getGlobalResourceBundle();
	}
	
	/**
	 * ウィジェットをウィジェット取得地域履歴一覧テーブルに追加する
	 * @param locationBean ウィジェット(地域情報Bean)
	 * @param context アプリケーションコンテキスト
	 * @return 処理件数
	 * @throws IOException 入出力例外
	 */
	public long insertOrReplace(
			LocationBean locationBean, Context context) throws IOException {
		
		// SQL取得
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		
		// SQL問い合わせ
		db.execSQL(prop.getProperty("sql_insert_table"),
				new String[]{
					Integer.toString(locationBean.getWidgetId())					// widget_id
					, locationBean.getJsonId()										// json_id
					, Long.toString(locationBean.getUpdateCount() + 1)				// update_count
					, Integer.toString( (locationBean.getGpsFlag()
							? GPSHelper.GPS_FLAG_TRUE: GPSHelper.GPS_FLAG_FALSE) )	// gps_flag
					, Double.toString(locationBean.getLatitude())					// latitude
					, Double.toString(locationBean.getLongitude())					// longitude
					, Integer.toString(locationBean.getAccuracy())					// accuracy
					, locationBean.getCountryNameCode()								// bind country_name_id
					, locationBean.getAdministrativeAreaName()						// bind administrative_area_name_id
					, locationBean.getSubAdministrativeAreaName()					// sub_administrative_area_name
					, locationBean.getLocalityName()								// locality_name
					, locationBean.getUpdateDate()									// update_date
					, locationBean.getRegistrationDate()							// registration_date
				}
		);
		
		return 1L;
	}
	
	/**
	 * テーブルデータを挿入する
	 * @param locationBean 地域情報Bean
	 * @return データ挿入件数
	 * @throws IOException 入出力例外
	 */
	public long insert(LocationBean locationBean, Context context) throws IOException {
		ContentValues values = new ContentValues();
		
		// カラム値を設定
		values.put(COLUMN_WIDGET_ID, locationBean.getWidgetId());
		values.put(COLUMN_JSON_ID, locationBean.getJsonId());
		values.put(COLUMN_UPDATE_COUNT, locationBean.getUpdateCount());
		values.put(COLUMN_GPS_FLAG, (locationBean.getGpsFlag() == true? 0: 1));
		values.put(COLUMN_LATITUDE, locationBean.getLatitude());
		values.put(COLUMN_LONGITUDE, locationBean.getLongitude());
		values.put(COLUMN_ACCURACY, locationBean.getAccuracy());
		values.put(COLUMN_COUNTRY_NAME_CODE, locationBean.getCountryNameCode());
		values.put(COLUMN_COUNTRY_NAME, locationBean.getCountryName());
		values.put(COLUMN_ADMINISTRATIVE_AREA_NAME, locationBean.getAdministrativeAreaName());
		values.put(COLUMN_SUB_ADMINISTRATIVE_AREA_NAME, locationBean.getSubAdministrativeAreaName());
		values.put(COLUMN_LOCALITY_NAME, locationBean.getLocalityName());
		values.put(COLUMN_UPDATE_DATE, locationBean.getUpdateDate());
		values.put(COLUMN_REGISTRATION_DATE, locationBean.getRegistrationDate());
		
		return db.insert(bundle.getString(context.getString(R.string.t_widget_locale_history)), null, values);
	}
	
	/**
	 * ウィジェットIDを元にテーブルのデータを更新する
	 * @param locationBean 地域情報Bean 
	 * @return 更新した件数
	 */
	public int update(LocationBean locationBean, Context context) {
		
		ContentValues values = new ContentValues();
		
		values.put(COLUMN_JSON_ID, locationBean.getJsonId());
		values.put(COLUMN_UPDATE_COUNT, locationBean.getUpdateCount() + 1);
		values.put(COLUMN_GPS_FLAG, (locationBean.getGpsFlag() == true? 0: 1));
		values.put(COLUMN_LATITUDE, locationBean.getLatitude());
		values.put(COLUMN_LONGITUDE, locationBean.getLongitude());
		values.put(COLUMN_ACCURACY, locationBean.getAccuracy());
		values.put(COLUMN_COUNTRY_NAME_CODE, locationBean.getCountryNameCode());
		values.put(COLUMN_COUNTRY_NAME, locationBean.getCountryName());
		values.put(COLUMN_ADMINISTRATIVE_AREA_NAME, locationBean.getAdministrativeAreaName());
		values.put(COLUMN_SUB_ADMINISTRATIVE_AREA_NAME, locationBean.getSubAdministrativeAreaName());
		values.put(COLUMN_LOCALITY_NAME, locationBean.getLocalityName());
		values.put(COLUMN_UPDATE_DATE, locationBean.getUpdateDate());
		values.put(COLUMN_REGISTRATION_DATE, locationBean.getRegistrationDate());
		
		// ウィジェットIDで更新
		return db.update(
				bundle.getString(context.getString(R.string.t_widget_locale_history))
				, values, COLUMN_WIDGET_ID + " = ?", new String[]{Integer.toString(locationBean.getWidgetId())});
	}
	
	/**
	 * テーブルの更新日付を更新する
	 * @param locationBean 地域情報Bean
	 * @return 更新した件数
	 */
	public int updateUpdateDate(LocationBean locationBean, Context context) {
		
		ContentValues values = new ContentValues();
		
		values.put(COLUMN_UPDATE_DATE, locationBean.getUpdateDate());
		
		return db.update(
				bundle.getString(context.getString(R.string.t_widget_locale_history))
				, values, COLUMN_WIDGET_ID + " = ?", new String[]{Integer.toString(locationBean.getWidgetId())});
	}
	
	/**
	 * 国名コードを取得する
	 * @param widgetId ウィジェットID
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public LocationBean selectCountryNameCodeByWidgetId(
			int widgetId, Context context) throws IOException {
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		StringBuilder sql = new StringBuilder(prop.getProperty("sql_select_country_name_code_by_widget_id"));
		LocationBean locationBean = new LocationBean();
		int index = 0;
		Cursor cursor = db.rawQuery(sql.toString(), new String[]{Integer.toString(widgetId)});
		
		try {
			if (cursor.moveToNext()) {
				locationBean.setCountryNameCode(cursor.getString(index++));
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		return locationBean;
	}
	
	/**
	 * テーブルのデータを1つのIDを指定して検索する
	 * @param widgetId ウィジェットID
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報Bean
	 * @throws IOException 入出力例外
	 */
	public LocationBean selectByWidgetId(int widgetId, Context context) throws IOException {

		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		StringBuilder sql = new StringBuilder(prop.getProperty("sql_select_by_widget_id"));
		LocationBean locationBean = new LocationBean();
		int index = 0;
		Cursor cursor = db.rawQuery(sql.toString(), new String[]{Integer.toString(widgetId)});

		try {
			if (cursor.moveToNext()) {
				locationBean.setWidgetId(cursor.getInt(index++));
				locationBean.setJsonId(cursor.getString(index++));
				locationBean.setUpdateCount(cursor.getLong(index++));
				locationBean.setGpsFlag((cursor.getInt(index++) == 0? true: false));
				locationBean.setLatitude(cursor.getDouble(index++));
				locationBean.setLongitude(cursor.getDouble(index++));
				locationBean.setAccuracy(cursor.getInt(index++));
				locationBean.setCountryNameCode(cursor.getString(index++));
				locationBean.setCountryName(cursor.getString(index++));
				locationBean.setAdministrativeAreaName(cursor.getString(index++));
				locationBean.setSubAdministrativeAreaName(cursor.getString(index++));
				locationBean.setLocalityName(cursor.getString(index++));
				locationBean.setUpdateDate(cursor.getString(index++));
				locationBean.setRegistrationDate(cursor.getString(index++));
			}

		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		// 先頭の要素のみを返却
		return locationBean;
	}

	/**
	 * テーブルのデータを1つのIDを指定して検索する
	 * @param widgetId ウィジェットID
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報Bean
	 * @throws IOException 入出力例外
	 */
	public LocationBean selectByWidgetIdForForeignCountry(int widgetId, Context context) throws IOException {

		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		StringBuilder sql = new StringBuilder(prop.getProperty("sql_select_by_widget_id_for_foreign_country"));
		LocationBean locationBean = new LocationBean();
		int index = 0;
		Cursor cursor = db.rawQuery(sql.toString(), new String[]{Integer.toString(widgetId)});

		try {
			if (cursor.moveToNext()) {
				locationBean.setWidgetId(cursor.getInt(index++));
				locationBean.setJsonId(cursor.getString(index++));
				locationBean.setUpdateCount(cursor.getLong(index++));
				locationBean.setGpsFlag((cursor.getInt(index++) == 0? true: false));
				locationBean.setLatitude(cursor.getDouble(index++));
				locationBean.setLongitude(cursor.getDouble(index++));
				locationBean.setAccuracy(cursor.getInt(index++));
				locationBean.setCountryNameCode(cursor.getString(index++));
				locationBean.setCountryName(cursor.getString(index++));
				locationBean.setLocalityName(cursor.getString(index++));
				locationBean.setUpdateDate(cursor.getString(index++));
				locationBean.setRegistrationDate(cursor.getString(index++));
			}

		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		// 先頭の要素のみを返却
		return locationBean;
	}

	
	/**
	 * テーブルのデータを1つ以上のIDを指定して検索する
	 * @param id ウィジェットID一覧
	 * @param アプリケーションコンテキスト
	 * @return 地域情報Bean一覧。検索にヒットしない場合はnull
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectByWidgetIds(int[] widgetIds, Context context) throws IOException {
		
		// ※SQLiteのAPIに、配列を指定してWHERE句を組み立てる機構がある。余裕あったらそれに置き換える
		
		List<LocationBean> list = new ArrayList<LocationBean>();
		
		// ウィジェットIDがひとつしか指定されていない場合、単体検索用メソッドに引き渡し
		if (widgetIds.length == 1) {
			list.add(selectByWidgetId(widgetIds[0], context));
			return list;
		}

		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		StringBuilder sql = new StringBuilder(prop.getProperty("sql_select_all_widgets"));
		LocationBean locationBean = null;
		
		// SQLのWHERE文構築
		for (int i = 0; i < widgetIds.length; i++) {
			
			if ( i <= 0)
				sql.append(" AND (");
			else
				sql.append(" OR ");
			
			sql.append("TWLH.widget_id = " + Integer.toString(widgetIds.length));
			
			if (i + 1 == widgetIds.length)
				sql.append(")");
		}
		
		Cursor cursor = db.rawQuery(sql.toString(), null);
		
		try {
			int index = 0;
			
			while (cursor.moveToNext()) {
				// 該当のIDが発見できた場合
				locationBean = new LocationBean();
				index = 0;
				
				locationBean.setWidgetId(cursor.getInt(index++));
				locationBean.setJsonId(cursor.getString(index++));
				locationBean.setUpdateCount(cursor.getLong(index++));
				locationBean.setGpsFlag((cursor.getInt(index++) == 0? true: false));
				locationBean.setLatitude(cursor.getDouble(index++));
				locationBean.setLongitude(cursor.getDouble(index++));
				locationBean.setAccuracy(cursor.getInt(index++));
				locationBean.setCountryNameCode(cursor.getString(index++));
				locationBean.setCountryName(cursor.getString(index++));
				locationBean.setAdministrativeAreaName(cursor.getString(index++));
				locationBean.setSubAdministrativeAreaName(cursor.getString(index++));
				locationBean.setLocalityName(cursor.getString(index++));
				locationBean.setUpdateDate(cursor.getString(index++));
				locationBean.setRegistrationDate(cursor.getString(index++));
				
				list.add(locationBean);
				
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		return list;
	}
	
	/**
	 * テーブルの全データ件数を取得する
	 * @return データ件数
	 * @throws IOException 入出力例外 
	 */
	public long countAllData(Context context) throws IOException {
		
		long ret = 0;
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		Cursor cursor = db.rawQuery(prop.getProperty("sql_count_all_raw"), null);
		
		try {
			if (cursor.moveToNext())
				ret = cursor.getLong(0);			
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		return ret;
	}
	
	/**
	 * テーブルのデータを削除する
	 * @param id 削除対象データのID
	 * @return 削除された件数
	 * @throws IOException 入出力例外
	 */
	public long deleteByWidgetIds(int[] widgetIds, Context context) throws IOException {
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		StringBuilder sql = new StringBuilder(prop.getProperty("sql_delete_table"));
		
		// SQLのWHERE文構築
		if (widgetIds != null) {
			sql.append(" WHERE ");
			
			for (int i = 0; i < widgetIds.length; i++) {
				if ( i > 0)
					sql.append(" OR ");
				
				sql.append("widget_id = " + Integer.toString(widgetIds[i]));
			}
		}
		
		// DBアクセスとCursorのクローズ
		db.execSQL(sql.toString());
		
		return widgetIds != null? widgetIds.length: 0;
	}

	/**
	 * 都道府県名一覧を取得する
	 * @param countryNameCode 国名コード
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報一覧
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectAdministrativeAreaNameList(
			String countryNameCode, Context context) throws IOException {
		
		List<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		
		// SQL取得と問い合わせ
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		Cursor cursor = db.rawQuery(
				prop.getProperty("sql_select_a_a_name_by_c_n_code"), new String[]{countryNameCode});
		
		try {
			// SQL問い合わせ結果取得
			while (cursor.moveToNext()) {
				bean = new LocationBean();
				
				bean.setCountryNameCode(countryNameCode);
				bean.setAdministrativeAreaName(cursor.getString(0));
				list.add(bean);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		return list;
	}
	
	/**
	 * 市区町村名一覧を取得する
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 都道府県名
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報一覧
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectLocalityNameList(
			String countryNameCode
			, String administrativeAreaName
			, Context context) throws IOException {
		
		List<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		
		// 地域名取得履歴テーブルのSQLファイル取得
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		
		Cursor cursor = db.rawQuery(
				prop.getProperty("sql_select_loc_name_by_a_a_name"), new String[]{administrativeAreaName});
		
		try {
			while (cursor.moveToNext()) {
				int index = 0;
				
				// 都道府県名と市区町村名の取り出し
				
				bean = new LocationBean();
				bean.setCountryNameCode(countryNameCode);
				bean.setAdministrativeAreaName(cursor.getString(index++));
				bean.setLocalityName(cursor.getString(index++));
				
				list.add(bean);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}

		return list;
	}

	/**
	 * 海外都市名一覧を取得する
	 * @param countryNameCode 国名コード
	 * @param context アプリケーションコンテキスト
	 * @return 海外都市情報一覧
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectForeignLocalityNameList(
			String countryNameCode
			, Context context) throws IOException {
		
		List<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		
		// 海外地域テーブルのSQLファイル取得
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_m_p_foreign_city));
		
		Cursor cursor = db.rawQuery(
				prop.getProperty("sql_select_foreign_loc_name_by_c_n_code"), new String[]{countryNameCode});
		
		try {
			while (cursor.moveToNext()) {
				// 国名コードと海外都市名の取得
				bean = new LocationBean();
				bean.setCountryNameCode(countryNameCode);
				bean.setLocalityName(cursor.getString(0));
				
				list.add(bean);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}

		return list;
	}

	
	/**
	 * 現存する全てのウィジェットのデータ(地域情報)を取得する
	 * @param context アプリケーションコンテキスト
	 * @return 全てのウィジェットID
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectAllForLocationBeanFormat(Context context) throws IOException {

		List<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		int index = 0;
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		Cursor cursor = db.rawQuery(prop.getProperty("sql_select_all_widgets"), null);

		try {
			while (cursor.moveToNext()) {
				
				// ※ VLocalityName を格納するように追加する
				index = 0;
				bean = new LocationBean();
			
				bean.setWidgetId(cursor.getInt(index++));
				bean.setJsonId(cursor.getString(index++));
				bean.setUpdateCount(cursor.getLong(index++));
				bean.setGpsFlag((cursor.getInt(index++) == GPSHelper.GPS_FLAG_TRUE? true: false));
				bean.setLatitude(cursor.getDouble(index++));
				bean.setLongitude(cursor.getDouble(index++));
				bean.setAccuracy(cursor.getInt(index++));
				bean.setCountryNameCode(cursor.getString(index++));
				bean.setCountryName(cursor.getString(index++));
				bean.setAdministrativeAreaName(cursor.getString(index++));
				bean.setSubAdministrativeAreaName(cursor.getString(index++));
				bean.setLocalityName(cursor.getString(index++));
				bean.setVLocalityName(cursor.getString(index++));
				bean.setUpdateDate(cursor.getString(index++));
				bean.setRegistrationDate(cursor.getString(index));
				
				list.add(bean);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}

		return list;
	}

	/**
	 * 海外の現存する全てのウィジェットのデータ(地域情報)を取得する
	 * @param context アプリケーションコンテキスト
	 * @return 全てのウィジェットID
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectAllForForeignLocationBeanFormat(Context context) throws IOException {

		List<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		int index = 0;
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		Cursor cursor = db.rawQuery(prop.getProperty("sql_select_all_foreign_widgets"), null);

		try {
			while (cursor.moveToNext()) {
				index = 0;
				bean = new LocationBean();
			
				// ※ VLocalityName を格納するように追加する
				bean.setWidgetId(cursor.getInt(index++));
				bean.setJsonId(cursor.getString(index++));
				bean.setUpdateCount(cursor.getLong(index++));
				bean.setGpsFlag((cursor.getInt(index++) == GPSHelper.GPS_FLAG_TRUE? true: false));
				bean.setLatitude(cursor.getDouble(index++));
				bean.setLongitude(cursor.getDouble(index++));
				bean.setAccuracy(cursor.getInt(index++));
				bean.setCountryNameCode(cursor.getString(index++));
				bean.setCountryName(cursor.getString(index++));
// 海外地域取得の場合は県名(AdministrativeAreaName) はいらない
//				bean.setAdministrativeAreaName(cursor.getString(index++));
				bean.setSubAdministrativeAreaName(cursor.getString(index++));
				bean.setLocalityName(cursor.getString(index++));
				bean.setVLocalityName(cursor.getString(index++));
				bean.setUpdateDate(cursor.getString(index++));
				bean.setRegistrationDate(cursor.getString(index));
				
				list.add(bean);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}

		return list;
	}

	
	/**
	 * デフォルトの気象観測地点情報が設定されたLocationBeanを取得する
	 * @param widgetId ウィジェットID
	 * @param gpsFlag GPSフラグ
	 * @param context アプリケーションコンテキスト
	 * @return 地域情報Bean
	 */
	public static LocationBean getDefaultLocationData(int widgetId, boolean gpsFlag, Context context) {
		
		LocationBean bean = new LocationBean();
		String dateString = CommonUtils.SQLITE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));
		
		bean.setWidgetId(widgetId);
		bean.setJsonId(context.getString(R.string.location_default_json_id));
		bean.setUpdateCount(0);
		bean.setGpsFlag(gpsFlag);
		bean.setLatitude(Double.parseDouble(context.getString(R.string.gps_default_latitude)));
		bean.setLongitude(Double.parseDouble(context.getString(R.string.gps_default_longitude)));
		bean.setAccuracy(Integer.parseInt(context.getString(R.string.location_default_accuracy)));
		bean.setCountryNameCode(context.getString(R.string.location_default_country_name_code));
		bean.setCountryName(context.getString(R.string.location_default_country_name));
		bean.setAdministrativeAreaName(context.getString(R.string.location_default_administrative_area_name));
		bean.setSubAdministrativeAreaName(context.getString(R.string.location_default_sub_administrative_area_name));
		bean.setLocalityName(context.getString(R.string.location_default_locality_name));
		bean.setUpdateDate(dateString);
		bean.setRegistrationDate(dateString);
		
		return bean;
	}

	/**
	 * ウィジェットの地域情報を更新する
	 * @param widgetId ウィジェットID
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 都道府県名
	 * @param localityName 市区町村名
	 * @param context アプリケーションコンテキスト
	 * @return 更新件数
	 * @throws IOException 入出力例外
	 */
	public int updateWidgetLocation(int widgetId, String countryNameCode,
			String administrativeAreaName, String localityName, Context context) throws IOException {

		// SQL取得
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		
		// SQL問い合わせ
		db.execSQL(prop.getProperty("sql_update_table_for_w_location"),
				new String[]{
					"0"
					, Integer.toString(GPSHelper.GPS_FLAG_FALSE)
					, localityName
					, localityName
					, countryNameCode							// bind country_name_code_id
					, administrativeAreaName					// bind administrative_area_name
					, localityName
					, localityName
					, CommonUtils.SQLITE_DATE_FORMAT.format(new Date(System.currentTimeMillis()))
					, Integer.toString(widgetId)				// 更新対象のウィジェットID
				}
		);
		
		return 1;
	}

	/**
	 * ウィジェットの地域情報を更新する
	 * @param widgetId ウィジェットID
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 都道府県名
	 * @param localityName 市区町村名
	 * @param context アプリケーションコンテキスト
	 * @return 更新件数
	 * @throws IOException 入出力例外
	 */
	public int updateWidgetForeignLocation(int widgetId,
			String countryNameCode, String localityName, Context context) throws IOException {

		// SQL取得
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_t_widget_locale_history));
		
		// SQL 問い合わせを実行し、
		// 日本の気象予報のものとほぼ同じSQL を使用し、
		// ウィジェット取得地域履歴一覧テーブルの県名(administrative area)はnull で更新しておく
		db.execSQL(prop.getProperty("sql_update_table_for_w_foreign_location"),
				new String[]{
					"0"
					, Integer.toString(GPSHelper.GPS_FLAG_FALSE)
					, localityName
					, localityName
					, countryNameCode							// bind country_name_code_id
					, localityName
					, CommonUtils.SQLITE_DATE_FORMAT.format(new Date(System.currentTimeMillis()))
					, Integer.toString(widgetId)				// 更新対象のウィジェットID
				}
		);
		
		return 1;
	}
	
	/**
	 * 指定されたウィジェットIDのGPSフラグを変更する
	 * @param widgetId ウィジェットID
	 * @param gpsFlag GPSフラグ
	 * @param context コンテキスト
	 * @return 更新カラム数
	 */
	public int setGpsFlag(int widgetId, boolean gpsFlag, Context context) {
		
		ContentValues cv = new ContentValues();
		cv.put("gps_flag", gpsFlag? 0: 1);
		
		return db.update(context.getString(R.string.t_widget_locale_history)
				, cv, "widget_id = ?", new String[]{Integer.toString(widgetId)});
	}
	
	/**
	 * 海外の国名一覧の名前を取得する
	 * @param countryNameCode 検索から除外する国名コード
	 * @param context アプリケーションコンテキスト
	 * @return 国名情報一覧
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> selectForeignCountryNameList(
			String exclusionCountryNameCode, Context context) throws IOException {
		
		List<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		
		// SQL取得と問い合わせ
		// ※ 国用のsql定義ファイルへ移動させる
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_m_country));
		Cursor cursor = db.rawQuery(
				prop.getProperty("sql_select_c_n_by_ex_c_n_code"), new String[]{exclusionCountryNameCode});
		
		try {
			// SQL問い合わせ結果取得
			while (cursor.moveToNext()) {
				bean = new LocationBean();
				
				bean.setCountryNameCode(cursor.getString(0));
				bean.setCountryName(cursor.getString(1));
				list.add(bean);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		return list;
	}

}
