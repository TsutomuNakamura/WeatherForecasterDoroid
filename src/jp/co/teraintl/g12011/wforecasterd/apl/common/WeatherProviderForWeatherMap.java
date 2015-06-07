package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * WeatherMap (ひとくちRSS)プロバイダ<br />
 * WeatherMap (ひとくちRSS) のデータを解析する
 * @author tsutomu
 *
 */
public class WeatherProviderForWeatherMap extends WeatherProvider {
	
	/** 全国の気象解説文エリアフラグ */
	public static final int CONTENT_WHOLE_OF_COUNTRY = 1;
	
	/** 都道府県の気象解説文エリアフラグ */
	public static final int CONTENT_ADMINISTRATIVE_AREA = 2;
	
	/** 地域の週間天気予報文エリアである場合 */
	public static final int CONTENT_LOCALITY_WEEKLY_WEATHER = 6;
	
	/** ログ出力用タグ */
	private final String LOG_TAG = this.getClass().getName();
	
	/** itemタグ名 */
	private final String TAG_ITEM = "item";
	/** pubDateタグ名 */
	private final String TAG_PUB_DATE = "pubDate";
	/** descriptionタグ名 */
	private final String TAG_DESCRIPTION = "description";
	
	/** forecastタグ名 */
	private final String TAG_FORECAST = "forecast";
	/** term属性名 */
	private final String ATTR_TERM = "term";
	/** week属性値 */
	private final String VAL_WEEK = "week";
	/** announce属性名 */
	private final String ATTR_ANNOUNCE = "announce";

	/** contentタグ名 */
	private final String TAG_CONTENT = "content";
	/** date属性名 */
	private final String ATTR_DATE = "date";
	/** wday属性名 */
	private final String ATTR_WDAY = "wday";

	/** weatherタグ名 */
	private final String TAG_WEATHER = "weather";
	
	/** temperatureタグ名 */
	private final String TAG_TEMPERATURE = "temperature";
	/** maxタグ(最高気温)名 */
	private final String TAG_MAX = "max";
	/** minタグ(最低気温)名 */
	private final String TAG_MIN = "min";

	/** rainfall タグ名 */
	private final String TAG_RAINFALL = "rainfall";
	/** probタグ名 */
	private final String TAG_PROB = "prob";
	/** hour属性名 */
	private final String ATTR_HOUR = "hour";
	
	// 晴れを意味する語句
	private final String[] PHRASE_SUNNY = {"晴れ", "快晴", "晴", "はれ"};
	// 曇りを意味する語句
	private final String[] PHRASE_CLOUDY = {"曇り", "曇", "くもり"};
	// 雨を意味する語句
	private final String[] PHRASE_RAIN = {"雨", "あめ"};
	// 雪を意味する語句
	private final String[] PHRASE_SNOW = {"雪", "ゆき", "霙", "みぞれ", "吹雪", "ふぶき"};
	// 雷雨を意味する語句
	private final String[] PHRASE_THUNDERSTORM = {"雷雨", "雷", "らいう", "かみなり"};
	// 天気を意味する語句の配列を格納したmap
	private List<String[]> weatherPhrases = new ArrayList<String[]>();
	
	// 天気予報接続詞「時々」及び「所により」
	private final String[] PHRASE_OCCASIONALLY = {"時々", "時時", "時折", "ときどき", "ときおり", "所により", "ところにより", "時", "所"};
	// 天気予報接続詞「後」
	private final String[] PHRASE_AFTER = {"後ち", "のち", "後"};
	// 接続詞を意味する語句の配列を格納したmap
	private List<String[]> conjunctionPhrases = new ArrayList<String[]>();
	
	// 単純化した天気文字列一覧
	private String simpleSunStr = "晴";
	private String simpleCldStr = "曇";
	private String simpleRanStr = "雨";
	private String simpleSnwStr = "雪";
	private String simpleThsStr = "雷";
	
	// 単純化した接続詞文字列一覧
	private String simpleAfterStr = "後";
	private String simpleOccaStr = "時";
	
	CommonUtils com;
	
