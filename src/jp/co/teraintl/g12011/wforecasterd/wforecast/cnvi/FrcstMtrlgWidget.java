package jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi;

import java.io.IOException;
import java.util.List;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.GPSHelper;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherLocationProvider;
import jp.co.teraintl.g12011.wforecasterd.apl.init.TableInitializer;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

/**
 * 気象予報表示ウィジェットクラス。<br />
 * Android 端末のトップスクリーンに気象予報を表示する。
 * @author tsutomu
 */
public class FrcstMtrlgWidget extends AppWidgetProvider {
	
	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName();
	
	/** ウィジェット作成アクション識別子 */
	private final int ACT_WIDGET_CREATE = 0;
	/** ウィジェット削除アクション識別子 */
	private final int ACT_WIDGET_DELETE = 1;
	
	@Override
	public void onEnabled(Context context) {
		
		TableInitializer tableInitializer = new TableInitializer();

		// ※GPSサービス取得 (PresentLocationAnalyzer 内でも呼んでいるが、問題ないか！？)
		GPSHelper.getInstance(context).setLocationManager(
				(LocationManager) context.getSystemService(Activity.LOCATION_SERVICE));
		
		try {
			// DBとテーブルの初期化
			if(tableInitializer.initTableIfNoData(context) > 0)
				Log.i(LOG_TAG, "onEnabled(): DBとテーブルを初期化しました。");				 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpdate(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgets) {
		
		WeatherLocationProvider provider = new WeatherLocationProvider(context);
		
		// ウィジェットリストを作成する
		List<LocationBean> widgets
				= provider.createDefaultWidgetList(appWidgets, context);
		
		// ウィジェットテーブルにウィジェット追加
		try {
			provider.setOrUpdateWidgetsLocale(widgets, context);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			// ===== トランザクション開始 =====
			CommonUtils.getInstance(context).startForcastTransaction(this);
			
			// 作成されたウィジェットのIDをログ出力
			printLogWidgetIds(appWidgets, ACT_WIDGET_CREATE);
			
			// 自分のIDをService に登録する
			Intent intent = getServiceIntent(appWidgets, context);
			
			// 更新対象ウィジェット(widgets)IDの追加
			context.startService(intent);
			
		} finally {
			// ===== トランザクション終了 =====
			CommonUtils.getInstance(context).finishForcastTransaction(this);
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] deletedWidgetIds) {
		
		try {
			CommonUtils.getInstance(context).startForcastTransaction(this);
			// ウィジェット取得地域履歴一覧テーブルからウィジェットを削除
			new WeatherLocationProvider(context).deleteWeatherLocation(deletedWidgetIds, context);	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			CommonUtils.getInstance(context).finishForcastTransaction(this);
		}
		
		// ログ出力
		printLogWidgetIds(deletedWidgetIds, ACT_WIDGET_DELETE);
	}
	
	@Override
	public void onDisabled(Context context) {
		
		// 常駐プロセス(サービスクラス)の停止
		Intent intent = new Intent(context, FrcstMtrlgService.class);
		intent.setAction(FrcstMtrlgService.STOP_ACTION);
		context.startService(intent);		// ※Serviceを停止させる場合もstartService
		
		// ※シングルトンオブジェクトの解放。現状これではできていない様子・・・
		CommonUtils.destroyInstance();
		GPSHelper.destroyInstance();
	}
	
	/**
	 * サービスに渡すintentを取得する
	 * @param appWidgets ウィジェットID配列
	 * @param context アプリケーションコンテキスト
	 * @return インテント
	 */
	private Intent getServiceIntent(int[] appWidgets, Context context) {
		
		Intent intent = new Intent(context, FrcstMtrlgService.class);
		intent.setAction(FrcstMtrlgService.NEW_WIDGET_CREATED_ACTION);

		return intent;
	}
	
	/**
	 * ウィジェットID一覧をログ出力する
	 * @param appWidgets ウィジェット配列
	 */
	private void printLogWidgetIds(int[] appWidgets, int actionType) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < appWidgets.length; i++) {
			sb.append((i < 1? "": ",") + appWidgets[i]);
		}
		if (actionType == ACT_WIDGET_CREATE) {
			Log.i(LOG_TAG, "printLogWidgetIds(): "
					+ "ウィジェットが作成されました[WidgetIds[]=" + sb.toString() + "]");
		} else if (actionType == ACT_WIDGET_DELETE) {
			Log.i(LOG_TAG, "printLogWidgetIds(): "
					+ "ウィジェットが削除されました[WidgetIds[]=" + sb.toString() + "]");
		}
	}
}
