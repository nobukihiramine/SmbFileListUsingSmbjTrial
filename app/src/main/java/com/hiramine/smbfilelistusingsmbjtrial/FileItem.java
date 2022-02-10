package com.hiramine.smbfilelistusingsmbjtrial;

import java.util.Comparator;

public class FileItem
{
	public static class FileItemComparator implements Comparator<FileItem>
	{
		@Override
		public int compare( FileItem fileitem1, FileItem fileitem2 )
		{
			// ファイル以外 < ファイル の順
			if( !fileitem1.isFile() && fileitem2.isFile() )
			{
				return -1;
			}
			if( fileitem1.isFile() && !fileitem2.isFile() )
			{
				return 1;
			}
			// ファイル同士、ディレクトリ同士の場合は、ファイル名（ディレクトリ名）の大文字小文字区別しない辞書順
			return fileitem1.getName().compareToIgnoreCase( fileitem2.getName() );
		}
	}

	public enum Type
	{
		FILE,
		DIRECTORY,
		WORKGROUP,
		SERVER,
		SHARE,
		UNKNOWN,
	}

	private final String m_strName;    // 表示名
	private final String m_strPath;    // パス
	private final Type   m_type;       // オブジェクトタイプ
	private final long   m_lLastModified;    // 最終更新日時
	private final long   m_lFileSize;  // ファイルサイズ

	// コンストラクタ
	public FileItem( String strName, String strPath, Type type, long lLastModified, long lFileSize )
	{
		m_strName = strName;
		m_strPath = strPath;
		m_type = type;
		m_lLastModified = lLastModified;
		m_lFileSize = lFileSize;
	}

	public String getName()
	{
		return m_strName;
	}

	public String getPath()
	{
		return m_strPath;
	}

	public Type getType()
	{
		return m_type;
	}

	public boolean isFile()
	{
		return ( Type.FILE == m_type );
	}

	public long getLastModified()
	{
		return m_lLastModified;
	}

	public long getFileSize()
	{
		return m_lFileSize;
	}
}
