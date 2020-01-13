package com.easy.timecontrollerview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class TimeControllerView extends View {

    /**
     * 数值放大，方便计算
     */
    private static final int AMPLIFY_ONE_BIG_SCALE_TABLE = 1000;

    /**
     * 最大的滑动动画持续时间
     */
    private static final int MAX_FLING_DURATION = 1000;
    /**
     * View宽度
     */
    private int mViewWidth;
    /**
     * View宽度
     */
    private int mViewHeight;
    /**
     * 表格部分高度
     */
    private int mChartHeight;
    /**
     * 轮盘部分高度
     */
    private int mWheelHeight;
    /**
     * 插值器
     */
    private LinearOutSlowInInterpolator mLinearOutSlowInInterpolator = new LinearOutSlowInInterpolator();
    /**
     * 刻度最小值
     */
    private int minValue = 0;
    /**
     * 刻度最大值
     */
    private int maxValue = 24000;
    /**
     * 默认值 ，实际值需要除AMPLIFY_ONE_BIG_SCALE_TABLE（1000）
     */
    private int currentValue = maxValue / 2 / AMPLIFY_ONE_BIG_SCALE_TABLE;
    /**
     * 刻度尺基线的宽度
     */
    private int lineWidthBase = dp2pix(1);
    /**
     * 刻度线最小刻度的宽度
     */
    private int lineWidthSmallScale = dp2pix(0.5f);
    /**
     * 刻度线最小刻度的高度
     */
    private int lineHeightSmallScale = dp2pix(10);
    /**
     * 刻度线最大刻度的宽度
     */
    private int lineWidthBigScale = dp2pix(1);
    /**
     * 控制器背景的宽度
     */
    private int lineWidthController = dp2pix(15);
    /**
     * 刻度线最大刻度的高度
     */
    private int lineHeightBigScale = dp2pix(20);
    /**
     * 最大刻度中小刻度数量
     */
    private int scaleTableScaleNum = 8;
    /**
     * 每格的宽度
     */
    private int scaleTableSmallScaleWidth = dp2pix(10);

    /**
     * 指示器
     */
    private Paint mSelectorPain;
    /**
     * 标题
     */
    private Paint mTitleTextPain;
    /**
     * 刻度线
     */
    private Paint mScaleLinePain;
    /**
     * 刻度线文字
     */
    private Paint mScaleTextPain;
    /**
     * 滑块初始背景
     */
    private Paint mControllerBgLinePain;
    /**
     * 滑块被选择背景
     */
    private Paint mControllerSelectedLinePain;
    /**
     * 滑块未被选择背景
     */
    private Paint mControllerUnSelectedLinePain;
    /**
     * 滚轮背景
     */
    private Paint mWheelBgPain;
    /**
     * 滚轮背景
     */
    private Paint mWheelBgRectPain;
    /**
     * 表线
     */
    private Paint mChartLinePain;
    /**
     * 手势识别器
     */
    private GestureDetector mGesture;
    /**
     * 各点速度等级记录
     */
    private List<Integer> mSpeedRank = new ArrayList<>();
    /**
     * 速度登记记录MAP
     * 0-Orank-HEIGHT
     */
    private Map<Integer, Float> mRankMap = new HashMap<>();
    /**
     * 平滑动画
     */
    private ObjectAnimator mFlingAnim;
    /**
     * 监听
     */
    private FlingAnimUpdateListener mFlingAnimUpdateListener = new FlingAnimUpdateListener();
    /**
     * 监听
     */
    private ValueUpdateListener mValueUpdateListener;

    /**
     * 滑块
     */
    private Bitmap mControllerBitmap;

    /**
     * 是否在使用滑块
     */
    private boolean isControllerAction = false;

    private int valueTotal = (maxValue - minValue) / AMPLIFY_ONE_BIG_SCALE_TABLE;

    public void setMinMaxValue(int min, int max) {
        this.minValue = min * AMPLIFY_ONE_BIG_SCALE_TABLE;
        this.maxValue = max * AMPLIFY_ONE_BIG_SCALE_TABLE;
        this.currentValue = this.maxValue / 2 % AMPLIFY_ONE_BIG_SCALE_TABLE == 0 ? this.maxValue / 2 + AMPLIFY_ONE_BIG_SCALE_TABLE / 2 : this.maxValue / 2;
        this.valueTotal = (this.maxValue - this.minValue) / AMPLIFY_ONE_BIG_SCALE_TABLE;
        initData();

        invalidate();
    }

    public TimeControllerView(Context context) {
        this(context, null);
    }

    public TimeControllerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initData();
        initBitmap();
        initPaint();
        initGuest();

    }

    private void initBitmap() {

        mControllerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.xx);
    }

    private void initGuest() {
        mGesture = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mFlingAnim != null) {
                    mFlingAnim.cancel();
                }

                float y = e2.getY();
                float x = e2.getX();

                if (y < mChartHeight || isControllerAction) {

                    if (x - 50 < mViewWidth / 2 && mViewWidth / 2 < x + 50 || isControllerAction) {
                        isControllerAction = true;

                        int timeIndex = (currentValue - minValue) / 1000;


                        if (y > mRankMap.get(0)) {
                            mSpeedRank.remove(timeIndex);
                            mSpeedRank.add(timeIndex, 0);
                        } else if (y < mRankMap.get(10)) {
                            mSpeedRank.remove(timeIndex);
                            mSpeedRank.add(timeIndex, 10);
                        } else {

                            Collection<Float> values = mRankMap.values();

                            List<Float> a = new ArrayList<>();
                            a.addAll(values);
                            a.add(y);

                            Float[] objects = a.toArray(new Float[a.size()]);

                            Arrays.sort(objects, Collections.reverseOrder());
                            for (int i1 = 0; i1 < objects.length; i1++) {

                                if (objects[i1] == y) {

                                    if (i1 == 0) {
                                        mSpeedRank.remove(timeIndex);
                                        mSpeedRank.add(timeIndex, 0);
                                    } else if (i1 == objects.length - 1) {
                                        mSpeedRank.remove(timeIndex);
                                        mSpeedRank.add(timeIndex, 10);
                                    } else {
                                        Float object1 = objects[i1 - 1];
                                        Float object2 = objects[i1 + 1];

                                        if (Math.abs(object1) - y > Math.abs(object2) - y) {
                                            mSpeedRank.remove(timeIndex);
                                            mSpeedRank.add(timeIndex, i1);
                                        } else {
                                            mSpeedRank.remove(timeIndex);
                                            mSpeedRank.add(timeIndex, i1 - 1);
                                        }
                                    }

                                    invalidate();
                                    return true;
                                }

                            }

                        }


                        invalidate();

                        return true;
                    }

                    return false;
                }

                if (!isControllerAction) {
                    currentValue += distanceX * scaleTableSmallScaleWidth;
                    if (currentValue >= maxValue) {
                        currentValue = maxValue - 500;
                    } else if (currentValue <= minValue) {
                        currentValue = minValue + 500;
                    }

                    invalidate();
                }

                return true;
            }
        });
        mGesture.setIsLongpressEnabled(false);
    }

    private void initData() {
        mSpeedRank.clear();
        for (int i = 0; i < valueTotal; i++) {
            mSpeedRank.add(4);
        }
    }

    private void initPaint() {
        mTitleTextPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitleTextPain.setColor(0xFFC3C3C3);

        mSelectorPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectorPain.setColor(0xFF0073FF);
        mSelectorPain.setStrokeWidth(lineWidthBigScale);

        mControllerBgLinePain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mControllerBgLinePain.setColor(0xFF181818);
        mControllerBgLinePain.setStrokeWidth(lineWidthController);
        mControllerBgLinePain.setStrokeCap(Paint.Cap.ROUND);

        mControllerSelectedLinePain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mControllerSelectedLinePain.setColor(0xFF0073FF);
        mControllerSelectedLinePain.setStrokeWidth(lineWidthController);
        mControllerSelectedLinePain.setStrokeCap(Paint.Cap.ROUND);

        mControllerUnSelectedLinePain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mControllerUnSelectedLinePain.setColor(0xFF85898A);
        mControllerUnSelectedLinePain.setStrokeWidth(lineWidthController);
        mControllerUnSelectedLinePain.setStrokeCap(Paint.Cap.ROUND);

        mScaleLinePain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePain.setColor(Color.GRAY);

        mWheelBgPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWheelBgPain.setColor(Color.BLACK);

        mWheelBgRectPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWheelBgRectPain.setStrokeWidth(dp2pix(4));
        mWheelBgRectPain.setColor(Color.BLACK);
        mWheelBgRectPain.setStyle(Paint.Style.STROKE);

        mScaleTextPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleTextPain.setColor(Color.WHITE);
        mScaleTextPain.setTextSize(sp2pix(18));
        mScaleTextPain.setTextAlign(Paint.Align.CENTER);

        mChartLinePain = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChartLinePain.setColor(0xFF85898A);
        mChartLinePain.setStrokeWidth(dp2pix(1));
        mChartLinePain.setTextSize(sp2pix(16));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesture.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isControllerAction = false;
            float y = event.getY();

            if (y < mChartHeight) {
                return false;
            }

            startSmoothAnim(revisedTarget(currentValue), 100);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);

        drawSpeedChartView(canvas);

        drawWheelView(canvas);

        canvas.restoreToCount(saved);
    }

    private void drawSpeedChartView(Canvas canvas) {
        drawSpeedChart(canvas);
    }

    private void drawSpeedChart(Canvas canvas) {

        Rect rect = drawSpeedTitle(canvas);

        int tHeight = (int) (mChartHeight - rect.height() * 2.5);
        int scaleCeilHeight = tHeight / 10;

        Rect textWidthAndHeight = getTextWidthAndHeight(mChartLinePain, "10");

        for (int i = 0; i <= 10; i++) {

            canvas.drawLine(
                    (float) (textWidthAndHeight.width() * 1.5), i * scaleCeilHeight + (int) (rect.height() * 2.5),
                    mViewWidth, i * scaleCeilHeight + (int) (+rect.height() * 2.5),
                    mChartLinePain);

            canvas.drawText(10 - i + "",
                    0, i * scaleCeilHeight + (int) (rect.height() * 2.5) + (int) (textWidthAndHeight.height() * 0.5),
                    mChartLinePain);

            mRankMap.put(10 - i, new Float(i * scaleCeilHeight + (int) (rect.height() * 2.5)));
        }

    }

    private void drawWheelView(Canvas canvas) {
        drawWheelBaseLine(canvas);
        drawWheel(canvas);
    }

    private void drawWheel(Canvas canvas) {

        Rect rect = drawTimeTitle(canvas);

        int centerX = mViewWidth / 2;

        int tWheelViewHeight = mChartHeight + rect.right + dp2pix(6);

        //画刻度线
        int everyScaleG = AMPLIFY_ONE_BIG_SCALE_TABLE / scaleTableScaleNum;
        float offset = currentValue % everyScaleG;
        float handOffset = offset / everyScaleG * scaleTableSmallScaleWidth;

        canvas.drawRoundRect(0, tWheelViewHeight, mViewWidth, mViewHeight, dp2pix(4), dp2pix(4), mWheelBgPain);

        float currentLeftHandValue;
        float currentRightHandValue;
        float leftLineX;
        float rightLineX;
        if (offset == 0) {
            float lineX = centerX;
            float currentHandValue = currentValue;

            drawScaleTable(canvas, tWheelViewHeight, lineX, currentHandValue, true);
            currentLeftHandValue = currentValue - everyScaleG;
            currentRightHandValue = currentValue + everyScaleG;
            leftLineX = lineX - scaleTableSmallScaleWidth;
            rightLineX = lineX + scaleTableSmallScaleWidth;
        } else {
            currentLeftHandValue = currentValue - offset % everyScaleG;
            currentRightHandValue = currentValue + everyScaleG - offset % everyScaleG;
            leftLineX = centerX - handOffset;
            rightLineX = centerX + scaleTableSmallScaleWidth - handOffset;
        }
        while (rightLineX < mViewWidth + 2 * scaleTableSmallScaleWidth) {
            //从中开始向左画指针
            if (currentLeftHandValue >= minValue) {
                drawScaleTable(canvas, tWheelViewHeight, leftLineX, currentLeftHandValue, false);
            }
            //从中开始向右画指针
            if (currentRightHandValue <= maxValue) {
                drawScaleTable(canvas, tWheelViewHeight, rightLineX, currentRightHandValue, false);
            }
            currentLeftHandValue -= everyScaleG;
            currentRightHandValue += everyScaleG;
            leftLineX -= scaleTableSmallScaleWidth;
            rightLineX += scaleTableSmallScaleWidth;
        }
        //画出中间的指针
        canvas.drawLine(centerX, tWheelViewHeight, centerX, mViewHeight, mSelectorPain);

        canvas.drawRoundRect(0, tWheelViewHeight, mViewWidth, mViewHeight, dp2pix(8), dp2pix(8), mWheelBgRectPain);
    }

    private void drawWheelBaseLine(Canvas canvas) {
        //画刻度基准线
        mScaleLinePain.setStrokeWidth(lineWidthBase);
//        canvas.drawLine(0, centerY, canvasWidth, centerY, mScaleLinePain);
    }

    private Rect drawTimeTitle(Canvas canvas) {

        String timeStr = "时间";

        Rect timeTextWidthAndHeight = getTextWidthAndHeight(mTitleTextPain, timeStr);


        mTitleTextPain.setTextSize(sp2pix(16));
        canvas.drawText(timeStr, 0, (float) (mChartHeight + timeTextWidthAndHeight.height() * 1.5), mTitleTextPain);

        return timeTextWidthAndHeight;
    }

    private Rect drawSpeedTitle(Canvas canvas) {

        String speedStr = "速度";

        Rect speedTextWidthAndHeight = getTextWidthAndHeight(mTitleTextPain, speedStr);

        mTitleTextPain.setTextSize(sp2pix(16));
        canvas.drawText(speedStr, 0, speedTextWidthAndHeight.height(), mTitleTextPain);
        return speedTextWidthAndHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        mChartHeight = (int) (mViewHeight * 0.72);
        mWheelHeight = mViewHeight - mChartHeight;

    }

    private void drawScaleTable(Canvas canvas, int centerY, float lineX, float currentHandWeight, boolean isSelelct) {
        if (currentHandWeight % AMPLIFY_ONE_BIG_SCALE_TABLE == 0) {
            mScaleLinePain.setStrokeWidth(lineWidthBigScale);
            canvas.drawLine(lineX, centerY, lineX, centerY + lineHeightBigScale, mScaleLinePain);
            canvas.drawText(String.valueOf((int) (currentHandWeight / AMPLIFY_ONE_BIG_SCALE_TABLE)), lineX, centerY + lineHeightBigScale + dp2pix(30), mScaleTextPain);
        } else {
            if (String.valueOf(currentHandWeight / AMPLIFY_ONE_BIG_SCALE_TABLE).contains(".5")) {
                mScaleLinePain.setStrokeWidth(lineWidthSmallScale);
                canvas.drawLine(lineX, centerY, lineX, (float) (centerY + lineHeightSmallScale * 1.3), mScaleLinePain);

                drawSpeedController(canvas, centerY, lineX, isSelelct, (int) currentHandWeight);

            } else {
                mScaleLinePain.setStrokeWidth(lineWidthSmallScale);
                canvas.drawLine(lineX, centerY, lineX, centerY + lineHeightSmallScale, mScaleLinePain);
            }
        }
    }

    private void drawSpeedController(Canvas canvas, int centerY, float lineX, boolean isSelelct, int number) {


        if (lineX * 1.2 < mControllerBitmap.getWidth()) {
            return;
        }

        if (lineX + mControllerBitmap.getWidth() + 50 > mViewWidth) {
            return;
        }

        String speedStr = "速度";
        Rect speedTextWidthAndHeight = getTextWidthAndHeight(mTitleTextPain, speedStr);

        canvas.drawLine(
                lineX, (float) (speedTextWidthAndHeight.height() * 2.5) + lineWidthController / 2,
                lineX, mChartHeight - lineWidthController / 2,
                mControllerBgLinePain);

        int rank = mSpeedRank.get((number - minValue) / AMPLIFY_ONE_BIG_SCALE_TABLE);

        float v = mRankMap.get(rank);
        canvas.drawLine(
                lineX, mChartHeight - lineWidthController / 2,
                lineX, v,
                isSelelct ? mControllerSelectedLinePain : mControllerUnSelectedLinePain);


        canvas.drawBitmap(mControllerBitmap, lineX - mControllerBitmap.getWidth() / 2, v - mControllerBitmap.getHeight() / 2, mControllerSelectedLinePain);

    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
        invalidate();
    }

    /**
     * @param anim 是否进行平滑动画
     */
    public void setBodyWeight(int bodyWeight, boolean anim) {
        if (anim) {
            startSmoothAnim(bodyWeight, MAX_FLING_DURATION);
        } else {
            setCurrentValue(bodyWeight);
        }
    }

    /**
     * 开始一个平滑动画
     */
    private void startSmoothAnim(int targetWeight, int duration) {
        if (targetWeight >= maxValue) {
            targetWeight = maxValue;
        } else if (targetWeight <= minValue) {
            targetWeight = minValue;
        } else {
            targetWeight = revisedTarget(targetWeight);
        }
        if (mFlingAnim != null) {
            mFlingAnim.cancel();
            mFlingAnim = null;
        }
        mFlingAnim = ObjectAnimator.ofInt(TimeControllerView.this, "currentValue", this.currentValue, targetWeight);
        mFlingAnim.setInterpolator(mLinearOutSlowInInterpolator);
        mFlingAnim.setDuration(duration);
        mFlingAnim.addUpdateListener(mFlingAnimUpdateListener);
        mFlingAnim.start();
    }

    /**
     * 对体重值进行修正，以符合刻度
     */
    private int revisedTarget(int targetWeight) {
        int oneScaleBox = AMPLIFY_ONE_BIG_SCALE_TABLE / scaleTableScaleNum;
        int offset = targetWeight % oneScaleBox;
        int half = 2;
        if (offset != 0) {
            if (offset > oneScaleBox / half) {
                targetWeight += oneScaleBox - offset;
            } else {
                targetWeight -= offset;
            }
        }


        int i = targetWeight / 1000;

        targetWeight = i * 1000 + 500;

        return targetWeight;
    }


    public ValueUpdateListener getValueUpdateListener() {
        return mValueUpdateListener;
    }

    public void setValueUpdateListener(ValueUpdateListener bodyWeightUpdateListener) {
        mValueUpdateListener = bodyWeightUpdateListener;
    }

    private class FlingAnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mValueUpdateListener != null) {
                mValueUpdateListener.update((Integer) animation.getAnimatedValue());
            }
        }
    }

    public interface ValueUpdateListener {
        void update(int value);
    }

    /**
     * 获取文字长宽
     */
    private Rect getTextWidthAndHeight(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    private int dp2pix(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    private int sp2pix(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
