/*
 * java_class_call_test.c
 * C から JNI を使ってコールするテストプログラムです。
 * コールするのは、 prog で指定しますが、
 * nishi-test/bin/mod_test/simple_cgi_mod です。
 *  Created on: 2015/11/12
 *      Author: nishi
 */
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

int main(void){
	printf("java_class_call_test.c start!\n"); /* prints !!!Hello World!!! */

	char *dir_s="/home/nishi/workspace-raspi/nishi-test2/bin";
	char *prog="mod_test/simple_cgi_mod";
	//注) Java Http Server の URL表記では、mod_test.simple_cgi_mod ですが。
	// これでは、見付かりません。

	chdir(dir_s);

	JNIEnv *jenv;
	JavaVM *jvm;
	JNIEnv jni;
	JavaVM vmi;
	JavaVMInitArgs vm_args;
	JavaVMOption options[4];

	options[0].optionString = (char *)"-Djava.class.path=.:/home/nishi/workspace-raspi/nishi-test/bin:/usr/java/jdk1.7.0_80/jre/lib/rt.jar";
	options[1].optionString = (char *)"-Djava.compiler=NONE";
	vm_args.version = JNI_VERSION_1_4;
	vm_args.options = options;
	vm_args.nOptions = 2;
	vm_args.ignoreUnrecognized = JNI_FALSE;
	//vm_args.ignoreUnrecognized = JNI_TRUE;

	/*
	JNIEnv *jnienv;
	JavaVM *javavm;
	JavaVMInitArgs vm_args;

	JavaVMOption options[1];
	options[0].optionString = "-Djava.class.path=.:/home/nishi/workspace-raspi/nishi-test/bin:/usr/java/jdk1.7.0_80/jre/lib/rt.jar";
	vm_args.version = JNI_VERSION_1_4;
	vm_args.options = options;
	vm_args.nOptions = 1;
	vm_args.ignoreUnrecognized = true;
	*/

	/*
	 * JavaVMを初期化，起動する
	 * JNIインターフェースへのポインタを返す
	 */
	//int result = JNI_CreateJavaVM(&javavm, (void **)&jnienv, &vm_args);


	JNI_CreateJavaVM(&jvm,(void **)&jenv,&vm_args);
	jni = *jenv;
	vmi = *jvm;


	/*
	 * クラスをさがす
	 */
	//jclass cls = jnienv->FindClass(prog);
	jclass cls = jni->FindClass(jenv, prog);
	if (cls == 0) {
		printf("cgiProcessor() :#5 cannot found %s\n",prog);
		return 1;
	}
	/*
	 * Methodをさがす
	 * GetStaticMethodIDの引数については後述
	 */
	jmethodID mid = jni->GetMethodID(jenv,cls, "cgi_go2", "(Ljava/lang/String;Ljava/lang/String;)V");
	//jmethodID mid = jni->GetStaticMethodID(env, cls, "testmethod", "()V");
	if (mid == 0) {
		printf("cgiProcessor() :#6 Could not locate method cgi_go with signature ()V");
		return 1;
	}
	/*
	 * インスタンス作成 -> これで実行されるみたい。
	 * パラメータもここで渡す必要がある。
	 */
	jstring method =jni->NewStringUTF(jenv,"GET");
	jstring parms =jni->NewStringUTF(jenv,"hello=java");
	jobject obj = jni->NewObject(jenv,cls, mid, method,parms);

	/*
	 * method() の実行
	 */
	//jni->CallVoidMethod(env, cls, mid, "hello","java");

	//何らかの処理が終わって、終了するときにはついでにJavaVMも終わらす必要があります.
	jint result = vmi->DestroyJavaVM(jvm);

	return EXIT_SUCCESS;

}

