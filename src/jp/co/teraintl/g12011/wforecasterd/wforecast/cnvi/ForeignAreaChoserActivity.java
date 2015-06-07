package jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi;

import java.io.IOException;
import java.util.List;

import jp.co.teraintl.g12011.wforecasterd.apl.common.CommonUtils;
import jp.co.teraintl.g12011.wforecasterd.apl.common.LocationBean;
import jp.co.teraintl.g12011.wforecasterd.apl.common.WeatherLocationProvider;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 海外地域手動登録アクティビティ
 * @author tsutomu
 *
 */
public class ForeignAreaChoserActivity extends ListActivity {
	
	/** ログ表示用タグ */
	private final String LOG_TAG = this.getClass().getName();

	/** 都道府県名 */
	private String foreignCountryName;
	
	/** 表示用地域名一覧 */
	private List<LocationBean> locationBeanList;
	
	/** 一覧表示用リストアダプタ */
	private ListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frcst_chose_area);
		
		Bundle extras = getIntent().getExtras();
		Context context = getApplicationContext();
		WeatherLocationProvider provider = new WeatherLocationProvider(context);

		// extraの情報を取得する
		if (extras != null) {
			
			if ((foreignCountryName = extras.getString(
					context.getString(R.string.intent_extra_country_name_code))) == null
					|| foreignCountryName.equals(CommonUtils.NULL_STRING)) {
				
				// 都市名に海外国名が設定されていない場合、国名一覧を取得
				try {
					// 海外地域を取得する(日本以外の国名)
					locationBeanList = provider.getForeignCountryNameList(context);

				} catch (IOException e) {
					e.printStackTrace();
				}
				
				adapter = new ListAdapter(
						getApplicationContext()
						, locationBeanList
						, extras.getInt(context.getString(R.string.intent_extra_widget_id))
						, true
				);
			} else {
				
				// インテントに海外の国名名が設定されている場合、海外都市名一覧を取得/表示
				try {
					locationBeanList = provider.getForeignLocalityNameList(
							extras.getString(context.getString(R.string.intent_extra_country_name_code))
							, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// アダプタを取得する
				adapter = new ListAdapter(
						getApplicationContext()
						, locationBeanList
						, extras.getInt(context.getString(R.string.intent_extra_widget_id))
						, false
				);
			}
			
			// リストアダプタの設定と表示
			setListAdapter(adapter);
		}
	}
	
	/**
	 * 国名又は海外都市名一覧をリスト表示するAdapter クラス
	 * @author tsutomu
	 */
	class ListAdapter extends ArrayAdapter<LocationBean> {

		/** 一覧表示用レイアウトインスタンス */
		private LayoutInflater mInflater;
		
		/** 海外都市一覧表示フラグ */
		private boolean countryNameListExpressFlag = true;
		
		/** ウィジェットID */
		private int widgetId;
		
		/**
		 * ListAdapterコンストラクタ
		 * @param context アプリケーションコンテキスト
		 * @param locationBeanList 地域情報Beanリスト
		 * @param widgetId ウィジェットID
		 * @param administrativeAreaExpressFlag 都道府県一覧表示フラグ
		 */
		public ListAdapter(Context context
				, List<LocationBean> locationBeanList
				, int widgetId
				, boolean countryNameListExpressFlag) {
			
			super(context, 0, locationBeanList);
			this.widgetId = widgetId;
			this.countryNameListExpressFlag = countryNameListExpressFlag;
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.frcst_chose_area_row, null);
						
			final LocationBean locationBean = this.getItem(position);
			if (locationBean != null) {
				TextView namesOfPlaces = null;
				
				if (countryNameListExpressFlag) {
					
					// 海外国名一覧表示フラグがtrue の場合、海外国名一覧を表示する
					namesOfPlaces = (TextView)convertView.findViewById(R.id.NamesOfPlaces);
					namesOfPlaces.setText(locationBean.getCountryName());
					
					// クリックイベントの追加
					namesOfPlaces.setOnClickListener(new ClickListener(
							widgetId
							, locationBean.getCountryNameCode()
							, locationBean.getCountryName()
					));
				} else {
					
					// 海外国名一覧表示フラグがfalse の場合、海外の都市名一覧を表示する
					namesOfPlaces = (TextView)convertView.findViewById(R.id.NamesOfPlaces);
					namesOfPlaces.setText(locationBean.getLocalityName());
					
					// クリックイベントの追加
					namesOfPlaces.setOnClickListener(new ClickListener(
							widgetId
							, locationBean.getCountryNameCode()
							, locationBean.getCountryName()
							, locationBean.getLocalityName()
					));
				}
			}
			
			return convertView;
		}
	}
	
	/**
	 * 地域手動登録クリックリスナ
	 * @author tsutomu
	 *
	 */
	class ClickListener implements View.OnClickListener {

		/** 国名コード */
		private String countryNameCode;
		
		/** 国名 */
		private String countryName;
		
		/** 都市名 */
		private String localityName;
		
		/** ウィジェットID */
		private int widgetId;
		
		//※参考 http://ichitcltk.hustle.ne.jp/gudon/modules/pico_rd/index.php?content_id=47
		/**
		 * コンストラクタ
		 * @param widgetId ウィジェットID
		 * @param countryNameCode 国名コード
		 * @param countryName 国名
		 */
		public ClickListener(
				int widgetId
				, String countryNameCode
				, String countryName) {
			super();
			this.widgetId = widgetId;
			this.countryNameCode = countryNameCode;
			this.countryName = countryName;
		}
		
		/**
		 * コンストラクタ
		 * @param widgetId ウィジェットID
		 * @param countryNameCode 国名コード
		 * @param countryName 国名
		 * @param localityName 都市名
		 */
		public ClickListener(
				int widgetId
				, String countryNameCode
				, String countryName
				, String localityName) {
			super();
			this.widgetId = widgetId;
			this.countryNameCode = countryNameCode;
			this.countryName = countryName;
			this.localityName = localityName;
		}
		
		@Override
		public void onClick(View v) {
			
			Context context = getApplicationContext();
			CommonUtils com = CommonUtils.getInstance(context);
			WeatherLocationProvider provider = new WeatherLocationProvider(context);
			
			if (localityName == null) {

				// 都市名が無い場合、都市名一覧を表示する
				Intent intent = new Intent(context, ForeignAreaChoserActivity.class);
				intent.putExtra(
						context.getString(R.string.intent_extra_widget_id), widgetId);
				intent.putExtra(
						context.getString(R.string.intent_extra_country_name_code), countryNameCode);
				// ※ "intent_extra_country_name" noextra は必要ないと思われる
				intent.putExtra(context.getString(
						R.string.intent_extra_country_name), countryName);
				startActivity(intent);
			} else {

				// 都市名が設定されている場合、対象のウィジェットIDの地域情報の設定。更新回数も0で設定
				try {
					// 国内の天気予報で県名"administrativeAreaName" を表示している領域に
					// 国名コード"intent_extra_country_name_code" を表示するよう指定する
					provider.resetWidgetForeignLocationData(
							widgetId, countryNameCode, localityName, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				Log.i(LOG_TAG, "海外地域を設定しました[WidgetId=" + widgetId
						+ "][gpsFlag=" +
						"][CountryNameCode="
						+ countryNameCode + "][LocalityName="
						+ localityName + "]");
				
				// ※地域を選択したら週間天気予報画面に遷移する方針でよいかどうか考える(日本の気象標示と同じ)
				// 最後に参照していた地域の気象を再度取得する
				LocationBean locationBean = com.loadLastRefedLocation();
				Log.i(LOG_TAG, "最終参照地域の気象詳細再表示[WidgetId="
						+ locationBean.getWidgetId() + "][CountryNameCode="
						+ locationBean.getCountryNameCode() + "[AdministrativeAreaName="
						+ locationBean.getLocalityName() + "]");
				
				startActivity(CommonUtils.getFrcstMtrlgIntent(locationBean, context, FrcstMtrlgActivity.class));
				
				// 気象予報を取得するためのサービスを即時実行する
				FrcstMtrlgService.startServiceImmediately(com, context);
			}
		}
	}
}
