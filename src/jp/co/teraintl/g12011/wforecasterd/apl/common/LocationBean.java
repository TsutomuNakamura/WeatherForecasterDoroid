package jp.co.teraintl.g12011.wforecasterd.apl.common;

/**
 * 地域情報を格納するBean
 * @author tsutomu
 */
public class LocationBean {
	
	/** シーケンシャルID */
	private int id;
	
	/** ウィジェットID */
	private int widgetId;
	
	/** JSON ID */
	private String jsonId;
	
	/** 更新回数 */
	private long updateCount;
	
	/** gps フラグ */
	private boolean gpsFlag;
	
	/** 緯度 */
	private double latitude;
	
	/** 経度 */
	private double longitude;
	
	/** 正確さ */
	private int accuracy;
	
	/** 国名コード */
	private String countryNameCode;
	
	/** 国名 */
	private String countryName;
	
	/** 地方名 */
	private String regionName;
	
	/** 都道府県名 */
	private String administrativeAreaName;
	
	/** 群などの集落名 */
	private String subAdministrativeAreaName;
	
	/** 市区町村名 */
	private String localityName;
	
	/** 表示用市区町村名 */
	private String vLocalityName;
	
	/** RSS URLスキーム */
	private String rssUrl;
	
	/** 更新日<br /> SQLiteの仕様に合わせて、Date型ではなく文字列型とする */
	private String updateDate;
	
	/** 新規登録日。<br /> SQLiteの仕様に合わせて、Date型ではなく文字列型とする */
	private String registrationDate;
	
	/**
	 * シーケンシャルIDを取得する
	 * @return シーケンシャルID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * シーケンシャルIDを設定する
	 * @param id シーケンシャルID
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * ウィジェットIDを取得する
	 * @return ウィジェットID
	 */
	public int getWidgetId() {
		return widgetId;
	}
	
	/**
	 * ウィジェットIDを設定する
	 * @param widgetId ウィジェットID
	 */
	public void setWidgetId(int widgetId) {
		this.widgetId = widgetId;
	}
	
	/**
	 * JSON ID を取得する
	 * @return JSON ID
	 */
	public String getJsonId() {
		return jsonId;
	}
	
	/**
	 * JSON ID を設定する
	 * @param jsonId JSON ID
	 */
	public void setJsonId(String jsonId) {
		this.jsonId = jsonId;
	}
	
	/**
	 * 更新回数を取得する
	 * @return 更新回数
	 */
	public long getUpdateCount() {
		return updateCount;
	}
	
