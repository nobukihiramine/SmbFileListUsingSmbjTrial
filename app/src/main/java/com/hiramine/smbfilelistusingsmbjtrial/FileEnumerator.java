package com.hiramine.smbfilelistusingsmbjtrial;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;

public class FileEnumerator
{
	// 定数
	private static final String LOGTAG = "FileEnumerator";

	public static final int RESULT_SUCCEEDED                    = 0;
	public static final int RESULT_FAILED_UNKNOWN_HOST          = 1;
	public static final int RESULT_FAILED_NO_ROUTE_TO_HOST      = 2;
	public static final int RESULT_FAILED_LOGON_FAILURE         = 3;
	public static final int RESULT_FAILED_BAD_NETWORK_NAME      = 4;
	public static final int RESULT_FAILED_NOT_FOUND             = 5;
	public static final int RESULT_FAILED_NOT_A_DIRECTORY       = 6;
	public static final int RESULT_FAILED_FUNCTION_AUTHENTICATE = 11;
	public static final int RESULT_FAILED_FUNCTION_CONNECTSHARE = 12;
	public static final int RESULT_FAILED_FUNCTION_LIST         = 13;
	public static final int RESULT_FAILED_UNKNOWN               = 99;

	// スレッドの作成と開始
	public void startEnumeration( Handler handler,
								  String strTargetPath,
								  String strUsername,
								  String strPassword )
	{
		Thread thread = new Thread( () -> threadfuncEnumerate( handler,
															   strTargetPath,
															   strUsername,
															   strPassword ) );
		thread.start();
	}

