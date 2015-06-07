package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 共通ユーティリティクラス
 * アプリケーションで全体的に共通で利用される属性やメソッドを持つクラス。
 * 本オブジェクトに記載することで、
 * メモリの節約とキャッシュ、各ソースコードの属性宣言を簡潔化する。<br />
 * このクラスに含まれるの属性やメソッドは全てスレッドセーフである。<br />
 * また、クラスローダーが破棄またはそれによってアンロードされるまで
 * 本クラスにあるインスタンスはGCされない(頻繁にGCされない)ことが
 * 想定されるため、大きいデータは持たない設計となっている。
 * 
 * スレッドについての参考文献
 * http://www.ibm.com/developerworks/jp/java/library/j-jtp0730/
 * 
 * @author tsutomu
 */
public class CommonUtils {
	
	/** ログ出力用タグ */
	private String LOG_TAG = this.getClass().getCanonicalName();
	
	/** CommonUtils インスタンス */
	private static CommonUtils commonUtils;
	
	/** CSV区切り文字解析時のデリミタ */
	public static final String CSV_DELIMITER = ",";
	
	public static final String NULL_STRING = "";
	
	// ※↓本来は不要なはず
	/** ウィジェットからActivityへ遷移する際のウィジェットデータ保存用MAP
	 * (Android APIの障害(?)が修正されれば、将来的に廃止する)
	 * key:ウィジェットID value:ウィジェットIDの対象地域情報Bean */
	public static List<LocationBean> cacheWidgets =
			Collections.synchronizedList(new ArrayList<LocationBean>());
	
	/** 新生ウィジェットとして扱われる更新回数 */
	public static long lifeSpanOfBabyWidget;
	
	/** 新生ウィジェットの更新間隔 */
	public static long intervalBabyWidget;
	
	/** 恒常更新ウィジェットの更新間隔 */
	public static long intervalConstantWidget;
	
	/** 入出力バッファ使用時に指定するデフォルトのバッファサイズ */
	public static final int DEF_BUFF_SIZE_8K = 8192;
	
	/** 前回DBをクリーンアップした日時(ミリ秒単位) */
	public static long lastDbCleanupDate;
	
	/** DBインスタンス取得時の権限(read only) */
	public static final char PERM_DB_READ_ONLY = 'r';
	
	/** DBインスタンス取得時の権限(writeable) */
	public static final char PERM_DB_WRITABLE = 'w';
	
	/** SQLiteのDate書式変換用日付フォーマット */
	public static final SimpleDateFormat SQLITE_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S");
	
	/** 発表/観測日時日付フォーマット */
	public static final SimpleDateFormat ANNOUNCE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/** WeatherBeanのDate書式変換用日付フォーマット */
	public static final SimpleDateFormat WEATHERBEAN_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	
	/** Widget表示用のDate書式変換用日付フォーマット */
	public static final SimpleDateFormat WIDGET_DATE_FORMAT = new SimpleDateFormat("MM/dd");

	private LocationBean lastRefedLocation;
	
	/** SQL "INSERT OR REPLACE INTO ..." 構文を引くためのキー */
	public static final String SQL_PROP_INS_OR_REPLACE = "sql_ins_or_rep_table";
	
	/** 気象予報プロバイダ(WeatherMap) */
	public static final int PROVIDER_WEATHER_MAP = 1;
	
	/** 気象予報プロバイダ(WorldWeatherOnline) */
	public static final int PROVIDER_WORLD_WEATHER_ONLINE = 2;
	
	/** 気象予報処理実行中フラグ(排他制御用フラグ) */
	private boolean forcastTransactionFlag = false;
	
	/** ロック取得中のスレッド名 */
	private String lockAuthorityThread;
	
	/** ロック取得タイムアウトのデフォルト値(10分) */
	private long TRANSACTION_LOCK_WAIT_DEFAULT = 1L * 1000L * 60L * 10L;
	
	/** ウィジェットを更新する間隔 */
	public static long WIDGET_UPDATE_INTERVAL = 1L * 1000L * 60L * 60L * 3L;
	
