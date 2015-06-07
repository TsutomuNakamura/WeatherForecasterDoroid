package jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherLocationProvider;
import jp.co.teraintl.g12011.wforecasterd.wforecast.model.MonitoringPointCourier;
import jp.co.teraintl.g12011.wforecasterd.wforecast.model.PresentLocationAnalyzer;
import jp.co.teraintl.g12011.wforecasterd.wforecast.model.WeatherForecaster;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * 気象予報取得・解析クラス
 * @author tsutomu
 *
 */
public class FrcstMtrlgAuthority {
	
	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName();
	
	/** 仮保存用地域情報インスタンス */
	private LocationBean tmpLocationBean;
	/** 地域情報インスタンス */
	private LocationBean monitoringPoint;
	/** 共通ユーティリティクラスのインスタンス */
	private CommonUtils com;
	/** 現在日時 */
	private Date currDate;
	
	WeatherLocationProvider provider;

	/**
	 * コンストラクタ
	 * @param context アプリケーションコンテキスト
	 */
	public FrcstMtrlgAuthority(Context context) {
		
		com = CommonUtils.getInstance(context);
		provider = new WeatherLocationProvider(context);
		currDate = new Date();
	}
	
	/**
	 * 現在存在するウィジェット(恒常更新ウィジェット)の中から、
	 * 引数に指定されたウィジェットを更新する。<br />
	 * @param context アプリケーションコンテキスト
	 * @param targetWidgets 更新対象ウィジェット
	 * @throws IOException 入出力例外
	 */
	public void startWeatherForecast(
			List<LocationBean> targetUpdateWidget, Context context) throws IOException {
		
		if (targetUpdateWidget == null || targetUpdateWidget.size() <= 0)
			return;
		
		// ===== トランザクション開始 =====
		// ※将来的にはAspectJ 等を使用して実現したい。
		com.startForcastTransaction(this);
		
		try {
			
			weatherForcast(targetUpdateWidget, context);
			
		} finally {
			// 気象予報取得処理トランザクションのロックを解放する。
			com.finishForcastTransaction(this);
		}
	}
	
	/**
	 * 気象予報取得処理。気象予報を取得し、ウィジェットに反映する。<br />
	 * また気象予報取得後、共通ユーティリティクラスの恒常更新ウィジェットの情報を更新する。
	 * @param widget 気象予報取得処理更新対象ウィジェットの一覧
	 * @param context アプリケーションコンテキスト
	 * @throws IOException 入出力例外
	 */
	private void weatherForcast(List<LocationBean> targetUpdateWidget, Context context) throws IOException {
		
		// ※ 未来対応：処理に失敗したら、処理失敗例外をスローする
		
		// 更新済みウィジェット情報
		List<LocationBean> updatedWidgets = new ArrayList<LocationBean>();
		
		for (LocationBean widget: targetUpdateWidget) {
			
			// 現在地取得
			tmpLocationBean = 
					new PresentLocationAnalyzer().getPresentLocation(context, widget);
			
			// 最寄り気象観測地点取得
			monitoringPoint =
					new MonitoringPointCourier().getNearestMonitoringPoint(context, tmpLocationBean);
			
			// 気象予報取得とウィジェットの更新
			List<WeatherBean> weatherList =
					new WeatherForecaster(context).getWeatherReport(monitoringPoint, context);
			
			if (weatherList == null || weatherList.size() == 0) {
				// 気象データ取得失敗
				// ※未来対応：独自の例外をスローする
				Log.e(LOG_TAG, "weatherForcast(): 気象予報取得処理が失敗しました。");
				return;
			} else {
				// ウィジェットの表示の更新
				updateWeatherWidgetView(weatherList, monitoringPoint, context);
			}
			
			// 共通ユーティリティクラスの恒常更新ウィジェット情報を更新する
			updatedWidgets.add(createUpdatedWidget(widget, monitoringPoint, currDate));
			
		}
		
		// ウィジェット取得地域履歴一覧テーブルの内容を更新する
		// 恒常更新ウィジェットを更新する(新生ウィジェットもこれに含まれる)
		provider.setOrUpdateWidgetsLocale(updatedWidgets, context);
	}

	/**
	 * 更新済みウィジェットを作成する。<br />
	 * 処理の内容としては、引数で渡されたウィジェットのウィジェットIDにて
	 * 気象観測地点をマージしたウィジェットオブジェクト(地域情報Bean)を返却する
	 * @param widget 更新対象ウィジェット
	 * @param monitoringPoint 気象観測地点情報
	 * @return ウィジェット(地域情報格納オブジェクト)
	 */
	private LocationBean createUpdatedWidget(
			LocationBean widget, LocationBean monitoringPoint, Date updateTime) {
		
		// 必要なものだけ更新する。全ては更新しない。
		widget.setId(monitoringPoint.getId());
		widget.setJsonId(monitoringPoint.getJsonId());
		widget.setUpdateCount(widget.getUpdateCount() + 1L);
		widget.setLatitude(monitoringPoint.getLatitude());
		widget.setLongitude(monitoringPoint.getLongitude());
		widget.setAccuracy(monitoringPoint.getAccuracy());
		widget.setCountryNameCode(monitoringPoint.getCountryNameCode());
		widget.setCountryName(monitoringPoint.getCountryName());
		widget.setRegionName(monitoringPoint.getRegionName());
		widget.setAdministrativeAreaName(monitoringPoint.getAdministrativeAreaName());
		widget.setSubAdministrativeAreaName(monitoringPoint.getSubAdministrativeAreaName());
		widget.setLocalityName(monitoringPoint.getLocalityName());
		widget.setVLocalityName(monitoringPoint.getVLocalityName());
		widget.setRssUrl(monitoringPoint.getRssUrl());
		
		widget.setUpdateDate(CommonUtils.SQLITE_DATE_FORMAT.format(updateTime));
		
		return widget;
	}

