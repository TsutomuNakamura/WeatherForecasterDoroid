package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 気象観測地点プロバイダ<br />
 * 気象観測地点に関する情報の操作や取得などを提供する。
 * @author tsutomu
 *
 */
public class MonitoringPointProvider {
	
	/** 共通ユーティリティインスタンス */
	private CommonUtils com;
	
	/**
	 * コンストラクタ
	 * @param context アプリケーションコンテキスト
	 */
	public MonitoringPointProvider(Context context) {
		com = CommonUtils.getInstance(context);
	}
	
	/**
	 * 引数に指定した現在地情報Beanから、気象観測地点候補リストを取得する
	 * @param context アプリケーション婚的sと
	 * @param currentLocation 現在地情報Bean
	 * @return 気象観測地点候補リスト
	 * @throws IOException 入出力例外
	 */
	public List<LocationBean> getCandidateForMonitoringPoint(
			Context context, LocationBean currentLocation) throws IOException {
		
		List<LocationBean> list = null;
		SQLiteDatabase db = null;
		
		db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
		
		try {
			MonitoringPointDao dao = new MonitoringPointDao(db, context);
			list = dao.selectCandidateForMonitoringPoint(context
					, currentLocation.getCountryNameCode(), currentLocation.getAdministrativeAreaName());
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return list;			
	}
	
	/**
	 * テーブル情報を初期化する
	 * @param context アプリケーションコンテキスト
	 * @return 更新件数
	 * @throws IOException 入出力例外
	 */
	public long initTablesIfNoData(Context context) throws IOException {
		
		long ret = 0;
		SQLiteDatabase db = null;
		
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_WRITABLE);
			MonitoringPointDao dao = new MonitoringPointDao(db, context);
			ret = dao.initTablesIfNoData(context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return ret;
	}
	
	/**
	 * 緯度、経度を検索します
	 * @param widgetId ウィジェットID
	 * @param countryNameCode 国名コード
	 * @param administrativeAreaName 都道府県名
	 * @param localityName 市区町村名
	 * @param context アプリケーションコンテキスト
	 * @return 緯度・経度
	 * @throws IOException 入出力例外
	 */
	public double[] searchLatitudeAndLongitudeFromDb(
			String countryNameCode, String administrativeAreaName
			, String localityName, Context context) throws IOException {
		
		SQLiteDatabase db = null;
		double[] ret = null;
		try {
			db = com.openSQLiteDatabase(context, CommonUtils.PERM_DB_READ_ONLY);
			MonitoringPointDao dao = new MonitoringPointDao(db, context);
			ret = dao.selectLatitudeAndLongitude(
					countryNameCode, administrativeAreaName, localityName, context);
		} finally {
			if (db.isOpen()) db.close();
		}
		
		return ret;
	}
}