	/**
	 * コンストラクタ
	 * @param context アプリケーションコンテキスト
	 */
	private CommonUtils(Context context) {
		
		// 新生ウィジェットと恒常更新ウィジェットの更新間隔を設定
		intervalBabyWidget = 1L * 1000L * 60L
				* Long.parseLong(context.getString(R.string.interval_baby_widget));
		intervalConstantWidget = 1L * 1000L * 60L
				* Long.parseLong(context.getString(R.string.interval_constant_widget));
		
		lifeSpanOfBabyWidget = Long.parseLong(
				context.getString(R.string.life_span_of_baby_widget));
	}
	
	/**
	 * インスタンスを取得する。
	 * @return CommonUtils インスタンス
	 */
	public static CommonUtils getInstance(Context context) {
		
		if (commonUtils == null)
			commonUtils = new CommonUtils(context);
		
		return commonUtils;
	}
	
	/**
	 * インスタンスを破棄する
	 */
	public static void destroyInstance() {
		
		// ※これでは破棄されない
		if (commonUtils != null) {
			try {
				commonUtils.finalize();
				commonUtils = null;
			} catch (Throwable e) {
				// ignore
			}
		}
	}
	
	/**
	 * assets ディレクトリ内のXMLリソースのプロパティを取得する
	 * @param fileName XMLプロパティファイル名
	 * @return リソースがロードされたProperties オブジェクト
	 * @throws IOException 入出力例外
	 */
	public Properties getAssetsXmlResourceProperty(Context context, String fileName) throws IOException {
		Properties prop = new Properties();
		
		InputStream inStream = new BufferedInputStream(
				context.getResources().getAssets().open(fileName), CommonUtils.DEF_BUFF_SIZE_8K);
		
		try {
			// assets ディレクトリ内のリソースをロードする
			prop.loadFromXML(inStream);
		} finally {
			// ストリームのクローズ
			if (inStream != null)
				inStream.close();
		}
		
		return prop;
	}
	
	/**
	 * アプリケーション全体で使用するDatabaseHelper を取得する
	 * @return DatabaseHelper
	 */
	public DatabaseHelper getDatabaseHelperInstance(Context context) {
		
		return new DatabaseHelper(
				context
				, context.getString(R.string.database_name)
				, null
				, Integer.parseInt(context.getString(R.string.database_version)));
	}
	
	/**
	 * DBHelper を取得する
	 * @param permission 権限
	 * @param context アプリケーションコンテキスト
	 * @return 本アプリケーションのDatabaseHelper
	 */
	public DatabaseHelper getDatabaseHelperInstance(char permission, Context context) {
		ResourceBundle bundle = getGlobalResourceBundle();
		
		return new DatabaseHelper(
				context
				, bundle.getString(context.getString(R.string.database_name))
				, null
				, Integer.parseInt(context.getString(R.string.database_version)));
	}
	
	/**
	 * global.properties のリソースバンドルを取得する
	 * @return global.propertiesリソースバンドル
	 */
	public ResourceBundle getGlobalResourceBundle() {
		return ResourceBundle.getBundle("global");
	}

	/**
	 * DBをオープンし、DBインスタンスを取得する。
	 * @param context アプリケーションコンテキスト
	 * @param permission リードオンリー(PERM_DB_READ_ONLY)/書き込み(PERM_DB_WRITABLE)権限
	 * @return DBインスタンス
	 */
	public SQLiteDatabase openSQLiteDatabase(Context context, char permission) {
		
		DatabaseHelper dbHelper = getDatabaseHelperInstance(context);
		
		if (permission == CommonUtils.PERM_DB_WRITABLE) {
			// 書き込み権限ありでDBをopen
			return dbHelper.getWritableDatabase();
		} else if (permission == CommonUtils.PERM_DB_READ_ONLY) {
			// リードオンリーでDBをopen
			return dbHelper.getReadableDatabase();
		}
		
		// どちらでもない場合はnull
		return null;
	}
	