	// スレッド関数
	private void threadfuncEnumerate( Handler handler,
									  String strTargetPath,
									  String strUsername,
									  String strPassword )
	{
		Log.d( LOGTAG, "Enumeration thread started." );

		// TargetPathから、HostName,ShareName,Pathを切り出す。
		// smb://hostname/sharename/directory1/directory2/filename
		Uri           uri                = Uri.parse( strTargetPath );
		String        strHostName        = uri.getHost();    // HostNameの切り出し
		List<String>  liststrPathSegment = uri.getPathSegments();
		String        strShareName       = liststrPathSegment.get( 0 );    // パスセグメントの先頭がShareName
		StringBuilder sb                 = new StringBuilder();
		// パスセグメントの先頭以外を「\\」で連結し、Pathを作る
		for( int i = 1; i < liststrPathSegment.size(); i++ )
		{
			if( sb.length() > 0 )
			{
				sb.append( "\\" );
			}
			sb.append( liststrPathSegment.get( i ) );
		}
		String strPath = sb.toString();

		String strDomain = "";    // Domainとして、空文字の他、"WORKGROUP"や適当な文字列など、何を指定しても特に動作変化見られず。

		// 呼び出し元スレッドに返却する用のメッセージ変数の取得
		Message message = Message.obtain( handler );

		try
		{
			// DiskShareの作成
			SmbConfig  smbconfig = SmbConfig.createDefaultConfig();
			SMBClient  smbclient = new SMBClient( smbconfig );
			Connection connection;
			try
			{
				connection = smbclient.connect( strHostName );
			}
			catch( UnknownHostException e )
			{    // ホストが存在しないか、名前解決できない
				message.what = RESULT_FAILED_UNKNOWN_HOST;
				message.obj = null;
				Log.w( LOGTAG, "Enumeration thread end. : Unknown host." );
				return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
			}
			catch( NoRouteToHostException e )
			{    // ホストへのルートがない
				message.what = RESULT_FAILED_NO_ROUTE_TO_HOST;
				message.obj = null;
				Log.w( LOGTAG, "Enumeration thread end. : No route to host." );
				return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
			}
			AuthenticationContext authenticationcontext = new AuthenticationContext( strUsername,
																					 strPassword.toCharArray(),
																					 strDomain );
			Session session;
			try
			{
				session = connection.authenticate( authenticationcontext );
			}
			catch( SMBApiException e )
			{
				if( NtStatus.STATUS_LOGON_FAILURE == e.getStatus() )
				{    // Connection#authenticate()の結果「Logon failure」
					message.what = RESULT_FAILED_LOGON_FAILURE;
					message.obj = null;
					Log.w( LOGTAG, "Enumeration thread end. : Logon failure." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
				else
				{    // Connection#authenticate()の結果「Logon failure」以外で失敗
					message.what = RESULT_FAILED_FUNCTION_AUTHENTICATE;
					message.obj = null;
					Log.e( LOGTAG, "Enumeration thread end. : Function authenticate() failed." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
			}
			DiskShare diskshare;
			try
			{
				diskshare = (DiskShare)session.connectShare( strShareName );
			}
			catch( SMBApiException e )
			{
				if( NtStatus.STATUS_BAD_NETWORK_NAME == e.getStatus() )
				{    // Session#connectShare()の結果「Bad network name」
					message.what = RESULT_FAILED_BAD_NETWORK_NAME;
					message.obj = null;
					Log.w( LOGTAG, "Enumeration thread end. : Bad network name." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
				else
				{    // Session#connectShare()の結果「Bad network name」以外で失敗
					message.what = RESULT_FAILED_FUNCTION_CONNECTSHARE;
					message.obj = null;
					Log.e( LOGTAG, "Enumeration thread end. : Function connectShare() failed." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
			}

			// 列挙
			List<FileIdBothDirectoryInformation> listFileInformation;
			try
			{
				listFileInformation = diskshare.list( strPath );
			}
			catch( SMBApiException e )
			{
				if( NtStatus.STATUS_OBJECT_NAME_NOT_FOUND == e.getStatus()    // ファイルがない場合
					|| NtStatus.STATUS_OBJECT_PATH_NOT_FOUND == e.getStatus() )    // フォルダがない場合
				{    // DiskShare#list()の結果「Not found」
					message.what = RESULT_FAILED_NOT_FOUND;
					message.obj = null;
					Log.w( LOGTAG, "Enumeration thread end. : Not found." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
				else if( NtStatus.STATUS_NOT_A_DIRECTORY == e.getStatus() )
				{	// DiskShare#list()の結果「Not a directory」
					message.what = RESULT_FAILED_NOT_A_DIRECTORY;
					message.obj = null;
					Log.w( LOGTAG, "Enumeration thread end. : Not a directory." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
				else
				{    // DiskShare#list()の結果、原因不明で失敗
					message.what = RESULT_FAILED_FUNCTION_LIST;
					message.obj = null;
					Log.e( LOGTAG, "Enumeration thread end. : Function list() failed." );
					return;    // ※注）関数を抜ける前にfinallyの処理が実行される。
				}
			}

			// SmbFileの配列を、FileItemのリストに変換
			List<FileItem> listFileItem = makeFileItemList( listFileInformation );

			// FileItemリストのソート
			sortFileItemList( listFileItem );

			// 成功
			message.what = RESULT_SUCCEEDED;
			message.obj = listFileItem;
			Log.d( LOGTAG, "Enumeration thread end. : Succeeded." );
		}
		catch( Exception e )
		{	// その他の失敗
			message.what = RESULT_FAILED_UNKNOWN;
			message.obj = e.getMessage();
			Log.e( LOGTAG, "Enumeration thread end. : Failed with unknown cause." );
		}
		finally
		{
			// 呼び出し元スレッドにメッセージ返却
			handler.sendMessage( message );
		}
	}

	// SmbFileの配列を、FileItemのリストに変換
	private static List<FileItem> makeFileItemList( @Nullable List<FileIdBothDirectoryInformation> listFileInformation )
	{
		if( null == listFileInformation )
		{    // nullの場合は、空のリストを返す。
			return new ArrayList<>();
		}

		List<FileItem> listFileItem = new ArrayList<>( listFileInformation.size() );    // 数が多い場合も想定し、初めに領域確保。
		for( FileIdBothDirectoryInformation fileinfo : listFileInformation )
		{
			if( fileinfo.getFileName().equals( "." )
				|| fileinfo.getFileName().equals( ".." ) )
			{    // 自身と親のパスである「.」と「..」は除外する
				continue;
			}
			listFileItem.add( createFileItem( fileinfo ) );
		}
		return listFileItem;
	}

	// FileIdBothDirectoryInformationデータから、FileItemデータの作成
	public static FileItem createFileItem( FileIdBothDirectoryInformation fileinfo )
	{
		FileItem.Type type;
		long          lAttributes = fileinfo.getFileAttributes();
		if( EnumWithValue.EnumUtils.isSet( lAttributes, FileAttributes.FILE_ATTRIBUTE_DIRECTORY ) )
		{
			type = FileItem.Type.DIRECTORY;
		}
		else
		{
			type = FileItem.Type.FILE;
		}

		long lLastModified = fileinfo.getLastWriteTime().toEpochMillis();

		long lFileSize = 0;
		if( FileItem.Type.FILE == type )
		{
			lFileSize = fileinfo.getEndOfFile();
		}

		return new FileItem( fileinfo.getFileName(),
							 fileinfo.getFileName(),
							 type,
							 lLastModified,
							 lFileSize );
	}

	// FileItemリストのソート
	public void sortFileItemList( List<FileItem> listFileItem )
	{
		Collections.sort( listFileItem, new FileItem.FileItemComparator() );
	}
}
