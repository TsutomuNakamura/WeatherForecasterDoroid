package jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherLocationProvider;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherProvider;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FrcstMtrlgActivity extends ListActivity {
	
	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName();

	/** メニューボタンのグループID */
	private final int MENU_GROUP_ID_GENERAL = Menu.FIRST;

	/** 最新取得メニューID */
	private final int MENU_ITEM_ID_GET_LATEST_INFO = Menu.FIRST;
	/** 地域設定メニューID */
	private final int MENU_ITEM_ID_SET_LOCATION = Menu.FIRST + 1;
	/** クレジットタイトルメニューID */
	private final int MENU_ITEM_ID_CREDIT_TITLE = Menu.FIRST + 2;

	/** 地域取得メニュー：GPSから取得するためのメニューID */
//	private static final int MENU_ITEM_GET_FROM_GPS = 0;
	/** 地域取得メニュー：一覧から選択するためのメニューID */
	private static final int MENU_ITEM_GET_FROM_LIST = 0;
	/** 地域取得メニュー：海外一覧から選択するためのメニューID */
	private static final int MENU_ITEM_GET_FROM_FOREIGN_LIST = 1;
	/** 地域取得メニュー：地図から選択するためのメニューID */
	private static final int MENU_ITEM_GET_FROM_MAP = 2;

	/** 現在の気象予報プロバイダ */
	private int currentProviderId;
	
	// Android 端末でシングルスレッドのため、スレッドセーフについては考慮しない
	
	/** アプリケーションコンテキスト */
	private Context context;
	
	/** 共通ユーティリティクラス */
	private CommonUtils com;

	/** 地域情報格納Bean */
	private LocationBean locationBean;
	
	/** ウィジェットID */
	private int widgetId;
	
	/** 最終参照地域取得フラグ */
	private boolean loadLastRefLocFlag;
	
	/** 過去の取得地域プロバイダクラスのインスタンス */
	private WeatherLocationProvider provider;
	
	/** 気象予報データ格納Bean リスト */
	List<WeatherBean> weatherBeanList;
	
	/** 気象地点選択肢一覧 */
	private final String[] MENU_ITEMS = {"日本の天気", "海外の天気"};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ヘッダ部分は、WeatherMap, WWOnline 共に同じ
		setContentView(R.layout.frcst_activity);
		context = getApplicationContext();
		com = CommonUtils.getInstance(context);
		provider = new WeatherLocationProvider(context);
		
		currentProviderId = com.getCurrentWeatherProviderId(context);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		Bundle extras = getIntent().getExtras();
		WeatherBean weatherCommentBean = null;
		ListAdapter adapter = null;

		if (extras != null) {
			// Activity のコンポーネント設定
			// ※FrcstMtrlgAuthority.java にて、ウィジェットにPendingIntentで値を渡す時に
			// FLAG_UPDATE_CURRENT を指定してもウィジェット上のextra値が更新されない。
			// そのため、現段階では、シングルトンインスタンスにウィジェットの情報を保持しておき、
			// 本アクティビティ起動時にはそちらのデータを読むようにする
			// locationBean.setWidgetId(extras.getInt(
			// context.getString(R.string.intent_extra_widget_id)));
			// locationBean.setGpsFlag(extras.getBoolean(
			// context.getString(R.string.intent_extra_gps_flag)));
			// locationBean.setCountryNameCode(extras.getString(
			// context.getString(R.string.intent_extra_country_name_code)));
			// locationBean.setAdministrativeAreaName(extras.getString(
			// context.getString(R.string.intent_extra_administrative_area_name)));
			// locationBean.setLocalityName(extras.getString(
			// context.getString(R.string.intent_extra_locality_name)));
			
			// メモリ上のウィジェットデータの読み込み
			// extras.getInt(context.getString(R.string.intent_extra_widget_id)) ←ウィジェットIDが取得される
			
			// 表示対象となっているウィジェットID取得
			widgetId = extras.getInt(context.getString(R.string.intent_extra_widget_id));
			loadLastRefLocFlag = extras.getBoolean(context.getString(
					R.string.intent_extra_load_last_ref_location_flg), false);
			
			// 対象のウィジェットIDのウィジェットオブジェクトを取得する
			if (loadLastRefLocFlag) {
				// 最後に参照していた地域を取得する
				locationBean = com.loadLastRefedLocation();
			} else {
				// 指定されたウィジェットIDのウィジェットを取得する
				locationBean = provider.getHistoryLocation(widgetId, context);
			}
			// ※ もし取得できなかった場合のエラーハンドリングを将来実装
			
			// 最終参照地域情報を保存
			if (locationBean != null) {
				com.saveLastRefedLocation(locationBean);
			}
			Log.i(LOG_TAG,
					"最終参照地域として情報を設定[WidgetId=" + locationBean.getWidgetId()
							+ "][GpsFlag=" + locationBean.getGpsFlag()
							+ "][CNameCode="
							+ locationBean.getCountryNameCode()
							+ "][AAreaName="
							+ locationBean.getAdministrativeAreaName() + "]");
		}

		// アプリケーションで使用するプロバイダ取得
		WeatherProvider provider = com.getCurrentWeatherProvider(context);

		try {
			// 気象予報データ履歴をDBから取得する
			weatherBeanList = provider.getWeeklyWeatherFromDb(locationBean, context);
		} catch (IOException e) {
			Log.w(LOG_TAG, "onCreate():IOException occured.", e);
		}

		// 気象解説と観測日時を取り出すためのBean(気象解説については今のところ一口天気予報のみ)
		try {
			// ※ここでも例外発生することあり(原因調査中)
			weatherCommentBean = weatherBeanList.get(0);

			// ヘッダ部分のレイアウトを構築する
			TextView monitoringPointLabel = (TextView) findViewById(R.id.monitoring_point_label);
			monitoringPointLabel.setText(
					// 日本国内の場合は都道府県名、海外の場合は国名をヘッダに表示する
					(locationBean.getCountryNameCode().equals("JP")? locationBean.getAdministrativeAreaName() : locationBean.getCountryName())
					+ " " + locationBean.getLocalityName() + " 気象予報");
			TextView widgetIdAndLocationType = (TextView) findViewById(R.id.widget_id_and_location_type);
			widgetIdAndLocationType.setText("ウィジェットID："
					+ locationBean.getWidgetId() + "　地域取得方法："
					+ (locationBean.getGpsFlag() ? "GPS" : "手動登録")
					+ System.getProperty("line.separator") + "発表："
					+ weatherCommentBean.getAnnounceDate());
			
			// 中央部分の週間天気予報部分のレイアウトを構築する
			adapter = new ListAdapter(getApplicationContext(), weatherBeanList);
		} catch (IndexOutOfBoundsException e) {
			// ※あくまで応急処置
			e.printStackTrace();
		}

		// footer が既に存在する場合はfooter を作成しない
		if (getListView().getFooterViewsCount() <= 0) {
			getListView().addFooterView(createFooter(weatherCommentBean));
		}

		// ※ footer がnullだと、setListAdapter で落ちる
		setListAdapter(adapter);
	}

	/**
	 * footer を作成する
	 * @param weatherCommentBean 気象予報格納Bean
	 * @return footer
	 */
	private View createFooter(WeatherBean weatherCommentBean) {
		View footer = null;
		
		// footerの追加と設定
		if (com.getCurrentWeatherProviderId(context) == CommonUtils.PROVIDER_WEATHER_MAP) {
			
			// WeatherMap の場合
			footer = View.inflate(context, R.layout.frcst_activity_row_footer, null);

			TextView wholeOfCountryCommentPubDate =
					(TextView) footer.findViewById(R.id.WholeOfCountryCommentPubDate);
			wholeOfCountryCommentPubDate.setText("("
					+ weatherCommentBean.getWholeOfCommunityWeatherCommentPubDate()
					+ ")");

			TextView wholeOfCountryComment =
					(TextView) footer.findViewById(R.id.WholeOfCountryComment);
			wholeOfCountryComment.setText(weatherCommentBean
					.getWholeOfCommunityWeatherComment());

			TextView administrativeAreaCommentLabel = (TextView) footer
					.findViewById(R.id.AdministrativeAreaCommentLabel);
			administrativeAreaCommentLabel.setText("■"
					+ locationBean.getAdministrativeAreaName() + "近郊の気象解説");

			TextView administrativeAreaCommentPubdate = (TextView) footer
					.findViewById(R.id.AdministrativeAreaCommentPubDate);
			administrativeAreaCommentPubdate.setText("("
					+ weatherCommentBean
							.getAdministrativeAreaWeatherCommentPubDate() + ")");

			TextView administrativeAreaComment = (TextView) footer
					.findViewById(R.id.AdministrativeAreaComment);
			administrativeAreaComment.setText(weatherCommentBean
					.getAdministrativeAreaWeatherComment());
		} else if (com.getCurrentWeatherProviderId(context) == CommonUtils.PROVIDER_WORLD_WEATHER_ONLINE) {
			
			// 気象情報提供者がWodld Weather Online の場合
			footer = View.inflate(context, R.layout.frcst_wwonline_activity_row_footer, null);
			
			TextView authorsCreditSubject =
					(TextView)footer.findViewById(R.id.AuthorsCreditSubject);
			authorsCreditSubject.setText(
					context.getString(R.string.authors_wwonline_credit_subject));
			
			TextView authorsCredit =
					(TextView)footer.findViewById(R.id.AuthorsCredit);
			authorsCredit.setText(
					context.getString(R.string.authors_wwonline_credit));
		}

		return footer;
	}

	/**
	 * 気象予報一覧を表示するリストアダプタクラス
	 * @author tsutomu
	 *
	 */
	class ListAdapter extends ArrayAdapter<WeatherBean> {

		/** 一覧表示用レイアウトインスタンス */
		private LayoutInflater mInflater;

		/** 気象アイコン */
		private ImageView weatherIcon;
		/** 気象予報対象の日時 */
		private TextView stringDate;
		/** 気象予報対象の曜日 */
		private TextView stringDayOfWeek;
		/** 気象 */
		private TextView weather;
		/** 最高気温 */
		private TextView maxTemp;
		/** 最低気温 */
		private TextView minTemp;

		/** 降水確率(0～6時) */
		private TextView rainFallProvOneQuarters;
		/** 降水確率(6～12時) */
		private TextView rainFallProvTwoQuarters;
		/** 降水確率(12～18時) */
		private TextView rainFallProvThreeQuarters;
		/** 降水確率(18時～24時) */
		private TextView rainFallProvFourQuarters;
		
		/** 降水確率要素配列 */
		private String[][] rainFallElements;

		/** 風向き */
		private TextView windDir;
		/** 風速 */
		private TextView windSpeed;

		/**
		 * コンストラクタ
		 * @param context アプリケーションコンテキスト
		 * @param weatherBeanList 気象予報データ格納Bean リスト
		 */
		public ListAdapter(Context context, List<WeatherBean> weatherBeanList) {
			super(context, 0, weatherBeanList);
			
			// レイアウトインスタンスの取得
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			// Activity が表示されるとき及び、画面がスクロールされて
			// 画面外にあった要素が画面内に映るときにも呼ばれる。
			// 画面外にある要素については、画面に表示される瞬間に構築されるということ
			
			// 文字や画像等を載せるView 要素が存在しない場合は、
			// 取得する気象予報プロバイダによってことなるレイアウトのView を取得する(気象予報プロバイダによって表示できる項目が違うため)
			if (convertView == null) {
				if (currentProviderId == CommonUtils.PROVIDER_WEATHER_MAP) {

					// 気象予報提供者がWeatherMapの場合(レイアウトはfrcst_activity_row.xml)
					convertView = mInflater.inflate(
							R.layout.frcst_activity_row, null);
				} else if (currentProviderId == CommonUtils.PROVIDER_WORLD_WEATHER_ONLINE) {

					// 気象予報提供者がWorldWeatherOnline の場合(レイアウトはfrcst_wwwonline_activity_row.xml)
					convertView = mInflater.inflate(
							R.layout.frcst_wwonline_activity_row, null);
				}
			}
			
			// インスタンス時に渡されたWeatherBean オブジェクトのリストを、
			// 表示されるときに１つずつ取得してレイアウトの要素にはめ込んでいく。
			final WeatherBean weatherBean = this.getItem(position);
			if (weatherBean != null) {

				// 天気予報データが存在する場合
				
				// frcst_activity_row.xml ファイルに定義されているレアウトの構築
				
				// レイアウトの気象予報表示(イメージアイコン)部分を構築する
				weatherIcon = (ImageView) convertView.findViewById(R.id.WeatherIcon);
				weatherIcon.setImageResource(weatherBean.getWeatherIconId());

				// レイアウトの日付表示部分を構築する
				stringDate = (TextView) convertView.findViewById(R.id.StringDate);
				stringDate.setText(weatherBean.getStringDate().substring(5));

				// レイアウトの曜日表示部分を構築する
				stringDayOfWeek = (TextView) convertView.findViewById(R.id.StringDayOfWeek);
				stringDayOfWeek.setText(
						"(" + com.getWeekStringJa(weatherBean.getDayOfWeek()) + ")");

				// レイアウトの気象予報表示(文字)部分を構築する
				weather = (TextView) convertView
						.findViewById(R.id.StringWeather);
				weather.setText(weatherBean.getWeather());

				// レイアウトの最高気温表示部分を構築する
				maxTemp = (TextView) convertView.findViewById(R.id.TempMax);
				maxTemp.setText((weatherBean.getHighestTemperature() == null ? "--"
						: Integer.toString(weatherBean.getHighestTemperature())));

				// レイアウトの最低気温表示部分を構築する
				minTemp = (TextView) convertView.findViewById(R.id.TempMin);
				minTemp.setText((weatherBean.getLowestTemperature() == null ? "--"
						: Integer.toString(weatherBean.getLowestTemperature())));

				if (currentProviderId == CommonUtils.PROVIDER_WEATHER_MAP) {

					// 一口天気予報の場合は降水確率を表示する
					
					// ※降水確率のレイアウトが崩れる件について、対策考慮中
					rainFallElements = splitToChanceOfRain(weatherBean
							.getChanceOfRain());
					for (int i = 0; i < rainFallElements.length; i++) {
						
						// 画面に表示するための各時間帯ごとの降水確率の文字列を設定していく
						
						switch (i) {
						case 0:
							rainFallProvOneQuarters = (TextView) convertView
									.findViewById(R.id.RainFallProvOneQuarters);
							rainFallProvOneQuarters
									.setText(rainFallElements[i][0] + ":"
											+ rainFallElements[i][1] + "%  ");
							break;
						case 1:
							rainFallProvTwoQuarters = (TextView) convertView
									.findViewById(R.id.RainFallProvTwoQuarters);
							rainFallProvTwoQuarters
									.setText(rainFallElements[i][0] + ":"
											+ rainFallElements[i][1] + "%  ");
							break;
						case 2:
							rainFallProvThreeQuarters = (TextView) convertView
									.findViewById(R.id.RainFallProvThreeQuarters);
							rainFallProvThreeQuarters
									.setText(rainFallElements[i][0] + ":"
											+ rainFallElements[i][1] + "%");
							break;
						case 3:
							rainFallProvFourQuarters = (TextView) convertView
									.findViewById(R.id.RainFallProvFourQuarters);
							rainFallProvFourQuarters
									.setText(rainFallElements[i][0] + ":"
											+ rainFallElements[i][1] + "%");
						}
					}
				} else if (currentProviderId == CommonUtils.PROVIDER_WORLD_WEATHER_ONLINE) {

					// World Weather Online の場合は風向きを表示する
					
					// 風向きを設定
					windDir = (TextView) convertView.findViewById(R.id.WindDir);
					windDir.setText("風向：" + weatherBean.getWindDirection());

					// 風速を設定
					int tmpWindSpeed = weatherBean.getWindSpeed() * 1000 / 60 / 60;
					windSpeed = (TextView) convertView.findViewById(R.id.WindSpeed);
					windSpeed.setText("風速：" + (tmpWindSpeed < 5 ? "5 m/s未満" : tmpWindSpeed + " m/s"));
				}
			}

			return convertView;
		}

		/**
		 * 降水確率Mapを文字列配列に分割する
		 * @param map 降水確率Map
		 * @return 分割された文字列配列
		 */
		private String[][] splitToChanceOfRain(Map<String, Integer> map) {

			Integer tmpInt = null;

			if (map == null) {

				// 気温データを格納するMap そのものがない場合は、データ無しとして「--」を返す
				return new String[][] { { "-24h", "--" } };
			} else if ((tmpInt = map.get("null")) != null) {

				// 気温データを格納するMap は存在するが中身が無い場合は、一日の気温として気象予報を返す
				return new String[][] { { "-24h", tmpInt.toString() } };
			}

			// Map が設定されている場合、6時間毎に香水確率の文字列データを格納して返す
			return new String[][] {
				{
					"0h-",
					((tmpInt = map.get("00-06")) == null ? "--" : tmpInt.toString())
				},
				{
					"6h-",
					((tmpInt = map.get("06-12")) == null ? "--" : tmpInt.toString())
				},
				{
					"12h-",
					((tmpInt = map.get("12-18")) == null ? "--" : tmpInt.toString())
				},
				{
					"18h-",
					((tmpInt = map.get("18-24")) == null ? "--" : tmpInt.toString())
				}
			};
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// メニューを作成する
		boolean ret = super.onCreateOptionsMenu(menu);

		menu.add(MENU_GROUP_ID_GENERAL, MENU_ITEM_ID_SET_LOCATION, Menu.NONE,
				"地域設定");

		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean ret = super.onOptionsItemSelected(item);

		Log.i(LOG_TAG, "メニューボタンが押下されました[GROUP_ID=" + item.getGroupId()
				+ "],[ITEM_ID=" + item.getItemId() + "]");

		switch (item.getGroupId()) {
		case MENU_GROUP_ID_GENERAL:

			switch (item.getItemId()) {
			case MENU_ITEM_ID_GET_LATEST_INFO:

				// 「最新情報取得」が選択されたとき
				Log.i(LOG_TAG, "最新情報取得が押下されました");

				break;
			case MENU_ITEM_ID_SET_LOCATION:

				// 「地域設定」が選択されたとき
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("地域取得方法選択");
				builder.setItems(MENU_ITEMS,
						
						// クリックを待ち受けるリスナの匿名クラスをアイテムとして設定する
						new DialogInterface.OnClickListener() {
							Intent intent = null;
							public void onClick(DialogInterface dialog, int item) {
								switch (item) {
								case MENU_ITEM_GET_FROM_LIST:

									// 地域設定(一覧)が選択されたとき、地域選択用のActivity を起動する
									Log.i(LOG_TAG,
											"地域設定(一覧)が選択されました。[WIDGET_ID="
													+ locationBean
															.getWidgetId()
													+ "]");

									// 地域一覧表示用の情報を設定する
									intent = new Intent(
											FrcstMtrlgActivity.this,
											AreaChoserActivity.class);

									// 日本の地域一覧を表示するため、country_name_code はJP を指定する
									intent.putExtra(
									context.getString(R.string.intent_extra_country_name_code),
									locationBean.getCountryNameCode());

									intent.putExtra(
											context.getString(R.string.intent_extra_widget_id),
											locationBean.getWidgetId());
									startActivityForResult(intent, 0);
									break;
								
								case MENU_ITEM_GET_FROM_FOREIGN_LIST:
									// 海外地域設定(一覧) が選択された時、地域選択用のActivity を起動する
									Log.i(LOG_TAG,
											"海外地域設定(一覧)が選択されました。[WIDGET_ID="
													+ locationBean
															.getWidgetId()
													+ "]");
									
									// 海外一覧表示用のActivity を起動する
									intent = new Intent(
											FrcstMtrlgActivity.this,
											ForeignAreaChoserActivity.class);
									// どこの国も設定されていないことを表す「""」をIntent に設定
									intent.putExtra(
											context.getString(R.string.intent_extra_country_name_code),
											CommonUtils.NULL_STRING);
									// 次のActivity に渡すためにウィジェットIDを指定
									intent.putExtra(
											context.getString(R.string.intent_extra_widget_id),
											locationBean.getWidgetId());
									startActivityForResult(intent, 0);
									break;
									
								case MENU_ITEM_GET_FROM_MAP:

									// ※未実装：地域設定(地図)が選択されたとき
									break;
								}
							}
						}).create().show();
				break;
				
			case MENU_ITEM_ID_CREDIT_TITLE:
				// クレジットが選択されたとき
				break;
			}
			break;
		}

		return ret;
	}
}