	public WeatherProviderForWeatherMap(Context context) {
		super(context);
		
		com = CommonUtils.getInstance(context);
		
		// 天気を意味する語句の配列を格納するArrayを初期化【追加順は固定。守ること】
		weatherPhrases.add(PHRASE_SUNNY);
		weatherPhrases.add(PHRASE_CLOUDY);
		weatherPhrases.add(PHRASE_RAIN);
		weatherPhrases.add(PHRASE_SNOW);
		weatherPhrases.add(PHRASE_THUNDERSTORM);
		
		// 接続詞を意味する語句の配列を格納するArrayを初期化【追加順は固定】
		conjunctionPhrases.add(PHRASE_AFTER);				// 「後」を意味する接続詞
		conjunctionPhrases.add(PHRASE_OCCASIONALLY);		// 「時々」を意味する接続詞
	}
	
	@Override
	public List<WeatherBean> getWeathers(LocationBean monitoringPoint, Context context)
			throws MalformedURLException, IOException, XmlPullParserException{
		
		Log.i(LOG_TAG, "getWeathers():start http request[URL=" + monitoringPoint.getRssUrl() + "]");
		List<WeatherBean> resultBeanList = new WeatherProviderForWeatherMap(context).parseWeathers(
				HttpClientHelper.getInstance().getConnectionStream(monitoringPoint.getRssUrl()));
		Log.i(LOG_TAG, "getWeathers():finish http request[URL=" + monitoringPoint.getRssUrl() + "]");
		
		// 天気情報解析メソッドの呼び出しとreturn
		return resultBeanList;
	}
	
	/**
	 * 気象予報リストを解析する
	 * @param inStream 気象予報取得接続先のストリーム
	 * @return 気象予報リスト
	 * @throws XmlPullParserException XML解析例外
	 * @throws IOException 入出力例外
	 */
	private ArrayList<WeatherBean> parseWeathers(InputStream inStream) throws XmlPullParserException, IOException {		
		
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, null);
		ResourceBundle bundle = com.getGlobalResourceBundle();
		
		int eventType = parser.getEventType();
		WeatherBean currentItem = null;						// 現在解析中の気象予報データ
		ArrayList<WeatherBean> resultList = null;			// 結果リスト
		String announceDate = null;							// 発表日時
		
		String tmpStr1 = null;								// 退避用仮変数1
		String tmpStr2 = null;								// 退避用仮変数1
		int tmpIconId = 0;									// 退避用仮IconId
		Integer tmpTemperature = null;						// 退避用仮気温
		
		int probCounter = 0;								// 降水確率設定用カウンタ
		

		// ※同じタグ名でタグがネストされていた場合は正常動作をサポートしない
		boolean itemFlag = false;							// アイテムタグ内侵入フラグ
		boolean wmForecastWeekFlag = false;				// 週間天気予報タグ内侵入フラグ
		boolean wmContentFlag = false;						// 天気予報コンテンツタグ内侵入フラグ
		boolean wmTemperatureFlag = false;					// 気温情報タグ内侵入フラグ
		boolean wmRainfallFlag = false;					// 降水情報タグ内侵入フラグ
		int itemFlagCount = 0;								// アイテムタグ内侵入回数
		int weatherBeanCount = 0;							// WeatherBean生成回数

		String nameSpaceWmPrefix = bundle.getString("rss_ns_uri_pfx_wm");
		String currentNameSpaceUriWm = null;
		String currentNameSpaceUri = null;
		String currentTag = null;
		String wholeOfCommunityWeatherCommentPubDate = null;
		String wholeOfCommunityWeatherComment = null;
		String administrativeAreaWeatherCommentPubDate = null;
		String administrativeAreaWeatherComment = null;
		
