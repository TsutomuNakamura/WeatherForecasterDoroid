package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 気象予報データ格納Bean
 * @author tsutomu
 */
public class WeatherBean {
	
	/** 予報する気象の日時 */
	private String stringDate;
	
	/** 予報する気象の曜日 */
	private int dayOfWeek;
	
	/** 観測日 */
	private String announceDate;
	
	/** 気象 */
	private String weather;
	
	/** 気象アイコン */
	private int weatherIconId;
	
	/** 最高気温 */
	private Integer highestTemperature;
	
	/** 最低気温 */
	private Integer lowestTemperature;
	
	/** 降水確率 */
	private Map<String, Integer> chanceOfRain = new HashMap<String, Integer>();
	
	/** 全国の気象解説文発表日時 */
	private String wholeOfCommunityWeatherCommentPubDate;
	
	/** 全国の気象解説文 */
	private String wholeOfCommunityWeatherComment;
	
	/** 都道府県の気象解説文発表日時 */
	private String administrativeAreaWeatherCommentPubDate;
	
	/** 都道府県の気象解説文 */
	private String administrativeAreaWeatherComment;
	
	/** 風向き */
	private String windDirection;
	
	/** 風速 */
	private int windSpeed;

	/**
	 * 予報する気象の日時を取得する
	 * @return 予報する気象の日時
	 */
	public String getStringDate() {
		return stringDate;
	}

	/**
	 * 予報する気象の日時を設定する
	 * @param stringDate 予報する気象の日時
	 */
	public void setStringDate(String stringDate) {
		this.stringDate = stringDate;
	}
	
	/**
	 * 予報する気象の曜日を取得する
	 * @return 予報する気象の曜日
	 */
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	
	/**
	 * 予報する気象の曜日を設定する
	 * @param dayOfWeek
	 */
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	/**
	 * 観測日を取得する
	 * @return 観測日
	 */
	public String getAnnounceDate() {
		return announceDate;
	}
	
	/**
	 * 観測日を設定する
	 * @param announceDate 観測日
	 */
	public void setAnnounceDate(String announceDate) {
		this.announceDate = announceDate;
	}

	/**
	 * 気象を取得する
	 * @return 気象
	 */
	public String getWeather() {
		return weather;
	}

	/**
	 * 気象を設定する
	 * @param weather 気象
	 */
	public void setWeather(String weather) {
		this.weather = weather;
	}
	
	/**
	 * 気象アイコンを取得する
	 * @return 気象アイコン
	 */
	public int getWeatherIconId() {
		return weatherIconId;
	}
	
	/**
	 * 気象アイコンを設定する
	 * @param weatherIconId 気象アイコン
	 */
	public void setWeatherIconId(int weatherIconId) {
		this.weatherIconId = weatherIconId;
	}

	/**
	 * 最高気温を取得する
	 * @return 最高気温
	 */
	public Integer getHighestTemperature() {
		return highestTemperature;
	}

	/**
	 * 最高気温を設定する
	 * @param highestTemperature 最高気温
	 */
	public void setHighestTemperature(Integer highestTemperature) {
		this.highestTemperature = highestTemperature;
	}

	/**
	 * 最低気温を取得する
	 * @return 最低気温
	 */
	public Integer getLowestTemperature() {
		return lowestTemperature;
	}

	/**
	 * 最低気温を設定する
	 * @param lowestTemperature 最低気温
	 */
	public void setLowestTemperature(Integer lowestTemperature) {
		this.lowestTemperature = lowestTemperature;
	}

	/**
	 * 降水確率を取得する
	 * @return 降水確率
	 */
	public Map<String, Integer> getChanceOfRain() {
		return chanceOfRain;
	}

	/**
	 * 降水確率を設定する
	 * @param chanceOfRain 降水確率
	 */
	public void setChanceOfRain(Map<String, Integer> chanceOfRain) {
		this.chanceOfRain = chanceOfRain;
	}
	
	/**
	 * 降水確率のMapに値を追加する
	 * @param hour 時間帯
	 * @param probability 降水確率
	 */
	public void putChanceOfRain(String hour, int probability) {
		chanceOfRain.put(hour, probability);
	}
	
	/**
	 * 全国の気象解説文発表日時を取得する
	 * @return 全国の気象解説文発表日時
	 */
	public String getWholeOfCommunityWeatherCommentPubDate() {
		return wholeOfCommunityWeatherCommentPubDate;
	}
	
	/**
	 * 全国の気象解説文発表日時を設定する
	 * @param wholeOfCommunityWeatherCommentPubDate 全国の気象解説文
	 */
	public void setWholeOfCommunityWeatherCommentPubDate(String wholeOfCommunityWeatherCommentPubDate) {
		this.wholeOfCommunityWeatherCommentPubDate = wholeOfCommunityWeatherCommentPubDate;
	}
	
	/**
	 * 全国の気象解説文を取得する
	 * @return 全国の気象解説文
	 */
	public String getWholeOfCommunityWeatherComment() {
		return wholeOfCommunityWeatherComment;
	}

	/**
	 * 全国の気象解説文を設定する
	 * @param wholeOfCommunityWeatherComment 全国の気象解説文
	 */
	public void setWholeOfCommunityWeatherComment(
			String wholeOfCommunityWeatherComment) {
		this.wholeOfCommunityWeatherComment = wholeOfCommunityWeatherComment;
	}
	
	/**
	 * 都道府県の気象解説文発表日時を取得する
	 * @return 都道府県の気象解説文発表日時
	 */
	public String getAdministrativeAreaWeatherCommentPubDate() {
		return administrativeAreaWeatherCommentPubDate;
	}
	
	/**
	 * 都道府県の気象解説文発表日時を設定する
	 * @param administrativeAreaWeatherCommentPubDate 都道府県の気象解説文発表日時
	 */
	public void setAdministrativeAreaWeatherCommentPubDate(String administrativeAreaWeatherCommentPubDate) {
		this.administrativeAreaWeatherCommentPubDate = administrativeAreaWeatherCommentPubDate;
	}
	
	/**
	 * 都道府県の気象解説文を取得する
	 * @return 都道府県の気象解説文
	 */
	public String getAdministrativeAreaWeatherComment() {
		return administrativeAreaWeatherComment;
	}

	/**
	 * 都道府県の気象解説文を設定する
	 * @param administrativeAreaWeatherComment 都道府県の気象解説文
	 */
	public void setAdministrativeAreaWeatherComment(
			String administrativeAreaWeatherComment) {
		this.administrativeAreaWeatherComment = administrativeAreaWeatherComment;
	}
	
	/**
	 * 風向きを取得する
	 * @return 風向き
	 */
	public String getWindDirection() {
		return windDirection;
	}
	
	/**
	 * 風向きを設定する
	 * @param windDirection 風向き
	 */
	public void setWindDirection(String windDirection) {
		this.windDirection = windDirection;
	}
	
	/**
	 * 風速を取得する
	 * @return 風速
	 */
	public int getWindSpeed() {
		return windSpeed;
	}
	
	/**
	 * 風速を設定する
	 * @param windSpeed 風速
	 */
	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
	}
}
