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
 * 地域手動登録アクティビティ
 * @author tsutomu
 *
 */
public class AreaChoserActivity extends ListActivity {
	
	/** ログ表示用タグ */
	private final String LOG_TAG = this.getClass().getName();

	/** 都道府県名 */
	private String administrativeAreaName;
	
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
			
			if ((administrativeAreaName = extras.getString(
					context.getString(R.string.intent_extra_administrative_area_name))) == null
					|| administrativeAreaName.equals("")) {
				
				// インテントに都道府県名が設定されていない場合、都道府県名一覧を取得/表示
				try {
					locationBeanList = provider.getAdministrativeAreaNameList(
							extras.getString(context.getString(R.string.intent_extra_country_name_code)), context);
				} catch (IOException e) {
					e.printStackTrace();
				}

				adapter = new ListAdapter(getApplicationContext(), locationBeanList
						, extras.getInt(context.getString(R.string.intent_extra_widget_id)), true);
			} else {
				
				// インテントに都道府県名が設定されている場合、市区町村名一覧を取得/表示
				try {
					locationBeanList = provider.getLocalityNameList(
							extras.getString(context.getString(R.string.intent_extra_country_name_code))
							, extras.getString(context.getString(R.string.intent_extra_administrative_area_name))
							, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
				adapter = new ListAdapter(getApplicationContext(), locationBeanList
						, extras.getInt(context.getString(R.string.intent_extra_widget_id)), false);
			}
			
			// リストアダプタの設定と表示
			setListAdapter(adapter);			
		}
	}
	
	/**
	 * 都道府県名又は地域名一覧をリスト表示するAdapterクラス
	 * @author tsutomu
	 */
	class ListAdapter extends ArrayAdapter<LocationBean> {

		/** 一覧表示用レイアウトインスタンス */
		private LayoutInflater mInflater;
		
		/** 都道府県一覧表示フラグ */
		private boolean administrativeAreaExpressFlag = true;
		
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
				, boolean administrativeAreaExpressFlag) {
			
			super(context, 0, locationBeanList);
			this.widgetId = widgetId;
			this.administrativeAreaExpressFlag = administrativeAreaExpressFlag;
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.frcst_chose_area_row, null);
						
			final LocationBean locationBean = this.getItem(position);
			if (locationBean != null) {
				TextView namesOfPlaces = null;
				
				if (administrativeAreaExpressFlag) {
					
					// 都道府県表示フラグがtrue の場合、都道府県一覧を表示する
					namesOfPlaces = (TextView)convertView.findViewById(R.id.NamesOfPlaces);
					namesOfPlaces.setText(locationBean.getAdministrativeAreaName());
					
					
					// クリックイベントの追加
					namesOfPlaces.setOnClickListener(new ClickListener(
							widgetId
							, "JP"
							, namesOfPlaces.getText().toString()
					));
				} else {
					
					// 都道府県表示フラグがfalse の場合、地域名一覧を表示する
					namesOfPlaces = (TextView)convertView.findViewById(R.id.NamesOfPlaces);
					namesOfPlaces.setText(locationBean.getLocalityName());
					
					// クリックイベントの追加
					namesOfPlaces.setOnClickListener(new ClickListener(
							widgetId
							, "JP"
							, locationBean.getAdministrativeAreaName()
							, namesOfPlaces.getText().toString()));
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
		
		/** 都道府県名 */
		private String administrativeAreaName;
		
		/** 地域名 */
		private String localityName;
		
		/** ウィジェットID */
		private int widgetId;
		
		//※参考 http://ichitcltk.hustle.ne.jp/gudon/modules/pico_rd/index.php?content_id=47
		/**
		 * コンストラクタ
		 * @param widgetId ウィジェットID
		 * @param countryNameCode 国名コード
		 * @param administrativeAreaName 都道府県名
		 */
		public ClickListener(
				int widgetId, String countryNameCode
				, String administrativeAreaName) {
			super();
			this.widgetId = widgetId;
			this.countryNameCode = countryNameCode;
			this.administrativeAreaName = administrativeAreaName;
		}
		
		/**
		 * コンストラクタ
		 * @param widgetId ウィジェットID
		 * @param countryNameCode 国名コード
		 * @param administrativeAreaName 都道府県名
		 * @param localityName 市区町村名
		 */
		public ClickListener(int widgetId
				, String countryNameCode, String administrativeAreaName, String localityName) {
			super();
			this.widgetId = widgetId;
			this.countryNameCode = countryNameCode;
			this.administrativeAreaName = administrativeAreaName;
			this.localityName = localityName;
		}
		
		@Override
		public void onClick(View v) {
			
			Context context = getApplicationContext();
			CommonUtils com = CommonUtils.getInstance(context);
			WeatherLocationProvider provider = new WeatherLocationProvider(context);
			
			if (localityName == null) {

				// 市区町村名が無い場合、市区町村一覧を表示する
				Intent intent = new Intent(context, AreaChoserActivity.class);
				intent.putExtra(
						context.getString(R.string.intent_extra_widget_id), widgetId);
				intent.putExtra(
						context.getString(R.string.intent_extra_country_name_code), countryNameCode);
				intent.putExtra(context.getString(
						R.string.intent_extra_administrative_area_name), administrativeAreaName);
				startActivity(intent);
			} else {

				// 対象のウィジェットIDの地域情報の設定。更新回数も0とする。
				try {
					provider.resetWidgetLocationData(widgetId, countryNameCode
									, administrativeAreaName, localityName, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				Log.i(LOG_TAG, "地域を設定しました[WidgetId=" + widgetId
						+ "][gpsFlag=" +
						"][CountryNameCode="
						+ countryNameCode + "][AdminiStrativeAreaName="
						+ administrativeAreaName + "][LocalityName="
						+ localityName + "]");
				
				// ※地域を選択したら週間天気予報画面に遷移する方針でよいかどうか考える
				// 最後に参照していた地域の気象を再度取得する(週間天気予報画面)
				LocationBean locationBean = com.loadLastRefedLocation();
				Log.i(LOG_TAG, "最終参照地域の気象詳細再表示[WidgetId="
						+ locationBean.getWidgetId() + "][CountryNameCode="
						+ locationBean.getCountryNameCode() + "[AdministrativeAreaName="
						+ locationBean.getAdministrativeAreaName() + "][LocalityName="
						+ locationBean.getLocalityName() + "]");
				
				startActivity(CommonUtils.getFrcstMtrlgIntent(locationBean, context, FrcstMtrlgActivity.class));
				
				// サービスを即時実行する
				FrcstMtrlgService.startServiceImmediately(com, context);
			}
		}
	}
}
