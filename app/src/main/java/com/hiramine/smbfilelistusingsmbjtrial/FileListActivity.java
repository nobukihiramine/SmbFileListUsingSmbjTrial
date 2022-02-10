package com.hiramine.smbfilelistusingsmbjtrial;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class FileListActivity extends AppCompatActivity
{
	// 定数
	private static final String LOGTAG            = "FileListActivity";
	public static final  String EXTRA_TARGET_PATH = "EXTRA_TARGET_PATH";
	public static final  String EXTRA_USERNAME    = "EXTRA_USERNAME";
	public static final  String EXTRA_PASSWORD    = "EXTRA_PASSWORD";

	// メンバー変数
	String          m_strTargetPath;
	FileListAdapter m_filelistadapter;

	// リスナー変数
	private final AdapterView.OnItemClickListener m_onitemclicklistener = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick( AdapterView<?> parent, View view, int position, long id )
		{
			FileItem fileitem = m_filelistadapter.getItem( position );
			Toast.makeText( FileListActivity.this, "Selected : " + fileitem.getPath(), Toast.LENGTH_LONG ).show();
		}
	};

	// Enumerationスレッドの結果取得
	private final Handler m_handlerEnumeration = new Handler( Looper.getMainLooper() )
	{
		@Override
		public void handleMessage( Message message )
		{
			Log.d( LOGTAG, "End Enumeration. : " + m_strTargetPath );
			switch( message.what )
			{
				case FileEnumerator.RESULT_SUCCEEDED: // RESULT_SUCCEEDED:
					@SuppressWarnings( "unchecked" )    // キャストのunchecked警告の抑止
					List<FileItem> listFileItem = (List<FileItem>)message.obj;
					m_filelistadapter = new FileListAdapter( listFileItem );
					ListView listview = findViewById( R.id.listview_main );
					listview.setAdapter( m_filelistadapter );    // 表示更新＆表示位置リセット
					return;
				case FileEnumerator.RESULT_FAILED_UNKNOWN_HOST:
					Toast.makeText( FileListActivity.this, "Unknown host. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_NO_ROUTE_TO_HOST:
					Toast.makeText( FileListActivity.this, "No route to host. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_LOGON_FAILURE:
					Toast.makeText( FileListActivity.this, "Logon failure. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_BAD_NETWORK_NAME:
					Toast.makeText( FileListActivity.this, "Bad network name. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_NOT_FOUND:
					Toast.makeText( FileListActivity.this, "Not found. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_NOT_A_DIRECTORY:
					Toast.makeText( FileListActivity.this, "Not a directory. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_FUNCTION_AUTHENTICATE:
					Toast.makeText( FileListActivity.this, "Function authenticate() failed. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_FUNCTION_CONNECTSHARE:
					Toast.makeText( FileListActivity.this, "Function connectShare() failed. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				case FileEnumerator.RESULT_FAILED_FUNCTION_LIST:
					Toast.makeText( FileListActivity.this, "Function list() failed. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					return;
				default:
					Toast.makeText( FileListActivity.this, "Failed with unknown cause. : " + m_strTargetPath, Toast.LENGTH_LONG ).show();
					finish();
					//return;
			}
		}
	};

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_file_list );

		// 呼び出し元からパラメータ取得
		Bundle extras = getIntent().getExtras();
		m_strTargetPath = extras.getString( EXTRA_TARGET_PATH );
		String strUsername = extras.getString( EXTRA_USERNAME );
		String strPassword = extras.getString( EXTRA_PASSWORD );

		// アイテムクリックリスナー
		ListView listview = findViewById( R.id.listview_main );
		listview.setOnItemClickListener( m_onitemclicklistener );

		// 別スレッドで列挙開始
		Log.d( LOGTAG, "Start Enumeration. : " + m_strTargetPath );
		FileEnumerator fileenumerator = new FileEnumerator();
		fileenumerator.startEnumeration( m_handlerEnumeration,
										 m_strTargetPath,
										 strUsername,
										 strPassword );
	}
}