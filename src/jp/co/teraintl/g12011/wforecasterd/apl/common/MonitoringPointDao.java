package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * 気象観測地点データにアクセスするオブジェクト。<br />
 * 本オブジェクトでアクセスの対象となるデータは国コードマスタ、気象観測地点地方名マスタ
 * 、気象観測地点都道府県名マスタ、気象観測地点郡名マスタ、気象観測地点市区町村名マスタとなる。<br /><br />
 * 
 * @author tsutomu
 */
public class MonitoringPointDao {
	
	private final String LOG_TAG = this.getClass().getName();
	
	/** SQLiteDatabase インスタンス */
	private SQLiteDatabase db;
	
	/** CSVファイルの区切り文字「,」 */
	private final String CSV_DELIMITER = ",";
	
	/** 共通ユーティリティインスタンス */
	private CommonUtils com;
	
	/**
	 * コンストラクタ
	 * @param db DAOにてデータアクセスするDB
	 */
	public MonitoringPointDao(SQLiteDatabase db, Context context) {
		this.db = db;
		com = CommonUtils.getInstance(context);
	}
	
	/**
	 * 気象観測地点候補をDBから検索する。<br />
	 * 指定した都道府県名を条件に都道府県内の気象観測地点を検索する。
	 * @param context アプリケーションコンテキスト
	 * @param countryNameCode 国名
	 * @param administrativeAreaName 都道府県名
	 * @return 気象観測地点候補一覧(LocationBean)
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean>selectCandidateForMonitoringPoint(
			Context context, String countryNameCode, String administrativeAreaName) throws IOException {

		Cursor cursor = null;
		ArrayList<LocationBean> list = new ArrayList<LocationBean>();
		LocationBean bean = null;
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_monitoring_point));
		
		try {
			cursor = db.rawQuery(prop.getProperty("sql_select_by_administrative_area_name"),
					new String[]{countryNameCode, administrativeAreaName});
			
			int index = 0;
			int id = 0;
			while (cursor.moveToNext()) {
				index = 0;
				bean = new LocationBean();
				bean.setId(id++);												// id
				bean.setLatitude(cursor.getDouble(index++));					// latitude
				bean.setLongitude(cursor.getDouble(index++));					// longitude
				bean.setCountryNameCode(cursor.getString(index++));				// country_name_code
				bean.setCountryName(cursor.getString(index++));					// country_name
				bean.setRegionName(cursor.getString(index++));					// region_name
				bean.setAdministrativeAreaName(cursor.getString(index++));		// administrative_area_name
				//bean.setSubAdministrativeAreaName(cursor.getString(index++));	// sub_administrative_area_name
				bean.setLocalityName(cursor.getString(index++));				// locality_name
				bean.setVLocalityName(cursor.getString(index++));				// v_locality_name
				bean.setRssUrl(cursor.getString(index++));						// rss_url
				
				list.add(bean);
			}
			
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		
		return list;
	}
		
	/**
	 * マスタ系の全テーブルを初期化する。<br />
	 * @return データ操作件数
	 */
	public long initTablesIfNoData(Context context) throws IOException {
		
		AssetManager asset = context.getResources().getAssets();
		BufferedReader br = null;
		String line = null;
		String[] recoard = null;
		String now = CommonUtils.SQLITE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));
		Properties prop = null;
		SQLiteStatement stmt = null;
		ResourceBundle bundle = com.getGlobalResourceBundle();
		int index = 0;
		long deleteRowCount = 0;
		long rowCount = 0;
		
		try {
			// テーブルのデータを削除する
			
			db.beginTransaction();
			Log.i(LOG_TAG, "initTableIfNoData():トランザクション開始(データ削除処理)");
			
			rowCount += db.delete(bundle.getString("m_country"), null, null);
			rowCount += db.delete(bundle.getString("m_p_region"), null, null);
			rowCount += db.delete(bundle.getString("m_p_administrative_area"), null, null);
			rowCount += db.delete(bundle.getString("m_p_sub_administrative_area"), null, null);
			rowCount += db.delete(bundle.getString("m_p_locality"), null, null);
			rowCount += db.delete(bundle.getString("m_p_foreign_city"), null, null);
			
			db.setTransactionSuccessful();
			Log.i(LOG_TAG, "initTablesIfNoData():トランザクションコミット(データ削除処理)");
		} finally {
			if (br != null)
				br.close();
			if (stmt != null)
				stmt.close();
			
			// トランザクション終了(DBトランザクションコミットがされていない場合はロールバック)
			if (db.inTransaction()) {
				db.endTransaction();
				Log.i(LOG_TAG, "initTablesIfNoData():トランザクション終了(データ削除処理)");
			}
		}
		
		try {
			// データ挿入処理
			
			db.beginTransaction();
			Log.i(LOG_TAG, "initTablesIfNoData():トランザクション開始(データ挿入処理)");
			
			// 国コードマスタ初期化
			// SQL読み込み(assets はシングルインスタンス(?)なのでcsvよりも先に読み込む)
			prop = com.getAssetsXmlResourceProperty(
					context, bundle.getString("assets_sql_m_country"));
			stmt = db.compileStatement(prop.getProperty("sql_insert_table"));
			br = new BufferedReader(new InputStreamReader(
					asset.open(bundle.getString("assets_csv_m_country_default"))), CommonUtils.DEF_BUFF_SIZE_8K);

			while ((line = br.readLine()) != null) {
				recoard = line.split(CSV_DELIMITER, -1);
				index = 0;
				
				stmt.bindString(index + 1, recoard[index++]);		// id
				stmt.bindString(index + 1, recoard[index++]);		// country_name_code
				stmt.bindString(index + 1, recoard[index++]);		// country_name
				stmt.bindString(++index, now);						// update_date
				stmt.bindString(++index, now);						// registration_date
				stmt.executeInsert();
				rowCount++;
			}
			stmt.close();
			br.close();
			
			// 気象観測地点地方名マスタ初期化
			prop = com	.getAssetsXmlResourceProperty(
					context, bundle.getString("assets_sql_m_p_region"));
			stmt = db.compileStatement(prop.getProperty("sql_insert_table"));
			br = new BufferedReader(new InputStreamReader(
					asset.open(bundle.getString("assets_csv_m_p_region_default"))), CommonUtils.DEF_BUFF_SIZE_8K);

			while ((line = br.readLine()) != null) {
				recoard = line.split(CSV_DELIMITER, -1);
				index = 0;
				
				stmt.bindString(index + 1, recoard[index++]);		// id
				stmt.bindString(index + 1, recoard[index++]);		// parent_id
				stmt.bindString(index + 1, recoard[index++]);		// region_name
				stmt.bindString(++index, now);						// update_date
				stmt.bindString(++index, now);						// registration_date
				stmt.executeInsert();
				rowCount++;
			}
			stmt.close();
			br.close();
			
			// 気象観測地点都道府県名マスタ初期化
			prop = com.getAssetsXmlResourceProperty(
					context, bundle.getString("assets_sql_m_p_administrative_area"));
			stmt = db.compileStatement(prop.getProperty("sql_insert_table"));
			br = new BufferedReader(new InputStreamReader(
					asset.open(bundle.getString("assets_csv_m_p_administrative_area_default"))), CommonUtils.DEF_BUFF_SIZE_8K);

			while ((line = br.readLine()) != null) {
				recoard = line.split(CSV_DELIMITER, -1);
				index = 0;
				
				stmt.bindString(index + 1, recoard[index++]);			// id
				stmt.bindString(index + 1, recoard[index++]);			// parent_id
				stmt.bindString(index + 1, recoard[index++]);			// administrative_area_name
				stmt.bindString(++index, now);							// update_date
				stmt.bindString(++index, now);							// registration_date
				stmt.executeInsert();
				rowCount++;
			}
			stmt.close();
			br.close();
			
			// 気象観測地点郡名マスタ初期化
			prop = com.getAssetsXmlResourceProperty(
					context, bundle.getString("assets_sql_m_p_sub_administrative_area"));
			stmt = db.compileStatement(prop.getProperty("sql_insert_table"));
			br = new BufferedReader(new InputStreamReader(
					asset.open(bundle.getString("assets_csv_m_p_sub_administrative_area_default"))), CommonUtils.DEF_BUFF_SIZE_8K);
			
			while ((line = br.readLine()) != null) {
				recoard = line.split(CSV_DELIMITER, -1);
				index = 0;

				stmt.bindString(index + 1, recoard[index++]);			// id
				stmt.bindString(index + 1, recoard[index++]);			// parent_id
				stmt.bindString(index + 1, recoard[index++]);			// sub_administrative_area_name
				stmt.bindString(++index, now);							// update_date
				stmt.executeInsert();
				rowCount++;
			}
			stmt.close();
			br.close();
			
			// 気象観測地点市区町村名マスタ初期化
			prop = com.getAssetsXmlResourceProperty(
					context, bundle.getString("assets_sql_m_p_locality"));
			stmt = db.compileStatement(prop.getProperty("sql_insert_table"));
			br = new BufferedReader(new InputStreamReader(
					asset.open(bundle.getString("assets_csv_m_p_locality_default"))), CommonUtils.DEF_BUFF_SIZE_8K);
			
			while ((line = br.readLine()) != null) {
				recoard = line.split(CSV_DELIMITER, -1);
				index = 0;

				stmt.bindString(index + 1, recoard[index++]);			// id
				stmt.bindString(index + 1, recoard[index++]);			// parent_administrative_area_id
				stmt.bindString(index + 1, recoard[index++]);			// parent_sub_administrative_area_id
				stmt.bindString(index + 1, recoard[index++]);			// latitude
				stmt.bindString(index + 1, recoard[index++]);			// longitude
				stmt.bindString(index + 1, recoard[index++]);			// locality_name
				stmt.bindString(index + 1, recoard[index++]);			// v_locality_name
				stmt.bindString(index + 1, recoard[index++]);			// rss_url
				stmt.bindString(++index, now);							// update_date
				stmt.bindString(++index, now);							// registration_date
				stmt.executeInsert();
				rowCount++;
			}
			
			// 海外都市一覧マスタ初期化
			prop = com.getAssetsXmlResourceProperty(
					context, bundle.getString("assets_sql_m_p_foreign_city"));
			stmt = db.compileStatement(prop.getProperty("sql_insert_table"));
			br = new BufferedReader(new InputStreamReader(
					asset.open(bundle.getString("assets_csv_m_p_foreign_city_default"))), CommonUtils.DEF_BUFF_SIZE_8K);
			
			while ((line = br.readLine()) != null) {
				recoard = line.split(CSV_DELIMITER, -1);
				index = 0;

				stmt.bindString(index + 1, recoard[index++]);			// id
				stmt.bindString(index + 1, recoard[index++]);			// country_name_code
				stmt.bindString(index + 1, recoard[index++]);			// latitude
				stmt.bindString(index + 1, recoard[index++]);			// longitude
				stmt.bindString(index + 1, recoard[index++]);			// city_name
				stmt.bindString(index + 1, recoard[index++]);			// v_city_name
				stmt.bindString(++index, now);							// update_date
				stmt.bindString(++index, now);							// registration_date
				stmt.executeInsert();
				rowCount++;
			}
			
			// DBトランザクションコミット
			db.setTransactionSuccessful();
			Log.i(LOG_TAG, "initTablesIfNoData():トランザクションコミット");
			
		} finally {
			if (br != null)
				br.close();
			if (stmt != null)
				stmt.close();
			
			// トランザクション終了(DBトランザクションコミットがされていない場合はロールバック)
			if (db.inTransaction()) {
				db.endTransaction();
				Log.i(LOG_TAG, "initTablesIfNoData():トランザクション終了");
			}
		}
		
		return deleteRowCount + rowCount;
	}
	
	/**
	 * 緯度、経度を地域情報マスタから検索する
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 都道府県名
	 * @param localityName 市区町村名
	 * @param context アプリケーションコンテキスト
	 * @return 緯度・経度
	 * @throws IOException 入出力例外
	 */
	public double[] selectLatitudeAndLongitude(String countryNameCode
			, String administrativeAreaName, String localityName, Context context) throws IOException {
		
		Properties prop = com.getAssetsXmlResourceProperty(
				context, context.getString(R.string.assets_sql_monitoring_point));
		
		Cursor cursor = db.rawQuery(
				prop.getProperty("sql_select_latitude_and_longitude")
				, new String[]{countryNameCode, administrativeAreaName, localityName});
		double[] ret = null;
		int index = 0;

		try {
			if (cursor.moveToNext()) {
				ret = new double[2];
				ret[0] = cursor.getLong(index++);
				ret[1] = cursor.getLong(index++);
			}
		} finally {
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}

		return ret;
	}
}