package app_lib;

public class Defs_ap_test2 {
	// initial value
	public static final String version="1.0";
	private static final String pg_dir="/home/nishi/workspace-raspi/nishi-test";
	private static final String nd_pg_dir="/home/pi-nishi/workspace-raspi/nishi-test";

	private static final String data_dir=pg_dir+"/data";

	private static final String tmp_dir=pg_dir+"/tmp";
	private static final String nd_tmp_dir=nd_pg_dir+"/tmp";

	public static final String nd_pg_bat=nd_pg_dir+"/node_ap_test2.bat";

	public static final String db=data_dir+"/node_data.db";
	public static final String act_f=tmp_dir+"/test2.act";	// 温度情報収集中
	public static final String run_f=tmp_dir+"/test2.run";	// host_ap_test2 deamon running sign
	public static final String stop_f=tmp_dir+"/test2.stop";	// host_ap_test2 deamon stop sign

	public static final String nd_act_f=nd_tmp_dir+"/nd.test2.act";	// node_ap_test2 deamon 温度情報収集中
	public static final String nd_run_f=nd_tmp_dir+"/nd.test2.run";	// node_ap_test2 deamon running sign
	public static final String nd_stop_f=nd_tmp_dir+"/nd.test2.stop";	// node_ap_test2 deamon stop sign

	public static final String stater="http://192.168.1.180/class-mod/node_app_test.node_ap_test2_mod";
	public static final String recever="http://192.168.1.150/class-mod/host_app_test.host_ap_test2_rcv";
}
