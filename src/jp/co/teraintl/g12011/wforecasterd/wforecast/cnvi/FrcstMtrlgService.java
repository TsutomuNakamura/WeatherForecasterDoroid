package jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherLocationProvider;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherProvider;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * ウィジェット更新用常駐プロセス。<br />
 * ウィジェット更新タイミングを管理する。
 * @author tsutomu
 */
public class FrcstMtrlgService extends IntentService {

	/** ログ出力時のタグ */
	private final String LOG_TAG = this.getClass().getName();
	
	/** サービス起動ステータス(スタート) */
	public static final String START_ACTION = "start";
	/** サービス起動ステータス(ストップ) */
	public static final String STOP_ACTION = "stop";
	/** サービス起動ステータス(インターバル) */
	public static final String INTERVAL_ACTION = "interval";
	/** サービス即時起動ステータス(即時実行) */
	public static final String IMMEDIATELY_ACTION = "immediately";
	/** 新規ウィジェット作成 */
	public static final String NEW_WIDGET_CREATED_ACTION = "new_widget_created";
	
	/** 自サービス再起動時に使用するインテント */
	private static Intent intervalServiceIntent = new Intent();
	
	/** 実行タイミングを指定するためのインテント */
	private PendingIntent pi;
	
	/** 実行を管理するAlarmManager */
	private AlarmManager manager;
	
	/**
	 * コンストラクタ
	 * @param name サービス名
	 */
	public FrcstMtrlgService(String name) {
		super(name);
	}
	
	/**
	 * コンストラクタ
	 */
	public FrcstMtrlgService() {
		super(FrcstMtrlgService.class.getName().substring(
				FrcstMtrlgService.class.getName().lastIndexOf(".")));
	}

	/** {@inheritDoc} */
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	/** {@inheritDoc} */
	@Override
	public void onHandleIntent(Intent intent) {
		
		// 指定されたアクション名
		String currentIntentAction = intent.getAction();
		
		// インターバル
		long interval = 0;
		
		// アプリケーションコンテキスト
		Context context = getApplicationContext();
		
		// 共通ユーテリティ
		CommonUtils com = CommonUtils.getInstance(context);
		
		// ウィジェット情報取りア出しのDBアクセス用プロバイダ
		WeatherLocationProvider wLProvider = new WeatherLocationProvider(context);
		
		// 全ウィジェット一覧
		List<LocationBean> allWidgets = null;
		
		// 更新対象となるウィジェットリスト
		List<LocationBean> targetWidgets = null;
		
		Log.i(LOG_TAG, "現在のAction:" + currentIntentAction);
		
		try {
			// 全ウィジェット取得
			allWidgets = wLProvider.getAllWidgets(context);
			
			// 更新対象のウィジェットを取得する
			targetWidgets = com.getTargetUpdateWidgets(allWidgets, new Date(), context);
			
			

			// 次回サービス起動時の呼び出すクラス(自サービスクラス)を指定する
			intervalServiceIntent.setClassName(getString(R.string.name_package_cnvi),
					getString(R.string.name_package_cnvi) + ".FrcstMtrlgService");
			intervalServiceIntent.setAction(INTERVAL_ACTION);
			
			// PendingIntentの作成
			pi = PendingIntent.getService(this, 0, intervalServiceIntent, 0);
			
			// 次回起動タイミングを指定するためのAlarmManager クラスの作成
			manager = (AlarmManager)getSystemService(ALARM_SERVICE);
			
			if (START_ACTION.equals(currentIntentAction) || INTERVAL_ACTION.equals(currentIntentAction)) {
				
				/* ==============================================================================
				 * アプリケーション初回起動時、及び通常のインターバル
				 * ============================================================================== */
				
				// 気象予報取得処理実行。(CommonUtili のウィジェットの情報もここで更新される)
				FrcstMtrlgAuthority authority = new FrcstMtrlgAuthority(context);
				
				try {
					// 気象予報開始
					authority.startWeatherForecast(targetWidgets, context);
					
					// 次回サービスの起動間隔を設定する
					interval = com.getServiceIntervalTime(allWidgets, context);

				} catch (ParseException e) {
					e.printStackTrace();
					interval = CommonUtils.intervalBabyWidget;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP
						, SystemClock.elapsedRealtime() + interval, pi);
				
				Log.i(LOG_TAG, "サービスを設定しました[ACTION=START_ACTION|INTERVAL_ACTION][TIME="+ interval + "]");
				
			} else if (NEW_WIDGET_CREATED_ACTION.equals(currentIntentAction)) {
				
				/* ==============================================================================
				 * 通常のインターバルの途中で、新しいウィジェットが追加されたとき
				 * ============================================================================== */

				// AlarmManager をキャンセルする
				manager = (AlarmManager)getSystemService(ALARM_SERVICE);
				if (manager != null && pi != null) {
					manager.cancel(pi);
				}
					
				// 次回起動時間を設定する
				manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, pi);
				Log.i(LOG_TAG, "サービスを設定しました[ACTION=NEW_WIDGET_CREATED_ACTION][TIME=" + 1000 + "]");
				
			} else if (STOP_ACTION.equals(currentIntentAction)) {
				
				/* ==============================================================================
				 * サービスの停止を意味するアクションが指定されたとき
				 * ============================================================================== */

				// サービス停止オプションが指定された場合
				if (manager != null && pi != null) {
					manager.cancel(pi);
				}
				stopSelf();
				Log.i(LOG_TAG, "サービスを停止しました");
				
			} else if (IMMEDIATELY_ACTION.equals(currentIntentAction)) {
				
				/* ==============================================================================
				 * サービスの即時実行を意味するアクションが指定されたとき
				 * ============================================================================== */

				if (manager != null && pi != null) {
					manager.cancel(pi);
				}
				
				// 即時実行オプションが指定された場合
				manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, pi);
				Log.i(LOG_TAG, "天気予報取得処理即時実行");
			}
			
			// DBの掃除。前回起動時刻情報が無い 又は 1日以上あいている場合、天気予報履歴テーブルの掃除
			if (CommonUtils.lastDbCleanupDate == 0
					|| (System.currentTimeMillis() - CommonUtils.lastDbCleanupDate)
					> Integer.parseInt(context.getString(R.string.interval_db_cleanup))) {
				
				WeatherProvider weatherProvider =
						CommonUtils.getInstance(context).getCurrentWeatherProvider(context);
				try {
					weatherProvider.cleanWeatherData(
							Long.parseLong(context.getString(R.string.interspace_date)), context);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 排他制御を利かせてサービスを起動する。<br />
	 * @param com 共通ユーティリティインスタンス
	 * @param context アプリケーションコンテキスト
	 */
	public static void startServiceImmediately(CommonUtils com, Context context) {

		try {
			// 気象予報取得処理トランザクション開始
			com.startForcastTransaction(FrcstMtrlgService.class);
			
			Intent intent = new Intent(context, FrcstMtrlgService.class);
			intent.setAction(FrcstMtrlgService.IMMEDIATELY_ACTION);
			
			context.startService(intent);
			
		} finally {
			com.finishForcastTransaction(FrcstMtrlgService.class);
		}
	}
}