		// rssを最後まで読み込む
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch(eventType) {
			case XmlPullParser.START_TAG:								// タグ開始イベント
				currentNameSpaceUriWm =
						parser.getNamespace(nameSpaceWmPrefix) == null
						? "(\"currentNameSpaceUriWm\" is no data)"
						: parser.getNamespace(nameSpaceWmPrefix);		// 名前空間プレフィックス「wm」のURI取得
				currentNameSpaceUri = 
						parser.getNamespace() == null
						? "(\"currentNameSpaceUri\" is no data)"
						: parser.getNamespace();						// 名前空間URIの取得
				currentTag =
						parser.getName() == null
						? "(\"currentTag\" is no data)"
						: parser.getName();								// タグ名を取得
				
				if (itemFlag) {
					
					// アイテムタグ内である場合。<item>
					if (itemFlagCount == WeatherProviderForWeatherMap.CONTENT_WHOLE_OF_COUNTRY) {
						
						// 一つ目のitem (全国の気象予報コンテンツ内)である場合
						if (currentTag.equals(TAG_PUB_DATE)) {
							
							// 全国の天気解説の発行日時を設定する
							wholeOfCommunityWeatherCommentPubDate = parser.nextText();
						} else if (currentTag.equals(TAG_DESCRIPTION)) {
							
							// 全国の天気解説文を設定する
							
							wholeOfCommunityWeatherComment = formatComment(parser.nextText());
						}
					} else if (itemFlagCount == WeatherProviderForWeatherMap.CONTENT_ADMINISTRATIVE_AREA) {
						
						// 二つ目のitem (都道府県の気象予報コンテンツ)である場合
						if (currentTag.equals(TAG_PUB_DATE)) {
							
							// 都道府県の気象予報解説の発行日時を設定する
							administrativeAreaWeatherCommentPubDate = parser.nextText();
						} else if (currentTag.equals(TAG_DESCRIPTION)) {
							
							// 都道府県の天気解説文を設定する
							administrativeAreaWeatherComment = formatComment(parser.nextText());
						}
					} else if (itemFlagCount == WeatherProviderForWeatherMap.CONTENT_LOCALITY_WEEKLY_WEATHER) {
						
						// 三つ目のitem (地域の週間天気予報コンテンツ)である場合
						if (currentNameSpaceUri.equals(currentNameSpaceUriWm)) {
							
							// 名前空間が「wm」である場合
							if (currentTag.equals(TAG_FORECAST)
									&& wmForecastWeekFlag != true) {
								
								if ((tmpStr1 = parser.getAttributeValue(null, ATTR_TERM)) != null &&
										tmpStr1.equals(VAL_WEEK)) {
									
									// 週間天気予報のタグである場合。<wm:forecast>
									wmForecastWeekFlag = true;
									resultList = new ArrayList<WeatherBean>();
									if ((tmpStr2 = parser.getAttributeValue(null, ATTR_ANNOUNCE)) != null)
										announceDate = tmpStr2;
								}
							} else if (currentTag.equals(TAG_CONTENT)
									&& wmForecastWeekFlag == true
									&& wmContentFlag != true) {
								// <wm:forecast>内の天気情報のタグである場合。<wm:content>
								currentItem = new WeatherBean();
								tmpStr1 = com.formatStringDate(parser.getAttributeValue(null, ATTR_DATE));	// 予報日付
								tmpStr2 = parser.getAttributeValue(null, ATTR_WDAY);					// 予報曜日
								currentItem.setStringDate(tmpStr1);
								currentItem.setDayOfWeek(com.getCalendarWeek(tmpStr2));
								currentItem.setAnnounceDate(announceDate);							// 発表日時
								
								// 先頭のBeanにのみ全国、都道府県の気象解説を設定する
								if (weatherBeanCount <= 0) {
									currentItem.setWholeOfCommunityWeatherCommentPubDate(wholeOfCommunityWeatherCommentPubDate);
									currentItem.setWholeOfCommunityWeatherComment(wholeOfCommunityWeatherComment);
									currentItem.setAdministrativeAreaWeatherCommentPubDate(administrativeAreaWeatherCommentPubDate);
									currentItem.setAdministrativeAreaWeatherComment(administrativeAreaWeatherComment);
									weatherBeanCount++;
									
								}
								
								wmContentFlag = true;
							} else if (currentTag.equals(TAG_WEATHER)
									&& wmForecastWeekFlag == true
									&& wmContentFlag == true) {
								// <wm:content>内の天気のタグである場合<wm:weather>
								tmpStr1 = parser.nextText();								// 気象文字列取得
								tmpIconId = assignWeatherIconId(tmpStr1); 					// ICON ID割当
								currentItem.setWeather(tmpStr1);						// 気象文字列
								currentItem.setWeatherIconId(tmpIconId);				// 気象アイコンID
							} else if (currentTag.equals(TAG_TEMPERATURE)
									&& wmForecastWeekFlag == true
									&& wmContentFlag == true) {
								// <wm:content>内の気温情報のタグである場合<wm:temperature>
								wmTemperatureFlag = true;
							} else if (currentTag.equals(TAG_MAX)
									&& wmForecastWeekFlag == true
									&& wmContentFlag == true
									&& wmTemperatureFlag == true) {
								// <wm:content>内の<wm:temperature>内の最高気温情報のタグである場合</wm:max>
								tmpTemperature = convertTemperature(parser.nextText(), true);
								currentItem.setHighestTemperature(tmpTemperature);
							} else if (currentTag.equals(TAG_MIN)
									&& wmForecastWeekFlag == true
									&& wmContentFlag == true
									&& wmTemperatureFlag == true) {
								// <wm:content>内の<wm:temperature>内の最低気温情報のタグである場合</wm:min>
								tmpTemperature = convertTemperature(parser.nextText(), false);
								currentItem.setLowestTemperature(tmpTemperature);
							} else if (currentTag.equals(TAG_RAINFALL)
									&& wmForecastWeekFlag == true
									&& wmContentFlag == true) {
								// <wm:content>内の降雨情報のタグである場合<wm:rainfall>
								wmRainfallFlag = true;
							} else if (currentTag.equals(TAG_PROB)
									&& wmForecastWeekFlag == true
									&& wmContentFlag == true
									&& wmRainfallFlag == true) {
								// <wm:content>内の<wm:rainfall>内の降水確率のタグである場合<wm:prob>
								tmpStr1 = parser.getAttributeValue(null, ATTR_HOUR);
								tmpStr2 = parser.nextText();
								
								currentItem.putChanceOfRain((String)(tmpStr1 != null || tmpStr1 != ""
										?tmpStr1 : probCounter++), Integer.parseInt(tmpStr2));
							}
						}
					}
				}
				
				if (currentTag.equals(TAG_ITEM) && !itemFlag) {
					
					// タグが<item> の場合
					itemFlag = true;
					itemFlagCount++;
				}
				
				break;
				
			case XmlPullParser.END_TAG:										// タグ終了イベント
				currentNameSpaceUriWm =
						parser.getNamespace(nameSpaceWmPrefix) == null
						? "(\"currentNameSpaceUriWm\" is no data)"
						: parser.getNamespace(nameSpaceWmPrefix);
				currentNameSpaceUri = 
						parser.getNamespace() == null
						? "(\"currentNameSpaceUri\" is no data)"
						: parser.getNamespace();
				currentTag =
						parser.getName() == null
						? "(\"currentTag\" is no data)"
						: parser.getName();
				
				
				if (currentNameSpaceUri.equals(currentNameSpaceUriWm)) {
					// 名前空間が「wm」である場合
					if (currentTag.equals(TAG_FORECAST)
							//&& parser.getAttributeValue(null, ATTR_TERM).equals(VAL_WEEK)
							&& wmForecastWeekFlag == true) {
						// 週間天気予報の終了タグである場合。</wm:forecast>
						wmForecastWeekFlag = false;
					} else if (currentTag.equals(TAG_CONTENT)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true) {
						// <wm:forecast>内の天気情報の終了タグである場合。</wm:content>
						resultList.add(currentItem);
						wmContentFlag = false;
					} else if (currentTag.equals(TAG_WEATHER)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true) {
						// <wm:content>内の天気の終了タグである場合</wm:weather>
					} else if (currentTag.equals(TAG_TEMPERATURE)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true
							&& wmTemperatureFlag == true) {
						// <wm:content>内の気温情報の終了タグである場合</wm:temperature>
						wmTemperatureFlag = false;
					} else if (currentTag.equals(TAG_MAX)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true
							&& wmTemperatureFlag == true) {
						// <wm:content>内の<wm:temperature>内の最高気温情報の終了タグである場合</wm:max>
					} else if (currentTag.equals(TAG_MIN)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true
							&& wmTemperatureFlag == true) {
						// <wm:content>内の<wm:temperature>内の最低気温情報の終了タグである場合</wm:min>
					} else if (currentTag.equals(TAG_RAINFALL)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true
							&& wmRainfallFlag == true) {
						// <wm:content>内の降雨情報の終了タグである場合</wm:rainfall>
						wmRainfallFlag = false;
					} else if (currentTag.equals(TAG_PROB)
							&& wmForecastWeekFlag == true
							&& wmContentFlag == true
							&& wmRainfallFlag == true) {
						// <wm:content>内の<wm:rainfall>内の降水確率の終了タグである場合</wm:prob>
					}
				}
				
				if (currentTag.equals(TAG_ITEM) && itemFlag) {
					
					// タグが</item>の場合
					itemFlag = false;
				}
				break;
			}
			
