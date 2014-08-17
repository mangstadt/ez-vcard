package ezvcard.android.TestActivity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import ezvcard.android.AndroidCustomFieldScribe;
import ezvcard.android.ContactOperations;
import ezvcard.android.R;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/*
 Copyright (c) 2014, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies,
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * A sample Android activity which demonstrates on how to use this library.
 * @author Pratyush
 * @author Michael Angstadt
 */
public class VcardParser extends ActionBarActivity {
    private static final String TAG = VcardParser.class.getSimpleName();
    private static final File vcardFile;
    static {
    	 String state = Environment.getExternalStorageState();
    	 if (!Environment.MEDIA_MOUNTED.equals(state)) {
    		 throw new RuntimeException("No external storage mounted.");
    	 }

    	 String path = Environment.getExternalStorageDirectory().toString() + "/" + "1517814042cards.vcf";
    	 vcardFile = new File(path);
    	 if (!vcardFile.exists()){
    		 throw new RuntimeException("vCard file does not exist: " + path);
    	 }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vcard_parser);
        VCardReader reader = null;
        try {
        	reader = new VCardReader(vcardFile);
            reader.registerScribe(new AndroidCustomFieldScribe());
            
            ContactOperations operations = new ContactOperations(getApplicationContext(), "Phone", "com.motorola.android.buacontactadapter");
            VCard vcard = null;
            while ((vcard = reader.readNext()) != null) {
                operations.insertContact(vcard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	closeQuietly(reader);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vcard_parser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private static void closeQuietly(Closeable closeable){
    	if (closeable == null){
    		return;
    	}
    	
    	try {
			closeable.close();
		} catch (IOException e){
			//ignore
		}
    }
}
