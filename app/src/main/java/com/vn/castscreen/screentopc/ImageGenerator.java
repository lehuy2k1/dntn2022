package com.vn.castscreen.screentopc;

import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;

import com.vn.castscreen.CastScreenApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public final class ImageGenerator {
    private volatile boolean isThreadRunning;
    private Handler mImageHandler;
    private ImageReader mImageReader;
    private HandlerThread mImageThread;
    private ByteArrayOutputStream mJpegOutputStream;
    private final Object mLock = new Object();
    private Bitmap mReusableBitmap;
    private VirtualDisplay mVirtualDisplay;


    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        private Bitmap mCleanBitmap;
        private Image mImage;
        private byte[] mJpegByteArray;
        private Image.Plane mPlane;
        private int mWidth;

        private ImageAvailableListener() {
        }

        @Override
        public void onImageAvailable(ImageReader imageReader) {
            synchronized (ImageGenerator.this.mLock) {
                if (ImageGenerator.this.isThreadRunning) {
                    try {
                        this.mImage = ImageGenerator.this.mImageReader.acquireLatestImage();
                        if (this.mImage != null) {
                            this.mPlane = this.mImage.getPlanes()[0];
                            this.mWidth = this.mPlane.getRowStride() / this.mPlane.getPixelStride();
                            if (this.mWidth > this.mImage.getWidth()) {
                                if (ImageGenerator.this.mReusableBitmap == null) {
                                    ImageGenerator.this.mReusableBitmap = Bitmap.createBitmap(this.mWidth, this.mImage.getHeight(), Bitmap.Config.ARGB_8888);
                                }
                                ImageGenerator.this.mReusableBitmap.copyPixelsFromBuffer(this.mPlane.getBuffer());
                                this.mCleanBitmap = Bitmap.createBitmap(ImageGenerator.this.mReusableBitmap, 0, 0, this.mImage.getWidth(), this.mImage.getHeight());
                            } else {
                                this.mCleanBitmap = Bitmap.createBitmap(this.mImage.getWidth(), this.mImage.getHeight(), Bitmap.Config.ARGB_8888);
                                this.mCleanBitmap.copyPixelsFromBuffer(this.mPlane.getBuffer());
                            }
                            Bitmap bitmap = this.mCleanBitmap;
                            this.mImage.close();
                            ImageGenerator.this.mJpegOutputStream.reset();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, ImageGenerator.this.mJpegOutputStream);
                            bitmap.recycle();
                            this.mJpegByteArray = ImageGenerator.this.mJpegOutputStream.toByteArray();
                            if (this.mJpegByteArray != null) {
                                if (CastScreenApplication.getAppData().getImageQueue().size() > 3) {
                                    CastScreenApplication.getAppData().getImageQueue().pollLast();
                                }
                                CastScreenApplication.getAppData().getImageQueue().add(this.mJpegByteArray);
                                this.mJpegByteArray = null;
                            }
                        }
                    } catch (UnsupportedOperationException unused) {
                    }
                }
            }
        }
    }

    public void start() {
        synchronized (this.mLock) {
            if (!this.isThreadRunning) {
                MediaProjection mediaProjection = CastScreenApplication.getMediaProjection();
                if (mediaProjection != null) {
                    this.mImageThread = new HandlerThread(ImageGenerator.class.getSimpleName(), -1);
                    this.mImageThread.start();
                    this.mImageReader = ImageReader.newInstance(CastScreenApplication.getAppData().getScreenSize().x, CastScreenApplication.getAppData().getScreenSize().y, 1, 2);
                    this.mImageHandler = new Handler(this.mImageThread.getLooper());
                    this.mJpegOutputStream = new ByteArrayOutputStream();
                    this.mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), this.mImageHandler);
                    this.mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenStreamVirtualDisplay", CastScreenApplication.getAppData().getScreenSize().x, CastScreenApplication.getAppData().getScreenSize().y, CastScreenApplication.getAppData().getScreenDensity(), 16, this.mImageReader.getSurface(), null, this.mImageHandler);
                    this.isThreadRunning = true;
                }
            }
        }
    }

    public void stop() {
        synchronized (this.mLock) {
            if (this.isThreadRunning) {
                this.mImageReader.setOnImageAvailableListener(null, null);
                this.mImageReader.close();
                this.mImageReader = null;
                try {
                    this.mJpegOutputStream.close();
                } catch (IOException unused) {
                }
                this.mVirtualDisplay.release();
                this.mVirtualDisplay = null;
                this.mImageHandler.removeCallbacksAndMessages(null);
                this.mImageThread.quit();
                this.mImageThread = null;
                if (this.mReusableBitmap != null) {
                    this.mReusableBitmap.recycle();
                    this.mReusableBitmap = null;
                }
                this.isThreadRunning = false;
            }
        }
    }
}
