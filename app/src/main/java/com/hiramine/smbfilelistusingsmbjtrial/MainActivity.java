package com.hiramine.smbfilelistusingsmbjtrial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{
	// Usage :
	// String TARGET_PATH = "smb://[hostname]/[sharename]/[directorys]/[filename]";
	// String USERNAME = "[username]";
	// String PASSWORD = "[password]";

	String TARGET_PATH = "smb://[hostname]/[sharename]/[directorys]/[filename]";
	String USERNAME = "[username]";
	String PASSWORD = "[password]";

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// アクティビティ呼出し
		Button button = findViewById( R.id.button_smbfilelist );
		button.setOnClickListener( view ->
								   {
									   Intent intent = new Intent( this, FileListActivity.class );
									   intent.putExtra( FileListActivity.EXTRA_TARGET_PATH, TARGET_PATH );
									   intent.putExtra( FileListActivity.EXTRA_USERNAME, USERNAME );
									   intent.putExtra( FileListActivity.EXTRA_PASSWORD, PASSWORD );
									   startActivity( intent );
								   } );
	}
}