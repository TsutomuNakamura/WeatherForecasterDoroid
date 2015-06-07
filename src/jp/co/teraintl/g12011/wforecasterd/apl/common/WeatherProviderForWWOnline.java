package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

/**
 * WorldWeatherOnline 用気象予報プロバイダ
 * @author tsutomu
 *
 */
public class WeatherProviderForWWOnline extends WeatherProvider {
	
	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName();
	
	/** 日本時間ロケール補正(9時間) */
	private final long jaLocaleTime = 1L * 1000L * 60L * 60L * 9L;
	
	/** WorldWeatherOnline 用の日付書式 */
	private final SimpleDateFormat wwonlineDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	/**
	 * コンストラクタ
	 * @param context アプリケーションコンテキスト
	 */
	public WeatherProviderForWWOnline(Context context) {
		super(context);
	}
	
	@Override
	public List<WeatherBean> getWeathers(
			LocationBean monitoringPoint, Context context)throws ClientProtocolException, IOException {
		
		HttpResponse httpResponse = null;
		HttpClientHelper httpClientHelper = HttpClientHelper.getInstance();
		Builder uriBuilder = new Uri.Builder();
		List<WeatherBean> weatherList = new ArrayList<WeatherBean>();
		
		JSONObject jsonData = null;
		JSONObject jsonWeather = null;
		List<String> weatherCodeList = new ArrayList<String>();
		String[] splitedWeatherLine = null;
		
		// http 通信用の文字列構築(気象観測地点の緯度と経度から取得)
		uriBuilder.scheme(context.getString(R.string.url_scheme_http));
		uriBuilder.authority(context.getString(R.string.url_authorigy_free_wwo));
		uriBuilder.path(context.getString(R.string.url_free_wwo_path));
		uriBuilder.appendQueryParameter(context.getString(R.string.url_free_wwo_query_param_key_latlon)
				, monitoringPoint.getLatitude() + "," + monitoringPoint.getLongitude());
		uriBuilder.appendQueryParameter(context.getString(R.string.url_free_wwo_query_param_key_format)
				, context.getString(R.string.url_free_wwo_query_param_value_format_json));
		uriBuilder.appendQueryParameter(context.getString(R.string.url_free_wwo_query_param_key_num_of_days)
				, context.getString(R.string.url_free_wwo_query_param_value_num_of_days_5));
		uriBuilder.appendQueryParameter(context.getString(R.string.url_free_wwo_query_param_key_api_key) 
				, context.getString(R.string.url_free_wwo_query_param_value_api_key));
		
		Log.i(LOG_TAG, "getWeathers(): HTTPリクエスト送信開始[URL=" + uriBuilder.toString() + "]");
		
		// http通信とレスポンスコードの取得
		httpResponse = httpClientHelper.sendRequest(uriBuilder);
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		BufferedReader br = null;
		
		Log.i(LOG_TAG, "getWeathers(): HTTPリクエスト送信完了[Response=" + statusCode + "]");
		
		if (httpResponse == null || statusCode != HttpStatus.SC_OK) {
			
			Log.e(LOG_TAG, "getWeathers(): HTTP通信によるデータ取得に失敗しました[URL="
					+ uriBuilder.toString() + "][Response=" + statusCode + "]");
			return null;
		}
		
		try {
			// Json解析開始
			HttpEntity httpEntity = httpResponse.getEntity();
			
			// Json のdataオブジェクト取得
			jsonData = new JSONObject(EntityUtils.toString(httpEntity)).getJSONObject("data");
			
			JSONArray currentConditionArray = jsonData.getJSONArray("current_condition");
			JSONArray jsonWeatherArray = jsonData.getJSONArray("weather");
			
			// WorldWeatherOnline 天気コードをListに展開する
			br = new BufferedReader(new InputStreamReader(
					context.getResources().getAssets().open(
							context.getString(R.string.assets_data_wwonline_weather_code_ja))));
			String line = null;
			while ((line = br.readLine()) != null) {
				weatherCodeList.add(line);
			}
			
			for (int i = 0; i < jsonWeatherArray.length(); i++) {
				// 天気予報行を初期化
				splitedWeatherLine = null;
				
				// 数日分の気象予報を取得する
				WeatherBean weather = new WeatherBean();
				jsonWeather = jsonWeatherArray.getJSONObject(i); 
				
				// ===== 日時 =====
				weather.setStringDate(jsonWeather.getString("date").replace("-", "/"));
				
				// ===== 曜日 =====
				Calendar cal = Calendar.getInstance();
				cal.setTime(CommonUtils.WEATHERBEAN_DATE_FORMAT.parse(weather.getStringDate()));
				weather.setDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
				
				// ===== データ取得日時 =====
				weather.setAnnounceDate(convertAnnounceDate(
						currentConditionArray.getJSONObject(0).getString("observation_time")));
				
				// ===== 天気 =====
				String weatherCode = jsonWeather.getString("weatherCode");
				
				for (int j = 0; j < weatherCodeList.size(); j++) {
					
					// ファイル内から、一致する天気予報コードの検索
					if (weatherCodeList.get(j).startsWith(weatherCode)) {
						splitedWeatherLine = weatherCodeList.get(j).split(CommonUtils.CSV_DELIMITER, -1);
						
						break;
					}
				}
				if (splitedWeatherLine == null) {
					// 天気予報取得失敗(異常)
					Log.e(LOG_TAG, "getWeathers(): 気象予報コードが発見できませんでした[WeatherCode=" + weatherCode + "]");
					
					// ※将来的要望。。。独自例外を作成し、それを飛ばす
					return null;
				}
				
				// ファイル内の天気(文字列)を取得する
				weather.setWeather(splitedWeatherLine[1]);
				
				// ===== 風向き(英語の頭文字表記) =====
				weather.setWindDirection(jsonWeather.getString("winddir16Point"));
				
				// ===== 風速(km/h) =====
				weather.setWindSpeed(Integer.parseInt(jsonWeather.getString("windspeedKmph")));
				
				// ===== 天気予報アイコンID =====
				// ※将来的対応/日中と夜でアイコンを切り替えるようにする
				weather.setWeatherIconId(convertWeatherIconId(splitedWeatherLine[2]));
				
				// ===== 最高気温 =====
				weather.setHighestTemperature(
						Integer.parseInt(jsonWeather.getString("tempMaxC")));
				
				// ===== 最低気温 =====
				weather.setLowestTemperature(
						Integer.parseInt(jsonWeather.getString("tempMinC")));
				
				// ===== 降水確率(WorldWeatherOnline には無い) =====
				
				
				weatherList.add(weather);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		} finally {
			if (br != null) br.close();
		}
		
		return weatherList;
	}
	
	/**
	 * 引数に指定したアイコン名から、アイコンIDを取得する
	 * @param weatherIconName アイコン名
	 * @return アイコンID
	 */
	private int convertWeatherIconId(String weatherIconName) {
		
		if (weatherIconName.equals("wh_cld_64px.png")) {
			return R.drawable.wh_cld_64px;
		} else if (weatherIconName.equals("wh_cld_occa_ran_64px.png")) {
			return R.drawable.wh_cld_occa_ran_64px;
		} else if (weatherIconName.equals("wh_cld_occa_snw_64px.png")) {
			return R.drawable.wh_cld_occa_snw_64px;
		} else if (weatherIconName.equals("wh_cld_occa_sun_64px.png")) {
			return R.drawable.wh_cld_occa_sun_64px;
		} else if (weatherIconName.equals("wh_cld_occa_ths_64px.png")) {
			return R.drawable.wh_cld_occa_ths_64px;
		} else if (weatherIconName.equals("wh_cld_then_ran_64px.png")) {
			return R.drawable.wh_cld_then_ran_64px;
		} else if (weatherIconName.equals("wh_cld_then_snw_64px.png")) {
			return R.drawable.wh_cld_then_snw_64px;
		} else if (weatherIconName.equals("wh_cld_then_sun_64px.png")) {
			return R.drawable.wh_cld_then_sun_64px;
		} else if (weatherIconName.equals("wh_cld_then_ths_64px.png")) {
			return R.drawable.wh_cld_then_ths_64px;
		} else if (weatherIconName.equals("wh_ran_64px.png")) {
			return R.drawable.wh_ran_64px;
		} else if (weatherIconName.equals("wh_ran_occa_cld_64px.png")) {
			return R.drawable.wh_ran_occa_cld_64px;
		} else if (weatherIconName.equals("wh_ran_occa_snw_64px.png")) {
			return R.drawable.wh_ran_occa_snw_64px;
		} else if (weatherIconName.equals("wh_ran_occa_sun_64px.png")) {
			return R.drawable.wh_ran_occa_sun_64px;
		} else if (weatherIconName.equals("wh_ran_occa_ths_64px.png")) {
			return R.drawable.wh_ran_occa_ths_64px;
		} else if (weatherIconName.equals("wh_ran_then_cld_64px.png")) {
			return R.drawable.wh_ran_then_cld_64px;
		} else if (weatherIconName.equals("wh_ran_then_snw_64px.png")) {
			return R.drawable.wh_ran_then_snw_64px;
		} else if (weatherIconName.equals("wh_ran_then_sun_64px.png")) {
			return R.drawable.wh_ran_then_sun_64px;
		} else if (weatherIconName.equals("wh_ran_then_ths_64px.png")) {
			return R.drawable.wh_ran_then_ths_64px;
		} else if (weatherIconName.equals("wh_snw_64px.png")) {
			return R.drawable.wh_snw_64px;
		} else if (weatherIconName.equals("wh_snw_occa_cld_64px.png")) {
			return R.drawable.wh_snw_occa_cld_64px;
		} else if (weatherIconName.equals("wh_snw_occa_ran_64px.png")) {
			return R.drawable.wh_snw_occa_ran_64px;
		} else if (weatherIconName.equals("wh_snw_occa_sun_64px.png")) {
			return R.drawable.wh_snw_occa_sun_64px;
		} else if (weatherIconName.equals("wh_snw_occa_ths_64px.png")) {
			return R.drawable.wh_snw_occa_ths_64px;
		} else if (weatherIconName.equals("wh_snw_then_cld_64px.png")) {
			return R.drawable.wh_snw_then_cld_64px;
		} else if (weatherIconName.equals("wh_snw_then_ran_64px.png")) {
			return R.drawable.wh_snw_then_ran_64px;
		} else if (weatherIconName.equals("wh_snw_then_sun_64px.png")) {
			return R.drawable.wh_snw_then_sun_64px;
		} else if (weatherIconName.equals("wh_snw_then_ths_64px.png")) {
			return R.drawable.wh_snw_then_ths_64px;
		} else if (weatherIconName.equals("wh_sun_64px.png")) {
			return R.drawable.wh_snw_64px;
		} else if (weatherIconName.equals("wh_sun_occa_cld_64px.png")) {
			return R.drawable.wh_sun_occa_cld_64px;
		} else if (weatherIconName.equals("wh_sun_occa_ran_64px.png")) {
			return R.drawable.wh_sun_occa_ran_64px;
		} else if (weatherIconName.equals("wh_sun_occa_snw_64px.png")) {
			return R.drawable.wh_sun_occa_snw_64px;
		} else if (weatherIconName.equals("wh_sun_occa_ths_64px.png")) {
			return R.drawable.wh_sun_occa_ths_64px;
		} else if (weatherIconName.equals("wh_sun_then_cld_64px.png")) {
			return R.drawable.wh_sun_then_cld_64px;
		} else if (weatherIconName.equals("wh_sun_then_ran_64px.png")) {
			return R.drawable.wh_sun_then_ran_64px;
		} else if (weatherIconName.equals("wh_sun_then_snw_64px.png")) {
			return R.drawable.wh_sun_then_snw_64px;
		} else if (weatherIconName.equals("wh_sun_then_ths_64px.png")) {
			return R.drawable.wh_sun_then_ths_64px;
		} else if (weatherIconName.equals("wh_ths_64px.png")) {
			return R.drawable.wh_ths_64px;
		} else if (weatherIconName.equals("wh_ths_occa_cld_64px.png")) {
			return R.drawable.wh_ths_occa_cld_64px;
		} else if (weatherIconName.equals("wh_ths_occa_ran_64px.png")) {
			return R.drawable.wh_ths_occa_ran_64px;
		} else if (weatherIconName.equals("wh_ths_occa_snw_64px.png")) {
			return R.drawable.wh_ths_occa_snw_64px;
		} else if (weatherIconName.equals("wh_ths_occa_sun_64px.png")) {
			return R.drawable.wh_ths_occa_sun_64px;
		} else if (weatherIconName.equals("wh_ths_then_cld_64px.png")) {
			return R.drawable.wh_ths_then_cld_64px;
		} else if (weatherIconName.equals("wh_ths_then_ran_64px.png")) {
			return R.drawable.wh_ths_then_ran_64px;
		} else if (weatherIconName.equals("wh_ths_then_snw_64px.png")) {
			return R.drawable.wh_ths_then_snw_64px;
		} else if (weatherIconName.equals("wh_ths_then_sun_64px.png")) {
			return R.drawable.wh_ths_then_sun_64px;
		}
		
		// 想定外のデータが取得された場合は-1 を返す
		return -1;
	}
	
	private String convertAnnounceDate(
			String announceDate) throws ParseException, java.text.ParseException {
		long time = System.currentTimeMillis();
		return convertAnnounceDate(announceDate, CommonUtils.SQLITE_DATE_FORMAT.format(time), time);
	}
	
	/**
	 * 発表、又は観測日時をSQLiteDB 保存用の書式に変更する
	 * @param announceDate 発表、又は観測日時。「HH:mm AM/PM」の形式であること
	 * @param stringNowDate 現在時刻。「yyyy-MM-dd HH:mm:ss」の形式であること
	 * @return フォーマット済み発表/観測日時「yyyy/MM/dd」
	 * @throws java.text.ParseException 
	 */
	private String convertAnnounceDate(String announceDate
			, String stringNowDate, long nowLongTime) throws ParseException, java.text.ParseException {
		
		// 多少の誤差を修正するための数字猶予(2時間)
		long roundingTime = 1L * 1000L * 60L * 60L * 2L;

		String[] splittedAnnounceDate = announceDate.split(" ", -1);
		String announce24hFormat = null;
		Calendar cal = null;
		
		// announceDate を24時形式に変更する
		if (splittedAnnounceDate[1].equals("PM")) {
			announce24hFormat = Integer.toString((Integer.parseInt(splittedAnnounceDate[0].substring(0, 2)) + 12))
					+ ":" + splittedAnnounceDate[0].substring(3, 5) + ":00";
		} else {
			announce24hFormat = Integer.parseInt(splittedAnnounceDate[0].substring(0, 2))
					+ ":" + splittedAnnounceDate[0].substring(3, 5) + ":00";
		}
		
		// アナウンス日時と現在時刻の文字列を組み合わせた後、パースしてミリ秒(GMT+09:00)へ
		long nowLongAnonTime = wwonlineDateFormat.parse(
				stringNowDate.substring(0, 10) + " " + announce24hFormat).getTime() + jaLocaleTime;


		if ( nowLongAnonTime >= (nowLongTime + roundingTime) ) {
			// 数時間以上先を進んでいる場合：日跨り誤差発生
			cal = Calendar.getInstance();
			cal.setTime(new Date(nowLongAnonTime));
			cal.add(Calendar.DATE, -1);
			
			return CommonUtils.ANNOUNCE_DATE_FORMAT.format(cal.getTime());

		}
		
		return stringNowDate.split(" ", -1)[0] + " " + announce24hFormat;
	}
}