	/**
	 * 日付(年/月/日)を、桁数を整形して取得する
	 * @param attributeValue 整形前日付書式文字列
	 * @return yyyy/mm/dd 形式の文字列
	 */
	public String formatStringDate(String attributeValue) {
		String delimiter = "/";
		String[] array = null;
		StringBuilder builder = new StringBuilder();
		array = attributeValue.split(delimiter, -1);
		
		for (int i = 0; i < array.length; i++) {
			switch(i) {
			case 0:
				builder.append(String.format("%04d", Integer.parseInt(array[i])));
				break;
			case 1:
				builder.append("/" + String.format("%02d", Integer.parseInt(array[i])));
				break;
			case 2:
				builder.append("/" + String.format("%02d", Integer.parseInt(array[i])));
				break;
			}
		}
		
		return builder.toString();
	}

	/**
	 * 最終参照地域を保持するための地域情報Bean を作成する。<br />
	 * 作成された地域情報Bean は本シングルトンオブジェクト内で保持され、
	 * 戻る処理等が発生した場合に、本メソッドで作成された地域情報Beanを
	 * 参照して週間天気予報を表示する場合等に役立てる。
	 * @param lastRefedLocation 地域情報Bean
	 */
	public void saveLastRefedLocation(LocationBean lastRefedLocation) {
		this.lastRefedLocation = lastRefedLocation.getCopyDeep();
	}
	
	/**
	 * 最終参照地域を保持するための地域情報Beanを取得する。
	 * @return 最終参照地域情報Bean
	 */
	public LocationBean loadLastRefedLocation() {
		
		return lastRefedLocation;
	}
	
	/**
	 * Calendarオブジェクトの曜日定数値から、日本語形式の曜日文字列を取得する。
	 * @param calWeek Calendarオブジェクトの曜日低数値
	 * @return 日本語形式の曜日文字列
	 */
	public String getWeekStringJa(int calWeek) {
		
		switch (calWeek) {
			case Calendar.SUNDAY:
				return "日";
			case Calendar.MONDAY:
				return "月";
			case Calendar.TUESDAY:
				return "火";
			case Calendar.WEDNESDAY:
				return "水";
			case Calendar.THURSDAY:
				return "木";
			case Calendar.FRIDAY:
				return "金";
			case Calendar.SATURDAY:
				return "土";
		}
		return null;
	}
	
	/**
	 * 文字列の曜日から、Calendarオブジェクトの曜日定数値を取得する。
	 * @param weekOfDay 文字列の曜日
	 * @return Calendarオブジェクトの曜日定数値
	 */
	public int getCalendarWeek(String weekOfDay) {
		
		weekOfDay.trim().toLowerCase();
		
		if (weekOfDay.startsWith("sun") || weekOfDay.startsWith("日")) {
			return Calendar.SUNDAY;
		} else if (weekOfDay.startsWith("mon") || weekOfDay.startsWith("月")) {
			return Calendar.MONDAY;
		} else if (weekOfDay.startsWith("tue") || weekOfDay.startsWith("火")) {
			return Calendar.TUESDAY;
		} else if (weekOfDay.startsWith("wed") || weekOfDay.startsWith("水")) {
			return Calendar.WEDNESDAY;
		} else if (weekOfDay.startsWith("thu") || weekOfDay.startsWith("木")) {
			return Calendar.THURSDAY;
		} else if (weekOfDay.startsWith("fri") || weekOfDay.startsWith("金")) {
			return Calendar.FRIDAY;
		} else if (weekOfDay.startsWith("sat") || weekOfDay.startsWith("土")) {
			return Calendar.SATURDAY;
		}
		
		return -1;
	}
	
	/**
	 * 更新対象のウィジェットを検索する。<br />
	 * 第1引数には、現在アプリで起動している全てのウィジェット一覧が
	 * 格納されているリストを指定するようにする。<br />
	 * 取得する法則としては・・・
	 * (1)新生ウィジェットが存在する場合は、その新生ウィジェットのみの一覧を取得
	 * (2)新生ウィジェットが存在しない場合は恒常更新ウィジェットの中から更新日付が
	 *    (現在時刻 - 恒常更新ウィジェットインターバル) より大きい
	 * (3)(2)も存在しない場合は、引数に指定された
　	 * @param widgetList ウィジェットリスト
	 * @param currentTime 現在日時(ミリ秒形式)
	 * @return 更新対象ウィジェット一覧
	 * @throws ParseException 解析例外
	 * @throws IOException 入出力例外 
	 */
	public List<LocationBean> getTargetUpdateWidgets(
			List<LocationBean> allWidgets, Date currentDate
			, Context context) throws ParseException, IOException {
		
		List<LocationBean> retList = new ArrayList<LocationBean>();
		
		// 新生ウィジェットを取得し、存在すれば返す
		retList = getTargetUpdateBabyWidgetList(allWidgets);
		if (retList.size() > 0)
			return retList;
		
		/* 新生ウィジェットが無く、恒常更新ウィジェットがある場合は、
		 * 恒常更新ウィジェットを更新対象とする */
		retList = getTargetUpdateConstantWidgetList(allWidgets, currentDate);
		if (retList.size() > 0)
			return retList;
		
		retList = getTargetUpdateOldestWidgetList(allWidgets);
		if (retList.size() > 0) {
			return retList;
		}
		
		// ※独自の例外をスローするようにしたい
		return allWidgets;
	}

