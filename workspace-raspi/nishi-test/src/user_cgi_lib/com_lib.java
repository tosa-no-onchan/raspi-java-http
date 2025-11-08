package user_cgi_lib;

import java.util.*;
import java.text.*;
/**
 * タイトル:  ネットモール土佐　カート帳票
 * 説明:
 * 著作権:   Copyright (c) 2001
 * 会社名:   ネットモール土佐
 * @author 西村信和
 * @version 1.1
 * update 2014.2.25
 */

public class com_lib{
  static boolean match;
  public com_lib() {
  }
  /**
   *  デリミター"," による分割を行います。
   */
  public static Hashtable<String,String> rec_separate(String in_s){
	  String key,s;
	  Hashtable<String,String> recs = new Hashtable<String, String>();
	  if(in_s != null){
		  StringTokenizer t = new StringTokenizer(in_s,",");
		  while (t.hasMoreTokens()) {
			  String rec = t.nextToken();
			  List<String> val = rec_div(rec,"=");
			  if(val.size() >= 1){
				  key=val.get(0);
				  s="";
				  if(val.size() >= 2){
					  s=val.get(1);
				  }
				  recs.put(key,s);
			  }
		  }
	  }
	  return recs;
  }
  /**
   *  パラメータの分割を行います。
   */
  public static List<String> rec_div(String s,String mark){
	  List<String> val = new ArrayList<String>();
	  if(s == null){
		  return val;
	  }
	  int prv=0;
	  for(int pos=0;;){
		  pos=s.indexOf(mark,pos);
		  // mark がありました
		  if(pos >=0){
			  if((pos - prv) > 0){
				  val.add(s.substring(prv,pos));
			  }
			  else{
				  val.add("");
			  }
			  pos ++;
			  prv = pos;
		  }
		  // mark が無です
		  else{
			  pos = s.length();
			  if((pos - prv) > 0){
				  val.add(s.substring(prv,pos));
			  }
			  break;
		  }
	  }
	  return val;
  }
  /**
   *  パラメータの分割を行います。
   */
  public static String[] rec_div_old(String s,String mark){
    String[] val;
    int cnt=1;
    if(s == null){
      val=new String[0];
      return val;
    }
    for(int pos=0;(pos=s.indexOf(mark,pos))>=0;cnt++){
      pos+=mark.length();
    }
    val=new String[cnt];
    if(cnt == 1){
      val[0] = s;
    }
    else{
      int prv=0;
      cnt=0;
      for(int pos=0;;cnt++){
        pos=s.indexOf(mark,pos);
        if(pos >=0){
          if((pos - prv) > 0){
            val[cnt] = s.substring(prv,pos);
          }
          else{
            val[cnt] = "";
          }
          pos += mark.length();
          prv = pos;
        }
        else{
          pos = s.length();
          if((pos - prv) > 0){
            val[cnt] = s.substring(prv,pos);
          }
          else{
            val[cnt] = "";
          }
          break;
        }
      }
    }
    return val;
  }
  /**
   *  パラメータの分割を行います。バグ版です。
   */
  public static String[] rec_div_bug(String s,String mark){
    StringTokenizer t = new StringTokenizer(s,mark);
    String[] val = new String[t.countTokens()];
    int i=0;
    while (t.hasMoreTokens()) {
      val[i++] = t.nextToken();
    }
    return val;
  }
  /**
   *  Stringの変換を行います。
   *  s : 被編集文字列
   *  old_s : 置換前文字列
   *  new_s : 置換後文字列
   */
  public static String replace(String s,String old_s,String new_s) {
    String o_s;
    o_s = s.replaceAll(old_s,new_s);
    return o_s;
  }
  /**
   *  Stringの先頭文字の変換を行います。
   *  s : 被編集文字列
   *  new_s : 置換後文字列
   */
  public static String replaceStart(String s,String new_s){
	String o_s = new_s+s.substring(new_s.length());
    return o_s;
  }
  /**
   *  Stringの変換を行います。
   *  s : 被編集文字列
   *  old_s : 置換前文字列
   *  new_s : 置換後文字列
   */
  public static String replace_old(String s,String old_s,String new_s){
    String o_s;
    o_s="";
    StringTokenizer t = new StringTokenizer(s,old_s);
    while (t.hasMoreTokens()) {
      o_s += (t.nextToken() + new_s);
    }
    return o_s;
  }
  /**
   * 文字列→１６進コード変換
   */
  public static String StringtoHex(String in){
	  // 文字コードを１６進に変換します
	  String h;
	  String o="";
	  byte[] b = in.getBytes();
	  for (int i =0; i < b.length;i++){
		  h = Integer.toHexString(b[i]).toUpperCase();
		  int l = h.length();
		  //System.out.println("\nform_lib:urlEncode() : #2 h="+h);
		  if(l > 2){
			  l -= 2;
			  o += h.substring(l);
		  }
		  else if(l == 1){
			  o += "0"+h;
		  }
		  else{
			  o += h;
		  }
	  }
	  return o;
  }
  /**
   * バイト文字列→１６進コード変換
   */
  public static String BytetoHex(byte[] b,int lg){
	  // 文字コードを１６進に変換します
	  String h;
	  String o="";
	  //byte[] b = in.getBytes();
	  for (int i =0; i < lg;i++){
		  h = Integer.toHexString(b[i]).toUpperCase();
		  int l = h.length();
		  //System.out.println("\nform_lib:urlEncode() : #2 h="+h);
		  if(l > 2){
			  l -= 2;
			  o += h.substring(l);
		  }
		  else if(l == 1){
			  o += "0"+h;
		  }
		  else{
			  o += h;
		  }
	  }
	  return o;
  }
  /**
   *  Spaceの挿入を行います。
   */
  public static String insertSpace(String i_s){
    String o_s="";
    int l = i_s.length();
    boolean sw=true;
    for(int i=0;i<l;i++){
      if(sw){
        o_s += i_s.charAt(i);
        sw=false;
      }
      else{
        o_s += (" " + i_s.charAt(i));
      }
    }
    return o_s;
  }
  /**
  *  今日のシステム日付、時刻を取得します。
  * String getDateTime(String format_s)
  *  format_s: 変換フォーマット
  *    "yyyy/M/d H:m:s" -> 戻り情報: "yyyy/m/d h:m:s"
  *    "yyyy/M/d" -> 戻り情報: "yyyy/m/d"
  *    "yyyy/MM/dd" -> 戻り情報: "yyyy/mm/md"
  */
  public static String getDateTime(String format_s){
	  if(format_s.equals("")==true){
		  format_s="yyyy/M/d H:m:s";
	  }
	  Date date = new Date(System.currentTimeMillis());
	  SimpleDateFormat sdf = new SimpleDateFormat(format_s);
	  return sdf.format(date);
  }
  /**
   * isHhmmNow(String hhmm_s,String hhmm_e) : 今の時刻が範囲内かチェック
   * 今の時刻が hhmm_s("hh:mm") と hhmm_e("hh:mm") の間かチェックします
   * 入力情報
   * 	String hhmm_s : 開始時間 "hh:mm"
   * 	String hhmm_e : 終了時間 "hh:mm"
   * 出力情報
   * 	boolean rc :  yes / no  → true / false
   */
	public static boolean isHhmmNow(String hhmm_s,String hhmm_e){
		boolean rc = false;
	    Calendar cal = Calendar.getInstance();
	    int hh = cal.get(Calendar.HOUR_OF_DAY);
	    int mm = cal.get(Calendar.MINUTE);
	    DecimalFormat df = new DecimalFormat("0");
	    df.setMinimumIntegerDigits(2);
	    String hhmm_now = df.format(hh)+":"+df.format(mm);

		// hhmm_s と hhmme は同日です。
		if(hhmm_s.compareTo(hhmm_e) < 0){
			//今の時刻は、hhmm_sより後で、hhmm_eより前であれば OK
			if(hhmm_now.compareTo(hhmm_s) >=0 && hhmm_now.compareTo(hhmm_e) <=0){
				rc=true;
			}
		}
		// hhmm_s と hhmme は日付を跨ぎます。
		else{
			// 今の時刻が hhmm_s より後か hhmm_e より前であれば OK
			if(hhmm_now.compareTo(hhmm_s) >= 0 || hhmm_now.compareTo(hhmm_e) <= 0){
				rc = true;
			}
		}
		return rc;
	}
}

