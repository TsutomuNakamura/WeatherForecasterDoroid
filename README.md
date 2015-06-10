# 気象予報士ドロイド君 #
課題用Android アプリケーション気象予報士ドロイド君のソースリポジトリです。

![ウィジェット表示の様子](https://github.com/TsutomuNakamura/WeatherForecasterDoroid/wiki/img/doroidkun01.png)
![天気一覧](https://github.com/TsutomuNakamura/WeatherForecasterDoroid/wiki/img/doroidkun02.png)

#* リポジトリ概要  
Android のウィジェットでドロイド君が気象予報をしてくれるアプリケーションです。
これは課題制となっており、プログラミング初心者が実際に動くAndroid アプリケーションを体験しながら、プログラムのソースコードに触るハンズオン形式を目指しています。  
課題を解きながらAndroid プログラミングの初めの一歩を踏み出しましょう。

#* Version  
0.1

# セットアップについて
## とりあえずソースコードがほしい場合
ソースコードはgit リポジトリで管理されています。
とりあえずソースコードのみほしい場合は次のコマンドでサンプルソースコードを取得することができます。
```
$ git clone https://TsutomuNakamura@bitbucket.org/TsutomuNakamura/weatherforecasterdoroid.git
```

## eclipse について
このリポジトリのソースはeclipse で開発されることを想定しています。
2014/09/08 現在、下記のサイトからダウンロードしたEclipse ADT(with the Android SDK for Windows) を使用してください。その他のeclipse についても、問題なく使用できるはずですが、動作確認まではしていません。
```
http://developer.android.com/sdk/index.html
```

Eclipse にてGit clone を行い、ソースコードを取得してください。
```
URI:
https://TsutomuNakamura@bitbucket.org/TsutomuNakamura/weatherforecasterdoroid.git
```

動作確認しているAndroid のAPI バージョンは2.3.3 です。

## 課題について
次のような課題を考案中
+   地域選択画面にて指定の地域を選択しても、その地域が表示されない  
地域選択画面にて地域を指定するとその地方の天気を表示する機能があるが、指定してもその地方の天気予報が表示されない。
ソースコードの問題となっている点を発見し、それを修正する。

+   ある地域を指定しても間違った地域を表示してしまう
地域一覧画面より、地域を指定しても、間違った地域を表示してしまう。
それがどこの地域なのかを発見し、修正する。

+   週間天気予報で、1日分しか表示されない  
ウィジェットをフリックすると週間天気予報が表示される仕様だが、フリックしても1 日分の天気予報しか表示されない。
WorldWeatherOnline の場合は5 日まで表示できるので、そのように改修してもらう。

+   世界天気予報を表示する  
今のところ、日本の天気予報しか表示されないようになっているが、海外の天気予報も表示できるようにする。
自分の好きな都市の緯度、軽度を調べ自分で実装してもらう。

+   GPS機能を追加する(発展)  
余裕のある人は、GPS の機能を追加するなどしてもらう。
よほど余裕がある人向け

## 気象予報情報の取得先について
WORLD WEATHER ONLINE
 http://www.worldweatheronline.com/

