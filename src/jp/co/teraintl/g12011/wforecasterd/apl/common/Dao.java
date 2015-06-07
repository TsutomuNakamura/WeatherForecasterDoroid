package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Dao 親クラス
 * @author tsutomu
 *
 */
public class Dao {
	
	/** SQLiteDatabase インスタンス */
	protected SQLiteDatabase db;
	
	public Dao(SQLiteDatabase db) {
		this.db = db;
	}
	
	/**
	 * ファイルの行数をカウントする。
	 * @param fileName ファイル名
	 * @param nullCharDenyFlag null文字の行を1行としてカウントするか否かのフラグ
	 * @return 行数
	 * @throws IOException 入出力例外
	 */
	public long countFileLines(Context context, String fileName, boolean nullCharDenyFlag) throws IOException {
		
		String line = null;
		long count = 0;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				context.getResources().getAssets().open(fileName)), CommonUtils.DEF_BUFF_SIZE_8K);
		while((line = br.readLine()) != null) {
			if (nullCharDenyFlag == true && line.equals(""))
				continue;
			
			count++;
		}
		
		return count;
	}
}
