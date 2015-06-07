package jp.co.teraintl.g12011.wforecasterd.wforecast.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.GPSHelper;
import jp.co.teraintl.g12011.wforecasterd.apl.common.HttpClientHelper;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherLocationProvider;
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

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

/**
 * 現在地解析クラス
 * @author tsutomu
 */
public class PresentLocationAnalyzer {

	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName();

	/**
	 * 現在地を取得する
	 * 
	 * @param context アプリケーションコンテキスト
	 * @param widget 解析元ウィジェット情報
	 * @return 現在地情報
	 */
	public LocationBean getPresentLocation(Context context, LocationBean widget) {

		WeatherLocationProvider historyLocationProvider = new WeatherLocationProvider(context); 
		Builder uriBuilder = new Uri.Builder();
		HttpClientHelper httpClientHelper = HttpClientHelper.getInstance();
		HttpResponse httpResponse = null;
		LocationBean locationBean = null;
		long currentMillis = System.currentTimeMillis();
		
		try {

			if (widget.getUpdateCount() == 0 || !widget.getGpsFlag()) {
				// 更新回数が0回の場合または地域情報が手動登録の場合は
				// 表示スピード優先のため、地域情報を変更しない(デフォルト地域情報)
	
				// 現在のデータを履歴として保存する
				historyLocationProvider.updateWeatherLocation(context, widget);
				Log.i(LOG_TAG, "GPS更新対象のウィジェットではありません。[WidgetId="
						+ widget.getWidgetId() + "]");
				return widget;
			}
			
			Log.i(LOG_TAG, "GPS更新対象のウィジェットです。[WidgetId="
					+ widget.getWidgetId() + "]");
			
			// ※アプリ起動時に1回のみならずここでも呼ぶべきか！？(初期化されやしないか)
			GPSHelper.getInstance(context).setLocationManager(
					(LocationManager) context.getSystemService(Activity.LOCATION_SERVICE));

			// http リクエスト送信時のurl構築
			uriBuilder.scheme(context.getString(R.string.url_scheme_http));
			uriBuilder.authority(context.getString(R.string.url_geo_authority));
			uriBuilder.path(context.getString(R.string.url_geo_path));
			
			uriBuilder.appendQueryParameter(context.getString(R.string.url_geo_queryParamKey_ll)
					, GPSHelper.getInstance(context).getLatitude() + "," + GPSHelper.getInstance(context).getLongitude()
			);
			uriBuilder.appendQueryParameter(
					context.getString(R.string.url_geo_queryParamKey_output)
					, context.getString(R.string.url_geo_queryParamValue_output)
			);
			uriBuilder.appendQueryParameter(
					context.getString(R.string.url_geo_queryParamKey_apikey)
					, GPSHelper.getInstance(context).getAPIKey()
			);
			uriBuilder.appendQueryParameter(
					context.getString(R.string.url_geo_queryParamKey_hl)
					, context.getString(R.string.url_geo_queryParamValue_hl)
			);
			uriBuilder.appendQueryParameter(
					context.getString(R.string.url_geo_queryparamkey_oe)
					, context.getString(R.string.url_geo_queryParamValue_oe)
			);

			// http リクエストを送信する
			Log.i(LOG_TAG, "httpリクエスト送信開始[URL=" + uriBuilder.toString() + "]");
			httpResponse = httpClientHelper.sendRequest(uriBuilder);
			
			// レスポンスコードの取得
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			Log.i(LOG_TAG, "httpリクエスト送信終了[Response=" + statusCode + "]");
			
			if (httpResponse == null || statusCode != HttpStatus.SC_OK) {
				Log.i(LOG_TAG, "逆ジオコーディングによる情報の取得に失敗しました。履歴から現在地を取得します。");
				
				// 取得履歴から現在地を取得し、呼び出し元へ処理を返す
				locationBean = historyLocationProvider.getHistoryLocation(widget.getWidgetId(), context);
				if (locationBean == null) {
					// 存在しない場合は、地域情報を変更しないで返す(※原則このパターンはあり得ない)
					locationBean = widget;
				}
				
				return locationBean;
			}
			
			// JSON解析開始
			HttpEntity httpEntity = httpResponse.getEntity();
			JSONObject rootObject = new JSONObject(EntityUtils.toString(httpEntity));
			JSONArray placemarkArray = rootObject.getJSONArray("Placemark");
			
			int targetIndex    = -1;
			int targetAccuracy = -1;
			int tmpAccuracy    = -1;
			
			// 取得対象となる地名のインデックスをjson オブジェクトの accuracy の値から判断する
			for (int i = 0; i < placemarkArray.length(); i++) {
				switch (tmpAccuracy =
						placemarkArray.getJSONObject(i).getJSONObject("AddressDetails").getInt("Accuracy")) {
				case GPSHelper.ACCURACY_COUNTRY:
				case GPSHelper.ACCURACY_PREFECTURAL:
				case GPSHelper.ACCURACY_COUNTY:
				case GPSHelper.ACCURACY_MUNICIPALITY:
				case GPSHelper.ACCURACY_POSTALCODE:
				case GPSHelper.ACCURACY_CROSSING:
				case GPSHelper.ACCURACY_CITYBLOCK:
					
					// 取得対象となる地名のPrecemark 配列インデックスを更新
					if (targetAccuracy < tmpAccuracy) {
						
						// できるだけ詳細なjson データのindex を設定するようにする
						targetAccuracy = tmpAccuracy;
						targetIndex = i;
					}
					break;
				case GPSHelper.ACCURACY_SUCHANDSUCH:
				case GPSHelper.ACCURACY_ROAD:
				case GPSHelper.ACCURACY_BUILDING:
				default:
					// 何もしない
				}
			}
			
			// 十分な情報が得られない場合、JSONException をスローする
			if (targetIndex == -1)
				throw new JSONException("解析するに十分な情報が取得できませんでした。");
			
			// 適切と判断されたインデックスから、placemarkArray オブジェクトを取得
			JSONObject targetObject = placemarkArray.getJSONObject(targetIndex);

			locationBean = new LocationBean();
			
			// 緯度、経度を設定する
			if (checkJsonObjValueExistence(rootObject, "name")) {
				StringTokenizer st = new StringTokenizer(getJSONObjectValue(rootObject, "name"), ",");
				// 緯度
				locationBean.setLatitude(new Double(st.nextToken()));
				// 経度
				locationBean.setLongitude(new Double(st.nextToken()));
			}
			
			// 取得したデータをBeanに格納
			locationBean.setWidgetId(widget.getWidgetId());
			locationBean.setGpsFlag(widget.getGpsFlag());
			if (checkJsonObjValueExistence(targetObject, "JSONId"))
				locationBean.setJsonId(getJSONObjectValue(targetObject, "JSONId"));
			if (checkJsonObjValueExistence(targetObject, "Accuracy"))
				locationBean.setAccuracy(Integer.parseInt(getJSONObjectValue(targetObject, "Accuracy")));
			if (checkJsonObjValueExistence(targetObject, "CountryNameCode"))
				locationBean.setCountryNameCode(getJSONObjectValue(targetObject, "CountryNameCode"));
			if (checkJsonObjValueExistence(targetObject, "CountryName"))
				locationBean.setCountryName(getJSONObjectValue(targetObject, "CountryName"));
			if (checkJsonObjValueExistence(targetObject, "AdministrativeAreaName"))
				locationBean.setAdministrativeAreaName(getJSONObjectValue(targetObject, "AdministrativeAreaName"));
			if (checkJsonObjValueExistence(targetObject, "SubAdministrativeAreaName"))
				locationBean.setSubAdministrativeAreaName(getJSONObjectValue(targetObject, "SubAdministrativeAreaName"));
			if (checkJsonObjValueExistence(targetObject, "LocalityName"))
				locationBean.setLocalityName(getJSONObjectValue(targetObject, "LocalityName"));
			locationBean.setUpdateDate(CommonUtils.SQLITE_DATE_FORMAT.format(currentMillis));
			locationBean.setRegistrationDate(CommonUtils.SQLITE_DATE_FORMAT.format(currentMillis));
						
			// 取得した地域情報をログ出力
			Log.i(LOG_TAG, "locationBean:[WidgetId=" + locationBean.getWidgetId() + "][JsonId=" + locationBean.getJsonId() + "],[Latitude="
					+ locationBean.getLatitude() + "],[Longitude=" + locationBean.getLongitude() + "],[Accuracy="
					+ locationBean.getAccuracy() + "],[CountryNameCode=" + locationBean.getCountryNameCode() + "],[CountryName="
					+ locationBean.getCountryName() + "],[AdministrativeAreaName=" + locationBean.getAdministrativeAreaName() + "],[SubAdministrativeAreaName="
					+ locationBean.getSubAdministrativeAreaName() + "],[LocalityName=" + locationBean.getLocalityName() + "],[UpdateDate="
					+ locationBean.getRegistrationDate() + "]");
			
			// 取得したデータを履歴として更新する
			historyLocationProvider.updateWeatherLocation(context, locationBean);
			
		} catch (MissingResourceException e) {
			locationBean = historyLocationProvider.getHistoryLocation(widget.getWidgetId(), context);
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			locationBean = historyLocationProvider.getHistoryLocation(widget.getWidgetId(), context);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			locationBean = historyLocationProvider.getHistoryLocation(widget.getWidgetId(), context);
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
		return locationBean;
	}
	
	/**
	 * 第1引数で指定したJSON オブジェクトに第2引数で指定したオブジェクトや要素が存在するかをチェックする。
	 * @param obj 検索元JSONオブジェクト
	 * @param objectName 検索する要素またはオブジェクト等
	 * @return 存在判定結果
	 * @throws JSONException JSON例外
	 */
	private boolean checkJsonObjValueExistence(
			JSONObject obj, String elementName)throws JSONException {
		boolean ret = false;
		try {
			if (elementName.equals("name")) {
				
				// 緯度、経度情報の存在確認
				ret = obj.has("name");
				
			} else if (elementName.equals("JSONId")) {
				
				// JSONID の存在確認
				ret = obj.has("id");
				
			} else if (elementName.equals("Accuracy")) {
				
				// 正確さ(Accuracy)の存在確認
				ret = (obj.getJSONObject("AddressDetails").has("Accuracy"));
				
			} else if (elementName.equals("CountryNameCode")) {
				
				// 国コード(CountryNameCode)の存在確認
				ret = (obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.has("CountryNameCode"));
				
			} else if (elementName.equals("CountryName")) {
				
				// 国名(CountryName)の存在確認
				ret = (obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.has("CountryName"));
				
			} else if (elementName.equals("AdministrativeAreaName")) {
				
				// 都道府県名(AdministrativeAreaName)の存在確認
				ret = (obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.has("AdministrativeAreaName"));
				
			} else if (elementName.equals("SubAdministrativeAreaName")) {
				
				// 群などの集落名(SubAdministrativeAreaName)の存在確認
				ret = (obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getJSONObject("SubAdministrativeArea")
						.has("SubAdministrativeAreaName"));
				
			} else if (elementName.equals("LocalityName") &&
					checkJsonObjValueExistence(obj, "SubAdministrativeAreaName")) {
				
				// 市区町村名(LocalityName)[群等の集落の中に【ある】]の存在確認
				ret = (obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getJSONObject("SubAdministrativeArea")
						.getJSONObject("Locality").has("LocalityName"));
				
			} else if (elementName.equals("LocalityName") 
					) {
				
				// 市区町村名(LocalityName)[群等の集落の中に【ない】]の存在確認
				ret = (obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getJSONObject("Locality").has("LocalityName"));
				
			}
		} catch (JSONException e) {
			// 検索対象の要素までの途中の要素等が発見されなかった場合
			// false を返す
		}
				
		return ret;
	}
	
	/**
	 * 第1引数で指定したJSONオブジェクトから第2引数で指定したオブジェクトや要素を取得する。
	 * @param obj 検索元JSONオブジェクト
	 * @param objectName 
	 * @return 取得した要素値
	 * @throws JSONException JSON例外
	 */
	private String getJSONObjectValue(
			JSONObject obj, String elementName) throws JSONException {
		
		String ret = null;
		
		try {
			if (elementName.equals("name")) {
				
				// 緯度、経度の値取得
				ret = obj.getString("name");
				
			} else if (elementName.equals("JSONId")) {
				
				// JSONID の値取得
				ret = (obj.getString("id"));
				
			}
			else if (elementName.equals("Accuracy")) {
				
				// 正確さ(Accuracy)の取得
				ret = (obj.getJSONObject("AddressDetails").getString("Accuracy"));
				
			} else if (elementName.equals("CountryNameCode")) {
				
				// 国コード(CountryNameCode)の値取得
				ret = obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getString("CountryNameCode");
				
			} else if (elementName.equals("CountryName")) {
				
				// 国名(CountryName)の値取得
				ret = obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getString("CountryName");
				
			} else if (elementName.equals("AdministrativeAreaName")) {
				
				// 都道府県名(AdministrativeAreaName)の値取得
				ret = obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getString("AdministrativeAreaName");
				
			} else if (elementName.equals("SubAdministrativeAreaName")) {
				
				// 群などの集落名(SubAdministrativeAreaName)の値取得
				ret = obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getJSONObject("SubAdministrativeArea")
						.getString("SubAdministrativeAreaName");
				
			} else if ( (elementName.equals("LocalityName") == true) &&
					checkJsonObjValueExistence(obj, "SubAdministrativeAreaName") ) {
				
				// 市区町村名(LocalityName)[群等の集落の中に【ある】]の値取得
				ret = obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getJSONObject("SubAdministrativeArea")
						.getJSONObject("Locality").getString("LocalityName");
				
			} else if ((elementName.equals("LocalityName") == true)) {
				
				// 市区町村名(LocalityName)[群等の集落の中に【ない】]の値取得
				ret = obj.getJSONObject("AddressDetails").getJSONObject("Country")
						.getJSONObject("AdministrativeArea")
						.getJSONObject("Locality").getString("LocalityName");
				
			}			
		} catch (JSONException e) {
			// null を返す
		}
		
		return ret;
	}	
}