	/**
	 * ウィジェットの表示を更新する。
	 * @param weatherList 気象予報データ格納Beanリスト
	 * @param monitoringPoint 観測地点
	 * @param context アプリケーションコンテキスト
	 */
	private void updateWeatherWidgetView(List<WeatherBean> weatherList,
			LocationBean monitoringPoint, Context context) {
		
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.frcstmtrlg_widget);
		int widgetId = monitoringPoint.getWidgetId();
		
		// ウィジェット押下時のイベント設定
		Intent intent = new Intent(context, FrcstMtrlgActivity.class);
		intent.putExtra(context.getString(R.string.intent_extra_widget_id), widgetId);
		intent.putExtra(context.getString(R.string.intent_extra_load_last_ref_location_flg), false);
		
		// 各ウィジェットID毎に異なるインテントと認識させる方法setType
		intent.setType(Integer.toString(widgetId));
		
		// Activityのページの遷移履歴(スタック)をクリアする
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		// ※PendingIntent.FLAG_UPDATE_CURRENT を第4引数に設定しても、extra の値が更新されない
		// 例えば手動で地域を変更した後に、ウィジェットをフリックして週間天気予報を
		// 表示させようとすると以前表示していた地域の情報を取ってこようとしてしまう。
		// そのため、週間天気予報Activity 側では、シングルトンインスタンス上のデータへアクセスして
		// 天気予報の表示元データを取得するようにしてくる
		PendingIntent pi = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		WeatherBean nearDayWeatherBean = getNearDayWeather(weatherList);

		try {
			// ウィジェットの内容設定
			// 日本国内と海外とで表示する項目を変更する
			if(monitoringPoint.getCountryNameCode().equals("JP")) {
				rv.setTextViewText(R.id.TextAdministrativeAreaName, monitoringPoint.getAdministrativeAreaName());
			} else {
				// ※ 国コードを表示するのではなく、もっと他の文字で海外を表現したい
				rv.setTextViewText(R.id.TextAdministrativeAreaName, monitoringPoint.getCountryNameCode());
			}
			
			rv.setTextViewText(R.id.TextLocalityName, monitoringPoint.getVLocalityName());
			rv.setTextViewText(R.id.TextTodaysDate, CommonUtils.WIDGET_DATE_FORMAT.format(
					CommonUtils.WEATHERBEAN_DATE_FORMAT.parse(nearDayWeatherBean.getStringDate())));
			rv.setTextViewText(R.id.TextTodaysHighestTemperature, 
					(nearDayWeatherBean.getHighestTemperature() == null || nearDayWeatherBean.getHighestTemperature().equals("")
					? "--": nearDayWeatherBean.getHighestTemperature().toString()) + "℃");
			rv.setTextViewText(R.id.TextTodaysLowestTemperature, 
					(nearDayWeatherBean.getLowestTemperature() == null || nearDayWeatherBean.getLowestTemperature().equals("")
					? "--": nearDayWeatherBean.getLowestTemperature().toString()) + "℃");
			rv.setImageViewResource(R.id.ImageTodaysWeather, nearDayWeatherBean.getWeatherIconId());
			
			rv.setOnClickPendingIntent(R.id.widget_base, pi);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// ウィジェットの更新
		awm.updateAppWidget(widgetId, rv);
		Log.i(LOG_TAG, "ウィジェットの表示を更新しました[WidgetId=" + widgetId + "]");
	}

	/**
	 * 本日、又は明日の天気予報を取得する。<br />
	 * 現在時刻を考慮して、最適な日時の気象予報を取得する。
	 * @param weatherList 気象予報リスト
	 * @return 気象予報データ格納Bean
	 */
	private WeatherBean getNearDayWeather(List<WeatherBean> weatherList) {
		
		long now = System.currentTimeMillis();
		
		for (int i = 0; i < weatherList.size(); i++) {
			if (weatherList.get(i).getStringDate()
					.equals(CommonUtils.WEATHERBEAN_DATE_FORMAT.format(now)) ||
					weatherList.get(i).getStringDate()
					.equals(CommonUtils.WEATHERBEAN_DATE_FORMAT.format(now + 86400000))) {
		
				return weatherList.get(i);
			}
		}
		
		return null;
	}
}
