package user_cgi_lib;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
 * 作成日: 2007/08/03
 *
 * TODO この生成されたファイルのテンプレートを変更するには次へジャンプ:
 * ウィンドウ - 設定 - Java - コード・スタイル - コード・テンプレート
 */

/**
 * @author nishi
 *
 *	commandExec Class :
 */
public class CommandExec {
	public List<String> result;
	public List<String> errors;
	public CommandExec(){
		
	}
	/**
	 * 外部コマンド実行 結果を返さない
	 */
	public int command_exec_noresp(String cmd_s[]){
		int rc=0;
		String line;
	    result = new ArrayList<String>();
	    errors = new ArrayList<String>();

		//String cmd_s[] = {"ls","-la"};
		try{
			Process ps = Runtime.getRuntime().exec(cmd_s);
			// エラー結果を表示します
			BufferedReader in = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
			while ((line = in.readLine()) != null ){
				rc=-1;
				errors.add(line);
			}
		}
		catch(SecurityException e){
			rc=-2;
		}
		catch(IOException e){
			rc=-3;
		}
		return rc;
	}	
	/**
	 * 外部コマンド実行 結果を返す
	 */
	public int command_exec(String cmd_s[]){
		int rc=0;
		String line;
	    result = new ArrayList<String>();
	    errors = new ArrayList<String>();
		//String cmd_s[] = {"ls","-la"};
		try{
			Process ps = Runtime.getRuntime().exec(cmd_s);
			// エラー結果を表示します
			BufferedReader in = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
			while ((line = in.readLine()) != null ){
				rc=-1;
				errors.add(line);
			}
			//実行結果を取得
			BufferedReader in2 = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			while ((line = in2.readLine()) != null ){
				result.add(line);
			}
			//ps.destroy();
			//rc = ps.exitValue();
			//System.out.println("debTermMovie command_exec() : #3 rc="+rc);
		}
		catch(SecurityException e){
			rc=-2;
		}
		catch(IOException e){
			rc=-3;
		}
		return rc;
	}	

}