	/**
	 * 新生ウィジェット一覧を取得する。
	 * @param widgetList ウィジェットリスト
	 * @return 新生ウィジェット一覧リスト
	 */
	public List<LocationBean> getTargetUpdateBabyWidgetList(List<LocationBean> widgetList) {
		
		List<LocationBean> retList = new ArrayList<LocationBean>();
		
		// 新生ウィジェット検索
		for (LocationBean widget: widgetList) {
			if(widget.getUpdateCount() <= lifeSpanOfBabyWidget) {
				retList.add(widget);
			}
		}

		return retList;
	}
	
	/**
	 * 恒常更新対象ウィジェットの中で、更新対象となるウィジェット一覧を取得する。<br />
	 * 恒常更新対象ウィジェットの中で、
	 * 恒常更新ウィジェットインターバルを過ぎたものの一覧を取得する。
	 * @param widgetList ウィジェットリスト
	 * @param currentDate 現在日時
	 * @return 更新対象ウィジェット
	 * @throws ParseException 解析例外
	 */
	public List<LocationBean> getTargetUpdateConstantWidgetList(
			List<LocationBean> widgetList, Date currentDate) throws ParseException {
		
		List<LocationBean> retList = new ArrayList<LocationBean>();
		
		for (LocationBean widget: widgetList) {
			if(currentDate.getTime() - CommonUtils.SQLITE_DATE_FORMAT
					.parse(widget.getUpdateDate()).getTime() >= intervalConstantWidget) {
				retList.add(widget);
			}
		}
		
		return retList;
	}
	
	/**
	 * 更新日時が最も古いウィジェットを返す。<br />
	 * 最も古いウィジェットが複数個ある場合は、すべてを返す。
	 * @param widgetList ウィジェットリスト
	 * @return 更新対象ウィジェット
	 * @throws ParseException 解析例外
	 */
	public List<LocationBean> getTargetUpdateOldestWidgetList(
			List<LocationBean> widgetList) throws ParseException {
		
		List<LocationBean> retList = new ArrayList<LocationBean>();
		
		// 更新日付が最も古いウィジェットを検索する
		for (LocationBean widget: widgetList) {
			if (retList.size() == 0) {
				retList.add(widget);
				continue;
			}
			
			long tmpWidgetUpdateTime =
					CommonUtils.SQLITE_DATE_FORMAT.parse(retList.get(0).getUpdateDate()).getTime();
			long lastWidgetUpdateTime =
					CommonUtils.SQLITE_DATE_FORMAT.parse(widget.getUpdateDate()).getTime();
			
			if (tmpWidgetUpdateTime < lastWidgetUpdateTime) {
				retList.clear();
				retList.add(widget);
			} else if (tmpWidgetUpdateTime == lastWidgetUpdateTime) {
				retList.add(widget);
			}
		}
		
		return retList;
	}

