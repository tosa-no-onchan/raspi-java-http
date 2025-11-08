package host_app_test;

public class Defs_ap_test1 {
	// initial value
	static final String version="1.0";
	private static final String pg_dir="/home/nishi/workspace-raspi/nishi-test";
	private static final String data_dir=pg_dir+"/data";
	private static final String tmp_dir=pg_dir+"/tmp";
	static final String pg_bat=pg_dir+"/host_ap_test1.bat";
	static final String db=data_dir+"/node_data.db";
	static final String act_f=tmp_dir+"/test1.act";	// 温度情報収集中
	static final String run_f=tmp_dir+"/test1.run";	// host_ap_test1 deamon running sign
	static final String stop_f=tmp_dir+"/test1.stop";	// host_ap_test1 deamon stop sign
}