	/**
	 * 更新回数を設定する
	 * @param updateCount 更新回数
	 */
	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}
	
	/**
	 * gps フラグを取得する
	 * @return gps フラグ
	 */
	public boolean getGpsFlag() {
		return gpsFlag;
	}
	
	/**
	 * gps フラグを設定する
	 * @param gpsFlag gps フラグ
	 */
	public void setGpsFlag(boolean gpsFlag) {
		this.gpsFlag = gpsFlag;
	}
	
	/**
	 * 緯度を取得する
	 * @return 緯度
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * 緯度を設定する
	 * @param latitude 緯度
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * 経度を取得する
	 * @return 経度
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * 経度を設定する
	 * @param longitude 経度
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * 正確さを取得する
	 * @return 正確さ
	 */
	public int getAccuracy() {
		return accuracy;
	}
	
	/**
	 * 正確さを設定する
	 * @param accuracy 正確さ
	 */
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
	
	/**
	 * 国名コードを取得する
	 * @return 国名コード
	 */
	public String getCountryNameCode() {
		return countryNameCode;
	}
	
	/**
	 * 国名コードを設定する
	 * @param countryNameCode 国名コード
	 */
	public void setCountryNameCode(String countryNameCode) {
		this.countryNameCode = countryNameCode;
	}
	
	/**
	 * 国名を取得する
	 * @return 国名
	 */
	public String getCountryName() {
		return countryName;
	}
	
	/**
	 * 国名を設定する
	 * @param countryName 国名
	 */
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	/**
	 * 地方名を取得する
	 * @return 地方名
	 */
	public String getRegionName() {
		return regionName;
	}
	
	/**
	 * 地方名を設定する
	 * @param regionName 地方名
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	/**
	 * 都道府県名を取得する
	 * @return 都道府県名
	 */
	public String getAdministrativeAreaName() {
		return administrativeAreaName;
	}
	
	/**
	 * 都道府県名を設定する
	 * @param administrativeAreaName 都道府県名
	 */
	public void setAdministrativeAreaName(String administrativeAreaName) {
		this.administrativeAreaName = administrativeAreaName;
	}
	
	/**
	 * 郡などの集落名を取得する
	 * @return 郡などの集落名
	 */
	public String getSubAdministrativeAreaName() {
		return subAdministrativeAreaName;
	}
	
	/**
	 * 郡などの集落名を設定する
	 * @param subAdministrativeAreaName 郡などの集落名
	 */
	public void setSubAdministrativeAreaName(String subAdministrativeAreaName) {
		this.subAdministrativeAreaName = subAdministrativeAreaName;
	}
	
	/**
	 * 市区町村名を取得する。
	 * 海外の地域を保持する場合は、都市名を取得する。
	 * @return 市区町村名・都市名
	 */
	public String getLocalityName() {
		return localityName;
	}
	
	/**
	 * 市区町村名を設定する
	 * 海外の地域を保持する場合は、都市名を設定する
	 * @param localityName 市区町村名・都市名
	 */
	public void setLocalityName(String localityName) {
		this.localityName = localityName;
	}
	
	/**
	 * 表示用市区町村名を取得する
	 * @return 表示用市区町村名
	 */
	public String getVLocalityName() {
		return vLocalityName;
	}
	
	/**
	 * 表示用市区町村名を設定する
	 * @param vLocalityName 表示用市区町村名
	 */
	public void setVLocalityName(String vLocalityName) {
		this.vLocalityName = vLocalityName;
	}
	
	/**
	 * RSS URL を取得する
	 * @return RSS URL
	 */
	public String getRssUrl() {
		return rssUrl;
	}
	
	/**
	 * RSS URL を設定する
	 * @param rssUrl RSS URL
	 */
	public void setRssUrl(String rssUrl) {
		this.rssUrl = rssUrl;
	}

	/**
	 * 更新日を取得する
	 * @return 更新日
	 */
	public String getUpdateDate() {
		return updateDate;
	}
	
	/**
	 * 更新日を設定する
	 * @param updateDate 更新日
	 */
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	
	/**
	 * 新規登録日を取得する
	 * @return 新規登録日
	 */
	public String getRegistrationDate() {
		return registrationDate;
	}
	
	/**
	 * 新規登録日を設定する
	 * @param registrationDate 新規登録日
	 */
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	
	/**
	 * このLocationBean オブジェクトと、引数で指定されたオブジェクトが内容的に同等なものか検証する。<br />
	 * 国コード(countryNameCode)と、都道府県名(administrativeAreaName)と、
	 * 郡名(SubAdministrativeAreaName), 市区町村名の文字列が同じであれば同等であるとする。
	 * @param locationBean 比較対象locationBean
	 * @return 検証結果
	 */
	public boolean equals(LocationBean locationBean) {
		
		if (
				this.getWidgetId() == locationBean.getWidgetId()
				&& validateStrValEquality(
						this.getCountryNameCode(), locationBean.getCountryNameCode())
				&& validateStrValEquality(
						this.getAdministrativeAreaName(), locationBean.getAdministrativeAreaName())
				&& validateStrValEquality(
						this.getSubAdministrativeAreaName(), locationBean.getSubAdministrativeAreaName())
				&& validateStrValEquality(
						this.getLocalityName(), locationBean.getLocalityName())
				) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 第1引数に指定された文字列と第2引数に指定された文字列が内容的に同等なものか検証する。<br />
	 * 両方の文字列の参照がnull pinter であった場合true を返す仕様 
	 * @param value1 文字列1
	 * @param value2 文字列2
	 * @return 検証結果
	 */
	private boolean validateStrValEquality(String value1, String value2) {
		
		if ((value1 == null) && (value2 == null))
			return true;
		else if ((value1 == null && value2 != null) || (value1 != null && value2 == null))
			return false;
		
		return value1.equals(value2);
	}
	
	/**
	 * 地域情報を格納するBean とその内容を別オブジェクトとして複製する。<br />
	 * 別オブジェクトとして作成されるため、変更の影響範囲がナローになる。
	 * @return 複製されたインスタンス
	 */
	public LocationBean getCopyDeep() {
		
		LocationBean ret = new LocationBean();
		
		ret.setId(this.id);
		ret.setWidgetId(this.widgetId);
		if (this.jsonId != null)
			ret.setJsonId(new String(this.jsonId));
		ret.setUpdateCount(this.updateCount);
		ret.setGpsFlag(this.gpsFlag);
		ret.setLatitude(this.latitude);
		ret.setLongitude(this.longitude);
		ret.setAccuracy(this.accuracy);
		if (this.countryNameCode != null)
			ret.setCountryNameCode(new String(this.countryNameCode));
		if (this.countryName != null)
			ret.setCountryName(new String(this.countryName));
		if (this.regionName != null)
			ret.setRegionName(new String(this.regionName));
		if (this.administrativeAreaName != null)
			ret.setAdministrativeAreaName(new String(this.administrativeAreaName));
		if (this.subAdministrativeAreaName != null)
			ret.setSubAdministrativeAreaName(new String(this.subAdministrativeAreaName));
		if (this.localityName != null)
			ret.setLocalityName(new String(this.localityName));
		if (this.vLocalityName != null)
			ret.setVLocalityName(new String(this.vLocalityName));
		if (this.rssUrl != null)
			ret.setRssUrl(new String(this.rssUrl));
		if (this.updateDate != null)
			ret.setUpdateDate(new String(this.updateDate));
		if (this.registrationDate != null)
			ret.setRegistrationDate(new String(this.registrationDate));
		
		return ret;
	}
}
