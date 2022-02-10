package com.hiramine.smbfilelistusingsmbjtrial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.core.content.res.ResourcesCompat;

public class FileListAdapter extends BaseAdapter
{
	public static class ViewHolder
	{
		private final ImageView m_imageviewFileType;
		private final TextView  m_textviewFileName;
		private final TextView  m_textviewLastModified;
		private final TextView  m_textviewFileSize;

		public ViewHolder( View convertView )
		{
			m_imageviewFileType = convertView.findViewById( R.id.imageview_type );
			m_textviewFileName = convertView.findViewById( R.id.textview_filename );
			m_textviewLastModified = convertView.findViewById( R.id.textview_lastmodified );
			m_textviewFileSize = convertView.findViewById( R.id.textview_filesize );
		}
	}

	// Staticメンバー
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat( "yyyy/MM/dd HH:mm", Locale.US );

	private final List<FileItem> m_listFileItem; // ファイル情報リスト

	// コンストラクタ
	public FileListAdapter( List<FileItem> list )
	{
		super();

		m_listFileItem = list;
	}

	public List<FileItem> getList()
	{
		return m_listFileItem;
	}

	@Override
	public int getCount()
	{
		return m_listFileItem.size();
	}

	@Override
	public FileItem getItem( int position )
	{
		return m_listFileItem.get( position );
	}

	@Override
	public long getItemId( int position )
	{
		return position;
	}

	// 一要素のビューの生成
	@Override
	public View getView( int position, View convertView, ViewGroup parent )
	{
		ViewHolder viewholder;
		if( null == convertView )
		{
			convertView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.listitem_file, parent, false );
			viewholder  = new ViewHolder( convertView );
			convertView.setTag( viewholder );
		}
		else
		{
			viewholder = (ViewHolder)convertView.getTag();
		}

		// アイテムの取得
		FileItem fileitem = getItem( position );

		// ファイルタイプ画像
		int iDrawableId;
		if( fileitem.isFile() )
		{
			iDrawableId = R.drawable.listitem_ic_file_24;
		}
		else
		{
			iDrawableId = R.drawable.listitem_ic_folder_24;
		}
		viewholder.m_imageviewFileType.setImageDrawable( ResourcesCompat.getDrawable( parent.getResources(), iDrawableId, null ) );

		// ファイル名
		viewholder.m_textviewFileName.setText( fileitem.getName() );
		viewholder.m_textviewFileName.setSelected( true );    // Needed for marquee effect.

		// 最終更新日時
		viewholder.m_textviewLastModified.setText( SIMPLE_DATE_FORMAT.format( fileitem.getLastModified() ) );

		// ファイルサイズ
		switch( fileitem.getType() )
		{
			case FILE:
				viewholder.m_textviewFileSize.setText( parent.getResources().getString( R.string.filesize_kb, (int)Math.ceil( fileitem.getFileSize() / 1024.0 ) ) );
				break;
			case DIRECTORY:
				viewholder.m_textviewFileSize.setText( R.string.filesize_directory );
				break;
			case WORKGROUP:
				viewholder.m_textviewFileSize.setText( R.string.filesize_workgroup );
				break;
			case SERVER:
				viewholder.m_textviewFileSize.setText( R.string.filesize_server );
				break;
			case SHARE:
				viewholder.m_textviewFileSize.setText( R.string.filesize_share );
				break;
			default:
				viewholder.m_textviewFileSize.setText( R.string.filesize_unknown );
				break;
		}

		return convertView;
	}

}
