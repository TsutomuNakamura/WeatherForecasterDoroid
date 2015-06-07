package jp.co.teraintl.g12011.wforecasterd.apl.init;

import java.io.IOException;
import jp.co.teraintl.g12011.wforecasterd.apl.common.MonitoringPointProvider;

import android.content.Context;
import android.util.Log;

/**
 * テーブル初期化クラス
 * @author tsutomu
 *
 */
public class TableInitializer {
	
	/** ログタグ */
	private final String LOG_TAG = this.getClass().getName();
	
	/**
	 * テーブルを初期化する
	 * @param context アプリケーション婚天気スト
	 * @return 処理件数
	 * @throws IOException 入出力例外
	 */
	public long initTableIfNoData(Context context) throws IOException {
		
		long ret = 0;
		long columns = 0;
				
		MonitoringPointProvider monitoringPointProvider = new MonitoringPointProvider(context);
		
		// 開始時間
		long startTime = System.currentTimeMillis();

		// 気象観測地点マスタ類のテーブル初期化
		columns = monitoringPointProvider.initTablesIfNoData(context);
		
		// 終了時間
		long finishTime = System.currentTimeMillis();
		
		Log.i(LOG_TAG, "initTableIfNoData finish.[TIME=" + (finishTime - startTime)
				+ "ms],[AMOUNT_COLUMNS=" + columns +"]");
		
		return ret;
	}
}
