package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.util.Properties;

import jp.co.teraintl.g12011.wforecasterd.wforecast.cnvi.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 現在地取得履歴テーブルの作成、バージョン等の管理をするクラス
 * @author tsutomu
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	
	/** アプリケーションコンテキスト */
	private Context context;
	
	/** 作成・削除対象となるテーブル一覧 */
	private String[] tableArr;
	
	/** テーブル作成用プロパティ文字列 */
	private final String TBL_CRT = "sql_create_table";
	
	/** テーブル削除用プロパティ文字列 */
	private final String TBL_DLT = "sql_drop_table";

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 * @param name DB名
	 * @param factory カーソルオブジェクト。nullを指定した場合は先頭から始まるカーソル
	 * @param version DBバージョン
	 */
	public DatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		
		// onCreate/onUpgrade メソッドは getWritableDatabase() メソッドが呼ばれたときに呼ばれるので、
		// 暗黙的スーパー・コンストラクタの後にcontext を設定しても問題ない
		this.context = context;
		
		// 作成するテーブル一覧
		tableArr = new String[]{
				context.getString(R.string.assets_sql_t_widget_locale_history)
				, context.getString(R.string.assets_sql_m_country)
				, context.getString(R.string.assets_sql_m_p_region)
				, context.getString(R.string.assets_sql_m_p_administrative_area)
				, context.getString(R.string.assets_sql_m_p_sub_administrative_area)
				, context.getString(R.string.assets_sql_m_p_locality)
				, context.getString(R.string.assets_sql_m_p_foreign_city)
				, context.getString(R.string.assets_sql_t_weather_history)
				, context.getString(R.string.assets_sql_t_woc_weather_comment)
				, context.getString(R.string.assets_sql_t_adar_weather_comment)
		};
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		handleTables(db, TBL_CRT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		handleTables(db, TBL_DLT);
	}
	
	/**
	 * テーブルを操作する
	 * @param db SQLiteDatabaseインスタンス
	 * @param propParam 操作文字列
	 */
	private void handleTables(SQLiteDatabase db, String propParam) {
		
		CommonUtils com = CommonUtils.getInstance(context);
		Properties prop = null;
		
		try {
			// テーブルを作成する
			for (int i = 0; i < tableArr.length; i++) {
				prop = com.getAssetsXmlResourceProperty(context, tableArr[i]);
				db.execSQL(prop.getProperty(propParam));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (prop != null)
				prop.clear();
		}
	}
}