	/**
	 * 新生ウィジェットが1つ以上存在する場合かどうかを確認する
	 * @param widgetList ウィジェットリスト
	 * @return 存在チェック結果
	 */
	public boolean existBabyWidget(List<LocationBean> widgetList) {
		
		for (LocationBean widget: widgetList) {
			if (widget.getUpdateCount() <= lifeSpanOfBabyWidget) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 次回サービス起動までのインターバル(ミリ秒)を取得する。<br />
	 * インターバルの決定方法は、現存する全てのウィジェットの中から
	 * 最も古いものが更新されるべき時間が採用される。<br />
	 * 更新されるポリシーは下記の通り。<br />
	 * ・新生ウィジェットがある場合
	 *     return 新生ウィジェット更新間隔として指定されたミリ秒
	 * ・新生ウィジェットが無い場合
	 *   ・最古ウィジェットの更新時刻 - 現在時刻 < 恒常更新ウィジェット更新間隔 の場合
	 *     return 恒常更新ウィジェットの更新間隔
	 * @throws ParseException 解析例外
	 * @throws IOException 入出力例外
	 */
	public long getServiceIntervalTime(
			List<LocationBean> allWidgets, Context context) throws ParseException, IOException {
		
		// ※GPS起動の場合の考慮が必要
		
		WeatherLocationProvider provider = new WeatherLocationProvider(context);
		List<LocationBean> widgets = provider.getAllWidgets(context);
		
		return existBabyWidget(widgets)? intervalBabyWidget: intervalConstantWidget;
	}

	/**
	 * アプリケーションで利用する気象予報プロバイダ識別子を取得する。
	 * 識別子については、string.xml の定義を参照
	 * @param context アプリケーションコンテキスト
	 * @return 気象予報プロバイダ識別子
	 */
	public int getCurrentWeatherProviderId(Context context) {
		return Integer.parseInt(context.getString(R.string.weather_provider_default));
	}
	
	/**
	 * アプリケーションで利用する気象予報プロバイダオブジェクトを取得する。
	 * @param context アプリケーションコンテキスト
	 * @return 気象予報プロバイダ
	 */
	public WeatherProvider getCurrentWeatherProvider(Context context) {
		
		switch (getCurrentWeatherProviderId(context)) {
			case CommonUtils.PROVIDER_WEATHER_MAP:
				return new WeatherProviderForWeatherMap(context);
			case CommonUtils.PROVIDER_WORLD_WEATHER_ONLINE:
				return new WeatherProviderForWWOnline(context);
		}
		
		return null;
	}
	
	/**
	 * 気象予報取得処理トランザクションのロックを取得する。<br />
	 * ロックの取得ができない場合、wait() を実行して
	 * デフォルトで指定されているタイムアウト時間を過ぎるまで取得を試みる。
	 * タイムアウト時間が過ぎでも取得できない場合は
	 * 取得失敗としてfalse を返却する。
	 * @return ロック取得成否
	 */
	public boolean startForcastTransaction(Object object) {
		
		return startForcastTransaction(TRANSACTION_LOCK_WAIT_DEFAULT, object);
	}
	
	/**
	 * 気象予報取得処理トランザクションのロックを取得する。
	 * @param lockWaitTime ロック取得タイムアウト時間
	 * @param forceExec ロック取得タイムアウト時
	 * @return ロック取得成否
	 */
	public boolean startForcastTransaction(long lockWaitTime, Object object) {
		return manageForcastTransaction(true, lockWaitTime, object);
	}
	
	/**
	 * 気象予報取得処理トランザクションの
	 * @return ロック解放成否
	 */
	public boolean finishForcastTransaction(Object object) {
		return manageForcastTransaction(false, 0L, object);
	}
	
	/**
	 * 気象予報取得処理トランザクションのロックの取得/解放を管理する。
	 * @param lockActionType 動作タイプ。true:ロック取得, false:ロック解放
	 * @param lockWaitTime ロック取得タイムアウト時間。ロック解放時は無視
	 * @return ロック取得または解放の成否
	 */
	private synchronized boolean manageForcastTransaction(
			boolean lockActionType, long lockWaitTime, Object object) {
		
		String logObj = object.getClass().getCanonicalName();
		String thName = Thread.currentThread().getName();

		// 既にロックを取得しており、且つそれが自スレッドによるものである場合
		if (lockActionType == true
				&& forcastTransactionFlag
				&& thName.equals(lockAuthorityThread)) {
			return true;
		}

		if (lockActionType) {
			
			// ロック取得処理の場合
			if (!forcastTransactionFlag) {
				
				/* 他のスレッドにロックを取得されていない場合、
				 * ロックを取得して処理を継続する。				 */
				forcastTransactionFlag = true;
				lockAuthorityThread = thName;
				Log.i(LOG_TAG, "manageForcastTransaction():ロックを取得しました[Prosecutor="
						+ logObj + "],[MyThread=" + lockAuthorityThread + "][Flag=" + forcastTransactionFlag + "]");
				return true;
			} else {
				
				// 他のスレッドにロックを取得されている場合、wait を実行する。
				try {
					Log.i(LOG_TAG, "manageForcastTransaction():waitを実行します[Prosecutor="
							+ logObj + "],[Flag=" + forcastTransactionFlag + "]");

					wait(lockWaitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (!forcastTransactionFlag) {
					// notify によってwaitが終了した場合
					Log.i(LOG_TAG, "manageForcastTransaction():notifyにより復帰[Prosecutor="
							+ logObj + "],[Flag=" + forcastTransactionFlag + "]");
					
					forcastTransactionFlag = true;
					return true;
				} else {
					// タイムアウトによってwait が終了した場合
					Log.i(LOG_TAG, "manageForcastTransaction():waitがタイムアウトしました" +
							"[Prosecutor=" + logObj + "],[Flag=" + forcastTransactionFlag + "]");
					return false;
				}
			}
		} else {

			if (!forcastTransactionFlag) {
				Log.i(LOG_TAG, "manageForcastTransaction():スレッドは既にロックされていません。" +
						"[LockActionType=" + lockActionType + "][MyThread=" + thName + "]");
				return true;
			}
			
			// ロック解放処理の場合
			if (thName.equals(lockAuthorityThread)) {
				forcastTransactionFlag = false;
				notify();
				Log.i(LOG_TAG, "manageForcastTransaction():ロックを解放しました" +
						"[LockActionType=" + lockActionType + "][MyThread=" + thName + "]");
				return true;
			} else {
				Log.e(LOG_TAG, "manageForcastTransaction():ロックを解放する権限がありません" +
						"[LockActionType=" + lockActionType + "][MyThread=" + thName + "]");
				return false;
			}
		}
	}

	/**
	 * 更新対象のウィジェットとしてDBへ追加する
	 * @param widgetIds ウィジェットID
	 * @param context アプリケーションコンテキスト
	 * @return 処理件数
	 * @throws IOException 入出力例外
	 */
	public long addNewWidgetsToDb(int[] widgetIds, Context context) throws IOException {
		
		List<LocationBean> newWidgets = new ArrayList<LocationBean>();
		
		// デフォルト値で埋める
		for (int i = 0; i < widgetIds.length; i++) {
			newWidgets.add(
					WeatherLocationDao.getDefaultLocationData(widgetIds[i], true, context));
		}
		
		return addWidgetsToDb(newWidgets, context);
	}
	
	/**
	 * 更新対象のウィジェットとしてDBへ追加する
	 * @param widgets ウィジェット
	 * @param context アプリケーションコンテキスト
	 * @return 処理件数
	 * @throws IOException 入出力例外
	 */
	public synchronized long addWidgetsToDb(
			List<LocationBean> widgets, Context context) throws IOException {
		
		WeatherLocationProvider provider = new WeatherLocationProvider(context);
		long count = 0L;
		
		for (LocationBean widget: widgets) {
			provider.insertWeatherLocation(widget, context);
			count++;
		}
		
		return count;
	}
	
	/**
	 * 気象予報詳細へ戻るためのインテントを作成する
	 * @param widget ウィジェット(地域情報Bean)
	 * @param context アプリケーションコンテキスト
	 * @param cls クラス
	 * @return 気象予報詳細Activity送信用インテント
	 */
	public static Intent getFrcstMtrlgIntent(
			LocationBean widget, Context context, Class<? extends Activity> cls) {
		
		Intent intent = new Intent(context, cls);
		
		// ※ 国内の気象予報と海外の気象予報で表示する項目を変更すること
		intent.putExtra(context.getString(
				R.string.intent_extra_widget_id), widget.getWidgetId());
		intent.putExtra(context.getString(
				R.string.intent_extra_load_last_ref_location_flg), true);
		
		// Activityに保存されているページの遷移履歴(スタック)をクリアする
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		return intent;
	}
}
