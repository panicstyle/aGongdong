package com.panicstyle.Gongdong;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import uk.co.senab.photoview.PhotoView;

public class ImageActivity extends AppCompatActivity {
    private static String TAG = "ImageActivity";
    protected String itemsLink;
    protected String m_fileName;
    protected String m_strBoardID;
    protected String m_strBoardNo;
    private GongdongApplication m_app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        setTitle("이미지보기");

        m_app = (GongdongApplication)getApplication();

        intenter();

        GlideUrl glideUrl = new GlideUrl(itemsLink, new LazyHeaders.Builder()
                .build());

        PhotoView photoView = (PhotoView) findViewById(R.id.widget_photoview);
        Glide.with(this).load(glideUrl).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);

        /*
        ImageView imageView = (ImageView) findViewById(R.id.imageView);


        Drawable drawable = LoadImageFromWebOperations(itemsLink);
        imageView.setImageDrawable(drawable);

        PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        mAttacher.update();
*/
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분
        itemsLink = extras.getString("ITEMS_LINK");
        m_fileName = extras.getString("FILENAME");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_image:
                SaveImage();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SaveImage() {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(itemsLink));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, m_fileName);
// You can change the name of the downloads, by changing "download" to everything you want, such as the mWebview title...
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);

    }
}