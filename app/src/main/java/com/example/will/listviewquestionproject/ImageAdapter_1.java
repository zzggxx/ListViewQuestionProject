package com.example.will.listviewquestionproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 第一种解决办法:findViewWithTag()
 * <p>
 * 由于使用findViewWithTag必须要有ListView的实例才行，那么我们在Adapter中怎样才能拿到ListView的实例呢？其实如果你仔细通读了上一篇文章就能知道，getView()方法中传入的第三个参数其实就是ListView的实例，那么这里我们定义一个全局变量mListView，然后在getView()方法中判断它是否为空，如果为空就把parent这个参数赋值给它。
 * <p>
 * 另外在getView()方法中我们还做了一个操作，就是调用了ImageView的setTag()方法，并把当前位置图片的URL地址作为参数传了进去，这个是为后续的findViewWithTag()方法做准备。
 * <p>
 * 最后，我们修改了BitmapWorkerTask的构造函数，这里不再通过构造函数把ImageView的实例传进去了，而是在onPostExecute()方法当中通过ListView的findVIewWithTag()方法来去获取ImageView控件的实例。获取到控件实例后判断下是否为空，如果不为空就让图片显示到控件上。
 * <p>
 * 这里我们可以尝试分析一下findViewWithTag的工作原理，其实顾名思义，这个方法就是通过Tag的名字来获取具备该Tag名的控件，我们先要调用控件的setTag()方法来给控件设置一个Tag，然后再调用ListView的findViewWithTag()方法使用相同的Tag名来找回控件。
 * <p>
 * 那么为什么用了findViewWithTag()方法之后，图片就不会再出现乱序情况了呢？其实原因很简单，由于ListView中的ImageView控件都是重用的，移出屏幕的控件很快会被进入屏幕的图片重新利用起来，那么getView()方法就会再次得到执行，而在getView()方法中会为这个ImageView控件设置新的Tag，这样老的Tag就会被覆盖掉，于是这时再调用findVIewWithTag()方法并传入老的Tag，就只能得到null了，而我们判断只有ImageView不等于null的时候才会设置图片，这样图片乱序的问题也就不存在了。
 */

// FIXME: 2018/10/31 这种方案的解决也是有问题的,当来回滑动的时候图片会一直变更知道正确位置,中间的体验是非常差劲的.

public class ImageAdapter_1 extends ArrayAdapter<String> {

    private ListView mListView;
    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
     */
    private LruCache<String, BitmapDrawable> mMemoryCache;

    public ImageAdapter_1(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable drawable) {
                return drawable.getBitmap().getByteCount();
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mListView == null) {
            mListView = (ListView) parent;
        }
        String url = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.image_item, null);
        } else {
            view = convertView;
        }
        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setTag(url);
        BitmapDrawable drawable = getBitmapFromMemoryCache(url);
        if (drawable != null) {
            image.setImageDrawable(drawable);
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask(/*image*/);
            task.execute(url);
        }
        return view;
    }

    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key      LruCache的键，这里传入图片的URL地址。
     * @param drawable LruCache的值，这里传入从网络上下载的BitmapDrawable对象。
     */
    public void addBitmapToMemoryCache(String key, BitmapDrawable drawable) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, drawable);
        }
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     *
     * @param key LruCache的键，这里传入图片的URL地址。
     * @return 对应传入键的BitmapDrawable对象，或者null。
     */
    public BitmapDrawable getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 异步下载图片的任务。
     *
     * @author guolin
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {

        //        private ImageView mImageView;
        private String mImageUrl;

//        public BitmapWorkerTask(ImageView imageView) {
//            mImageView = imageView;
//        }

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            mImageUrl = params[0];
            // 在后台开始下载图片
            Bitmap bitmap = downloadBitmap(mImageUrl);
            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
            addBitmapToMemoryCache(mImageUrl, drawable);
            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            ImageView imageView = (ImageView) mListView.findViewWithTag(mImageUrl);
            if (imageView != null && drawable != null) {
                imageView.setImageDrawable(drawable);
            }
        }

        /**
         * 建立HTTP请求，并获取Bitmap对象。
         *
         * @param imageUrl 图片的URL地址
         * @return 解析后的Bitmap对象
         */
        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return bitmap;
        }

    }
}