			// 次のタグまで進む
			eventType = parser.next();
		}
		
		return resultList;
	}

	/**
	 * 気象から、気象予報アイコンのpath を割当をする
	 * @param weather 気象予報アイコンId
	 * @return 気象予報アイコンpathのID。取得失敗時は-1
	 */
	private int assignWeatherIconId(String weatherStr) {
		
		String[] simpleWeather = new String[3];
		
		// 先頭側の天気を解析する
		simpleWeather[0] = analyzeWeatherStringFront(weatherStr);
		// 解析失敗
		if (simpleWeather[0] == null) return -1;
		
		// 接続詞を解析する
		simpleWeather[1] = analyzeConjectionString(weatherStr);
		
		// 後方側の天気を解析する
		if (simpleWeather[1] != null) {
			simpleWeather[2] = analyzeWeatherStringTail(weatherStr);
			// 解析失敗
			if (simpleWeather[2] == null) return -1;
		}
		
		if (simpleWeather[0] == simpleSunStr) {
			// 先頭が晴れの場合
			if (simpleWeather[1] == null) {
				return R.drawable.wh_sun_64px;						// 晴れ
			} else if (simpleWeather[1] == simpleAfterStr) {
				// 接続詞が「後」の場合
				if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_sun_then_cld_64px;			// 晴れ後曇り
				else if (simpleWeather[2] == simpleRanStr)
					return R.drawable.wh_sun_then_ran_64px;			// 晴れ後雨
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_sun_then_snw_64px;			// 晴れ後雪
				else
					return R.drawable.wh_sun_then_ths_64px;			// 晴れ後雷雨
			} else {
				// 接続詞がそれ以外(「時々」)の場合
				if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_sun_occa_cld_64px;			// 晴れ時々曇り
				else if (simpleWeather[2] == simpleRanStr)
					return R.drawable.wh_sun_occa_ran_64px;			// 晴れ時々雨
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_sun_occa_snw_64px;			// 晴れ時々雪
				else
					return R.drawable.wh_sun_occa_ths_64px;			// 晴れ時々雷雨
			}
		} else if (simpleWeather[0] == simpleCldStr) {
			// 先頭が曇りの場合
			if (simpleWeather[1] == null) {
				return R.drawable.wh_cld_64px;						// 曇り
			} else if (simpleWeather[1] == simpleAfterStr) {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_cld_then_sun_64px;			// 曇り後晴れ
				else if (simpleWeather[2] == simpleRanStr)
					return R.drawable.wh_cld_then_ran_64px;			// 曇り後雨
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_cld_then_snw_64px;			// 曇り後雪
				else
					return R.drawable.wh_cld_then_ths_64px;			// 曇り後雷雨
			} else {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_cld_occa_sun_64px;			// 曇り時々晴れ
				else if (simpleWeather[2] == simpleRanStr)
					return R.drawable.wh_cld_occa_ran_64px;			// 曇り時々雨
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_cld_occa_snw_64px;			// 曇り時々雪
				else
					return R.drawable.wh_cld_occa_ths_64px;			// 曇り時々雷雨
			}
			
		} else if (simpleWeather[0] == simpleRanStr) {
			// 先頭が雨の場合
			if (simpleWeather[1] == null) {
				return R.drawable.wh_ran_64px;						// 雨
			} else if (simpleWeather[1] == simpleAfterStr) {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_ran_then_sun_64px;			// 雨後晴れ
				else if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_ran_then_cld_64px;			// 雨後曇り
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_ran_then_snw_64px;			// 雨後雪
				else
					return R.drawable.wh_ran_then_ths_64px;			// 雨後雷雨
			} else {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_ran_occa_sun_64px;			// 雨時々晴れ
				else if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_ran_occa_cld_64px;			// 雨時々曇り
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_ran_occa_snw_64px;			// 雨時々雪
				else
					return R.drawable.wh_ran_occa_ths_64px;			// 雨時々雷雨
			}
			
		} else if (simpleWeather[0] == simpleSnwStr) {
			// 先頭が雪の場合
			if (simpleWeather[1] == null) {
				return R.drawable.wh_snw_64px;						// 雪
			} else if (simpleWeather[1] == simpleAfterStr) {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_snw_then_sun_64px;			// 雪後晴れ
				else if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_snw_then_cld_64px;			// 雪後曇り
				else if (simpleWeather[2] == simpleRanStr)
					return R.drawable.wh_snw_then_ran_64px;			// 雪後雨
				else
					return R.drawable.wh_snw_then_ths_64px;			// 雪後雷雨
			} else {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_snw_occa_sun_64px;			// 雪時々晴れ
				else if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_snw_occa_cld_64px;			// 雪時々曇り
				else if (simpleWeather[2] == simpleRanStr)
					return R.drawable.wh_snw_occa_ran_64px;			// 雪時々雨
				else
					return R.drawable.wh_snw_occa_ths_64px;			// 雪時々雷雨
			}
		} else {
			// それ以外(先頭が雷の場合)
			if (simpleWeather[1] == null) {
				return R.drawable.wh_ths_64px;
			} else if (simpleWeather[1] == simpleAfterStr) {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_ths_then_sun_64px;			// 雷雨後晴れ
				else if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_ths_then_cld_64px;			// 雷雨後曇り
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_ths_then_ran_64px;			// 雷雨後雨
				else
					return R.drawable.wh_ths_then_snw_64px;			// 雷雨後雪
			} else {
				if (simpleWeather[2] == simpleSunStr)
					return R.drawable.wh_ths_occa_sun_64px;			// 雷雨後晴れ
				else if (simpleWeather[2] == simpleCldStr)
					return R.drawable.wh_ths_occa_cld_64px;			// 雷雨後曇り
				else if (simpleWeather[2] == simpleSnwStr)
					return R.drawable.wh_ths_occa_ran_64px;			// 雷雨後雨
				else
					return R.drawable.wh_ths_occa_snw_64px;			// 雷雨後雪
			}
		}
		
		// ※到達不能エリア
	}
	
	/**
	 * 天気予報文字列の先頭の天気を解析する。
	 * @param weatherString 解析対象天気予報文字列
	 * @return 解析結果文字列。「晴」、「曇」・・・など
	 */
	private String analyzeWeatherStringFront(String weatherStr) {
		
		String[] tmpFrases = null;
		
		// 文字列の先頭側の天気を解析する
		for(int i = 0; i < weatherPhrases.size(); i++) {
			for (int j = 0; j < (tmpFrases = weatherPhrases.get(i)).length; j++) {

				// 天気をあらわす文字列が発見された場合、シンプル化した文字列を返す
				if (weatherStr.startsWith(tmpFrases[j]))
					return getSimpleWeatherStr(tmpFrases);
			}
		}
		
		// 何も見つからなかった場合
		return null;
	}
	
	private String analyzeWeatherStringTail(String weatherStr) {
		
		String[] tmpFrases = null;
		
		// 文字列の後尾側の天気を解析する
		for (int i = 0; i < weatherPhrases.size(); i++) {
			for (int j = 0; j < (tmpFrases = weatherPhrases.get(i)).length; j++) {

				// 天気をあらわす文字列が発見された場合、シンプル化した文字列を返す
				if(weatherStr.endsWith(tmpFrases[j]))
					return getSimpleWeatherStr(tmpFrases);
			}
		}
		
		// 何も見つからなかった場合
		return null;
	}
	
	private String analyzeConjectionString(String weatherStr) {
		String[] tmpFrases = null;
		
		// 文字列の接続詞を解析する
		for (int i = 0; i < conjunctionPhrases.size(); i++) {
			for (int j = 0; j < (tmpFrases = conjunctionPhrases.get(i)).length; j++) {
				if(weatherStr.indexOf(tmpFrases[j]) != -1) {
					// 接続詞を表す文字列が発見された場合
					
					if (tmpFrases == PHRASE_AFTER) {
						// 「後」を意味する接続詞の場合
						return simpleAfterStr;
					} else {
						// それ以外(「時々」)を意味する接続詞の場合
						return simpleOccaStr;
					}
				}
			}
		}
		
		// 何も見つからなかった場合
		return null;
	}
	
	/**
	 * 気象予報文字列をシンプル化する
	 * @param tmpFrases 気象予報文字列
	 * @return シンプル化した気象予報文字列
	 */
	private String getSimpleWeatherStr(String[] tmpFrases) {
		if (tmpFrases == PHRASE_SUNNY) {
			// 「晴」を意味する天気の場合
			return simpleSunStr;
		}else if (tmpFrases == PHRASE_RAIN) {
			// 「雨」を意味する天気の場合
			return simpleRanStr;
		} else if (tmpFrases == PHRASE_SNOW) {
			// 「雪」を意味する天気の場合
			return simpleSnwStr;
		} else if (tmpFrases == PHRASE_THUNDERSTORM) {
			// 「雷雨」を意味する天気の場合
			return simpleThsStr;
		} else {
			// それ以外「曇」を意味する天気の場合
			return simpleCldStr;
		}
	}
	
	/**
	 * 最高気温、最低気温文字列を表示用に変換する
	 * @param targetString 最高/最低気温文字列
	 * @param highestFlag 最高気温フラグ(false を指定した場合は最低気温)
	 * @return 変換済み気温文字列
	 */
	private Integer convertTemperature(String targetString, boolean highestFlag) {
		try {
			return Integer.parseInt(targetString);
		} catch (NumberFormatException e) {
			// ※将来的希望...朝頃には、本日の天気の最低気温が表示されなくなってしまうので、
			// バックアップから気温を取得するように変更したい
			
			// null または数字以外が含まれていた場合null を返す
			return null;
		}
	}
	
	/**
	 * 気象予報解説の文字列を表示用に整形する
	 * @param comment 気象予報解説文
	 * @return 整形済み気象予報解説文
	 */
	private String formatComment(String comment) {
		// 「<br />」及び改行コードを取り除く
		String ret = comment.replace("<br />", "").replace("\r\n", "").replace("\n", "").replace("\r", "");
		String br = System.getProperty("line.separator");
		
		// 一口RSSの記述パターンに合わせて適当な場所に改行コードを挿入/削除する
		ret = ret.replace("。", "。" + br)
				.replace("。" + br + ")", "。)")
				.replace("。" + br + "）", "。）")
				.replace("))", "))" + br)
				.replace("））", "））" + br)
				.replace("【", br + "【")
				.replace("】", "】" + br);
		
		return ret;
	}

	@Override
	public long storeWeatherDataIfNotExist(List<WeatherBean> weatherList,
			LocationBean monitoringPointBean, Context context) throws IOException {
		// ※現在は未使用であるため、特に何もしない
		return 0;
	}
}
